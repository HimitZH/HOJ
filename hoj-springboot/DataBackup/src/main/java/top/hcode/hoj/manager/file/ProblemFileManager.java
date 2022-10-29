package top.hcode.hoj.manager.file;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusSystemErrorException;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.dao.problem.LanguageEntityService;
import top.hcode.hoj.dao.problem.ProblemCaseEntityService;
import top.hcode.hoj.dao.problem.ProblemEntityService;
import top.hcode.hoj.dao.problem.TagEntityService;
import top.hcode.hoj.exception.ProblemIDRepeatException;
import top.hcode.hoj.pojo.dto.ProblemDTO;
import top.hcode.hoj.pojo.entity.problem.*;
import top.hcode.hoj.pojo.vo.ImportProblemVO;
import top.hcode.hoj.shiro.AccountProfile;
import top.hcode.hoj.utils.Constants;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.*;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/10 14:40
 * @Description:
 */
@Component
@Slf4j(topic = "hoj")
public class ProblemFileManager {
    @Autowired
    private LanguageEntityService languageEntityService;

    @Autowired
    private ProblemEntityService problemEntityService;

    @Autowired
    private ProblemCaseEntityService problemCaseEntityService;

    @Autowired
    private TagEntityService tagEntityService;

    /**
     * @param file
     * @MethodName importProblem
     * @Description zip文件导入题目 仅超级管理员可操作
     * @Return
     * @Since 2021/5/27
     */
    public void importProblem(MultipartFile file) throws StatusFailException, StatusSystemErrorException {

        String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
        if (!"zip".toUpperCase().contains(suffix.toUpperCase())) {
            throw new StatusFailException("请上传zip格式的题目文件压缩包！");
        }

        String fileDirId = IdUtil.simpleUUID();
        String fileDir = Constants.File.TESTCASE_TMP_FOLDER.getPath() + File.separator + fileDirId;
        String filePath = fileDir + File.separator + file.getOriginalFilename();
        // 文件夹不存在就新建
        FileUtil.mkdir(fileDir);
        try {
            file.transferTo(new File(filePath));
        } catch (IOException e) {
            FileUtil.del(fileDir);
            throw new StatusSystemErrorException("服务器异常：评测数据上传失败！");
        }

        // 将压缩包压缩到指定文件夹
        ZipUtil.unzip(filePath, fileDir);

        // 删除zip文件
        FileUtil.del(filePath);


        // 检查文件是否存在
        File testCaseFileList = new File(fileDir);
        File[] files = testCaseFileList.listFiles();
        if (files == null || files.length == 0) {
            FileUtil.del(fileDir);
            throw new StatusFailException("评测数据压缩包里文件不能为空！");
        }


        HashMap<String, File> problemInfo = new HashMap<>();
        HashMap<String, File> testcaseInfo = new HashMap<>();

        for (File tmp : files) {
            if (tmp.isFile()) {
                // 检查文件是否时json文件
                if (!tmp.getName().endsWith("json")) {
                    FileUtil.del(fileDir);
                    throw new StatusFailException("编号为：" + tmp.getName() + "的文件格式错误，请使用json文件！");
                }
                String tmpPreName = tmp.getName().substring(0, tmp.getName().lastIndexOf("."));
                problemInfo.put(tmpPreName, tmp);
            }
            if (tmp.isDirectory()) {
                testcaseInfo.put(tmp.getName(), tmp);
            }
        }

        // 读取json文件生成对象
        HashMap<String, ImportProblemVO> problemVoMap = new HashMap<>();
        for (String key : problemInfo.keySet()) {
            // 若有名字不对应，直接返回失败
            if (testcaseInfo.getOrDefault(key, null) == null) {
                FileUtil.del(fileDir);
                throw new StatusFailException("请检查编号为：" + key + "的题目数据文件与测试数据文件夹是否一一对应！");
            }
            try {
                FileReader fileReader = new FileReader(problemInfo.get(key));
                ImportProblemVO importProblemVo = JSONUtil.toBean(fileReader.readString(), ImportProblemVO.class);
                problemVoMap.put(key, importProblemVo);
            } catch (Exception e) {
                FileUtil.del(fileDir);
                throw new StatusFailException("请检查编号为：" + key + "的题目json文件的格式：" + e.getLocalizedMessage());
            }
        }

        QueryWrapper<Language> languageQueryWrapper = new QueryWrapper<>();
        languageQueryWrapper.eq("oj", "ME");
        List<Language> languageList = languageEntityService.list(languageQueryWrapper);

        HashMap<String, Long> languageMap = new HashMap<>();
        for (Language language : languageList) {
            languageMap.put(language.getName(), language.getId());
        }

        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        List<ProblemDTO> problemDTOS = new LinkedList<>();
        List<Tag> tagList = tagEntityService.list(new QueryWrapper<Tag>().eq("oj", "ME"));
        HashMap<String, Tag> tagMap = new HashMap<>();
        for (Tag tag : tagList) {
            tagMap.put(tag.getName().toUpperCase(), tag);
        }
        for (String key : problemInfo.keySet()) {
            ImportProblemVO importProblemVo = problemVoMap.get(key);
            // 格式化题目语言
            List<Language> languages = new LinkedList<>();
            for (String lang : importProblemVo.getLanguages()) {
                Long lid = languageMap.getOrDefault(lang, null);

                if (lid == null) {
                    throw new StatusFailException("请检查编号为：" + key + "的题目的代码语言是否有错，不要添加不支持的语言！");
                }
                languages.add(new Language().setId(lid).setName(lang));
            }

            // 格式化题目代码模板
            List<CodeTemplate> codeTemplates = new LinkedList<>();
            for (Map<String, String> tmp : importProblemVo.getCodeTemplates()) {
                String language = tmp.getOrDefault("language", null);
                String code = tmp.getOrDefault("code", null);
                Long lid = languageMap.getOrDefault(language, null);
                if (language == null || code == null || lid == null) {
                    FileUtil.del(fileDir);
                    throw new StatusFailException("请检查编号为：" + key + "的题目的代码模板列表是否有错，不要添加不支持的语言！");
                }
                codeTemplates.add(new CodeTemplate().setCode(code).setStatus(true).setLid(lid));
            }

            // 格式化标签
            List<Tag> tags = new LinkedList<>();
            for (String tagStr : importProblemVo.getTags()) {
                Tag tag = tagMap.getOrDefault(tagStr.toUpperCase(), null);
                if (tag == null) {
                    tags.add(new Tag().setName(tagStr).setOj("ME"));
                } else {
                    tags.add(tag);
                }
            }

            Problem problem = BeanUtil.mapToBean(importProblemVo.getProblem(), Problem.class, true);
            if (problem.getAuthor() == null) {
                problem.setAuthor(userRolesVo.getUsername());
            }
            problem.setIsGroup(false);
            List<ProblemCase> problemCaseList = new LinkedList<>();
            for (Map<String, Object> tmp : importProblemVo.getSamples()) {
                problemCaseList.add(BeanUtil.mapToBean(tmp, ProblemCase.class, true));
            }

            // 格式化用户额外文件和判题额外文件
            if (importProblemVo.getUserExtraFile() != null) {
                JSONObject userExtraFileJson = JSONUtil.parseObj(importProblemVo.getUserExtraFile());
                problem.setUserExtraFile(userExtraFileJson.toString());
            }
            if (importProblemVo.getJudgeExtraFile() != null) {
                JSONObject judgeExtraFileJson = JSONUtil.parseObj(importProblemVo.getJudgeExtraFile());
                problem.setJudgeExtraFile(judgeExtraFileJson.toString());
            }


            ProblemDTO problemDto = new ProblemDTO();
            problemDto.setProblem(problem)
                    .setCodeTemplates(codeTemplates)
                    .setTags(tags)
                    .setLanguages(languages)
                    .setUploadTestcaseDir(fileDir + File.separator + key)
                    .setIsUploadTestCase(true)
                    .setSamples(problemCaseList);

            Constants.JudgeMode judgeMode = Constants.JudgeMode.getJudgeMode(importProblemVo.getJudgeMode());
            if (judgeMode == null) {
                problemDto.setJudgeMode(Constants.JudgeMode.DEFAULT.getMode());
            } else {
                problemDto.setJudgeMode(judgeMode.getMode());
            }
            problemDTOS.add(problemDto);
        }

        if (problemDTOS.size() == 0) {
            throw new StatusFailException("警告：未成功导入一道以上的题目，请检查文件格式是否正确！");
        } else {
            HashSet<String> repeatProblemIDSet = new HashSet<>();
            HashSet<String> failedProblemIDSet = new HashSet<>();
            int failedCount = 0;
            for (ProblemDTO problemDto : problemDTOS) {
                try {
                    boolean isOk = problemEntityService.adminAddProblem(problemDto);
                    if (!isOk) {
                        failedCount++;
                    }
                } catch (ProblemIDRepeatException e) {
                    repeatProblemIDSet.add(problemDto.getProblem().getProblemId());
                    failedCount++;
                } catch (Exception e) {
                    log.error("", e);
                    failedProblemIDSet.add(problemDto.getProblem().getProblemId());
                    failedCount++;
                }
            }
            if (failedCount > 0) {
                int successCount = problemDTOS.size() - failedCount;
                String errMsg = "[导入结果] 成功数：" + successCount + ",  失败数：" + failedCount +
                        ",  重复失败的题目ID：" + repeatProblemIDSet;
                if (failedProblemIDSet.size() > 0) {
                    errMsg = errMsg + "<br/>未知失败的题目ID：" + failedProblemIDSet;
                }
                throw new StatusFailException(errMsg);
            }
        }
    }


    /**
     * @param pidList
     * @param response
     * @MethodName exportProblem
     * @Description 导出指定的题目包括测试数据生成zip 仅超级管理员可操作
     * @Return
     * @Since 2021/5/28
     */
    public void exportProblem(List<Long> pidList, HttpServletResponse response) {

        QueryWrapper<Language> languageQueryWrapper = new QueryWrapper<>();
        languageQueryWrapper.eq("oj", "ME");
        List<Language> languageList = languageEntityService.list(languageQueryWrapper);

        HashMap<Long, String> languageMap = new HashMap<>();
        for (Language language : languageList) {
            languageMap.put(language.getId(), language.getName());
        }

        List<Tag> tagList = tagEntityService.list();

        HashMap<Long, String> tagMap = new HashMap<>();
        for (Tag tag : tagList) {
            tagMap.put(tag.getId(), tag.getName());
        }

        String workDir = Constants.File.FILE_DOWNLOAD_TMP_FOLDER.getPath() + File.separator + IdUtil.simpleUUID();

        // 使用线程池
        ExecutorService threadPool = new ThreadPoolExecutor(
                2, // 核心线程数
                4, // 最大线程数。最多几个线程并发。
                3,//当非核心线程无任务时，几秒后结束该线程
                TimeUnit.SECONDS,// 结束线程时间单位
                new LinkedBlockingDeque<>(200), //阻塞队列，限制等候线程数
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.DiscardOldestPolicy());//队列满了，尝试去和最早的竞争，也不会抛出异常！

        List<FutureTask<Void>> futureTasks = new ArrayList<>();

        for (Long pid : pidList) {

            futureTasks.add(new FutureTask<>(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    String testcaseWorkDir = Constants.File.TESTCASE_BASE_FOLDER.getPath() + File.separator + "problem_" + pid;
                    File file = new File(testcaseWorkDir);

                    List<HashMap<String, Object>> problemCases = new LinkedList<>();
                    if (!file.exists() || file.listFiles() == null) { // 本地为空 尝试去数据库查找
                        QueryWrapper<ProblemCase> problemCaseQueryWrapper = new QueryWrapper<>();
                        problemCaseQueryWrapper.eq("pid", pid);
                        List<ProblemCase> problemCaseList = problemCaseEntityService.list(problemCaseQueryWrapper);
                        FileUtil.mkdir(testcaseWorkDir);
                        // 写入本地
                        for (int i = 0; i < problemCaseList.size(); i++) {
                            String filePreName = testcaseWorkDir + File.separator + (i + 1);
                            String inputName = filePreName + ".in";
                            String outputName = filePreName + ".out";
                            FileWriter infileWriter = new FileWriter(inputName);
                            infileWriter.write(problemCaseList.get(i).getInput());
                            FileWriter outfileWriter = new FileWriter(outputName);
                            outfileWriter.write(problemCaseList.get(i).getOutput());

                            ProblemCase problemCase = problemCaseList.get(i).setPid(null)
                                    .setInput(inputName)
                                    .setOutput(outputName)
                                    .setGmtCreate(null)
                                    .setStatus(null)
                                    .setId(null)
                                    .setGmtModified(null);
                            HashMap<String, Object> problemCaseMap = new HashMap<>();
                            BeanUtil.beanToMap(problemCase, problemCaseMap, false, true);
                            problemCases.add(problemCaseMap);
                        }
                        FileUtil.copy(testcaseWorkDir, workDir, true);

                    } else {
                        String infoPath = testcaseWorkDir + File.separator + "info";
                        if (FileUtil.exist(infoPath)) {
                            FileReader reader = new FileReader(infoPath);
                            JSONObject jsonObject = JSONUtil.parseObj(reader.readString());
                            JSONArray testCases = jsonObject.getJSONArray("testCases");
                            for (int i = 0; i < testCases.size(); i++) {
                                JSONObject jsonObject1 = testCases.get(i, JSONObject.class);
                                HashMap<String, Object> problemCaseMap = new HashMap<>();
                                problemCaseMap.put("input", jsonObject1.getStr("inputName"));
                                problemCaseMap.put("output", jsonObject1.getStr("outputName"));
                                Integer score = jsonObject1.getInt("score");
                                Integer groupNum = jsonObject1.getInt("groupNum");
                                if (score != null && score > 0) {
                                    problemCaseMap.put("score", score);
                                }
                                if (groupNum != null) {
                                    problemCaseMap.put("groupNum", groupNum);
                                }
                                problemCases.add(problemCaseMap);
                            }
                        }
                        FileUtil.copy(testcaseWorkDir, workDir, true);
                    }
                    ImportProblemVO importProblemVo = problemEntityService.buildExportProblem(pid, problemCases, languageMap, tagMap);
                    String content = JSONUtil.toJsonStr(importProblemVo);
                    FileWriter fileWriter = new FileWriter(workDir + File.separator + "problem_" + pid + ".json");
                    fileWriter.write(content);
                    return null;
                }
            }));

        }
        // 提交到线程池进行执行
        for (FutureTask<Void> futureTask : futureTasks) {
            threadPool.submit(futureTask);
        }
        // 所有任务执行完成且等待队列中也无任务关闭线程池
        if (!threadPool.isShutdown()) {
            threadPool.shutdown();
        }
        // 阻塞主线程, 直至线程池关闭
        try {
            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            log.error("线程池异常--------------->", e);
        }

        String fileName = "problem_export_" + System.currentTimeMillis() + ".zip";
        // 将对应文件夹的文件压缩成zip
        ZipUtil.zip(workDir, Constants.File.FILE_DOWNLOAD_TMP_FOLDER.getPath() + File.separator + fileName);
        // 将zip变成io流返回给前端
        FileReader fileReader = new FileReader(Constants.File.FILE_DOWNLOAD_TMP_FOLDER.getPath() + File.separator + fileName);
        BufferedInputStream bins = new BufferedInputStream(fileReader.getInputStream());//放到缓冲流里面
        OutputStream outs = null;//获取文件输出IO流
        BufferedOutputStream bouts = null;
        try {
            outs = response.getOutputStream();
            bouts = new BufferedOutputStream(outs);
            response.setContentType("application/x-download");
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            int bytesRead = 0;
            byte[] buffer = new byte[1024 * 10];
            //开始向网络传输文件流
            while ((bytesRead = bins.read(buffer, 0, 1024 * 10)) != -1) {
                bouts.write(buffer, 0, bytesRead);
            }
            bouts.flush();
        } catch (IOException e) {
            log.error("导出题目数据的压缩文件异常------------>", e);
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            Map<String, Object> map = new HashMap<>();
            map.put("status", ResultStatus.SYSTEM_ERROR);
            map.put("msg", "导出题目数据失败，请重新尝试！");
            map.put("data", null);
            try {
                response.getWriter().println(JSONUtil.toJsonStr(map));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } finally {
            try {
                bins.close();
                if (outs != null) {
                    outs.close();
                }
                if (bouts != null) {
                    bouts.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 清空临时文件
            FileUtil.del(workDir);
            FileUtil.del(Constants.File.FILE_DOWNLOAD_TMP_FOLDER.getPath() + File.separator + fileName);
        }
    }

}
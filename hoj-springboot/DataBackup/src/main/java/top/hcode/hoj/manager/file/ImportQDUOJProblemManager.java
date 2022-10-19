package top.hcode.hoj.manager.file;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusSystemErrorException;
import top.hcode.hoj.dao.problem.LanguageEntityService;
import top.hcode.hoj.dao.problem.ProblemEntityService;
import top.hcode.hoj.dao.problem.TagEntityService;
import top.hcode.hoj.exception.ProblemIDRepeatException;
import top.hcode.hoj.pojo.dto.ProblemDTO;
import top.hcode.hoj.pojo.dto.QDOJProblemDTO;
import top.hcode.hoj.pojo.entity.problem.Language;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.problem.ProblemCase;
import top.hcode.hoj.pojo.entity.problem.Tag;
import top.hcode.hoj.pojo.vo.UserRolesVO;
import top.hcode.hoj.utils.Constants;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/10 14:47
 * @Description:
 */
@Component
@Slf4j(topic = "hoj")
public class ImportQDUOJProblemManager {

    @Autowired
    private LanguageEntityService languageEntityService;

    @Autowired
    private ProblemEntityService problemEntityService;

    @Autowired
    private TagEntityService tagEntityService;

    /**
     * @param file
     * @MethodName importQDOJProblem
     * @Description zip文件导入题目 仅超级管理员可操作
     * @Return
     * @Since 2021/5/27
     */
    public void importQDOJProblem(MultipartFile file) throws StatusFailException, StatusSystemErrorException {

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
            throw new StatusSystemErrorException("服务器异常：qduoj题目上传失败！");
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
            throw new StatusFailException("压缩包里文件不能为空！");
        }


        HashMap<String, File> problemInfo = new HashMap<>();
        for (File tmp : files) {
            if (tmp.isDirectory()) {
                File[] problemAndTestcase = tmp.listFiles();
                if (problemAndTestcase == null || problemAndTestcase.length == 0) {
                    FileUtil.del(fileDir);
                    throw new StatusFailException("编号为：" + tmp.getName() + "的文件夹为空！");
                }
                for (File problemFile : problemAndTestcase) {
                    if (problemFile.isFile()) {
                        // 检查文件是否时json文件
                        if (!problemFile.getName().endsWith("json")) {
                            FileUtil.del(fileDir);
                            throw new StatusFailException("编号为：" + tmp.getName() + "的文件夹里面的题目数据格式错误，请使用json文件！");
                        }
                        problemInfo.put(tmp.getName(), problemFile);
                    }
                }
            }
        }

        // 读取json文件生成对象
        HashMap<String, QDOJProblemDTO> problemVoMap = new HashMap<>();
        for (String key : problemInfo.keySet()) {
            try {
                FileReader fileReader = new FileReader(problemInfo.get(key));
                JSONObject problemJson = JSONUtil.parseObj(fileReader.readString());
                QDOJProblemDTO qdojProblemDto = QDOJProblemToProblemVo(problemJson);
                problemVoMap.put(key, qdojProblemDto);
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
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        List<Tag> tagList = tagEntityService.list(new QueryWrapper<Tag>().eq("oj", "ME"));
        HashMap<String, Tag> tagMap = new HashMap<>();
        for (Tag tag : tagList) {
            tagMap.put(tag.getName().toUpperCase(), tag);
        }

        List<ProblemDTO> problemDTOS = new LinkedList<>();
        for (String key : problemInfo.keySet()) {
            QDOJProblemDTO qdojProblemDto = problemVoMap.get(key);
            // 格式化题目语言
            List<Language> languages = new LinkedList<>();
            for (String lang : qdojProblemDto.getLanguages()) {
                Long lid = languageMap.getOrDefault(lang, null);
                languages.add(new Language().setId(lid).setName(lang));
            }

            // 格式化标签
            List<Tag> tags = new LinkedList<>();
            for (String tagStr : qdojProblemDto.getTags()) {
                Tag tag = tagMap.getOrDefault(tagStr.toUpperCase(), null);
                if (tag == null) {
                    tags.add(new Tag().setName(tagStr).setOj("ME"));
                } else {
                    tags.add(tag);
                }
            }

            Problem problem = qdojProblemDto.getProblem();
            if (problem.getAuthor() == null) {
                problem.setAuthor(userRolesVo.getUsername());
            }
            ProblemDTO problemDto = new ProblemDTO();

            String mode = Constants.JudgeMode.DEFAULT.getMode();
            if (qdojProblemDto.getIsSpj()) {
                mode = Constants.JudgeMode.SPJ.getMode();
            }

            problemDto.setJudgeMode(mode)
                    .setProblem(problem)
                    .setCodeTemplates(qdojProblemDto.getCodeTemplates())
                    .setTags(tags)
                    .setLanguages(languages)
                    .setUploadTestcaseDir(fileDir + File.separator + key + File.separator + "testcase")
                    .setIsUploadTestCase(true)
                    .setSamples(qdojProblemDto.getSamples());

            problemDTOS.add(problemDto);
        }
        if (problemDTOS.size() == 0) {
            throw new StatusFailException("警告：未成功导入一道以上的题目，请检查文件格式是否正确！");
        } else {
            HashSet<String> repeatProblemTitleSet = new HashSet<>();
            HashSet<String> failedProblemTitleSet = new HashSet<>();
            int failedCount = 0;
            for (ProblemDTO problemDto : problemDTOS) {
                try {
                    boolean isOk = problemEntityService.adminAddProblem(problemDto);
                    if (!isOk) {
                        failedCount++;
                    }
                } catch (ProblemIDRepeatException e) {
                    repeatProblemTitleSet.add(problemDto.getProblem().getTitle());
                    failedCount++;
                } catch (Exception e) {
                    log.error("", e);
                    failedProblemTitleSet.add(problemDto.getProblem().getTitle());
                    failedCount++;
                }
            }
            if (failedCount > 0) {
                int successCount = problemDTOS.size() - failedCount;
                String errMsg = "[导入结果] 成功数：" + successCount + ",  失败数：" + failedCount +
                        ",  重复失败的题目标题：" + repeatProblemTitleSet;
                if (failedProblemTitleSet.size() > 0) {
                    errMsg = errMsg + "<br/>未知失败的题目标题：" + failedProblemTitleSet;
                }
                throw new StatusFailException(errMsg);
            }
        }
    }

    private QDOJProblemDTO QDOJProblemToProblemVo(JSONObject problemJson) {
        QDOJProblemDTO qdojProblemDto = new QDOJProblemDTO();
        List<String> tags = (List<String>) problemJson.get("tags");
        qdojProblemDto.setTags(tags.stream().map(UnicodeUtil::toString).collect(Collectors.toList()));
        qdojProblemDto.setLanguages(Arrays.asList("C", "C With O2", "C++", "C++ With O2", "Java", "Python3", "Python2", "Golang", "C#"));
        Object spj = problemJson.getObj("spj");
        boolean isSpj = !JSONUtil.isNull(spj);
        qdojProblemDto.setIsSpj(isSpj);

        Problem problem = new Problem();
        if (isSpj) {
            JSONObject spjJson = JSONUtil.parseObj(spj);
            problem.setSpjCode(spjJson.getStr("code"))
                    .setSpjLanguage(spjJson.getStr("language"));
        }
        problem.setAuth(1)
                .setIsGroup(false)
                .setIsUploadCase(true)
                .setSource(problemJson.getStr("source", null))
                .setDifficulty(1)
                .setProblemId(problemJson.getStr("display_id"))
                .setIsRemoveEndBlank(true)
                .setOpenCaseResult(true)
                .setCodeShare(false)
                .setType(problemJson.getStr("rule_type").equals("ACM") ? 0 : 1)
                .setTitle(problemJson.getStr("title"))
                .setDescription(UnicodeUtil.toString(problemJson.getJSONObject("description").getStr("value")))
                .setInput(UnicodeUtil.toString(problemJson.getJSONObject("input_description").getStr("value")))
                .setOutput(UnicodeUtil.toString(problemJson.getJSONObject("output_description").getStr("value")))
                .setHint(UnicodeUtil.toString(problemJson.getJSONObject("hint").getStr("value")))
                .setTimeLimit(problemJson.getInt("time_limit"))
                .setMemoryLimit(problemJson.getInt("memory_limit"));

        JSONArray samples = problemJson.getJSONArray("samples");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < samples.size(); i++) {
            JSONObject sample = (JSONObject) samples.get(i);
            String input = sample.getStr("input");
            String output = sample.getStr("output");
            sb.append("<input>").append(input).append("</input>");
            sb.append("<output>").append(output).append("</output>");
        }
        problem.setExamples(sb.toString());

        int sumScore = 0;
        JSONArray testcaseList = problemJson.getJSONArray("test_case_score");
        List<ProblemCase> problemSamples = new LinkedList<>();
        for (int i = 0; i < testcaseList.size(); i++) {
            JSONObject testcase = (JSONObject) testcaseList.get(i);
            String input = testcase.getStr("input_name");
            String output = testcase.getStr("output_name");
            Integer score = testcase.getInt("score", null);
            problemSamples.add(new ProblemCase().setInput(input).setOutput(output).setScore(score));
            if (score != null) {
                sumScore += score;
            }
        }
        problem.setIsRemote(false);
        problem.setIoScore(sumScore);
        qdojProblemDto.setSamples(problemSamples);
        qdojProblemDto.setProblem(problem);
        return qdojProblemDto;

    }
}
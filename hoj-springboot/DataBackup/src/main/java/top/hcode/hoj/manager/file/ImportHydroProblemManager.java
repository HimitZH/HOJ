package top.hcode.hoj.manager.file;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.Yaml;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusSystemErrorException;
import top.hcode.hoj.dao.problem.LanguageEntityService;
import top.hcode.hoj.dao.problem.ProblemEntityService;
import top.hcode.hoj.dao.problem.TagEntityService;
import top.hcode.hoj.exception.ProblemIDRepeatException;
import top.hcode.hoj.pojo.bo.HydroConfigYamlBO;
import top.hcode.hoj.pojo.dto.ProblemDTO;
import top.hcode.hoj.pojo.entity.problem.Language;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.problem.ProblemCase;
import top.hcode.hoj.pojo.entity.problem.Tag;
import top.hcode.hoj.pojo.vo.UserRolesVO;
import top.hcode.hoj.utils.Constants;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * @Author Himit_ZH
 * @Date 2022/10/15
 */
@Component
@Slf4j(topic = "hoj")
public class ImportHydroProblemManager {


    @Resource
    private TagEntityService tagEntityService;

    @Resource
    private ProblemEntityService problemEntityService;

    @Resource
    private LanguageEntityService languageEntityService;

    /**
     * 导入hydro的题目
     *
     * @param file
     * @throws StatusSystemErrorException
     * @throws StatusFailException
     */
    public void importHydroProblem(MultipartFile file) throws StatusSystemErrorException, StatusFailException {

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
            throw new StatusSystemErrorException("服务器异常：hydro题目上传失败！");
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

        List<Tag> tagList = tagEntityService.list(new QueryWrapper<Tag>().eq("oj", "ME"));
        HashMap<String, Tag> tagMap = new HashMap<>();
        for (Tag tag : tagList) {
            tagMap.put(tag.getName().toUpperCase(), tag);
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

        List<ProblemDTO> problemDTOList = new ArrayList<>();
        for (File dir : files) {
            if (dir.isDirectory()) {
                String rootDirPath = fileDir + File.separator + dir.getName();
                ProblemDTO problemDto = buildProblemDto(userRolesVo.getUsername(), rootDirPath, tagMap, languageMap, languageList);
                problemDTOList.add(problemDto);
            }
        }

        if (problemDTOList.size() == 0) {
            throw new StatusFailException("警告：未成功导入一道以上的题目，请检查文件格式是否正确！");
        } else {
            HashSet<String> repeatProblemTitleSet = new HashSet<>();
            HashSet<String> failedProblemTitleSet = new HashSet<>();
            int failedCount = 0;
            for (ProblemDTO problemDto : problemDTOList) {
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
                int successCount = problemDTOList.size() - failedCount;
                String errMsg = "[导入结果] 成功数：" + successCount + ",  失败数：" + failedCount +
                        ",  重复失败的题目标题：" + repeatProblemTitleSet;
                if (failedProblemTitleSet.size() > 0) {
                    errMsg = errMsg + "<br/>未知失败的题目标题：" + failedProblemTitleSet;
                }
                throw new StatusFailException(errMsg);
            }
        }

    }

    private ProblemDTO buildProblemDto(String author,
                                       String rootDirPath,
                                       HashMap<String, Tag> tagMap,
                                       HashMap<String, Long> languageMap,
                                       List<Language> languageList) {
        ProblemDTO dto = new ProblemDTO();
        Problem problem = new Problem();
        problem.setIsGroup(false)
                .setAuth(1)
                .setIsRemote(false)
                .setIsRemoveEndBlank(true)
                .setOpenCaseResult(true)
                .setCodeShare(false)
                .setType(1)
                .setDifficulty(1)
                .setAuthor(author)
                .setIsUploadCase(true);

        String testDataDirPath = rootDirPath + File.separator + "testdata";
        String configYaml = FileUtil.readString(testDataDirPath + File.separator + "config.yaml", StandardCharsets.UTF_8);
        parseConfigYaml(configYaml, dto, problem, testDataDirPath, languageMap, languageList);
        // config配置文件没有配置cases，则去遍历testdata文件夹获取
        if (CollectionUtils.isEmpty(dto.getSamples())) {
            dto.setSamples(buildSamples(testDataDirPath));
        }
        List<ProblemCase> samples = dto.getSamples();
        if (!CollectionUtils.isEmpty(samples) && samples.get(0).getScore() == null) {
            int len = samples.size();
            int averScore = 100 / len;
            for (int i = 0; i < len; i++) {
                if (i != len - 1) {
                    samples.get(i).setScore(averScore);
                } else {
                    samples.get(i).setScore(averScore + 100 % len);
                }
            }
        }

        String problemYaml = FileUtil.readString(rootDirPath + File.separator + "problem.yaml", StandardCharsets.UTF_8);
        parseProblemYaml(problemYaml, dto, problem, tagMap);

        String problemMarkPath = rootDirPath + File.separator + "problem_zh.md";
        String problemMarkdown = "";
        if (FileUtil.exist(problemMarkPath)) {
            problemMarkdown = FileUtil.readString(problemMarkPath, StandardCharsets.UTF_8);
        } else {
            problemMarkdown = FileUtil.readString(rootDirPath + File.separator + "problem.md", StandardCharsets.UTF_8);
        }

        parseMarkdown(problemMarkdown, problem, rootDirPath);

        dto.setProblem(problem);
        dto.setJudgeMode(problem.getJudgeMode());
        dto.setIsUploadTestCase(true);
        dto.setUploadTestcaseDir(testDataDirPath);
        return dto;
    }

    private List<ProblemCase> buildSamples(String testDataDirPath) {
        File testCaseFileList = new File(testDataDirPath);
        File[] files = testCaseFileList.listFiles();
        HashMap<String, String> inputMap = new HashMap<>();
        HashMap<String, String> outputMap = new HashMap<>();
        for (File file : files) {
            String fileName = file.getName();
            if (fileName.endsWith(".in")) {
                inputMap.put(fileName.replaceAll("\\.in", ""), fileName);
            } else if (fileName.endsWith(".out")) {
                outputMap.put(fileName.replaceAll("\\.out", ""), fileName);
            } else if (fileName.endsWith(".ans")) {
                outputMap.put(fileName.replaceAll("\\.ans", ""), fileName);
            }
        }
        List<ProblemCase> samples = new ArrayList<>();
        if (inputMap.size() > 0) {
            for (String key : inputMap.keySet()) {
                samples.add(new ProblemCase()
                        .setOutput(outputMap.get(key))
                        .setInput(inputMap.get(key)));
            }
        }
        return samples;
    }

    private void parseConfigYaml(String configYaml,
                                 ProblemDTO dto,
                                 Problem problem,
                                 String testDataDirPath,
                                 HashMap<String, Long> languageMap,
                                 List<Language> languageList) {
        Yaml yaml = new Yaml();
        HydroConfigYamlBO hydroConfigYamlBO = yaml.loadAs(configYaml, HydroConfigYamlBO.class);
        if (hydroConfigYamlBO != null) {
            if (Objects.equals(hydroConfigYamlBO.getType(), "default")) {
                problem.setJudgeMode(Constants.JudgeMode.DEFAULT.getMode());

                if (hydroConfigYamlBO.getChecker() != null) {
                    problem.setJudgeMode(Constants.JudgeMode.SPJ.getMode());
                    String code = FileUtil.readString(testDataDirPath + File.separator + hydroConfigYamlBO.getChecker(), StandardCharsets.UTF_8);
                    problem.setSpjCode(code);
                    if (hydroConfigYamlBO.getChecker().endsWith("cc")) {
                        problem.setSpjLanguage("C++");
                    } else {
                        problem.setSpjLanguage("C");
                    }
                }

            } else if (Objects.equals(hydroConfigYamlBO.getType(), "interactive")) {
                problem.setJudgeMode(Constants.JudgeMode.INTERACTIVE.getMode());
                String code = FileUtil.readString(testDataDirPath + File.separator + hydroConfigYamlBO.getInteractor(), StandardCharsets.UTF_8);
                problem.setSpjCode(code);
                if (hydroConfigYamlBO.getInteractor().endsWith("cc")) {
                    problem.setSpjLanguage("C++");
                } else {
                    problem.setSpjLanguage("C");
                }
            }

            if (!CollectionUtils.isEmpty(hydroConfigYamlBO.getJudge_extra_files())) {
                JSONObject jsonObject = new JSONObject();
                for (String fileName : hydroConfigYamlBO.getJudge_extra_files()) {
                    String code = FileUtil.readString(testDataDirPath + File.separator + fileName, StandardCharsets.UTF_8);
                    jsonObject.set(fileName, code);
                }
                problem.setJudgeExtraFile(jsonObject.toString());
            }

            if (!CollectionUtils.isEmpty(hydroConfigYamlBO.getUser_extra_files())) {
                JSONObject jsonObject = new JSONObject();
                for (String fileName : hydroConfigYamlBO.getUser_extra_files()) {
                    String code = FileUtil.readString(testDataDirPath + File.separator + fileName, StandardCharsets.UTF_8);
                    jsonObject.set(fileName, code);
                }
                problem.setUserExtraFile(jsonObject.toString());
            }

            if (hydroConfigYamlBO.getTime() != null) {
                String timeStr = hydroConfigYamlBO.getTime().toLowerCase();
                if (timeStr.endsWith("ms")) {
                    double time = Double.parseDouble(timeStr.replace("ms", ""));
                    problem.setTimeLimit((int) time);
                } else if (timeStr.endsWith("s")) {
                    double time = Double.parseDouble(timeStr.replace("s", ""));
                    problem.setTimeLimit((int) (time * 1000));
                }
            }

            if (hydroConfigYamlBO.getMemory() != null) {
                String memoryStr = hydroConfigYamlBO.getMemory().toLowerCase();
                if (memoryStr.endsWith("m")) {
                    double memory = Double.parseDouble(memoryStr.replace("m", ""));
                    problem.setMemoryLimit((int) memory);
                } else if (memoryStr.endsWith("mb")) {
                    double memory = Double.parseDouble(memoryStr.replace("mb", ""));
                    problem.setMemoryLimit((int) memory);
                }
            }

            problem.setJudgeCaseMode(Constants.JudgeCaseMode.DEFAULT.getMode());
            if (hydroConfigYamlBO.getCases() != null) {
                List<ProblemCase> samples = new ArrayList<>();
                for (HashMap<String, String> map : hydroConfigYamlBO.getCases()) {
                    samples.add(new ProblemCase()
                            .setInput(map.get("input"))
                            .setOutput(map.get("output"))
                    );
                }
                dto.setSamples(samples);
            } else if (hydroConfigYamlBO.getSubtasks() != null) {
                problem.setJudgeCaseMode(Constants.JudgeCaseMode.SUBTASK_LOWEST.getMode());
                List<ProblemCase> samples = new ArrayList<>();
                int groupNum = 1;
                for (HashMap<String, Object> taskMap : hydroConfigYamlBO.getSubtasks()) {
                    Integer score = (Integer) taskMap.get("score");
                    List<Map<String, String>> cases = (List<Map<String, String>>) taskMap.get("cases");
                    for (Map<String, String> caseMap : cases) {
                        samples.add(new ProblemCase()
                                .setGroupNum(groupNum)
                                .setScore(score)
                                .setOutput(caseMap.get("output"))
                                .setInput(caseMap.get("input")));
                    }
                    groupNum++;
                }
                dto.setSamples(samples);
            }

            if (hydroConfigYamlBO.getLangs() != null) {
                dto.setLanguages(buildLanguageList(hydroConfigYamlBO.getLangs(), languageMap));
            } else {
                dto.setLanguages(languageList);
            }
        }
    }

    private void parseProblemYaml(String problemYaml,
                                  ProblemDTO dto,
                                  Problem problem,
                                  HashMap<String, Tag> tagMap) {
        Yaml yaml = new Yaml();
        Map<String, Object> map = yaml.load(problemYaml);

        String pid = (String) map.get("pid");
        if (!"null".equals(pid)) {
            problem.setProblemId(pid);
        }
        problem.setTitle((String) map.get("title"));
        List<String> tags = (List<String>) map.get("tag");
        if (!CollectionUtils.isEmpty(tags)) {
            // 格式化标签
            List<Tag> tagList = new LinkedList<>();
            for (String tagStr : tags) {
                Tag tag = tagMap.getOrDefault(tagStr.toUpperCase(), null);
                if (tag == null) {
                    tagList.add(new Tag().setName(tagStr).setOj("ME"));
                } else {
                    tagList.add(tag);
                }
            }
            dto.setTags(tagList);
        }
    }

    private void parseMarkdown(String md, Problem problem, String rootDirPath) {
        List<String> inputExampleList = ReUtil.findAll("```input\\d+\n([\\s\\S]*?)\n```", md, 1);
        List<String> outputExampleList = ReUtil.findAll("```output\\d+\n([\\s\\S]*?)\n```", md, 1);
        if (!CollectionUtils.isEmpty(inputExampleList)) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < inputExampleList.size(); i++) {
                sb.append("<input>");
                String input = inputExampleList.get(i);
                sb.append(input).append("</input>");
                sb.append("<output>");
                if (outputExampleList != null && outputExampleList.size() > i) {
                    String output = outputExampleList.get(i);
                    sb.append(output);
                }
                sb.append("</output>");
            }
            problem.setExamples(sb.toString());
        }

        // 处理题面中的图片等文件
        // file://0eoF-SShe0X83tQXNw1mb.png
        List<String> fileNameList = ReUtil.findAll("\\(file://([\\s\\S]*?)\\)", md, 1);
        //将文件保存指定目录
        String additionalFilePath = rootDirPath + File.separator + "additional_file";
        if (!CollectionUtils.isEmpty(fileNameList) && FileUtil.exist(additionalFilePath)) {
            for (String filename : fileNameList) {
                String filePath = additionalFilePath + File.separator + filename;
                if (FileUtil.exist(filePath)) {
                    FileUtil.copyFile(filePath, Constants.File.MARKDOWN_FILE_FOLDER.getPath() + File.separator + filename, StandardCopyOption.REPLACE_EXISTING);
                    String lowerName = filename.toLowerCase();
                    if (lowerName.endsWith(".png")
                            || lowerName.equals(".jpg")
                            || lowerName.equals(".gif")
                            || lowerName.equals(".jpeg")
                            || lowerName.equals(".webp")) {
                        md = md.replace("file://" + filename, Constants.File.IMG_API.getPath() + filename);
                    } else {
                        md = md.replace("file://" + filename, Constants.File.FILE_API.getPath() + filename);
                    }
                }
            }
        }

        String description = md.replaceAll("```input\\d+\n[\\s\\S]*?\n```", "")
                .replaceAll("```output\\d+\n[\\s\\S]*?\n```", "");
        problem.setDescription(description);
    }

    private List<Language> buildLanguageList(List<String> langList, HashMap<String, Long> languageMap) {
        List<Language> languages = new LinkedList<>();
        for (String lang : langList) {
            String finalLang = "";
            if (lang.contains("cc") && lang.contains("o2")) {
                finalLang = "C++ With O2";
            } else if (lang.contains("cc")) {
                finalLang = "C++";
            } else if (lang.contains("c") && lang.contains("o2")) {
                finalLang = "C With O2";
            } else if (lang.contains("cs")) {
                finalLang = "C#";
            } else if (lang.contains("c")) {
                finalLang = "C";
            } else if (lang.contains("go")) {
                finalLang = "Golang";
            } else if (lang.contains("java")) {
                finalLang = "Java";
            } else if (lang.contains("php")) {
                finalLang = "PHP";
            } else if (lang.contains("nodejs")) {
                finalLang = "JavaScript Node";
            } else if (lang.contains("py2")) {
                finalLang = "PyPy2";
            } else if (lang.contains("py3")) {
                finalLang = "PyPy3";
            } else if (lang.contains("py")) {
                finalLang = "Python3";
            }
            Long lid = languageMap.getOrDefault(finalLang, null);
            languages.add(new Language().setId(lid));
        }
        return languages;
    }
}

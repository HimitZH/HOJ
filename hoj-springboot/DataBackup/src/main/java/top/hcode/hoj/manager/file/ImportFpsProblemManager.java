package top.hcode.hoj.manager.file;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.XmlUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.pojo.dto.ProblemDto;
import top.hcode.hoj.pojo.entity.problem.CodeTemplate;
import top.hcode.hoj.pojo.entity.problem.Language;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.problem.ProblemCase;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.dao.problem.LanguageEntityService;
import top.hcode.hoj.dao.problem.ProblemEntityService;
import top.hcode.hoj.utils.Constants;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/10 14:44
 * @Description:
 */

@Component
public class ImportFpsProblemManager {

    private final static List<String> timeUnits = Arrays.asList("ms", "s");
    private final static List<String> memoryUnits = Arrays.asList("kb", "mb");
    private static final Map<String, String> fpsMapHOJ = new HashMap<String, String>() {
        {
            put("Python", "Python3");
            put("Go", "Golang");
            put("C", "C");
            put("C++", "C++");
            put("Java", "Java");
            put("C#", "C#");
        }
    };

    @Resource
    private LanguageEntityService languageEntityService;

    @Resource
    private ProblemEntityService problemEntityService;

    /**
     * @param file
     * @MethodName importFpsProblem
     * @Description zip文件导入题目 仅超级管理员可操作
     * @Return
     * @Since 2021/10/06
     */
    @Transactional(rollbackFor = Exception.class)
    public void importFPSProblem(MultipartFile file) throws IOException, StatusFailException {
        String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
        if (!"xml".toUpperCase().contains(suffix.toUpperCase())) {
            throw new StatusFailException("请上传xml后缀格式的fps题目文件！");
        }
        // 获取当前登录的用户
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        List<ProblemDto> problemDtoList = parseFps(file.getInputStream(), userRolesVo.getUsername());
        for (ProblemDto problemDto : problemDtoList) {
            problemEntityService.adminAddProblem(problemDto);
        }

    }

    private List<ProblemDto> parseFps(InputStream inputStream, String username){
        Document document = XmlUtil.readXML(inputStream);
        Element rootElement = XmlUtil.getRootElement(document);
        String version = rootElement.getAttribute("version");

        List<ProblemDto> problemDtoList = new ArrayList<>();

        String fileDirId = IdUtil.simpleUUID();
        String fileDir = Constants.File.TESTCASE_TMP_FOLDER.getPath() + File.separator + fileDirId;

        int index = 1;
        for (Element item : XmlUtil.getElements(rootElement, "item")) {

            Problem problem = new Problem();

            problem.setAuthor(username)
                    .setType(0)
                    .setIsUploadCase(true)
                    .setDifficulty(1)
                    .setIsRemoveEndBlank(true)
                    .setOpenCaseResult(true)
                    .setCodeShare(false)
                    .setIsRemote(false)
                    .setAuth(1)
                    .setIsGroup(false)
                    .setProblemId(String.valueOf(System.currentTimeMillis()));

            Element title = XmlUtil.getElement(item, "title");
            // 标题
            problem.setTitle(title.getTextContent());

            HashMap<String, String> srcMapUrl = new HashMap<>();
            List<Element> images = XmlUtil.getElements(item, "img");
            for (Element img : images) {
                Element srcElement = XmlUtil.getElement(img, "src");
                if (srcElement == null) {
                    continue;
                }
                String src = srcElement.getTextContent();
                String base64 = XmlUtil.getElement(img, "base64").getTextContent();
                String[] split = src.split("\\.");

                byte[] decode = Base64.getDecoder().decode(base64);
                String fileName = IdUtil.fastSimpleUUID() + "." + split[split.length - 1];

                FileUtil.writeBytes(decode, Constants.File.MARKDOWN_FILE_FOLDER.getPath() + File.separator + fileName);
                srcMapUrl.put(src, Constants.File.IMG_API.getPath() + fileName);
            }

            Element descriptionElement = XmlUtil.getElement(item, "description");
            String description = descriptionElement.getTextContent();
            for (Map.Entry<String, String> entry : srcMapUrl.entrySet()) {
                description = description.replaceAll(entry.getKey(), entry.getValue());
            }
            // 题目描述
            problem.setDescription(description);

            Element inputElement = XmlUtil.getElement(item, "input");
            String input = inputElement.getTextContent();
            for (Map.Entry<String, String> entry : srcMapUrl.entrySet()) {
                input = input.replaceAll(entry.getKey(), entry.getValue());
            }
            // 输入描述
            problem.setInput(input);

            Element outputElement = XmlUtil.getElement(item, "output");
            String output = outputElement.getTextContent();
            for (Map.Entry<String, String> entry : srcMapUrl.entrySet()) {
                output = output.replaceAll(entry.getKey(), entry.getValue());
            }
            // 输出描述
            problem.setOutput(output);

            // 提示
            Element hintElement = XmlUtil.getElement(item, "hint");
            String hint = hintElement.getTextContent();
            for (Map.Entry<String, String> entry : srcMapUrl.entrySet()) {
                hint = hint.replaceAll(entry.getKey(), entry.getValue());
            }
            problem.setHint(hint);

            // 来源
            Element sourceElement = XmlUtil.getElement(item, "source");
            String source = sourceElement.getTextContent();
            problem.setSource(source);

            // ms
            Integer timeLimit = getTimeLimit(version, item);
            problem.setTimeLimit(timeLimit);

            // mb
            Integer memoryLimit = getMemoryLimit(item);
            problem.setMemoryLimit(memoryLimit);

            // 题面用例
            List<Element> sampleInputs = XmlUtil.getElements(item, "sample_input");
            List<Element> sampleOutputs = XmlUtil.getElements(item, "sample_output");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < sampleInputs.size(); i++) {
                sb.append("<input>").append(sampleInputs.get(i).getTextContent()).append("</input>");
                sb.append("<output>").append(sampleOutputs.get(i).getTextContent()).append("</output>");
            }
            problem.setExamples(sb.toString());


            QueryWrapper<Language> languageQueryWrapper = new QueryWrapper<>();
            languageQueryWrapper.eq("oj", "ME");
            List<Language> languageList = languageEntityService.list(languageQueryWrapper);

            HashMap<String, Long> languageMap = new HashMap<>();
            for (Language language : languageList) {
                languageMap.put(language.getName(), language.getId());
            }

            // 题目模板
            List<Element> templateNodes = XmlUtil.getElements(item, "template");
            List<CodeTemplate> codeTemplates = new ArrayList<>();
            for (Element templateNode : templateNodes) {
                String templateLanguage = templateNode.getAttribute("language");
                String templateCode = templateNode.getTextContent();
                if (templateLanguage == null || templateCode == null) {
                    continue;
                }
                String lang = fpsMapHOJ.get(templateLanguage);
                if (lang != null) {
                    codeTemplates.add(new CodeTemplate()
                            .setCode(templateCode)
                            .setLid(languageMap.get(lang)));
                }

            }

            // spj
            Element spjNode = XmlUtil.getElement(item, "spj");
            if (spjNode != null) {
                String spjLanguage = spjNode.getAttribute("language");
                String spjCode = spjNode.getTextContent();
                if (("C".equals(spjLanguage) || "C++".equals(spjLanguage)) && !StringUtils.isEmpty(spjCode)) {
                    problem.setSpjLanguage(spjLanguage)
                            .setSpjCode(spjCode);
                }
            }

            // 题目评测数据
            List<Element> testInputs = XmlUtil.getElements(item, "test_input");
            List<Element> testOutputs = XmlUtil.getElements(item, "test_output");
            List<ProblemCase> problemSamples = new LinkedList<>();
            String problemTestCaseDir = fileDir + File.separator + index;
            for (int i = 0; i < testInputs.size(); i++) {
                String infileName = (i + 1) + ".in";
                String outfileName = (i + 1) + ".out";
                FileWriter infileWriter = new FileWriter(problemTestCaseDir + File.separator + infileName);
                FileWriter outfileWriter = new FileWriter(problemTestCaseDir + File.separator + outfileName);
                infileWriter.write(testInputs.get(i).getTextContent());
                outfileWriter.write(testOutputs.get(i).getTextContent());
                problemSamples.add(new ProblemCase()
                        .setInput(infileName).setOutput(outfileName));
            }
            String mode = Constants.JudgeMode.DEFAULT.getMode();
            if (problem.getSpjLanguage() != null) {
                mode = Constants.JudgeMode.SPJ.getMode();
            }
            ProblemDto problemDto = new ProblemDto();
            problemDto.setSamples(problemSamples)
                    .setIsUploadTestCase(true)
                    .setUploadTestcaseDir(problemTestCaseDir)
                    .setLanguages(languageList)
                    .setTags(null)
                    .setJudgeMode(mode)
                    .setProblem(problem)
                    .setCodeTemplates(codeTemplates);

            problemDtoList.add(problemDto);
            index++;
        }
        return problemDtoList;
    }


    private Integer getTimeLimit(String version, Element item) {
        Element timeLimitNode = XmlUtil.getElement(item, "time_limit");
        String timeUnit = timeLimitNode.getAttribute("unit");
        String timeLimit = timeLimitNode.getTextContent();
        int index = timeUnits.indexOf(timeUnit.toLowerCase());
        if (index == -1) {
            throw new RuntimeException("Invalid time limit unit:" + timeUnit);
        }
        if ("1.1".equals(version)) {
            return Integer.parseInt(timeLimit) * (int) Math.pow(1000, index);
        } else {
            double tmp = (Double.parseDouble(timeLimit) * Math.pow(1000, index));
            return (int) tmp;
        }
    }

    private Integer getMemoryLimit(Element item) {
        Element memoryLimitNode = XmlUtil.getElement(item, "memory_limit");
        String memoryUnit = memoryLimitNode.getAttribute("unit");
        String memoryLimit = memoryLimitNode.getTextContent();
        int index = memoryUnits.indexOf(memoryUnit.toLowerCase());
        if (index == -1) {
            throw new RuntimeException("Invalid memory limit unit:" + memoryUnit);
        }

        return Integer.parseInt(memoryLimit) * (int) Math.pow(1000, index - 1);
    }
}
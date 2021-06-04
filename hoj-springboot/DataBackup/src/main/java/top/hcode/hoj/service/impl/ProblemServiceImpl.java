package top.hcode.hoj.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.YamlJsonParser;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;
import top.hcode.hoj.crawler.problem.CFProblemStrategy;
import top.hcode.hoj.crawler.problem.HDUProblemStrategy;
import top.hcode.hoj.crawler.problem.ProblemContext;
import top.hcode.hoj.crawler.problem.ProblemStrategy;
import top.hcode.hoj.pojo.dto.ProblemDto;
import top.hcode.hoj.pojo.entity.*;
import top.hcode.hoj.pojo.vo.ImportProblemVo;
import top.hcode.hoj.pojo.vo.ProblemVo;
import top.hcode.hoj.dao.ProblemMapper;
import top.hcode.hoj.service.ProblemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.utils.Constants;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
@Service
public class ProblemServiceImpl extends ServiceImpl<ProblemMapper, Problem> implements ProblemService {

    @Autowired
    private ProblemMapper problemMapper;

    @Autowired
    private ProblemCaseServiceImpl problemCaseService;


    @Autowired
    private ProblemLanguageServiceImpl problemLanguageService;

    @Autowired
    private LanguageServiceImpl languageService;

    @Autowired
    private TagServiceImpl tagService;

    @Autowired
    private ProblemTagServiceImpl problemTagService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CodeTemplateServiceImpl codeTemplateService;

    @Override
    public Page<ProblemVo> getProblemList(int limit, int currentPage, Long pid, String title, Integer difficulty,
                                          Long tid, String oj) {

        //新建分页
        Page<ProblemVo> page = new Page<>(currentPage, limit);

        return page.setRecords(problemMapper.getProblemList(page, pid, title, difficulty, tid, oj));
    }

    @Override
    @Transactional
    public boolean adminUpdateProblem(ProblemDto problemDto) {

        Problem problem = problemDto.getProblem();

        if (!problemDto.getIsSpj()) {
            problem.setSpjLanguage(null).setSpjCode(null);
        }
        problem.setProblemId(problem.getProblemId().toUpperCase());
        // 后面许多表的更新或删除需要用到题目id
        long pid = problemDto.getProblem().getId();
        Map<String, Object> map = new HashMap<>();
        map.put("pid", pid);
        //查询出原来题目的关联表数据
        List<ProblemTag> oldProblemTags = (List<ProblemTag>) problemTagService.listByMap(map);
        List<ProblemLanguage> oldProblemLanguages = (List<ProblemLanguage>) problemLanguageService.listByMap(map);
        List<ProblemCase> oldProblemCases = (List<ProblemCase>) problemCaseService.listByMap(map);
        List<CodeTemplate> oldProblemTemplate = (List<CodeTemplate>) codeTemplateService.listByMap(map);

        Map<Long, Integer> mapOldPT = new HashMap<>();
        Map<Long, Integer> mapOldPL = new HashMap<>();
        Map<Integer, Integer> mapOldPCT = new HashMap<>();
        List<Long> needDeleteProblemCases = new LinkedList<>();
        HashMap<Long, ProblemCase> oldProblemMap = new HashMap<>();

        // 登记一下原有的tag的id
        oldProblemTags.stream().forEach(problemTag -> {
            mapOldPT.put(problemTag.getTid(), 0);
        });
        // 登记一下原有的language的id
        oldProblemLanguages.stream().forEach(problemLanguage -> {
            mapOldPL.put(problemLanguage.getLid(), 0);
        });
        // 登记一下原有的codeTemplate的id
        oldProblemTemplate.stream().forEach(codeTemplate -> {
            mapOldPCT.put(codeTemplate.getId(), 0);
        });
        // 登记一下原有的case的id
        oldProblemCases.stream().forEach(problemCase -> {
            needDeleteProblemCases.add(problemCase.getId());
            oldProblemMap.put(problemCase.getId(), problemCase);
        });


        // 与前端上传的数据进行对比，添加或删除！
        /**
         * 处理tag表与problem_tag表的删除与更新
         */
        List<ProblemTag> problemTagList = new LinkedList<>(); // 存储新的problem_tag表数据
        for (Tag tag : problemDto.getTags()) {
            if (tag.getId() == null) { // 没有主键表示为新添加的标签
                boolean addTagResult = tagService.save(tag);
                if (addTagResult) {
                    problemTagList.add(new ProblemTag()
                            .setPid(pid).setTid(tag.getId()));
                }
            } else { // 已有主键的需要记录一下，若原先在problem_tag有的，现在不见了，表明需要删除
                mapOldPT.put(tag.getId(), 1); // 更新记录，说明该tag未删除
            }
        }
        // 放入需要删除的tagId列表
        List<Long> needDeleteTids = new LinkedList<>();
        for (Long key : mapOldPT.keySet()) {
            if (mapOldPT.get(key) == 0) { // 记录表中没有更新原来的存在Tid，则表明该tag已不被该problem使用
                needDeleteTids.add(key);
            }
        }
        boolean deleteTagsFromProblemResult = true;
        if (needDeleteTids.size() > 0) {
            QueryWrapper<ProblemTag> tagWrapper = new QueryWrapper<>();
            tagWrapper.eq("pid", pid).in("tid", needDeleteTids);
            // 执行批量删除操作
            deleteTagsFromProblemResult = problemTagService.remove(tagWrapper);
        }
        // 执行批量插入操作
        boolean addTagsToProblemResult = true;
        if (problemTagList.size() > 0) {
            addTagsToProblemResult = problemTagService.saveOrUpdateBatch(problemTagList);
        }

        /**
         * 处理code_template表
         */
        boolean deleteTemplate = true;
        boolean saveOrUpdateCodeTemplate = true;
        for (CodeTemplate codeTemplate : problemDto.getCodeTemplates()) {
            if (codeTemplate.getId() != null) {
                mapOldPCT.put(codeTemplate.getId(), 1);
            }
        }
        // 需要删除的模板
        List<Integer> needDeleteCTs = new LinkedList<>();
        for (Integer key : mapOldPCT.keySet()) {
            if (mapOldPCT.get(key) == 0) {
                needDeleteCTs.add(key);
            }
        }
        if (needDeleteCTs.size() > 0) {
            deleteTemplate = codeTemplateService.removeByIds(needDeleteCTs);
        }
        if (problemDto.getCodeTemplates().size() > 0) {
            saveOrUpdateCodeTemplate = codeTemplateService.saveOrUpdateBatch(problemDto.getCodeTemplates());
        }


        /**
         *  处理problem_language表的更新与删除
         */

        // 根据上传来的language列表的每一个name字段查询对应的language表的id，更新problem_language
        //构建problem_language实体列表
        List<ProblemLanguage> problemLanguageList = new LinkedList<>();
        for (Language language : problemDto.getLanguages()) { // 遍历插入
            if (mapOldPL.get(language.getId()) != null) { // 如果记录中有，则表式该language原来已有选中。
                mapOldPL.put(language.getId(), 1); // 记录一下，新数据也有该language
            } else { // 没有记录，则表明为新添加的language
                problemLanguageList.add(new ProblemLanguage().setLid(language.getId()).setPid(pid));
            }
        }
        // 放入需要删除的languageId列表
        List<Long> needDeleteLids = new LinkedList<>();
        for (Long key : mapOldPL.keySet()) {
            if (mapOldPL.get(key) == 0) { // 记录表中没有更新原来的存在Lid，则表明该language已不被该problem使用
                needDeleteLids.add(key);
            }
        }
        boolean deleteLanguagesFromProblemResult = true;
        if (needDeleteLids.size() > 0) {
            QueryWrapper<ProblemLanguage> LangWrapper = new QueryWrapper<>();
            LangWrapper.eq("pid", pid).in("lid", needDeleteLids);
            // 执行批量删除操作
            deleteLanguagesFromProblemResult = problemLanguageService.remove(LangWrapper);
        }
        // 执行批量添加操作
        boolean addLanguagesToProblemResult = true;
        if (problemLanguageList.size() > 0) {
            addLanguagesToProblemResult = problemLanguageService.saveOrUpdateBatch(problemLanguageList);
        }


        /**
         *  处理problem_case表的增加与删除
         */

        boolean checkProblemCase = true;

        if (!problem.getIsRemote() && problemDto.getSamples().size() > 0) { // 如果是自家的题目才有测试数据
            int sumScore = 0;
            // 新增加的case列表
            List<ProblemCase> newProblemCaseList = new LinkedList<>();
            // 需要修改的case列表
            List<ProblemCase> needUpdateProblemCaseList = new LinkedList<>();
            // 遍历上传的case列表，如果还存在，则从需要删除的测试样例列表移除该id
            for (ProblemCase problemCase : problemDto.getSamples()) {
                if (problemCase.getId() != null) { // 已存在的case
                    needDeleteProblemCases.remove(problemCase.getId());
                    // 跟原先的数据做对比，如果变动 则加入需要修改的case列表
                    ProblemCase oldProblemCase = oldProblemMap.get(problemCase.getId());
                    if (!oldProblemCase.getInput().equals(problemCase.getInput()) ||
                            !oldProblemCase.getOutput().equals(problemCase.getOutput())) {
                        needUpdateProblemCaseList.add(problemCase);
                    } else if (problem.getType().intValue() == Constants.Contest.TYPE_OI.getCode()) {
                        if (oldProblemCase.getScore().intValue() != problemCase.getScore()) {
                            needUpdateProblemCaseList.add(problemCase);
                        }
                    }
                } else {
                    newProblemCaseList.add(problemCase.setPid(pid));
                }
                if (problemCase.getScore() != null) {
                    sumScore += problemCase.getScore();
                }
            }
            // 设置oi总分数，根据每个测试点的加和
            if (problem.getType().intValue() == Constants.Contest.TYPE_OI.getCode()) {
                problem.setIoScore(sumScore);
            }
            // 执行批量删除操作
            boolean deleteCasesFromProblemResult = true;
            if (needDeleteProblemCases.size() > 0) {
                deleteCasesFromProblemResult = problemCaseService.removeByIds(needDeleteProblemCases);
            }
            // 执行批量添加操作
            boolean addCasesToProblemResult = true;
            if (newProblemCaseList.size() > 0) {
                addCasesToProblemResult = problemCaseService.saveBatch(newProblemCaseList);
            }
            // 执行批量修改操作
            boolean updateCasesToProblemResult = true;
            if (needUpdateProblemCaseList.size() > 0) {
                updateCasesToProblemResult = problemCaseService.saveOrUpdateBatch(needUpdateProblemCaseList);
            }
            checkProblemCase = addCasesToProblemResult && deleteCasesFromProblemResult && updateCasesToProblemResult;

            // 只要有新添加，修改，删除都需要更新版本号 同时更新测试数据
            String caseVersion = String.valueOf(System.currentTimeMillis());
            String testcaseDir = problemDto.getUploadTestcaseDir();
            if (needDeleteProblemCases.size() > 0 || newProblemCaseList.size() > 0
                    || needUpdateProblemCaseList.size() > 0 || !StringUtils.isEmpty(testcaseDir)) {
                problem.setCaseVersion(caseVersion);
                // 如果是选择上传测试文件的，临时文件路径不为空，则需要遍历对应文件夹，读取数据，写入数据库,先前的题目数据一并清空。
                if (problemDto.getIsUploadTestCase() && !StringUtils.isEmpty(testcaseDir)) {
                    // 获取代理bean对象执行异步方法===》根据测试文件初始info
                    applicationContext.getBean(ProblemServiceImpl.class).initUploadTestCase(problemDto.getIsSpj(), caseVersion, pid, testcaseDir, problemDto.getSamples());
                } else {
                    applicationContext.getBean(ProblemServiceImpl.class).initHandTestCase(problemDto.getIsSpj(), problem.getCaseVersion(), pid, problemDto.getSamples());
                }
            }
            // 变化成spj或者取消 同时更新测试数据
            else if (problemDto.getChangeSpj() != null && problemDto.getChangeSpj()) {
                problem.setCaseVersion(caseVersion);
                if (problemDto.getIsUploadTestCase()) {
                    // 获取代理bean对象执行异步方法===》根据测试文件初始info
                    applicationContext.getBean(ProblemServiceImpl.class).initUploadTestCase(problemDto.getIsSpj(), caseVersion, pid, null, problemDto.getSamples());
                } else {
                    applicationContext.getBean(ProblemServiceImpl.class).initHandTestCase(problemDto.getIsSpj(), problem.getCaseVersion(), pid, problemDto.getSamples());
                }
            }
        }

        // 更新problem表
        boolean problemUpdateResult = problemMapper.updateById(problem) == 1;

        if (problemUpdateResult && checkProblemCase && deleteLanguagesFromProblemResult && deleteTagsFromProblemResult
                && addLanguagesToProblemResult && addTagsToProblemResult && deleteTemplate && saveOrUpdateCodeTemplate) {
            return true;
        } else {
            return false;
        }

    }

    @Override
    @Transactional
    public boolean adminAddProblem(ProblemDto problemDto) {

        Problem problem = problemDto.getProblem();

        if (!problemDto.getIsSpj()) {
            problem.setSpjLanguage(null).setSpjCode(null);
        }

        // 设置测试样例的版本号
        problem.setCaseVersion(String.valueOf(System.currentTimeMillis()));
        problem.setProblemId(problem.getProblemId().toUpperCase());
        boolean addProblemResult = problemMapper.insert(problem) == 1;
        long pid = problem.getId();
        // 为新的题目添加对应的language
        List<ProblemLanguage> problemLanguageList = new LinkedList<>();
        for (Language language : problemDto.getLanguages()) {
            problemLanguageList.add(new ProblemLanguage().setPid(pid).setLid(language.getId()));
        }
        boolean addLangToProblemResult = problemLanguageService.saveOrUpdateBatch(problemLanguageList);

        // 为新的题目添加对应的codeTemplate
        boolean addProblemCodeTemplate = true;
        if (problemDto.getCodeTemplates() != null && problemDto.getCodeTemplates().size() > 0) {
            for (CodeTemplate codeTemplate : problemDto.getCodeTemplates()) {
                codeTemplate.setPid(pid);
            }
            addProblemCodeTemplate = codeTemplateService.saveOrUpdateBatch(problemDto.getCodeTemplates());
        }


        boolean addCasesToProblemResult = true;
        // 为新的题目添加对应的case
        if (problemDto.getIsUploadTestCase()) { // 如果是选择上传测试文件的，则需要遍历对应文件夹，读取数据。。
            int sumScore = 0;
            String testcaseDir = problemDto.getUploadTestcaseDir();
            // 如果是io题目统计总分
            List<ProblemCase> problemCases = problemDto.getSamples();
            for (ProblemCase problemCase : problemCases) {
                if (problemCase.getScore() != null) {
                    sumScore += problemCase.getScore();
                }
                problemCase.setPid(pid);
            }
            // 设置oi总分数，根据每个测试点的加和
            if (problem.getType().intValue() == Constants.Contest.TYPE_OI.getCode()) {
                problem.setIoScore(sumScore);
            }
            addCasesToProblemResult = problemCaseService.saveOrUpdateBatch(problemCases);
            // 获取代理bean对象执行异步方法===》根据测试文件初始info
            applicationContext.getBean(ProblemServiceImpl.class).initUploadTestCase(problemDto.getIsSpj(),
                    problem.getCaseVersion(), pid, testcaseDir, problemDto.getSamples());
        } else {
            // oi题目需要求取平均值，给每个测试点初始oi的score值，默认总分100分
            if (problem.getType().intValue() == Constants.Contest.TYPE_OI.getCode()) {
                problem.setIoScore(100);
                final int averScore = 100 / problemDto.getSamples().size();
                problemDto.getSamples().forEach(problemCase -> problemCase.setPid(pid).setScore(averScore)); // 设置好新题目的pid及分数
                addCasesToProblemResult = problemCaseService.saveOrUpdateBatch(problemDto.getSamples());
            } else {
                problemDto.getSamples().forEach(problemCase -> problemCase.setPid(pid)); // 设置好新题目的pid
                addCasesToProblemResult = problemCaseService.saveOrUpdateBatch(problemDto.getSamples());
            }
            initHandTestCase(problemDto.getIsSpj(), problem.getCaseVersion(), pid, problemDto.getSamples());
        }

        // 为新的题目添加对应的tag，可能tag是原表已有，也可能是新的，所以需要判断。
        List<ProblemTag> problemTagList = new LinkedList<>();
        for (Tag tag : problemDto.getTags()) {
            if (tag.getId() == null) { //id为空 表示为原tag表中不存在的 插入后可以获取到对应的tagId
                try {
                    tagService.save(tag);
                } catch (Exception ignored) {
                    tag = tagService.getOne(new QueryWrapper<Tag>().eq("name", tag.getName()));
                }
            }
            problemTagList.add(new ProblemTag().setTid(tag.getId()).setPid(pid));
        }
        boolean addTagsToProblemResult = problemTagService.saveOrUpdateBatch(problemTagList);


        if (addProblemResult && addCasesToProblemResult && addLangToProblemResult
                && addTagsToProblemResult && addProblemCodeTemplate) {
            return true;
        } else {
            return false;
        }
    }

    // 初始化上传文件的测试数据，写成json文件
    @Async
    public void initUploadTestCase(Boolean isSpj,
                                   String version,
                                   Long problemId,
                                   String tmpTestcaseDir,
                                   List<ProblemCase> problemCaseList) {

        String testCasesDir = Constants.File.TESTCASE_BASE_FOLDER.getPath() + File.separator + "problem_" + problemId;

        // 将之前的临时文件夹里面的评测文件全部复制到指定文件夹(覆盖)
        if (tmpTestcaseDir != null) {
            FileUtil.copyFilesFromDir(new File(tmpTestcaseDir), new File(testCasesDir), true);
        }

        JSONObject result = new JSONObject();
        result.set("isSpj", isSpj);
        result.set("version", version);
        result.set("testCasesSize", problemCaseList.size());
        result.set("testCases", new JSONArray());


        for (ProblemCase problemCase : problemCaseList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("caseId", null);
            jsonObject.set("score", problemCase.getScore());
            jsonObject.set("inputName", problemCase.getInput());
            jsonObject.set("outputName", problemCase.getOutput());
            // 读取输出文件
            FileReader readFile = new FileReader(testCasesDir + "/" + problemCase.getOutput(), CharsetUtil.UTF_8);
            String output = readFile.readString().replaceAll("\r\n", "\n");

            // spj是根据特判程序输出判断结果，所以无需初始化测试数据
            if (!isSpj) {
                // 原数据MD5
                jsonObject.set("outputMd5", DigestUtils.md5DigestAsHex(output.getBytes()));
                // 原数据大小
                jsonObject.set("outputSize", output.getBytes().length);
                // 去掉全部空格的MD5，用来判断pe
                jsonObject.set("allStrippedOutputMd5", DigestUtils.md5DigestAsHex(output.replaceAll("\\s+", "").getBytes()));
                // 默认去掉文末空格的MD5
                jsonObject.set("EOFStrippedOutputMd5", DigestUtils.md5DigestAsHex(rtrim(output).getBytes()));
            }

            ((JSONArray) result.get("testCases")).put(jsonObject);
        }

        FileWriter infoFile = new FileWriter(testCasesDir + "/info", CharsetUtil.UTF_8);
        // 写入记录文件
        infoFile.write(JSONUtil.toJsonStr(result));
        // 删除临时上传文件夹
        FileUtil.del(tmpTestcaseDir);
    }


    // 初始化手动输入上传的测试数据，写成json文件
    @Async
    public void initHandTestCase(Boolean isSpj,
                                 String version,
                                 Long problemId,
                                 List<ProblemCase> problemCaseList) {

        JSONObject result = new JSONObject();
        result.set("isSpj", isSpj);
        result.set("version", version);
        result.set("testCasesSize", problemCaseList.size());
        result.set("testCases", new JSONArray());

        String testCasesDir = Constants.File.TESTCASE_BASE_FOLDER.getPath() + File.separator + "problem_" + problemId;
        FileUtil.del(testCasesDir);
        for (int index = 0; index < problemCaseList.size(); index++) {
            JSONObject jsonObject = new JSONObject();
            String inputName = (index + 1) + ".in";
            jsonObject.set("caseId", problemCaseList.get(index).getId());
            jsonObject.set("score", problemCaseList.get(index).getScore());
            jsonObject.set("inputName", inputName);
            // 生成对应文件
            FileWriter infileWriter = new FileWriter(testCasesDir + "/" + inputName, CharsetUtil.UTF_8);
            // 将该测试数据的输入写入到文件
            infileWriter.write(problemCaseList.get(index).getInput());

            String outputName = (index + 1) + ".out";
            jsonObject.set("outputName", outputName);
            // 生成对应文件
            String outputData = problemCaseList.get(index).getOutput().replaceAll("\r\n", "\n");
            FileWriter outFile = new FileWriter(testCasesDir + "/" + outputName, CharsetUtil.UTF_8);
            outFile.write(outputData);

            // spj是根据特判程序输出判断结果，所以无需初始化测试数据
            if (!isSpj) {
                // 原数据MD5
                jsonObject.set("outputMd5", DigestUtils.md5DigestAsHex(outputData.getBytes()));
                // 原数据大小
                try {
                    jsonObject.set("outputSize", outputData.getBytes(CharsetUtil.UTF_8).length);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                // 去掉全部空格的MD5，用来判断pe
                jsonObject.set("allStrippedOutputMd5", DigestUtils.md5DigestAsHex(outputData.replaceAll("\\s+", "").getBytes()));
                // 默认去掉文末空格的MD5
                jsonObject.set("EOFStrippedOutputMd5", DigestUtils.md5DigestAsHex(rtrim(outputData).getBytes()));
            }

            ((JSONArray) result.get("testCases")).put(index, jsonObject);
        }

        FileWriter infoFile = new FileWriter(testCasesDir + "/info", CharsetUtil.UTF_8);
        // 写入记录文件
        infoFile.write(JSONUtil.toJsonStr(result));
    }

    @Override
    public ProblemStrategy.RemoteProblemInfo getOtherOJProblemInfo(String OJName, String problemId, String author) throws Exception {

        ProblemStrategy problemStrategy;

        switch (OJName) {
            case "HDU":
                problemStrategy = new HDUProblemStrategy();
                break;
            case "CF":
                problemStrategy = new CFProblemStrategy();
                break;
            default:
                throw new Exception("未知的OJ的名字，暂时不支持！");
        }

        ProblemContext problemContext = new ProblemContext(problemStrategy);
        return problemContext.getProblemInfo(problemId, author);
    }


    @Override
    @Transactional
    public boolean adminAddOtherOJProblem(ProblemStrategy.RemoteProblemInfo remoteProblemInfo, String OJName) {

        Problem problem = remoteProblemInfo.getProblem();
        boolean addProblemResult = problemMapper.insert(problem) == 1;
        // 为新的其它oj题目添加对应的language
        QueryWrapper<Language> languageQueryWrapper = new QueryWrapper<>();
        languageQueryWrapper.eq("oj", OJName);
        List<Language> OJLanguageList = languageService.list(languageQueryWrapper);
        List<ProblemLanguage> problemLanguageList = new LinkedList<>();
        for (Language language : OJLanguageList) {
            problemLanguageList.add(new ProblemLanguage().setPid(problem.getId()).setLid(language.getId()));
        }
        boolean addProblemLanguageResult = problemLanguageService.saveOrUpdateBatch(problemLanguageList);


        boolean addProblemTagResult = true;
        List<Tag> addTagList = remoteProblemInfo.getTagList();

        List<Tag> needAddTagList = new LinkedList<>();

        HashMap<String, Tag> tagFlag = new HashMap<>();

        if (addTagList != null && addTagList.size() > 0) {
            List<Tag> tagList = tagService.list();
            // 已存在的tag不进行添加
            for (Tag hasTag : tagList) {
                tagFlag.put(hasTag.getName(), hasTag);
            }
            for (Tag tmp : addTagList) {
                if (tagFlag.get(tmp.getName()) == null) {
                    needAddTagList.add(tmp);
                } else {
                    needAddTagList.add(tagFlag.get(tmp.getName()));
                }
            }
            tagService.saveOrUpdateBatch(needAddTagList);
            List<ProblemTag> problemTagList = new LinkedList<>();
            for (Tag tmp : needAddTagList) {
                problemTagList.add(new ProblemTag().setTid(tmp.getId()).setPid(problem.getId()));
            }
            addProblemTagResult = problemTagService.saveOrUpdateBatch(problemTagList);
        } else {
            QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
            tagQueryWrapper.eq("name", OJName);
            Tag OJNameTag = tagService.getOne(tagQueryWrapper, false);
            if (OJNameTag == null) {
                OJNameTag = new Tag();
                OJNameTag.setName(OJName);
                tagService.saveOrUpdate(OJNameTag);
            }
            addProblemTagResult = problemTagService.saveOrUpdate(new ProblemTag().setTid(OJNameTag.getId())
                    .setPid(problem.getId()));
        }

        return addProblemResult && addProblemTagResult && addProblemLanguageResult;
    }

    @Override
    public ImportProblemVo buildExportProblem(Long pid, List<HashMap<String, Object>> problemCaseList,
                                              HashMap<Long, String> languageMap, HashMap<Long, String> tagMap) {
        // 导出相当于导入
        ImportProblemVo importProblemVo = new ImportProblemVo();
        Problem problem = problemMapper.selectById(pid);
        problem.setCaseVersion(null)
                .setGmtCreate(null)
                .setId(null)
                .setAuth(1)
                .setIsUploadCase(true)
                .setAuthor(null)
                .setGmtModified(null);
        HashMap<String, Object> problemMap = new HashMap<>();
        BeanUtil.beanToMap(problem, problemMap, false, true);
        importProblemVo.setProblem(problemMap);
        QueryWrapper<CodeTemplate> codeTemplateQueryWrapper = new QueryWrapper<>();
        codeTemplateQueryWrapper.eq("pid", pid).eq("status", true);
        List<CodeTemplate> codeTemplates = codeTemplateService.list(codeTemplateQueryWrapper);
        List<HashMap<String, String>> codeTemplateList = new LinkedList<>();
        for (CodeTemplate codeTemplate : codeTemplates) {
            HashMap<String, String> tmp = new HashMap<>();
            tmp.put("language", languageMap.get(codeTemplate.getLid()));
            tmp.put("code", codeTemplate.getCode());
            codeTemplateList.add(tmp);
        }
        importProblemVo.setCodeTemplates(codeTemplateList);
        importProblemVo.setIsSpj(problem.getSpjCode() != null);

        importProblemVo.setSamples(problemCaseList);

        QueryWrapper<ProblemTag> problemTagQueryWrapper = new QueryWrapper<>();
        problemTagQueryWrapper.eq("pid", pid);
        List<ProblemTag> problemTags = problemTagService.list(problemTagQueryWrapper);
        importProblemVo.setTags(problemTags.stream().map(problemTag -> tagMap.get(problemTag.getTid())).collect(Collectors.toList()));

        QueryWrapper<ProblemLanguage> problemLanguageQueryWrapper = new QueryWrapper<>();
        problemLanguageQueryWrapper.eq("pid", pid);
        List<ProblemLanguage> problemLanguages = problemLanguageService.list(problemLanguageQueryWrapper);
        importProblemVo.setLanguages(problemLanguages.stream().map(problemLanguage -> languageMap.get(problemLanguage.getLid())).collect(Collectors.toList()));


        return importProblemVo;
    }

    // 去除末尾的空白符
    public static String rtrim(String value) {
        if (value == null) return null;
        return value.replaceAll("\\s+$", "");
    }

}

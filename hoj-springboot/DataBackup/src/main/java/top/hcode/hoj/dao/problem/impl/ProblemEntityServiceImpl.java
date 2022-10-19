package top.hcode.hoj.dao.problem.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import top.hcode.hoj.dao.judge.JudgeEntityService;
import top.hcode.hoj.dao.problem.*;
import top.hcode.hoj.exception.ProblemIDRepeatException;
import top.hcode.hoj.mapper.ProblemMapper;
import top.hcode.hoj.pojo.bo.Pair_;
import top.hcode.hoj.pojo.dto.ProblemDTO;
import top.hcode.hoj.pojo.entity.problem.*;
import top.hcode.hoj.pojo.vo.ImportProblemVO;
import top.hcode.hoj.pojo.vo.ProblemCountVO;
import top.hcode.hoj.pojo.vo.ProblemVO;
import top.hcode.hoj.utils.Constants;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
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
public class ProblemEntityServiceImpl extends ServiceImpl<ProblemMapper, Problem> implements ProblemEntityService {

    @Autowired
    private ProblemMapper problemMapper;

    @Autowired
    private JudgeEntityService judgeEntityService;

    @Autowired
    private ProblemCaseEntityService problemCaseEntityService;

    @Autowired
    private ProblemLanguageEntityService problemLanguageEntityService;

    @Autowired
    private TagEntityService tagEntityService;

    @Autowired
    private ProblemTagEntityService problemTagEntityService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CodeTemplateEntityService codeTemplateEntityService;

    private final static Pattern EOL_PATTERN = Pattern.compile("[^\\S\\n]+(?=\\n)");

    @Override
    public Page<ProblemVO> getProblemList(int limit, int currentPage, Long pid, String title, Integer difficulty,
                                          List<Long> tid, String oj) {

        //新建分页
        Page<ProblemVO> page = new Page<>(currentPage, limit);
        Integer tagListSize = null;
        if (tid != null) {
            tid = tid.stream().distinct().collect(Collectors.toList());
            tagListSize = tid.size();
        }

        List<ProblemVO> problemList = problemMapper.getProblemList(page, pid, title, difficulty, tid, tagListSize, oj);

        if (problemList.size() > 0) {
            List<Long> pidList = problemList.stream().map(ProblemVO::getPid).collect(Collectors.toList());
            List<ProblemCountVO> problemListCount = judgeEntityService.getProblemListCount(pidList);
            for (ProblemVO problemVo : problemList) {
                for (ProblemCountVO problemCountVo : problemListCount) {
                    if (problemVo.getPid().equals(problemCountVo.getPid())) {
                        problemVo.setProblemCountVo(problemCountVo);
                        break;
                    }
                }
            }
        }

        return page.setRecords(problemList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean adminUpdateProblem(ProblemDTO problemDto) {

        Problem problem = problemDto.getProblem();
        if (Constants.JudgeMode.DEFAULT.getMode().equals(problemDto.getJudgeMode())) {
            problem.setSpjLanguage(null).setSpjCode(null);
        }

        String ojName = "ME";
        if (problem.getIsRemote()) {
            String problemId = problem.getProblemId();
            ojName = problemId.split("-")[0];
        }

        /**
         *  problem_id唯一性检查
         */
        String problemId = problem.getProblemId().toUpperCase();
        QueryWrapper<Problem> problemQueryWrapper = new QueryWrapper<>();
        problemQueryWrapper.eq("problem_id", problemId);
        Problem existedProblem = problemMapper.selectOne(problemQueryWrapper);

        problem.setProblemId(problem.getProblemId().toUpperCase());
        if (problem.getIsGroup() == null) {
            problem.setIsGroup(false);
        }
        // 后面许多表的更新或删除需要用到题目id
        long pid = problemDto.getProblem().getId();

        if (existedProblem != null && existedProblem.getId() != pid) {
            throw new RuntimeException("The problem_id [" + problemId + "] already exists. Do not reuse it!");
        }

        Map<String, Object> map = new HashMap<>();
        map.put("pid", pid);
        //查询出原来题目的关联表数据
        List<ProblemLanguage> oldProblemLanguages = (List<ProblemLanguage>) problemLanguageEntityService.listByMap(map);
        List<ProblemCase> oldProblemCases = (List<ProblemCase>) problemCaseEntityService.listByMap(map);
        List<CodeTemplate> oldProblemTemplate = (List<CodeTemplate>) codeTemplateEntityService.listByMap(map);
        List<ProblemTag> oldProblemTags = (List<ProblemTag>) problemTagEntityService.listByMap(map);

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
                tag.setOj(ojName);
                boolean addTagResult = tagEntityService.save(tag);
                if (addTagResult) {
                    problemTagList.add(new ProblemTag()
                            .setPid(pid).setTid(tag.getId()));
                }
                // 已存在tag 但是新添加的
            } else if (mapOldPT.getOrDefault(tag.getId(), null) == null) {
                problemTagList.add(new ProblemTag()
                        .setPid(pid).setTid(tag.getId()));
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
            deleteTagsFromProblemResult = problemTagEntityService.remove(tagWrapper);
        }
        // 执行批量插入操作
        boolean addTagsToProblemResult = true;
        if (problemTagList.size() > 0) {
            addTagsToProblemResult = problemTagEntityService.saveOrUpdateBatch(problemTagList);
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
            deleteTemplate = codeTemplateEntityService.removeByIds(needDeleteCTs);
        }
        if (problemDto.getCodeTemplates().size() > 0) {
            saveOrUpdateCodeTemplate = codeTemplateEntityService.saveOrUpdateBatch(problemDto.getCodeTemplates());
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
            deleteLanguagesFromProblemResult = problemLanguageEntityService.remove(LangWrapper);
        }
        // 执行批量添加操作
        boolean addLanguagesToProblemResult = true;
        if (problemLanguageList.size() > 0) {
            addLanguagesToProblemResult = problemLanguageEntityService.saveOrUpdateBatch(problemLanguageList);
        }


        /**
         *  处理problem_case表的增加与删除
         */

        boolean checkProblemCase = true;

        if (!problem.getIsRemote() && problemDto.getSamples().size() > 0) { // 如果是自家的题目才有测试数据
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
                        // 分数变动 或者 subtask分组编号变更
                        if (!Objects.equals(oldProblemCase.getScore(), problemCase.getScore())
                                || !Objects.equals(oldProblemCase.getGroupNum(), problemCase.getGroupNum())) {
                            needUpdateProblemCaseList.add(problemCase);
                        }
                    }
                } else {
                    newProblemCaseList.add(problemCase.setPid(pid));
                }
            }
            // 设置oi总分数，根据每个测试点的加和
            if (problem.getType().intValue() == Constants.Contest.TYPE_OI.getCode()) {
                int sumScore = calProblemTotalScore(problem.getJudgeCaseMode(), problemDto.getSamples());
                problem.setIoScore(sumScore);
            }
            // 执行批量删除操作
            boolean deleteCasesFromProblemResult = true;
            if (needDeleteProblemCases.size() > 0) {
                deleteCasesFromProblemResult = problemCaseEntityService.removeByIds(needDeleteProblemCases);
            }
            // 执行批量添加操作
            boolean addCasesToProblemResult = true;
            if (newProblemCaseList.size() > 0) {
                addCasesToProblemResult = problemCaseEntityService.saveBatch(newProblemCaseList);
            }
            // 执行批量修改操作
            boolean updateCasesToProblemResult = true;
            if (needUpdateProblemCaseList.size() > 0) {
                updateCasesToProblemResult = problemCaseEntityService.saveOrUpdateBatch(needUpdateProblemCaseList);
            }
            checkProblemCase = addCasesToProblemResult && deleteCasesFromProblemResult && updateCasesToProblemResult;

            // 只要有新添加，修改，删除，或者变更judgeCaseMode 都需要更新版本号 同时更新测试数据
            String caseVersion = String.valueOf(System.currentTimeMillis());
            String testcaseDir = problemDto.getUploadTestcaseDir();
            if (needDeleteProblemCases.size() > 0 || newProblemCaseList.size() > 0
                    || needUpdateProblemCaseList.size() > 0 || !StringUtils.isEmpty(testcaseDir)
                    || (problemDto.getChangeJudgeCaseMode() != null && problemDto.getChangeJudgeCaseMode())) {
                problem.setCaseVersion(caseVersion);
                // 如果是选择上传测试文件的，则需要遍历对应文件夹，读取数据，写入数据库,先前的题目数据一并清空。
                if (problemDto.getIsUploadTestCase()) {
                    // 获取代理bean对象执行异步方法===》根据测试文件初始info
                    applicationContext.getBean(ProblemEntityServiceImpl.class)
                            .initUploadTestCase(problemDto.getJudgeMode(),
                                    problem.getJudgeCaseMode(),
                                    caseVersion,
                                    pid,
                                    testcaseDir,
                                    problemDto.getSamples());
                } else {
                    applicationContext.getBean(ProblemEntityServiceImpl.class)
                            .initHandTestCase(problemDto.getJudgeMode(),
                                    problem.getJudgeCaseMode(),
                                    problem.getCaseVersion(),
                                    pid,
                                    problemDto.getSamples());
                }
            }
            // 变化成spj或interactive或者取消 同时更新测试数据
            else if (problemDto.getChangeModeCode() != null && problemDto.getChangeModeCode()) {
                problem.setCaseVersion(caseVersion);
                if (problemDto.getIsUploadTestCase()) {
                    // 获取代理bean对象执行异步方法===》根据测试文件初始info
                    applicationContext.getBean(ProblemEntityServiceImpl.class)
                            .initUploadTestCase(problemDto.getJudgeMode(),
                                    problem.getJudgeCaseMode(),
                                    caseVersion,
                                    pid,
                                    null,
                                    problemDto.getSamples());
                } else {
                    applicationContext.getBean(ProblemEntityServiceImpl.class)
                            .initHandTestCase(problemDto.getJudgeMode(),
                                    problem.getJudgeCaseMode(),
                                    problem.getCaseVersion(),
                                    pid,
                                    problemDto.getSamples());
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
    @Transactional(rollbackFor = Exception.class)
    public boolean adminAddProblem(ProblemDTO problemDto) {

        Problem problem = problemDto.getProblem();

        if (Constants.JudgeMode.DEFAULT.getMode().equals(problemDto.getJudgeMode())) {
            problem.setSpjLanguage(null)
                    .setSpjCode(null);
        }

        // 设置测试样例的版本号
        problem.setCaseVersion(String.valueOf(System.currentTimeMillis()));
        if (problem.getIsGroup() == null) {
            problem.setIsGroup(false);
        }

        // 如果没有提供problemId,则或者生成 P1000之类的，以problem表的id作为数字
        if (problem.getProblemId() == null) {
            problem.setProblemId(UUID.fastUUID().toString());
            problemMapper.insert(problem);

            UpdateWrapper<Problem> problemUpdateWrapper = new UpdateWrapper<>();
            problemUpdateWrapper.set("problem_id", "P" + problem.getId());
            problemUpdateWrapper.eq("id", problem.getId());
            problemMapper.update(null, problemUpdateWrapper);
            problem.setProblemId("P" + problem.getId());
        } else {
            // problem_id唯一性检查
            String problemId = problem.getProblemId().toUpperCase();
            QueryWrapper<Problem> problemQueryWrapper = new QueryWrapper<>();
            problemQueryWrapper.eq("problem_id", problemId);
            int existedProblem = problemMapper.selectCount(problemQueryWrapper);
            if (existedProblem > 0) {
                throw new ProblemIDRepeatException("The problem_id [" + problemId + "] already exists. Do not reuse it!");
            }
            problem.setProblemId(problemId);
            problemMapper.insert(problem);
        }
        Long pid = problem.getId();
        if (pid == null) {
            throw new ProblemIDRepeatException("The problem with problem_id [" + problem.getProblemId() + "] insert failed!");
        }

        // 为新的题目添加对应的language
        List<ProblemLanguage> problemLanguageList = new LinkedList<>();
        for (Language language : problemDto.getLanguages()) {
            problemLanguageList.add(new ProblemLanguage().setPid(pid).setLid(language.getId()));
        }
        boolean addLangToProblemResult = problemLanguageEntityService.saveOrUpdateBatch(problemLanguageList);

        // 为新的题目添加对应的codeTemplate
        boolean addProblemCodeTemplate = true;
        if (problemDto.getCodeTemplates() != null && problemDto.getCodeTemplates().size() > 0) {
            for (CodeTemplate codeTemplate : problemDto.getCodeTemplates()) {
                codeTemplate.setPid(pid);
            }
            addProblemCodeTemplate = codeTemplateEntityService.saveOrUpdateBatch(problemDto.getCodeTemplates());
        }


        boolean addCasesToProblemResult = true;
        // 为新的题目添加对应的case
        if (problemDto.getIsUploadTestCase()) { // 如果是选择上传测试文件的，则需要遍历对应文件夹，读取数据。。
            int sumScore = 0;
            String testcaseDir = problemDto.getUploadTestcaseDir();
            // 如果是io题目统计总分
            List<ProblemCase> problemCases = problemDto.getSamples();
            if (problemCases.size() == 0) {
                throw new RuntimeException("The test cases of problem must not be empty!");
            }
            for (ProblemCase problemCase : problemCases) {
                if (problemCase.getScore() != null) {
                    sumScore += problemCase.getScore();
                }
                if (StringUtils.isEmpty(problemCase.getOutput())) {
                    String filePreName = problemCase.getInput().split("\\.")[0];
                    problemCase.setOutput(filePreName + ".out");
                }
                problemCase.setPid(pid);
            }
            // 设置oi总分数，根据每个测试点的加和
            if (problem.getType().intValue() == Constants.Contest.TYPE_OI.getCode()) {
                UpdateWrapper<Problem> problemUpdateWrapper = new UpdateWrapper<>();
                problemUpdateWrapper.eq("id", pid)
                        .set("io_score", sumScore);
                problemMapper.update(null, problemUpdateWrapper);
            }
            addCasesToProblemResult = problemCaseEntityService.saveOrUpdateBatch(problemCases);
            // 获取代理bean对象执行异步方法===》根据测试文件初始info
            applicationContext.getBean(ProblemEntityServiceImpl.class).initUploadTestCase(
                    problemDto.getJudgeMode(),
                    problem.getJudgeCaseMode(),
                    problem.getCaseVersion(),
                    pid,
                    testcaseDir,
                    problemDto.getSamples());
        } else {
            // oi题目需要求取平均值，给每个测试点初始oi的score值，默认总分100分
            if (problem.getType().intValue() == Constants.Contest.TYPE_OI.getCode()) {
                for (ProblemCase problemCase : problemDto.getSamples()) {
                    // 设置好新题目的pid和累加总分数
                    problemCase.setPid(pid);
                }
                int sumScore = calProblemTotalScore(problem.getJudgeCaseMode(), problemDto.getSamples());
                addCasesToProblemResult = problemCaseEntityService.saveOrUpdateBatch(problemDto.getSamples());
                UpdateWrapper<Problem> problemUpdateWrapper = new UpdateWrapper<>();
                problemUpdateWrapper.eq("id", pid)
                        .set("io_score", sumScore);
                problemMapper.update(null, problemUpdateWrapper);
            } else {
                problemDto.getSamples().forEach(problemCase -> problemCase.setPid(pid)); // 设置好新题目的pid
                addCasesToProblemResult = problemCaseEntityService.saveOrUpdateBatch(problemDto.getSamples());
            }
            initHandTestCase(problemDto.getJudgeMode(),
                    problem.getJudgeCaseMode(),
                    problem.getCaseVersion(),
                    pid,
                    problemDto.getSamples());
        }

        // 为新的题目添加对应的tag，可能tag是原表已有，也可能是新的，所以需要判断。
        List<ProblemTag> problemTagList = new LinkedList<>();
        if (problemDto.getTags() != null) {
            for (Tag tag : problemDto.getTags()) {
                if (tag.getId() == null) { //id为空 表示为原tag表中不存在的 插入后可以获取到对应的tagId
                    Tag existedTag = tagEntityService.getOne(new QueryWrapper<Tag>().eq("name", tag.getName())
                            .eq("oj", "ME"), false);
                    if (existedTag == null) {
                        tag.setOj("ME");
                        tagEntityService.save(tag);
                    } else {
                        tag = existedTag;
                    }
                }
                problemTagList.add(new ProblemTag().setTid(tag.getId()).setPid(pid));
            }
        }
        boolean addTagsToProblemResult = true;
        if (problemTagList.size() > 0) {
            addTagsToProblemResult = problemTagEntityService.saveOrUpdateBatch(problemTagList);
        }


        if (addCasesToProblemResult && addLangToProblemResult
                && addTagsToProblemResult && addProblemCodeTemplate) {
            return true;
        } else {
            return false;
        }
    }

    // 初始化上传文件的测试数据，写成json文件
    @Async
    public void initUploadTestCase(String judgeMode,
                                   String judgeCaseMode,
                                   String version,
                                   Long problemId,
                                   String tmpTestcaseDir,
                                   List<ProblemCase> problemCaseList) {

        String testCasesDir = Constants.File.TESTCASE_BASE_FOLDER.getPath() + File.separator + "problem_" + problemId;

        // 将之前的临时文件夹里面的评测文件全部复制到指定文件夹(覆盖)

        if (!StringUtils.isEmpty(tmpTestcaseDir)) {
            FileUtil.clean(testCasesDir);
            File testCasesDirFile = new File(testCasesDir);
            FileUtil.copyFilesFromDir(new File(tmpTestcaseDir), testCasesDirFile, true);
        }

        List<String> listFileNames = FileUtil.listFileNames(testCasesDir);

        if (StringUtils.isEmpty(judgeCaseMode)) {
            judgeCaseMode = Constants.JudgeCaseMode.DEFAULT.getMode();
        }
        JSONObject result = new JSONObject();
        result.set("mode", judgeMode);
        result.set("judgeCaseMode", judgeCaseMode);
        result.set("version", version);
        result.set("testCasesSize", problemCaseList.size());

        JSONArray testCaseList = new JSONArray(problemCaseList.size());

        for (ProblemCase problemCase : problemCaseList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("caseId", problemCase.getId());
            if (judgeCaseMode.equals(Constants.JudgeCaseMode.SUBTASK_AVERAGE.getMode())
                    || judgeCaseMode.equals(Constants.JudgeCaseMode.SUBTASK_LOWEST.getMode())) {
                jsonObject.set("groupNum", problemCase.getGroupNum());
            }

            jsonObject.set("score", problemCase.getScore());
            jsonObject.set("inputName", problemCase.getInput());
            jsonObject.set("outputName", problemCase.getOutput());

            listFileNames.remove(problemCase.getInput());
            listFileNames.remove(problemCase.getOutput());

            // 读取输入文件
            FileReader inputFile = new FileReader(testCasesDir + File.separator + problemCase.getInput(), CharsetUtil.UTF_8);
            String input = inputFile.readString().replaceAll("\r\n", "\n");

            FileWriter inputFileWriter = new FileWriter(testCasesDir + File.separator + problemCase.getInput(), CharsetUtil.UTF_8);
            inputFileWriter.write(input);

            // 读取输出文件
            String output = "";
            String outputFilePath = testCasesDir + File.separator + problemCase.getOutput();
            if (FileUtil.exist(outputFilePath)) {
                FileReader outputFile = new FileReader(outputFilePath, CharsetUtil.UTF_8);
                output = outputFile.readString().replaceAll("\r\n", "\n");
                FileWriter outFileWriter = new FileWriter(testCasesDir + File.separator + problemCase.getOutput(), CharsetUtil.UTF_8);
                outFileWriter.write(output);
            } else {
                FileWriter fileWriter = new FileWriter(outputFilePath);
                fileWriter.write("");
            }

            // spj和interactive是根据特判程序输出判断结果，所以无需初始化测试数据
            if (Constants.JudgeMode.DEFAULT.getMode().equals(judgeMode)) {
                // 原数据MD5
                jsonObject.set("outputMd5", DigestUtils.md5DigestAsHex(output.getBytes(StandardCharsets.UTF_8)));
                // 原数据大小
                jsonObject.set("outputSize", output.getBytes(StandardCharsets.UTF_8).length);
                // 去掉全部空格的MD5，用来判断pe
                jsonObject.set("allStrippedOutputMd5", DigestUtils.md5DigestAsHex(output.replaceAll("\\s+", "").getBytes(StandardCharsets.UTF_8)));
                // 默认去掉文末空格的MD5
                jsonObject.set("EOFStrippedOutputMd5", DigestUtils.md5DigestAsHex(rtrim(output).getBytes(StandardCharsets.UTF_8)));
            }

            testCaseList.add(jsonObject);
        }

        result.set("testCases", testCaseList);

        FileWriter infoFile = new FileWriter(testCasesDir + "/info", CharsetUtil.UTF_8);
        // 写入记录文件
        infoFile.write(JSONUtil.toJsonStr(result));
        // 删除临时上传文件夹
        FileUtil.del(tmpTestcaseDir);
        // 删除非测试数据的文件
        listFileNames.remove("info");
        if (!CollectionUtils.isEmpty(listFileNames)) {
            for (String filename : listFileNames) {
                FileUtil.del(testCasesDir + File.separator + filename);
            }
        }
    }


    // 初始化手动输入上传的测试数据，写成json文件
    @Async
    public void initHandTestCase(String judgeMode,
                                 String judgeCaseMode,
                                 String version,
                                 Long problemId,
                                 List<ProblemCase> problemCaseList) {

        JSONObject result = new JSONObject();
        result.set("mode", judgeMode);
        if (StringUtils.isEmpty(judgeCaseMode)) {
            judgeCaseMode = Constants.JudgeCaseMode.DEFAULT.getMode();
        }
        result.set("judgeCaseMode", judgeCaseMode);
        result.set("version", version);
        result.set("testCasesSize", problemCaseList.size());

        JSONArray testCaseList = new JSONArray(problemCaseList.size());

        String testCasesDir = Constants.File.TESTCASE_BASE_FOLDER.getPath() + File.separator + "problem_" + problemId;
        FileUtil.del(testCasesDir);
        for (int index = 0; index < problemCaseList.size(); index++) {
            JSONObject jsonObject = new JSONObject();
            String inputName = (index + 1) + ".in";
            jsonObject.set("caseId", problemCaseList.get(index).getId());
            if (judgeCaseMode.equals(Constants.JudgeCaseMode.SUBTASK_AVERAGE.getMode())
                    || judgeCaseMode.equals(Constants.JudgeCaseMode.SUBTASK_LOWEST.getMode())) {
                jsonObject.set("groupNum", problemCaseList.get(index).getGroupNum());
            }
            jsonObject.set("score", problemCaseList.get(index).getScore());
            jsonObject.set("inputName", inputName);
            // 生成对应文件
            FileWriter infileWriter = new FileWriter(testCasesDir + "/" + inputName, CharsetUtil.UTF_8);
            // 将该测试数据的输入写入到文件
            String inputData = problemCaseList.get(index).getInput().replaceAll("\r\n", "\n");
            infileWriter.write(inputData);

            String outputName = (index + 1) + ".out";
            jsonObject.set("outputName", outputName);
            // 生成对应文件
            String outputData = problemCaseList.get(index).getOutput().replaceAll("\r\n", "\n");
            FileWriter outFile = new FileWriter(testCasesDir + "/" + outputName, CharsetUtil.UTF_8);
            outFile.write(outputData);

            // spj和interactive是根据特判程序输出判断结果，所以无需初始化测试数据
            if (Constants.JudgeMode.DEFAULT.getMode().equals(judgeMode)) {
                // 原数据MD5
                jsonObject.set("outputMd5", DigestUtils.md5DigestAsHex(outputData.getBytes(StandardCharsets.UTF_8)));
                // 原数据大小
                jsonObject.set("outputSize", outputData.getBytes(StandardCharsets.UTF_8).length);
                // 去掉全部空格的MD5，用来判断pe
                jsonObject.set("allStrippedOutputMd5", DigestUtils.md5DigestAsHex(outputData.replaceAll("\\s+", "").getBytes(StandardCharsets.UTF_8)));
                // 默认去掉文末空格的MD5
                jsonObject.set("EOFStrippedOutputMd5", DigestUtils.md5DigestAsHex(rtrim(outputData).getBytes(StandardCharsets.UTF_8)));
            }

            testCaseList.add(jsonObject);
        }

        result.set("testCases", testCaseList);

        FileWriter infoFile = new FileWriter(testCasesDir + "/info", CharsetUtil.UTF_8);
        // 写入记录文件
        infoFile.write(JSONUtil.toJsonStr(result));
    }


    @Override
    @SuppressWarnings("All")
    public ImportProblemVO buildExportProblem(Long pid, List<HashMap<String, Object>> problemCaseList,
                                              HashMap<Long, String> languageMap, HashMap<Long, String> tagMap) {
        // 导出相当于导入
        ImportProblemVO importProblemVo = new ImportProblemVO();
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
        List<CodeTemplate> codeTemplates = codeTemplateEntityService.list(codeTemplateQueryWrapper);
        List<HashMap<String, String>> codeTemplateList = new LinkedList<>();
        for (CodeTemplate codeTemplate : codeTemplates) {
            HashMap<String, String> tmp = new HashMap<>();
            tmp.put("language", languageMap.get(codeTemplate.getLid()));
            tmp.put("code", codeTemplate.getCode());
            codeTemplateList.add(tmp);
        }
        importProblemVo.setCodeTemplates(codeTemplateList);
        importProblemVo.setJudgeMode(problem.getJudgeMode());
        importProblemVo.setSamples(problemCaseList);

        if (!StringUtils.isEmpty(problem.getUserExtraFile())) {
            HashMap<String, String> userExtraFileMap = (HashMap<String, String>) JSONUtil.toBean(problem.getUserExtraFile(), Map.class);
            importProblemVo.setUserExtraFile(userExtraFileMap);
        }

        if (!StringUtils.isEmpty(problem.getJudgeExtraFile())) {
            HashMap<String, String> judgeExtraFileMap = (HashMap<String, String>) JSONUtil.toBean(problem.getJudgeExtraFile(), Map.class);
            importProblemVo.setUserExtraFile(judgeExtraFileMap);
        }

        QueryWrapper<ProblemTag> problemTagQueryWrapper = new QueryWrapper<>();
        problemTagQueryWrapper.eq("pid", pid);
        List<ProblemTag> problemTags = problemTagEntityService.list(problemTagQueryWrapper);
        importProblemVo.setTags(problemTags.stream().map(problemTag -> tagMap.get(problemTag.getTid())).collect(Collectors.toList()));

        QueryWrapper<ProblemLanguage> problemLanguageQueryWrapper = new QueryWrapper<>();
        problemLanguageQueryWrapper.eq("pid", pid);
        List<ProblemLanguage> problemLanguages = problemLanguageEntityService.list(problemLanguageQueryWrapper);
        importProblemVo.setLanguages(problemLanguages.stream().map(problemLanguage -> languageMap.get(problemLanguage.getLid())).collect(Collectors.toList()));


        return importProblemVo;
    }

    // 去除每行末尾的空白符
    public static String rtrim(String value) {
        if (value == null) return null;
        return EOL_PATTERN.matcher(StrUtil.trimEnd(value)).replaceAll("");
    }


    private Integer calProblemTotalScore(String judgeCaseMode, List<ProblemCase> problemCaseList) {
        int sumScore = 0;
        if (Constants.JudgeCaseMode.SUBTASK_LOWEST.getMode().equals(judgeCaseMode)) {
            HashMap<Integer, Integer> groupNumMapScore = new HashMap<>();
            for (ProblemCase problemCase : problemCaseList) {
                groupNumMapScore.merge(problemCase.getGroupNum(), problemCase.getScore(), Math::min);
            }
            for (Integer minScore : groupNumMapScore.values()) {
                sumScore += minScore;
            }
        } else if (Constants.JudgeCaseMode.SUBTASK_AVERAGE.getMode().equals(judgeCaseMode)) {
            // 预处理 切换成Map Key: groupNum Value: <count,sum_score>
            HashMap<Integer, Pair_<Integer, Integer>> groupNumMapScore = new HashMap<>();
            for (ProblemCase problemCase : problemCaseList) {
                Pair_<Integer, Integer> pair = groupNumMapScore.get(problemCase.getGroupNum());
                if (pair == null) {
                    groupNumMapScore.put(problemCase.getGroupNum(), new Pair_<>(1, problemCase.getScore()));
                } else {
                    int count = pair.getKey() + 1;
                    int score = pair.getValue() + problemCase.getScore();
                    groupNumMapScore.put(problemCase.getGroupNum(), new Pair_<>(count, score));
                }
            }
            for (Pair_<Integer, Integer> pair : groupNumMapScore.values()) {
                sumScore += (int) Math.round(pair.getValue() * 1.0 / pair.getKey());
            }
        } else {
            for (ProblemCase problemCase : problemCaseList) {
                if (problemCase.getScore() != null) {
                    sumScore += problemCase.getScore();
                }
            }
        }
        return sumScore;
    }
}

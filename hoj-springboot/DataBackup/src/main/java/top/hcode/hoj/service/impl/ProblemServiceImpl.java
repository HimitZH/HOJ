package top.hcode.hoj.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import top.hcode.hoj.crawler.problem.HDUProblemStrategy;
import top.hcode.hoj.crawler.problem.ProblemContext;
import top.hcode.hoj.crawler.problem.ProblemStrategy;
import top.hcode.hoj.dao.ProblemCaseMapper;
import top.hcode.hoj.pojo.dto.ProblemDto;
import top.hcode.hoj.pojo.entity.*;
import top.hcode.hoj.pojo.vo.ProblemVo;
import top.hcode.hoj.dao.ProblemMapper;
import top.hcode.hoj.service.ProblemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.service.ToJudgeService;
import top.hcode.hoj.utils.Constants;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    private ProblemCaseMapper problemCaseMapper;

    @Autowired
    private ProblemLanguageServiceImpl problemLanguageService;

    @Autowired
    private TagServiceImpl tagService;

    @Autowired
    private ProblemTagServiceImpl problemTagService;

    @Autowired
    private ProblemCountServiceImpl problemCountService;


    @Override
    public Page<ProblemVo> getProblemList(int limit, int currentPage, Long pid, String title, Integer difficulty, Long tid) {

        //新建分页
        Page<ProblemVo> page = new Page<>(currentPage, limit);

        return page.setRecords(problemMapper.getProblemList(page, pid, title, difficulty, tid));
    }

    @Override
    @Transactional
    public boolean adminUpdateProblem(ProblemDto problemDto) {

        Problem problem = problemDto.getProblem();

        // 后面许多表的更新或删除需要用到题目id
        long pid = problemDto.getProblem().getId();
        Map<String, Object> map = new HashMap<>();
        map.put("pid", pid);
        //查询出原来题目的关联表数据
        List<ProblemTag> oldProblemTags = (List<ProblemTag>) problemTagService.listByMap(map);
        List<ProblemLanguage> oldProblemLanguages = (List<ProblemLanguage>) problemLanguageService.listByMap(map);
        List<ProblemCase> oldProblemCases = (List<ProblemCase>) problemCaseService.listByMap(map);

        Map<Long, Integer> mapOldPT = new HashMap<>();
        Map<Long, Integer> mapOldPL = new HashMap<>();
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
        // 如果是选择上传测试文件的，则需要遍历对应文件夹，读取数据，写入数据库,先前的题目数据一并清空。
        if (problemDto.getIsUploadTestCase()) {
            // 如果是选择了上传测试文件的模式，但是数据为空，那就是修改题目，但是没修改测试数据，需要判断
            if (problemDto.getSamples().size() > 0) {
                int sumScore = 0;
                String testcaseDir = problemDto.getUploadTestcaseDir();
                // 此时ProblemCase里面的input和output存的是文件名字，拼接上文件路径即可访问
                for (ProblemCase problemCase : problemDto.getSamples()) {
                    FileReader infileReader = new FileReader(testcaseDir + File.separator + problemCase.getInput());
                    String input = infileReader.readString();
                    FileReader outfileReader = new FileReader(testcaseDir + File.separator + problemCase.getOutput());
                    String output = outfileReader.readString();
                    Assert.notBlank(input, problemCase.getInput() + "的评测数据为空！修改题目失败！");
                    Assert.notBlank(output, problemCase.getInput() + "的评测数据为空！修改题目失败！");
                    // 如果不为空，则正常将数据写入
                    problemCase.setPid(pid);
                    problemCase.setInput(input);
                    problemCase.setOutput(output);
                    if (problemCase.getScore() != null) {
                        sumScore += problemCase.getScore();
                    }
                }
                // 设置oi总分数，根据每个测试点的加和
                if (problem.getType().intValue() == Constants.Contest.TYPE_OI.getCode()) {
                    problem.setIoScore(sumScore);
                }

                // 将原先题目的评测数据清空，重新插入新的
                QueryWrapper<ProblemCase> problemCaseQueryWrapper = new QueryWrapper<>();
                problemCaseQueryWrapper.eq("pid", pid);

                boolean remove = problemCaseService.remove(problemCaseQueryWrapper);
                boolean add = problemCaseService.saveOrUpdateBatch(problemDto.getSamples());
                // 设置新的测试样例版本号
                problem.setCaseVersion(String.valueOf(System.currentTimeMillis()));
                checkProblemCase = remove && add;
            }

        } else {
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
                    newProblemCaseList.add(problemCase);
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

            // 只要有新添加，修改，删除都需要更新版本号
            if (needDeleteProblemCases.size() > 0 || newProblemCaseList.size() > 0 || needUpdateProblemCaseList.size() > 0) {
                problem.setCaseVersion(String.valueOf(System.currentTimeMillis()));
            }

        }

        // 更新problem表
        boolean problemUpdateResult = problemMapper.updateById(problem) == 1;

        if (problemUpdateResult && checkProblemCase && deleteLanguagesFromProblemResult && deleteTagsFromProblemResult
                && addLanguagesToProblemResult && addTagsToProblemResult) {
            // 修改数据库成功后，如果有进行文件上传操作，则进行删除
            if (problemDto.getIsUploadTestCase() && problemDto.getSamples().size() > 0) {
                FileUtil.del(problemDto.getUploadTestcaseDir());
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    @Transactional
    public boolean adminAddProblem(ProblemDto problemDto) {

        Problem problem = problemDto.getProblem();
        // 设置测试样例的版本号
        problem.setCaseVersion(String.valueOf(System.currentTimeMillis()));
        boolean addProblemResult = problemMapper.insert(problem) == 1;
        long pid = problem.getId();
        // 为新的题目添加对应的language
        List<ProblemLanguage> problemLanguageList = new LinkedList<>();
        for (Language language : problemDto.getLanguages()) {
            problemLanguageList.add(new ProblemLanguage().setPid(pid).setLid(language.getId()));
        }
        boolean addLangToProblemResult = problemLanguageService.saveOrUpdateBatch(problemLanguageList);

        boolean addCasesToProblemResult = false;
        // 为新的题目添加对应的case
        if (problemDto.getIsUploadTestCase()) { // 如果是选择上传测试文件的，则需要遍历对应文件夹，读取数据，写入数据库。
            int sumScore = 0;
            String testcaseDir = problemDto.getUploadTestcaseDir();
            // 此时ProblemCase里面的input和output存的是文件名字，拼接上文件路径即可访问
            for (ProblemCase problemCase : problemDto.getSamples()) {
                FileReader infileReader = new FileReader(testcaseDir + File.separator + problemCase.getInput());
                String input = infileReader.readString();
                FileReader outfileReader = new FileReader(testcaseDir + File.separator + problemCase.getOutput());
                String output = outfileReader.readString();
                Assert.notBlank(input, problemCase.getInput() + "的评测数据为空！新建题目失败！");
                Assert.notBlank(output, problemCase.getInput() + "的评测数据为空！新建题目失败！");
                // 如果不为空，则正常将数据写入
                problemCase.setPid(pid);
                problemCase.setInput(input);
                problemCase.setOutput(output);
                if (problemCase.getScore() != null) {
                    sumScore += problemCase.getScore();
                }
            }
            addCasesToProblemResult = problemCaseService.saveOrUpdateBatch(problemDto.getSamples());
            // 设置oi总分数，根据每个测试点的加和
            if (problem.getType().intValue() == Constants.Contest.TYPE_OI.getCode()) {
                problem.setIoScore(sumScore);
            }
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
        }

        // 为新的题目添加对应的tag，可能tag是原表已有，也可能是新的，所以需要判断。
        List<ProblemTag> problemTagList = new LinkedList<>();
        for (Tag tag : problemDto.getTags()) {
            if (tag.getId() == null) { //id为空 表示为原tag表中不存在的 插入后可以获取到对应的tagId
                tagService.save(tag);
            }
            problemTagList.add(new ProblemTag().setTid(tag.getId()).setPid(pid));
        }
        boolean addTagsToProblemResult = problemTagService.saveOrUpdateBatch(problemTagList);

        // 为新的题目初始化problem_count表
        boolean initProblemCountResult = problemCountService.save(new ProblemCount().setPid(pid));


        if (addProblemResult && addCasesToProblemResult && addLangToProblemResult
                && addTagsToProblemResult && initProblemCountResult) {
            // 修改数据库成功后，如果有进行文件上传操作，则进行删除
            if (problemDto.getIsUploadTestCase()) {
                FileUtil.del(problemDto.getUploadTestcaseDir());
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Problem getOtherOJProblemInfo(String OJName, String problemId, String author) throws Exception {

        ProblemStrategy problemStrategy;

        switch (OJName) {
            case "HDU":
                problemStrategy = new HDUProblemStrategy();
                break;
            default:
                throw new Exception("未知的OJ的名字，暂时不支持！");
        }

        ProblemContext problemContext = new ProblemContext(problemStrategy);
        return problemContext.getProblemInfo(problemId,author);
    }


}

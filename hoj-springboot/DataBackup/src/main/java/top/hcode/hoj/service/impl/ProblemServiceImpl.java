package top.hcode.hoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import top.hcode.hoj.pojo.dto.ProblemDto;
import top.hcode.hoj.pojo.entity.*;
import top.hcode.hoj.pojo.vo.ProblemVo;
import top.hcode.hoj.dao.ProblemMapper;
import top.hcode.hoj.service.ProblemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.service.ToJudgeService;

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
    private ProblemLanguageServiceImpl problemLanguageService;

    @Autowired
    private TagServiceImpl tagService;

    @Autowired
    private ProblemTagServiceImpl problemTagService;

    @Autowired
    private ProblemCountServiceImpl problemCountService;

    @Autowired
    private ToJudgeService toJudgeService;

    @Override
    public Page<ProblemVo> getProblemList(int limit, int currentPage, Long pid, String title, Integer difficulty, Long tid) {

        //新建分页
        Page<ProblemVo> page = new Page<>(currentPage, limit);

        return page.setRecords(problemMapper.getProblemList(page, pid, title, difficulty, tid));
    }

    @Override
    @Transactional
    public boolean adminUpdateProblem(ProblemDto problemDto) {
        // 更新problem表
        Problem problem = problemDto.getProblem();
        if (!StringUtils.isEmpty(problem.getSpjLanguage())) { // 如果是特判题目，规格化特判语言 SPJ-C SPJ-C++
            problem.setSpjLanguage("SPJ-" + problem.getSpjLanguage());
        }
        boolean problemUpdateResult = problemMapper.updateById(problem) == 1;

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
        // 可能需要更新或新增加的case列表
        List<ProblemCase> problemCaseList = new LinkedList<>();
        // 遍历上传的case列表，如果
        for (ProblemCase problemCase : problemDto.getSamples()) {
            if (problemCase.getId() != null) { // 已存在的case
                needDeleteProblemCases.remove(problemCase.getId());
                problemCaseList.add(problemCase);
            } else {
                problemCaseList.add(problemCase);
            }
        }

        // 执行批量删除操作
        boolean deleteCasesFromProblemResult = true;
        if (needDeleteProblemCases.size() > 0) {
            deleteCasesFromProblemResult = problemCaseService.removeByIds(needDeleteProblemCases);
        }
        // 执行批量添加或更新操作
        boolean addCasesToProblemResult = true;
        if (problemCaseList.size() > 0) {
            addCasesToProblemResult = problemCaseService.saveOrUpdateBatch(problemCaseList);
        }

        // 评测数据有被修改则需要对判题机本地的数据进行重新初始化
        toJudgeService.initTestCase(problemDto.getProblem().getId(),
                !StringUtils.isEmpty(problemDto.getProblem().getSpjCode()));


        if (problemUpdateResult && deleteCasesFromProblemResult && deleteLanguagesFromProblemResult && deleteTagsFromProblemResult
                && addCasesToProblemResult && addLanguagesToProblemResult && addTagsToProblemResult) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    @Transactional
    public boolean adminAddProblem(ProblemDto problemDto) {

        Problem problem = problemDto.getProblem();
        if (!StringUtils.isEmpty(problem.getSpjLanguage())) { // 如果是特判题目，规格化特判语言 SPJ-C SPJ-C++
            problem.setSpjLanguage("SPJ-" + problem.getSpjLanguage());
        }
        boolean addProblemResult = problemMapper.insert(problem) == 1;
        long pid = problemDto.getProblem().getId();
        // 为新的题目添加对应的language
        List<ProblemLanguage> problemLanguageList = new LinkedList<>();
        for (Language language : problemDto.getLanguages()) {
            problemLanguageList.add(new ProblemLanguage().setPid(pid).setLid(language.getId()));
        }
        boolean addLangToProblemResult = problemLanguageService.saveOrUpdateBatch(problemLanguageList);


        // 为新的题目添加对应的case
        problemDto.getSamples().forEach(problemCase -> problemCase.setPid(pid)); // 设置好新题目的pid
        boolean addCasesToProblemResult = problemCaseService.saveOrUpdateBatch(problemDto.getSamples());

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
            return true;
        } else {
            return false;
        }
    }
}

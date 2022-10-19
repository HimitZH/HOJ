package top.hcode.hoj.manager.group.problem;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.exception.StatusNotFoundException;
import top.hcode.hoj.dao.group.GroupEntityService;
import top.hcode.hoj.dao.group.GroupProblemEntityService;
import top.hcode.hoj.dao.judge.JudgeEntityService;
import top.hcode.hoj.dao.problem.ProblemCaseEntityService;
import top.hcode.hoj.dao.problem.ProblemEntityService;
import top.hcode.hoj.dao.problem.TagEntityService;
import top.hcode.hoj.judge.Dispatcher;
import top.hcode.hoj.pojo.dto.ProblemDTO;
import top.hcode.hoj.pojo.entity.group.Group;
import top.hcode.hoj.pojo.dto.CompileDTO;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.problem.ProblemCase;
import top.hcode.hoj.pojo.entity.problem.Tag;
import top.hcode.hoj.pojo.vo.ProblemVO;
import top.hcode.hoj.pojo.vo.UserRolesVO;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.validator.GroupValidator;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
@Component
@RefreshScope
public class GroupProblemManager {

    @Autowired
    private GroupProblemEntityService groupProblemEntityService;

    @Autowired
    private GroupEntityService groupEntityService;

    @Autowired
    private ProblemEntityService problemEntityService;

    @Autowired
    private JudgeEntityService judgeEntityService;

    @Autowired
    private ProblemCaseEntityService problemCaseEntityService;

    @Autowired
    private GroupValidator groupValidator;

    @Autowired
    private TagEntityService tagEntityService;

    @Autowired
    private Dispatcher dispatcher;

    @Value("${hoj.judge.token}")
    private String judgeToken;

    public IPage<ProblemVO> getProblemList(Integer limit, Integer currentPage, Long gid) throws StatusNotFoundException, StatusForbiddenException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }

        if (!groupValidator.isGroupMember(userRolesVo.getUid(), gid) && !isRoot) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;

        return groupProblemEntityService.getProblemList(limit, currentPage, gid);
    }

    public IPage<Problem> getAdminProblemList(Integer limit, Integer currentPage, Long gid) throws StatusNotFoundException, StatusForbiddenException {

        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }

        if (!groupValidator.isGroupAdmin(userRolesVo.getUid(), gid) && !isRoot) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;

        return groupProblemEntityService.getAdminProblemList(limit, currentPage, gid);
    }

    public Problem getProblem(Long pid) throws StatusForbiddenException, StatusNotFoundException, StatusFailException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Problem problem = problemEntityService.getById(pid);

        if (problem == null) {
            throw new StatusNotFoundException("该题目不存在！");
        }

        Long gid = problem.getGid();

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }

        if (!groupValidator.isGroupRoot(userRolesVo.getUid(), gid) && !userRolesVo.getUsername().equals(problem.getAuthor()) && !isRoot) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        return problem;
    }

    public void addProblem(ProblemDTO problemDto) throws StatusForbiddenException, StatusNotFoundException, StatusFailException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Long gid = problemDto.getProblem().getGid();

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }

        if (!isRoot && !groupValidator.isGroupAdmin(userRolesVo.getUid(), gid)) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        problemDto.getProblem().setProblemId(group.getShortName() + problemDto.getProblem().getProblemId());

        QueryWrapper<Problem> problemQueryWrapper = new QueryWrapper<>();
        problemQueryWrapper.eq("problem_id", problemDto.getProblem().getProblemId().toUpperCase());
        int sameProblemIDCount = problemEntityService.count(problemQueryWrapper);
        if (sameProblemIDCount > 0) {
            throw new StatusFailException("该题目的Problem ID已存在，请更换！");
        }

        problemDto.getProblem().setIsGroup(true);

        List<Tag> tagList = new LinkedList<>();
        for (Tag tag : problemDto.getTags()) {
            if (tag.getGid() != null && tag.getGid().longValue() != gid) {
                throw new StatusForbiddenException("对不起，您无权限操作！");
            }

            if (tag.getId() == null) {
                tag.setGid(gid);
            }

            tagList.add(tag);
        }

        problemDto.setTags(tagList);

        boolean isOk = problemEntityService.adminAddProblem(problemDto);
        if (!isOk) {
            throw new StatusFailException("添加失败");
        }
    }

    public void updateProblem(ProblemDTO problemDto) throws StatusForbiddenException, StatusNotFoundException, StatusFailException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Long pid = problemDto.getProblem().getId();

        Problem problem = problemEntityService.getById(pid);

        if (problem == null) {
            throw new StatusNotFoundException("该题目不存在！");
        }

        Long gid = problem.getGid();

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }

        if (!userRolesVo.getUsername().equals(problem.getAuthor()) && !isRoot
                && !groupValidator.isGroupRoot(userRolesVo.getUid(), gid)) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        problemDto.getProblem().setProblemId(group.getShortName() + problemDto.getProblem().getProblemId());
        String problemId = problemDto.getProblem().getProblemId().toUpperCase();

        QueryWrapper<Problem> problemQueryWrapper = new QueryWrapper<>();
        problemQueryWrapper.eq("problem_id", problemId);

        Problem existedProblem = problemEntityService.getOne(problemQueryWrapper);

        problemDto.getProblem().setModifiedUser(userRolesVo.getUsername());

        if (existedProblem != null && existedProblem.getId().longValue() != pid) {
            throw new StatusFailException("当前的Problem ID 已被使用，请重新更换新的！");
        }

        problemDto.getProblem().setIsGroup(problem.getIsGroup());

        List<Tag> tagList = new LinkedList<>();
        for (Tag tag : problemDto.getTags()) {
            if (tag.getGid() != null && tag.getGid().longValue() != gid) {
                throw new StatusForbiddenException("对不起，您无权限操作！");
            }

            if (tag.getId() == null) {
                tag.setGid(gid);
            }

            tagList.add(tag);
        }

        problemDto.setTags(tagList);

        boolean isOk = problemEntityService.adminUpdateProblem(problemDto);
        if (isOk) {
            if (existedProblem == null) {
                UpdateWrapper<Judge> judgeUpdateWrapper = new UpdateWrapper<>();
                judgeUpdateWrapper.eq("pid", problemDto.getProblem().getId())
                        .set("display_pid", problemId);
                judgeEntityService.update(judgeUpdateWrapper);
            }
        } else {
            throw new StatusFailException("修改失败");
        }
    }

    public void deleteProblem(Long pid) throws StatusForbiddenException, StatusNotFoundException, StatusFailException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Problem problem = problemEntityService.getById(pid);

        if (problem == null) {
            throw new StatusNotFoundException("该题目不存在！");
        }

        Long gid = problem.getGid();

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }

        if (!groupValidator.isGroupRoot(userRolesVo.getUid(), gid) && !userRolesVo.getUsername().equals(problem.getAuthor()) && !isRoot) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        boolean isOk = problemEntityService.removeById(pid);
        if (isOk) {
            FileUtil.del(Constants.File.TESTCASE_BASE_FOLDER.getPath() + File.separator + "problem_" + pid);
        } else {
            throw new StatusFailException("删除失败！");
        }
    }

    public List<ProblemCase> getProblemCases(Long pid, Boolean isUpload) throws StatusForbiddenException, StatusNotFoundException, StatusFailException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Problem problem = problemEntityService.getById(pid);

        if (problem == null) {
            throw new StatusNotFoundException("该题目不存在！");
        }

        Long gid = problem.getGid();

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }

        if (!userRolesVo.getUsername().equals(problem.getAuthor()) && !isRoot
                && !groupValidator.isGroupRoot(userRolesVo.getUid(), gid)) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        QueryWrapper<ProblemCase> problemCaseQueryWrapper = new QueryWrapper<>();
        problemCaseQueryWrapper.eq("pid", pid).eq("status", 0);
        if (isUpload) {
            problemCaseQueryWrapper.last("order by length(input) asc,input asc");
        }
        return problemCaseEntityService.list(problemCaseQueryWrapper);
    }

    public List<Tag> getAllProblemTagsList(Long gid) throws StatusNotFoundException, StatusForbiddenException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }

        if (!isRoot && !groupValidator.isGroupAdmin(userRolesVo.getUid(), gid)) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        List<Tag> tagList;
        QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
        tagQueryWrapper.isNull("gid").or().eq("gid", gid);
        tagList = tagEntityService.list(tagQueryWrapper);

        return tagList;
    }

    public void compileSpj(CompileDTO compileDTO, Long gid) throws StatusForbiddenException, StatusNotFoundException, StatusFailException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }

        if (!isRoot && !groupValidator.isGroupAdmin(userRolesVo.getUid(), gid)) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        if (StringUtils.isEmpty(compileDTO.getCode()) ||
                StringUtils.isEmpty(compileDTO.getLanguage())) {
            throw new StatusFailException("参数不能为空！");
        }

        compileDTO.setToken(judgeToken);
        dispatcher.dispatch(Constants.TaskType.COMPILE_SPJ, compileDTO);
    }

    public void compileInteractive(CompileDTO compileDTO, Long gid) throws StatusForbiddenException, StatusNotFoundException, StatusFailException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }

        if (!isRoot && !groupValidator.isGroupAdmin(userRolesVo.getUid(), gid)) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        if (StringUtils.isEmpty(compileDTO.getCode()) ||
                StringUtils.isEmpty(compileDTO.getLanguage())) {
            throw new StatusFailException("参数不能为空！");
        }

        compileDTO.setToken(judgeToken);
        dispatcher.dispatch(Constants.TaskType.COMPILE_INTERACTIVE, compileDTO);
    }

    public void changeProblemAuth(Long pid, Integer auth) throws StatusForbiddenException, StatusNotFoundException, StatusFailException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Problem problem = problemEntityService.getById(pid);

        if (problem == null) {
            throw new StatusNotFoundException("该题目不存在！");
        }

        Long gid = problem.getGid();

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }

        if (!userRolesVo.getUsername().equals(problem.getAuthor()) && !isRoot
                && !groupValidator.isGroupRoot(userRolesVo.getUid(), gid)) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        UpdateWrapper<Problem> problemUpdateWrapper = new UpdateWrapper<>();
        problemUpdateWrapper.eq("id", pid)
                .set("auth", auth)
                .set("modified_user", userRolesVo.getUsername());

        boolean isOk = problemEntityService.update(problemUpdateWrapper);
        if (!isOk) {
            throw new StatusFailException("修改失败");
        }
    }

    public void applyPublic(Long pid, Boolean isApplied) throws StatusNotFoundException, StatusForbiddenException, StatusFailException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Problem problem = problemEntityService.getById(pid);

        if (problem == null) {
            throw new StatusNotFoundException("该题目不存在！");
        }
        Long gid = problem.getGid();
        Group group = groupEntityService.getById(gid);
        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }
        if (!userRolesVo.getUsername().equals(problem.getAuthor()) && !isRoot
                && !groupValidator.isGroupRoot(userRolesVo.getUid(), gid)) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }
        UpdateWrapper<Problem> problemUpdateWrapper = new UpdateWrapper<>();
        problemUpdateWrapper.eq("id", pid);
        if (isApplied) { // 申请
            problemUpdateWrapper.set("apply_public_progress", 1);
        } else { // 取消
            problemUpdateWrapper.set("apply_public_progress", null);
            problemUpdateWrapper.set("is_group", true);
        }
        boolean isOk = problemEntityService.update(problemUpdateWrapper);
        if (!isOk) {
            throw new StatusFailException("修改失败");
        }
    }
}

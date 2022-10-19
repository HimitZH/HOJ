package top.hcode.hoj.manager.group.contest;

import cn.hutool.core.map.MapUtil;
import org.springframework.transaction.annotation.Transactional;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.exception.StatusNotFoundException;
import top.hcode.hoj.dao.contest.ContestEntityService;
import top.hcode.hoj.dao.contest.ContestProblemEntityService;
import top.hcode.hoj.dao.group.GroupEntityService;
import top.hcode.hoj.dao.judge.JudgeEntityService;
import top.hcode.hoj.dao.problem.ProblemEntityService;
import top.hcode.hoj.manager.admin.contest.AdminContestProblemManager;
import top.hcode.hoj.pojo.dto.ContestProblemDTO;
import top.hcode.hoj.pojo.dto.ProblemDTO;
import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.pojo.entity.contest.ContestProblem;
import top.hcode.hoj.pojo.entity.group.Group;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.problem.Tag;
import top.hcode.hoj.pojo.vo.UserRolesVO;
import top.hcode.hoj.validator.GroupValidator;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
@Component
public class GroupContestProblemManager {

    @Autowired
    private GroupEntityService groupEntityService;

    @Autowired
    private ContestEntityService contestEntityService;

    @Autowired
    private AdminContestProblemManager adminContestProblemManager;

    @Autowired
    private ProblemEntityService problemEntityService;

    @Autowired
    private ContestProblemEntityService contestProblemEntityService;

    @Autowired
    private JudgeEntityService judgeEntityService;

    @Autowired
    private GroupValidator groupValidator;

    public HashMap<String, Object> getContestProblemList(Integer limit, Integer currentPage, String keyword, Long cid, Integer problemType, String oj) throws StatusNotFoundException, StatusForbiddenException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Contest contest = contestEntityService.getById(cid);

        if (contest == null) {
            throw new StatusNotFoundException("该比赛不存在！");
        }

        Long gid = contest.getGid();

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }

        if (!groupValidator.isGroupRoot(userRolesVo.getUid(), gid) && !userRolesVo.getUid().equals(contest.getUid()) && !isRoot) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        return adminContestProblemManager.getProblemList(limit, currentPage, keyword, cid, problemType, oj);
    }

    public Map<Object, Object> addProblem(ProblemDTO problemDto) throws StatusNotFoundException, StatusForbiddenException, StatusFailException {
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

        QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("problem_id", problemDto.getProblem().getProblemId().toUpperCase());

        Problem problem = problemEntityService.getOne(queryWrapper);
        if (problem != null) {
            throw new StatusFailException("该题目的Problem ID已存在，请更换！");
        }

        problemDto.getProblem().setAuth(3);
        problemDto.getProblem().setIsGroup(true);

        List<Tag> tagList = new LinkedList<>();
        for (Tag tag : problemDto.getTags()) {
            if (tag.getGid() != null && !tag.getGid().equals(gid)) {
                throw new StatusForbiddenException("对不起，您无权限操作！");
            }

            if (tag.getId() == null) {
                tag.setGid(gid);
            }

            tagList.add(tag);
        }

        problemDto.setTags(tagList);

        boolean isOk = problemEntityService.adminAddProblem(problemDto);
        if (isOk) {
            return MapUtil.builder().put("pid", problemDto.getProblem().getId()).map();
        } else {
            throw new StatusFailException("添加失败");
        }
    }

    public ContestProblem getContestProblem(Long pid, Long cid) throws StatusNotFoundException, StatusForbiddenException, StatusFailException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Contest contest = contestEntityService.getById(cid);

        if (contest == null) {
            throw new StatusNotFoundException("该比赛不存在！");
        }

        Long gid = contest.getGid();

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }

        if (!userRolesVo.getUid().equals(contest.getUid()) && !isRoot
                && !groupValidator.isGroupRoot(userRolesVo.getUid(), gid)) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        QueryWrapper<ContestProblem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cid", cid).eq("pid", pid);

        ContestProblem contestProblem = contestProblemEntityService.getOne(queryWrapper);
        if (contestProblem == null) {
            throw new StatusFailException("该比赛题目不存在！");
        }
        return contestProblem;
    }

    public void updateContestProblem(ContestProblem contestProblem) throws StatusNotFoundException, StatusForbiddenException, StatusFailException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Long cid = contestProblem.getCid();

        Contest contest = contestEntityService.getById(cid);

        if (contest == null) {
            throw new StatusNotFoundException("该比赛不存在！");
        }

        Long gid = contest.getGid();

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }

        if (!userRolesVo.getUid().equals(contest.getUid()) && !isRoot
                && !groupValidator.isGroupRoot(userRolesVo.getUid(), gid)) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        boolean isOk = contestProblemEntityService.saveOrUpdate(contestProblem);
        if (isOk) {
            contestProblemEntityService.syncContestRecord(contestProblem.getPid(), contestProblem.getCid(), contestProblem.getDisplayId());
        } else {
            throw new StatusFailException("更新失败！");
        }
    }

    public void deleteContestProblem(Long pid, Long cid) throws StatusNotFoundException, StatusForbiddenException, StatusFailException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Contest contest = contestEntityService.getById(cid);

        if (contest == null) {
            throw new StatusNotFoundException("该比赛不存在！");
        }

        Long gid = contest.getGid();

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }

        if (!userRolesVo.getUid().equals(contest.getUid()) && !isRoot
                && !groupValidator.isGroupRoot(userRolesVo.getUid(), gid)) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        QueryWrapper<ContestProblem> contestProblemQueryWrapper = new QueryWrapper<>();
        contestProblemQueryWrapper.eq("cid", cid).eq("pid", pid);
        boolean isOk = contestProblemEntityService.remove(contestProblemQueryWrapper);
        if (isOk) {
            UpdateWrapper<Judge> judgeUpdateWrapper = new UpdateWrapper<>();
            judgeUpdateWrapper.eq("cid", cid).eq("pid", pid);
            judgeEntityService.remove(judgeUpdateWrapper);
        } else {
            throw new StatusFailException("删除失败！");
        }
    }

    public void addProblemFromPublic(ContestProblemDTO contestProblemDto) throws StatusNotFoundException, StatusForbiddenException, StatusFailException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Long pid = contestProblemDto.getPid();

        Problem problem = problemEntityService.getById(pid);

        if (problem == null || problem.getAuth() != 1 || problem.getIsGroup()) {
            throw new StatusNotFoundException("该题目不存在或已被隐藏！");
        }

        Long cid = contestProblemDto.getCid();

        Contest contest = contestEntityService.getById(cid);

        if (contest == null) {
            throw new StatusNotFoundException("该比赛不存在！");
        }

        Long gid = contest.getGid();

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }

        if (!userRolesVo.getUid().equals(contest.getUid()) && !isRoot
                && !groupValidator.isGroupRoot(userRolesVo.getUid(), gid)) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        String displayId = contestProblemDto.getDisplayId();

        QueryWrapper<ContestProblem> contestProblemQueryWrapper = new QueryWrapper<>();
        contestProblemQueryWrapper.eq("cid", cid)
                .and(wrapper -> wrapper.eq("pid", pid)
                        .or()
                        .eq("display_id", displayId));
        ContestProblem contestProblem = contestProblemEntityService.getOne(contestProblemQueryWrapper, false);
        if (contestProblem != null) {
            throw new StatusFailException("添加失败，该题目已添加或者题目的比赛展示ID已存在！");
        }

        String displayName = problem.getTitle();

        ContestProblem newCProblem = new ContestProblem();

        boolean isOk = contestProblemEntityService.saveOrUpdate(newCProblem
                .setCid(cid).setPid(pid).setDisplayTitle(displayName).setDisplayId(displayId));
        if (!isOk) {
            throw new StatusFailException("添加失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void addProblemFromGroup(String problemId, Long cid, String displayId) throws StatusNotFoundException, StatusForbiddenException, StatusFailException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Contest contest = contestEntityService.getById(cid);

        if (contest == null) {
            throw new StatusNotFoundException("该训练不存在！");
        }

        Long gid = contest.getGid();

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }

        if (!userRolesVo.getUid().equals(contest.getUid()) && !isRoot
                && !groupValidator.isGroupRoot(userRolesVo.getUid(), gid)) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        QueryWrapper<Problem> problemQueryWrapper = new QueryWrapper<>();
        problemQueryWrapper.eq("problem_id", problemId).eq("gid", gid);

        Problem problem = problemEntityService.getOne(problemQueryWrapper);

        if (problem == null) {
            throw new StatusNotFoundException("该题目不存在或不是团队题目！");
        }

        QueryWrapper<ContestProblem> contestProblemQueryWrapper = new QueryWrapper<>();
        contestProblemQueryWrapper.eq("cid", cid)
                .and(wrapper -> wrapper.eq("pid", problem.getId())
                        .or()
                        .eq("display_id", displayId));

        ContestProblem contestProblem = contestProblemEntityService.getOne(contestProblemQueryWrapper);
        if (contestProblem != null) {
            throw new StatusFailException("添加失败，该题目已添加或者题目的比赛展示ID已存在！");
        }

        ContestProblem newCProblem = new ContestProblem();
        String displayName = problem.getTitle();

        boolean updateProblem = problemEntityService.saveOrUpdate(problem.setAuth(3));

        boolean isOk = contestProblemEntityService.saveOrUpdate(newCProblem
                .setCid(cid).setPid(problem.getId()).setDisplayTitle(displayName).setDisplayId(displayId));
        if (!isOk || !updateProblem) {
            throw new StatusFailException("添加失败");
        }
    }
}

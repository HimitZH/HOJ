package top.hcode.hoj.manager.group.discussion;

import org.springframework.util.StringUtils;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.exception.StatusNotFoundException;
import top.hcode.hoj.dao.discussion.DiscussionEntityService;
import top.hcode.hoj.dao.group.GroupEntityService;
import top.hcode.hoj.dao.problem.ProblemEntityService;
import top.hcode.hoj.dao.user.UserAcproblemEntityService;
import top.hcode.hoj.pojo.entity.discussion.Discussion;
import top.hcode.hoj.pojo.entity.group.Group;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.user.UserAcproblem;
import top.hcode.hoj.pojo.vo.UserRolesVO;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.RedisUtils;
import top.hcode.hoj.validator.GroupValidator;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
@Component
public class GroupDiscussionManager {

    @Autowired
    private GroupEntityService groupEntityService;

    @Autowired
    private DiscussionEntityService discussionEntityService;

    @Autowired
    private GroupValidator groupValidator;

    @Autowired
    private ProblemEntityService problemEntityService;

    @Autowired
    private UserAcproblemEntityService userAcproblemEntityService;

    @Autowired
    private RedisUtils redisUtils;

    public IPage<Discussion> getDiscussionList(Integer limit,
                                               Integer currentPage,
                                               Long gid,
                                               String pid) throws StatusNotFoundException, StatusForbiddenException {
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

        QueryWrapper<Discussion> discussionQueryWrapper = new QueryWrapper<>();

        if (!StringUtils.isEmpty(pid)) {
            discussionQueryWrapper.eq("pid", pid);
        }

        IPage<Discussion> iPage = new Page<>(currentPage, limit);

        discussionQueryWrapper
                .eq("status", 0)
                .eq("gid", gid)
                .orderByDesc("top_priority")
                .orderByDesc("gmt_create")
                .orderByDesc("like_num")
                .orderByDesc("view_num");

        return discussionEntityService.page(iPage, discussionQueryWrapper);
    }

    public IPage<Discussion> getAdminDiscussionList(Integer limit, Integer currentPage, Long gid) throws StatusNotFoundException, StatusForbiddenException {
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

        QueryWrapper<Discussion> discussionQueryWrapper = new QueryWrapper<>();

        IPage<Discussion> iPage = new Page<>(currentPage, limit);

        discussionQueryWrapper
                .eq("gid", gid)
                .orderByDesc("top_priority")
                .orderByDesc("gmt_create")
                .orderByDesc("like_num")
                .orderByDesc("view_num");

        return discussionEntityService.page(iPage, discussionQueryWrapper);
    }

    public void addDiscussion(Discussion discussion) throws StatusForbiddenException, StatusNotFoundException, StatusFailException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");
        boolean isProblemAdmin = SecurityUtils.getSubject().hasRole("problem_admin");
        boolean isAdmin = SecurityUtils.getSubject().hasRole("admin");

        Long gid = discussion.getGid();

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }

        if (!isRoot && !groupValidator.isGroupMember(userRolesVo.getUid(), gid)) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        String problemId = discussion.getPid();
        if (problemId != null) {
            QueryWrapper<Problem> problemQueryWrapper = new QueryWrapper<>();
            problemQueryWrapper.eq("problem_id", problemId);
            Problem problem = problemEntityService.getOne(problemQueryWrapper);

            if (problem == null) {
                throw new StatusNotFoundException("该题目不存在");
            } else if (problem.getIsGroup()) {
                discussion.setGid(problem.getGid());
            }
        }

        if (!isRoot && !isProblemAdmin && !isAdmin) {
            QueryWrapper<UserAcproblem> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uid", userRolesVo.getUid()).select("distinct pid");
            int userAcProblemCount = userAcproblemEntityService.count(queryWrapper);

            if (userAcProblemCount < 10) {
                throw new StatusForbiddenException("对不起，您暂时不能评论！请先去提交题目通过10道以上!");
            }

            String lockKey = Constants.Account.DISCUSSION_ADD_NUM_LOCK.getCode() + userRolesVo.getUid();
            Integer num = (Integer) redisUtils.get(lockKey);
            if (num == null) {
                redisUtils.set(lockKey, 1, 3600 * 24);
            } else if (num >= 5) {
                throw new StatusForbiddenException("对不起，您今天发帖次数已超过5次，已被限制！");
            } else {
                redisUtils.incr(lockKey, 1);
            }
        }

        discussion.setAuthor(userRolesVo.getUsername())
                .setAvatar(userRolesVo.getAvatar())
                .setUid(userRolesVo.getUid());

        if (groupValidator.isGroupRoot(userRolesVo.getUid(), gid)) {
            discussion.setRole("root");
        } else if (groupValidator.isGroupAdmin(userRolesVo.getUid(), gid)) {
            discussion.setRole("admin");
        } else {
            discussion.setTopPriority(false);
        }

        boolean isOk = discussionEntityService.save(discussion);
        if (!isOk) {
            throw new StatusFailException("添加失败");
        }
    }

    public void updateDiscussion(Discussion discussion) throws StatusForbiddenException, StatusNotFoundException, StatusFailException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Long gid = discussion.getGid();

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }

        if (!groupValidator.isGroupAdmin(userRolesVo.getUid(), gid) && !discussion.getUid().equals(userRolesVo.getUid()) && !isRoot) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        boolean isOk = discussionEntityService.updateById(discussion);
        if (!isOk) {
            throw new StatusFailException("修改失败");
        }
    }

    public void deleteDiscussion(Long did) throws StatusForbiddenException, StatusNotFoundException, StatusFailException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Discussion discussion = discussionEntityService.getById(did);

        Long gid = discussion.getGid();

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }

        if (!groupValidator.isGroupAdmin(userRolesVo.getUid(), gid) && !discussion.getUid().equals(userRolesVo.getUid()) && !isRoot) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        boolean isOk = discussionEntityService.removeById(did);
        if (!isOk) {
            throw new StatusFailException("删除失败");
        }
    }
}

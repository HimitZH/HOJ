package top.hcode.hoj.manager.oj;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import top.hcode.hoj.annotation.HOJAccessEnum;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.exception.StatusNotFoundException;
import top.hcode.hoj.config.NacosSwitchConfig;
import top.hcode.hoj.config.SwitchConfig;
import top.hcode.hoj.dao.discussion.DiscussionEntityService;
import top.hcode.hoj.dao.discussion.DiscussionLikeEntityService;
import top.hcode.hoj.dao.discussion.DiscussionReportEntityService;
import top.hcode.hoj.dao.problem.CategoryEntityService;
import top.hcode.hoj.dao.problem.ProblemEntityService;
import top.hcode.hoj.dao.user.UserAcproblemEntityService;
import top.hcode.hoj.exception.AccessException;
import top.hcode.hoj.pojo.entity.discussion.Discussion;
import top.hcode.hoj.pojo.entity.discussion.DiscussionLike;
import top.hcode.hoj.pojo.entity.discussion.DiscussionReport;
import top.hcode.hoj.pojo.entity.problem.Category;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.user.UserAcproblem;
import top.hcode.hoj.pojo.vo.DiscussionVO;
import top.hcode.hoj.shiro.AccountProfile;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.RedisUtils;
import top.hcode.hoj.validator.AccessValidator;
import top.hcode.hoj.validator.CommonValidator;
import top.hcode.hoj.validator.GroupValidator;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/11 15:21
 * @Description:
 */
@Component
public class DiscussionManager {
    @Autowired
    private DiscussionEntityService discussionEntityService;

    @Autowired
    private DiscussionLikeEntityService discussionLikeEntityService;

    @Autowired
    private CategoryEntityService categoryEntityService;

    @Autowired
    private DiscussionReportEntityService discussionReportEntityService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private UserAcproblemEntityService userAcproblemEntityService;

    @Autowired
    private ProblemEntityService problemEntityService;

    @Autowired
    private GroupValidator groupValidator;

    @Autowired
    private AccessValidator accessValidator;

    @Autowired
    private NacosSwitchConfig nacosSwitchConfig;

    @Autowired
    private CommonValidator commonValidator;

    public IPage<Discussion> getDiscussionList(Integer limit,
                                               Integer currentPage,
                                               Integer categoryId,
                                               String pid,
                                               boolean onlyMine,
                                               String keyword,
                                               boolean admin) {
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        QueryWrapper<Discussion> discussionQueryWrapper = new QueryWrapper<>();

        IPage<Discussion> iPage = new Page<>(currentPage, limit);

        if (categoryId != null) {
            discussionQueryWrapper.eq("category_id", categoryId);
        }

        if (!StringUtils.isEmpty(keyword)) {

            final String key = keyword.trim();

            discussionQueryWrapper.and(wrapper -> wrapper.like("title", key).or()
                    .like("author", key).or()
                    .like("id", key).or()
                    .like("description", key));
        }

        boolean isAdmin = SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("problem_admin")
                || SecurityUtils.getSubject().hasRole("admin");

        if (!StringUtils.isEmpty(pid)) {
            discussionQueryWrapper.eq("pid", pid);
        }

        if (!(admin && isAdmin)) {
            discussionQueryWrapper.isNull("gid");
        }

        discussionQueryWrapper
                .eq(!(admin && isAdmin), "status", 0)
                .orderByDesc("top_priority")
                .orderByDesc("gmt_create")
                .orderByDesc("like_num")
                .orderByDesc("view_num");

        if (onlyMine && userRolesVo != null) {
            discussionQueryWrapper.eq("uid", userRolesVo.getUid());
        }
        IPage<Discussion> discussionIPage = discussionEntityService.page(iPage, discussionQueryWrapper);
        List<Discussion> records = discussionIPage.getRecords();
        if (!CollectionUtils.isEmpty(records)) {
            for (Discussion discussion : records) {
                if (userRolesVo == null) {
                    discussion.setContent(null);
                } else if (!userRolesVo.getUid().equals(discussion.getUid())) {
                    discussion.setContent(null);
                }
            }
        }
        return discussionIPage;
    }

    public DiscussionVO getDiscussion(Integer did) throws StatusNotFoundException, StatusForbiddenException, AccessException {

        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        String uid = null;

        if (userRolesVo != null) {
            uid = userRolesVo.getUid();
        }

        DiscussionVO discussionVo = discussionEntityService.getDiscussion(did, uid);

        if (discussionVo == null) {
            throw new StatusNotFoundException("对不起，该讨论不存在！");
        }

        if (discussionVo.getGid() != null && uid == null) {
            throw new StatusNotFoundException("对不起，请先登录后再访问团队讨论！");
        }

        if (discussionVo.getStatus() == 1) {
            throw new StatusForbiddenException("对不起，该讨论已被封禁！");
        }

        if (discussionVo.getGid() != null) {
            accessValidator.validateAccess(HOJAccessEnum.GROUP_DISCUSSION);
            if (!isRoot && !discussionVo.getUid().equals(uid)
                    && !groupValidator.isGroupMember(uid, discussionVo.getGid())) {
                throw new StatusForbiddenException("对不起，您无权限操作！");
            }
        } else {
            accessValidator.validateAccess(HOJAccessEnum.PUBLIC_DISCUSSION);
        }

        // 浏览量+1
        UpdateWrapper<Discussion> discussionUpdateWrapper = new UpdateWrapper<>();
        discussionUpdateWrapper.setSql("view_num=view_num+1").eq("id", discussionVo.getId());
        discussionEntityService.update(discussionUpdateWrapper);
        discussionVo.setViewNum(discussionVo.getViewNum() + 1);

        return discussionVo;
    }

    public void addDiscussion(Discussion discussion) throws StatusFailException, StatusForbiddenException, StatusNotFoundException {

        commonValidator.validateContent(discussion.getTitle(), "讨论标题", 255);
        commonValidator.validateContent(discussion.getDescription(), "讨论描述", 255);
        commonValidator.validateContent(discussion.getContent(), "讨论", 65535);
        commonValidator.validateNotEmpty(discussion.getCategoryId(), "讨论分类");

        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");
        boolean isProblemAdmin = SecurityUtils.getSubject().hasRole("problem_admin");
        boolean isAdmin = SecurityUtils.getSubject().hasRole("admin");

        String problemId = discussion.getPid();
        if (problemId != null) {
            QueryWrapper<Problem> problemQueryWrapper = new QueryWrapper<>();
            problemQueryWrapper.eq("problem_id", problemId);
            int problemCount = problemEntityService.count(problemQueryWrapper);
            if (problemCount == 0) {
                throw new StatusNotFoundException("对不起，该题目不存在，无法发布题解!");
            }
        }

        if (discussion.getGid() != null) {
            if (!isRoot
                    && !discussion.getUid().equals(userRolesVo.getUid())
                    && !groupValidator.isGroupMember(userRolesVo.getUid(), discussion.getGid())) {
                throw new StatusForbiddenException("对不起，您无权限操作！");
            }
        }

        // 除管理员外 其它用户需要AC20道题目以上才可发帖，同时限制一天只能发帖5次
        if (!isRoot && !isProblemAdmin && !isAdmin) {
            QueryWrapper<UserAcproblem> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uid", userRolesVo.getUid()).select("distinct pid");
            int userAcProblemCount = userAcproblemEntityService.count(queryWrapper);
            SwitchConfig switchConfig = nacosSwitchConfig.getSwitchConfig();
            if (userAcProblemCount < switchConfig.getDefaultCreateDiscussionACInitValue()) {
                throw new StatusForbiddenException("对不起，您暂时不能评论！请先去提交题目通过" + switchConfig.getDefaultCreateDiscussionACInitValue() + "道以上!");
            }

            String lockKey = Constants.Account.DISCUSSION_ADD_NUM_LOCK.getCode() + userRolesVo.getUid();
            Integer num = (Integer) redisUtils.get(lockKey);
            if (num == null) {
                redisUtils.set(lockKey, 1, 3600 * 24);
            } else if (num >= switchConfig.getDefaultCreateDiscussionDailyLimit()) {
                throw new StatusForbiddenException("对不起，您今天发帖次数已超过" + switchConfig.getDefaultCreateDiscussionDailyLimit() + "次，已被限制！");
            } else {
                redisUtils.incr(lockKey, 1);
            }
        }

        discussion.setAuthor(userRolesVo.getUsername())
                .setAvatar(userRolesVo.getAvatar())
                .setUid(userRolesVo.getUid());

        if (SecurityUtils.getSubject().hasRole("root")) {
            discussion.setRole("root");
        } else if (SecurityUtils.getSubject().hasRole("admin")
                || SecurityUtils.getSubject().hasRole("problem_admin")) {
            discussion.setRole("admin");
        } else {
            // 如果不是管理员角色，一律重置为不置顶
            discussion.setTopPriority(false);
        }

        boolean isOk = discussionEntityService.saveOrUpdate(discussion);
        if (!isOk) {
            throw new StatusFailException("发布失败，请重新尝试！");
        }
    }


    public void updateDiscussion(Discussion discussion) throws StatusFailException, StatusForbiddenException, StatusNotFoundException {

        commonValidator.validateNotEmpty(discussion.getId(), "讨论ID");
        commonValidator.validateContent(discussion.getTitle(), "讨论标题", 255);
        commonValidator.validateContent(discussion.getDescription(), "讨论描述", 255);
        commonValidator.validateContent(discussion.getContent(), "讨论", 65535);
        commonValidator.validateNotEmpty(discussion.getCategoryId(), "讨论分类");

        QueryWrapper<Discussion> discussionQueryWrapper = new QueryWrapper<>();
        discussionQueryWrapper
                .select("id", "uid", "gid")
                .eq("id", discussion.getId());

        Discussion oriDiscussion = discussionEntityService.getOne(discussionQueryWrapper);
        if (oriDiscussion == null) {
            throw new StatusNotFoundException("更新失败，该讨论不存在！");
        }

        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");
        boolean isProblemAdmin = SecurityUtils.getSubject().hasRole("problem_admin");
        boolean isAdmin = SecurityUtils.getSubject().hasRole("admin");

        if (!isRoot
                && !oriDiscussion.getUid().equals(userRolesVo.getUid())
                && !(oriDiscussion.getGid() != null
                && groupValidator.isGroupAdmin(userRolesVo.getUid(), oriDiscussion.getGid()))) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }
        UpdateWrapper<Discussion> discussionUpdateWrapper = new UpdateWrapper<>();
        discussionUpdateWrapper.set("title", discussion.getTitle())
                .set("content", discussion.getContent())
                .set("description", discussion.getDescription())
                .set("category_id", discussion.getCategoryId())
                .set(isRoot || isProblemAdmin || isAdmin,
                        "top_priority", discussion.getTopPriority())
                .eq("id", discussion.getId());
        boolean isOk = discussionEntityService.update(discussionUpdateWrapper);
        if (!isOk) {
            throw new StatusFailException("修改失败");
        }
    }

    public void removeDiscussion(Integer did) throws StatusFailException, StatusForbiddenException, StatusNotFoundException {

        QueryWrapper<Discussion> discussionQueryWrapper = new QueryWrapper<>();
        discussionQueryWrapper
                .select("id", "uid", "gid")
                .eq("id", did);

        Discussion discussion = discussionEntityService.getOne(discussionQueryWrapper);
        if (discussion == null) {
            throw new StatusNotFoundException("删除失败，该讨论已不存在！");
        }

        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        if (!isRoot
                && !discussion.getUid().equals(userRolesVo.getUid())
                && !(discussion.getGid() != null
                && groupValidator.isGroupAdmin(userRolesVo.getUid(), discussion.getGid()))) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        UpdateWrapper<Discussion> discussionUpdateWrapper = new UpdateWrapper<Discussion>().eq("id", did);
        // 如果不是是管理员,则需要附加当前用户的uid条件
        if (!SecurityUtils.getSubject().hasRole("root")
                && !SecurityUtils.getSubject().hasRole("admin")
                && !SecurityUtils.getSubject().hasRole("problem_admin")) {
            discussionUpdateWrapper.eq("uid", userRolesVo.getUid());
        }
        boolean isOk = discussionEntityService.remove(discussionUpdateWrapper);
        if (!isOk) {
            throw new StatusFailException("删除失败，无权限或者该讨论不存在");
        }

    }

    @Transactional(rollbackFor = Exception.class)
    public void addDiscussionLike(Integer did, boolean toLike) throws StatusFailException, StatusForbiddenException, StatusNotFoundException {
        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        Discussion discussion = discussionEntityService.getById(did);

        if (discussion == null) {
            throw new StatusNotFoundException("对不起，该讨论不存在！");
        }

        if (discussion.getGid() != null) {
            boolean isRoot = SecurityUtils.getSubject().hasRole("root");
            if (!isRoot && !discussion.getUid().equals(userRolesVo.getUid())
                    && !groupValidator.isGroupMember(userRolesVo.getUid(), discussion.getGid())) {
                throw new StatusForbiddenException("对不起，您无权限操作！");
            }
        }

        QueryWrapper<DiscussionLike> discussionLikeQueryWrapper = new QueryWrapper<>();
        discussionLikeQueryWrapper.eq("did", did).eq("uid", userRolesVo.getUid());

        DiscussionLike discussionLike = discussionLikeEntityService.getOne(discussionLikeQueryWrapper, false);

        if (toLike) { // 添加点赞
            if (discussionLike == null) { // 如果不存在就添加
                boolean isSave = discussionLikeEntityService.saveOrUpdate(new DiscussionLike().setUid(userRolesVo.getUid()).setDid(did));
                if (!isSave) {
                    throw new StatusFailException("点赞失败，请重试尝试！");
                }
            }
            // 点赞+1
            UpdateWrapper<Discussion> discussionUpdateWrapper = new UpdateWrapper<>();
            discussionUpdateWrapper.eq("id", discussion.getId())
                    .setSql("like_num=like_num+1");
            discussionEntityService.update(discussionUpdateWrapper);
            // 当前帖子要不是点赞者的 才发送点赞消息
            if (!userRolesVo.getUsername().equals(discussion.getAuthor())) {
                discussionEntityService.updatePostLikeMsg(discussion.getUid(),
                        userRolesVo.getUid(),
                        did,
                        discussion.getGid());
            }
        } else { // 取消点赞
            if (discussionLike != null) { // 如果存在就删除
                boolean isDelete = discussionLikeEntityService.removeById(discussionLike.getId());
                if (!isDelete) {
                    throw new StatusFailException("取消点赞失败，请重试尝试！");
                }
            }
            // 点赞-1
            UpdateWrapper<Discussion> discussionUpdateWrapper = new UpdateWrapper<>();
            discussionUpdateWrapper.setSql("like_num=like_num-1").eq("id", did);
            discussionEntityService.update(discussionUpdateWrapper);
        }

    }

    public List<Category> getDiscussionCategory() {
        return categoryEntityService.list();
    }

    public List<Category> upsertDiscussionCategory(List<Category> categoryList) throws StatusFailException {
        List<Category> categories = categoryList.stream().filter(category -> category.getName() != null
                        && !category.getName().trim().isEmpty())
                .collect(Collectors.toList());
        boolean isOk = categoryEntityService.saveOrUpdateBatch(categories);
        if (!isOk) {
            throw new StatusFailException("修改失败");
        }
        return categoryEntityService.list();
    }

    public void addDiscussionReport(DiscussionReport discussionReport) throws StatusFailException {
        boolean isOk = discussionReportEntityService.saveOrUpdate(discussionReport);
        if (!isOk) {
            throw new StatusFailException("举报失败，请重新尝试");
        }
    }
}
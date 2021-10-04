package top.hcode.hoj.controller.oj;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.Category;
import top.hcode.hoj.pojo.entity.Discussion;
import top.hcode.hoj.pojo.entity.DiscussionLike;
import top.hcode.hoj.pojo.entity.DiscussionReport;
import top.hcode.hoj.pojo.vo.DiscussionVo;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.service.impl.CategoryServiceImpl;
import top.hcode.hoj.service.impl.DiscussionLikeServiceImpl;
import top.hcode.hoj.service.impl.DiscussionReportServiceImpl;
import top.hcode.hoj.service.impl.DiscussionServiceImpl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2021/05/04 10:14
 * @Description: 负责讨论与评论模块的数据接口
 */
@RestController
@RequestMapping("/api")
public class DiscussionController {

    @Autowired
    private DiscussionServiceImpl discussionService;

    @Autowired
    private DiscussionLikeServiceImpl discussionLikeService;

    @Autowired
    private CategoryServiceImpl categoryService;

    @Autowired
    private DiscussionReportServiceImpl discussionReportService;

    @GetMapping("/discussions")
    public CommonResult getDiscussionList(@RequestParam(value = "limit", required = false, defaultValue = "15") Integer limit,
                                          @RequestParam(value = "currentPage", required = false, defaultValue = "1") Integer currentPage,
                                          @RequestParam(value = "cid", required = false) Integer categoryId,
                                          @RequestParam(value = "pid", required = false) String pid,
                                          @RequestParam(value = "onlyMine", required = false, defaultValue = "false") Boolean onlyMine,
                                          @RequestParam(value = "keyword", required = false) String keyword,
                                          @RequestParam(value = "admin", defaultValue = "false") Boolean admin,
                                          HttpServletRequest request) {

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

        if (!StringUtils.isEmpty(pid)) {
            discussionQueryWrapper.eq("pid", pid);
        }


        boolean isAdmin = SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("problem_admin")
                || SecurityUtils.getSubject().hasRole("admin");
        discussionQueryWrapper
                .eq(!(admin && isAdmin), "status", 0)
                .orderByDesc("top_priority")
                .orderByDesc("gmt_modified")
                .orderByDesc("like_num")
                .orderByDesc("view_num");

        if (onlyMine) {
            HttpSession session = request.getSession();
            UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
            discussionQueryWrapper.eq("uid", userRolesVo.getUid());
        }

        IPage<Discussion> discussionList = discussionService.page(iPage, discussionQueryWrapper);

        if (discussionList.getTotal() == 0) { // 未查询到一条数据
            return CommonResult.successResponse(discussionList, "暂无数据");
        } else {
            return CommonResult.successResponse(discussionList, "获取成功");
        }
    }

    @GetMapping("/discussion")
    public CommonResult getDiscussion(@RequestParam(value = "did", required = true) Integer did,
                                      HttpServletRequest request) {

        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        String uid = null;

        if (userRolesVo != null) {
            uid = userRolesVo.getUid();
        }

        DiscussionVo discussion = discussionService.getDiscussion(did, uid);

        if (discussion == null) {
            return CommonResult.errorResponse("对不起，该讨论不存在！", CommonResult.STATUS_NOT_FOUND);
        }

        if (discussion.getStatus() == 1) {
            return CommonResult.errorResponse("对不起，该讨论已被封禁！", CommonResult.STATUS_FORBIDDEN);
        }

        // 浏览量+1
        UpdateWrapper<Discussion> discussionUpdateWrapper = new UpdateWrapper<>();
        discussionUpdateWrapper.setSql("view_num=view_num+1").eq("id", discussion.getId());
        discussionService.update(discussionUpdateWrapper);
        discussion.setViewNum(discussion.getViewNum() + 1);

        return CommonResult.successResponse(discussion, "获取成功");
    }

    @PostMapping("/discussion")
    @RequiresPermissions("discussion_add")
    @RequiresAuthentication
    public CommonResult addDiscussion(@RequestBody Discussion discussion, HttpServletRequest request) {
        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        discussion.setAuthor(userRolesVo.getUsername())
                .setAvatar(userRolesVo.getAvatar())
                .setUid(userRolesVo.getUid());

        if (SecurityUtils.getSubject().hasRole("root")) {
            discussion.setRole("root");
        } else if (SecurityUtils.getSubject().hasRole("admin") || SecurityUtils.getSubject().hasRole("problem_admin")) {
            discussion.setRole("admin");
        } else {
            // 如果不是管理员角色，一律重置为不置顶
            discussion.setTopPriority(false);
        }

        boolean isOk = discussionService.saveOrUpdate(discussion);
        if (isOk) {
            return CommonResult.successResponse(null, "发布成功！");
        } else {
            return CommonResult.errorResponse("发布失败，请重新尝试！");
        }
    }

    @PutMapping("/discussion")
    @RequiresPermissions("discussion_edit")
    @RequiresAuthentication
    public CommonResult updateDiscussion(@RequestBody Discussion discussion) {
        boolean isOk = discussionService.updateById(discussion);
        if (isOk) {
            return CommonResult.successResponse(null, "修改成功");
        } else {
            return CommonResult.errorResponse("修改失败");
        }
    }

    @DeleteMapping("/discussion")
    @RequiresPermissions("discussion_del")
    @RequiresAuthentication
    public CommonResult removeDiscussion(@RequestParam("did") Integer did, HttpServletRequest request) {
        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        UpdateWrapper<Discussion> discussionUpdateWrapper = new UpdateWrapper<Discussion>().eq("id", did);
        // 如果不是是管理员,则需要附加当前用户的uid条件
        if (!SecurityUtils.getSubject().hasRole("root")
                && !SecurityUtils.getSubject().hasRole("admin")
                && !SecurityUtils.getSubject().hasRole("problem_admin")) {
            discussionUpdateWrapper.eq("uid", userRolesVo.getUid());
        }
        boolean isOk = discussionService.remove(discussionUpdateWrapper);
        if (isOk) {
            return CommonResult.successResponse(null, "删除成功");
        } else {
            return CommonResult.errorResponse("删除失败，无权限或者该讨论不存在");
        }

    }

    @GetMapping("/discussion-like")
    @Transactional
    @RequiresAuthentication
    public CommonResult addDiscussionLike(@RequestParam("did") Integer did,
                                          @RequestParam("toLike") Boolean toLike,
                                          HttpServletRequest request) {
        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        QueryWrapper<DiscussionLike> discussionLikeQueryWrapper = new QueryWrapper<>();
        discussionLikeQueryWrapper.eq("did", did).eq("uid", userRolesVo.getUid());

        DiscussionLike discussionLike = discussionLikeService.getOne(discussionLikeQueryWrapper, false);

        if (toLike) { // 添加点赞
            if (discussionLike == null) { // 如果不存在就添加
                boolean isSave = discussionLikeService.saveOrUpdate(new DiscussionLike().setUid(userRolesVo.getUid()).setDid(did));
                if (!isSave) {
                    return CommonResult.errorResponse("点赞失败，请重试尝试！");
                }
            }
            // 点赞+1
            Discussion discussion = discussionService.getById(did);
            if (discussion != null) {
                discussion.setLikeNum(discussion.getLikeNum() + 1);
                discussionService.updateById(discussion);
                // 更新点赞消息
                discussionService.updatePostLikeMsg(discussion.getUid(), userRolesVo.getUid(), did);
            }
            return CommonResult.successResponse(null, "点赞成功");
        } else { // 取消点赞
            if (discussionLike != null) { // 如果存在就删除
                boolean isDelete = discussionLikeService.removeById(discussionLike.getId());
                if (!isDelete) {
                    return CommonResult.errorResponse("取消点赞失败，请重试尝试！");
                }
            }
            // 点赞-1
            UpdateWrapper<Discussion> discussionUpdateWrapper = new UpdateWrapper<>();
            discussionUpdateWrapper.setSql("like_num=like_num-1").eq("id", did);
            discussionService.update(discussionUpdateWrapper);
            return CommonResult.successResponse(null, "取消成功");
        }

    }

    @GetMapping("/discussion-category")
    public CommonResult getDiscussionCategory() {

        List<Category> categoryList = categoryService.list();
        return CommonResult.successResponse(categoryList, "获取成功");
    }

    /**
     * @MethodName addDiscussionReport
     * @Params * @param uid content reporter
     * @Description 添加讨论举报
     * @Return
     * @Since 2021/5/11
     */
    @PostMapping("/discussion-report")
    @RequiresAuthentication
    public CommonResult addDiscussionReport(@RequestBody DiscussionReport discussionReport) {
        boolean isOk = discussionReportService.saveOrUpdate(discussionReport);
        if (isOk) {
            return CommonResult.successResponse(null, "举报成功");
        } else {
            return CommonResult.errorResponse("举报失败，请重新尝试");
        }
    }

}
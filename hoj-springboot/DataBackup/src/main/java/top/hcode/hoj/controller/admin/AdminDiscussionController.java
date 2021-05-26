package top.hcode.hoj.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.Discussion;
import top.hcode.hoj.pojo.entity.DiscussionReport;
import top.hcode.hoj.service.impl.DiscussionReportServiceImpl;
import top.hcode.hoj.service.impl.DiscussionServiceImpl;

import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2021/5/15 20:35
 * @Description:
 */
@RestController
@RequestMapping("/api/admin")
public class AdminDiscussionController {

    @Autowired
    private DiscussionReportServiceImpl discussionReportService;

    @Autowired
    private DiscussionServiceImpl discussionService;

    @PutMapping("/discussion")
    @RequiresRoles(value = {"root", "admin"}, logical = Logical.OR)
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
    @RequiresRoles(value = {"root", "admin"}, logical = Logical.OR)
    @RequiresAuthentication
    public CommonResult removeDiscussion(@RequestBody List<Integer> didList) {
        boolean isOk = discussionService.removeByIds(didList);
        if (isOk) {
            return CommonResult.successResponse(null, "删除成功");
        } else {
            return CommonResult.errorResponse("删除失败");
        }
    }

    @GetMapping("/discussion-report")
    @RequiresRoles(value = {"root", "admin"}, logical = Logical.OR)
    @RequiresAuthentication
    public CommonResult getDiscussionReport(@RequestParam(value = "limit", defaultValue = "10") Integer limit,
                                            @RequestParam(value = "currentPage", defaultValue = "1") Integer currentPage) {
        QueryWrapper<DiscussionReport> discussionReportQueryWrapper = new QueryWrapper<>();

        discussionReportQueryWrapper.orderByAsc("status");
        IPage<DiscussionReport> iPage = new Page<>(currentPage, limit);
        IPage<DiscussionReport> discussionReportList = discussionReportService.page(iPage, discussionReportQueryWrapper);
        return CommonResult.successResponse(discussionReportList, "获取成功");
    }

    @PutMapping("/discussion-report")
    @RequiresRoles(value = {"root", "admin"}, logical = Logical.OR)
    @RequiresAuthentication
    public CommonResult updateDiscussionReport(@RequestBody DiscussionReport discussionReport) {
        boolean isOk = discussionReportService.updateById(discussionReport);
        if (isOk) {
            return CommonResult.successResponse(null, "修改成功");
        } else {
            return CommonResult.errorResponse("修改失败");
        }
    }

}
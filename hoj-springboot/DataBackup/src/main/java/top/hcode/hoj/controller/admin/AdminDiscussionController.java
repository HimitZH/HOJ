package top.hcode.hoj.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;

import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.discussion.Discussion;
import top.hcode.hoj.pojo.entity.discussion.DiscussionReport;

import top.hcode.hoj.pojo.vo.DiscussionReportVO;
import top.hcode.hoj.service.admin.discussion.AdminDiscussionService;

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
    private AdminDiscussionService adminDiscussionService;

    @PutMapping("/discussion")
    @RequiresRoles(value = {"root", "admin","problem_admin"}, logical = Logical.OR)
    @RequiresAuthentication
    public CommonResult<Void> updateDiscussion(@RequestBody Discussion discussion) {
        return adminDiscussionService.updateDiscussion(discussion);
    }

    @DeleteMapping("/discussion")
    @RequiresRoles(value = {"root", "admin","problem_admin"}, logical = Logical.OR)
    @RequiresAuthentication
    public CommonResult<Void> removeDiscussion(@RequestBody List<Integer> didList) {
        return adminDiscussionService.removeDiscussion(didList);
    }

    @GetMapping("/discussion-report")
    @RequiresRoles(value = {"root", "admin","problem_admin"}, logical = Logical.OR)
    @RequiresAuthentication
    public CommonResult<IPage<DiscussionReportVO>> getDiscussionReport(@RequestParam(value = "limit", defaultValue = "10") Integer limit,
                                                                       @RequestParam(value = "currentPage", defaultValue = "1") Integer currentPage) {
        return adminDiscussionService.getDiscussionReport(limit, currentPage);
    }

    @PutMapping("/discussion-report")
    @RequiresRoles(value = {"root", "admin","problem_admin"}, logical = Logical.OR)
    @RequiresAuthentication
    public CommonResult<Void> updateDiscussionReport(@RequestBody DiscussionReport discussionReport) {
        return adminDiscussionService.updateDiscussionReport(discussionReport);
    }

}
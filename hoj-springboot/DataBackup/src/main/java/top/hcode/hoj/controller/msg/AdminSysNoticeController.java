package top.hcode.hoj.controller.msg;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.msg.AdminSysNotice;
import top.hcode.hoj.service.msg.impl.AdminSysNoticeServiceImpl;

import javax.annotation.Resource;

/**
 * @Author: Himit_ZH
 * @Date: 2021/10/1 20:38
 * @Description: 负责管理员发送系统通知
 */
@RestController
@RequestMapping("/api/admin/msg")
public class AdminSysNoticeController {

    @Resource
    private AdminSysNoticeServiceImpl adminSysNoticeService;

    @GetMapping("/notice")
    @RequiresAuthentication
    @RequiresRoles("root")
    public CommonResult getSysNotice(@RequestParam(value = "limit", required = false) Integer limit,
                                     @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                     @RequestParam(value = "type", required = false) String type) {

        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 5;

        return CommonResult.successResponse(adminSysNoticeService.getSysNotice(limit, currentPage, type));
    }

    @PostMapping("/notice")
    @RequiresAuthentication
    @RequiresRoles("root")
    public CommonResult addSysNotice(@RequestBody AdminSysNotice adminSysNotice) {

        boolean isOk = adminSysNoticeService.saveOrUpdate(adminSysNotice);
        if (isOk) {
            return CommonResult.successResponse("发布成功");
        } else {
            return CommonResult.errorResponse("发布失败");
        }
    }


    @DeleteMapping("/notice")
    @RequiresAuthentication
    @RequiresRoles("root")
    public CommonResult deleteSysNotice(@RequestParam("id") Long id) {
        boolean isOk = adminSysNoticeService.removeById(id);
        if (isOk) {
            return CommonResult.successResponse("删除成功");
        } else {
            return CommonResult.errorResponse("删除失败");
        }
    }


    @PutMapping("/notice")
    @RequiresAuthentication
    @RequiresRoles("root")
    public CommonResult updateSysNotice(@RequestBody AdminSysNotice adminSysNotice) {
        boolean isOk = adminSysNoticeService.saveOrUpdate(adminSysNotice);
        if (isOk) {
            return CommonResult.successResponse("更新成功");
        } else {
            return CommonResult.errorResponse("更新失败");
        }
    }
}
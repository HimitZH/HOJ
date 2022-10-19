package top.hcode.hoj.controller.msg;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.msg.AdminSysNotice;
import top.hcode.hoj.pojo.vo.AdminSysNoticeVO;
import top.hcode.hoj.service.msg.AdminNoticeService;

import javax.annotation.Resource;

/**
 * @Author: Himit_ZH
 * @Date: 2021/10/1 20:38
 * @Description: 负责管理员发送系统通知
 */
@RestController
@RequestMapping("/api/admin/msg")
public class AdminNoticeController {

    @Resource
    private AdminNoticeService adminNoticeService;

    @GetMapping("/notice")
    @RequiresAuthentication
    @RequiresRoles("root")
    public CommonResult<IPage<AdminSysNoticeVO>> getSysNotice(@RequestParam(value = "limit", required = false) Integer limit,
                                                              @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                                              @RequestParam(value = "type", required = false) String type) {

        return adminNoticeService.getSysNotice(limit, currentPage, type);
    }

    @PostMapping("/notice")
    @RequiresAuthentication
    @RequiresRoles("root")
    public CommonResult<Void> addSysNotice(@RequestBody AdminSysNotice adminSysNotice) {

        return adminNoticeService.addSysNotice(adminSysNotice);
    }


    @DeleteMapping("/notice")
    @RequiresAuthentication
    @RequiresRoles("root")
    public CommonResult<Void> deleteSysNotice(@RequestParam("id") Long id) {

        return adminNoticeService.deleteSysNotice(id);
    }


    @PutMapping("/notice")
    @RequiresAuthentication
    @RequiresRoles("root")
    public CommonResult<Void> updateSysNotice(@RequestBody AdminSysNotice adminSysNotice) {

        return adminNoticeService.updateSysNotice(adminSysNotice);
    }
}
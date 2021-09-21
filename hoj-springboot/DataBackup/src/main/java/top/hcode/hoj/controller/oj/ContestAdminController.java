package top.hcode.hoj.controller.oj;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.CheckACDto;
import top.hcode.hoj.pojo.entity.Contest;
import top.hcode.hoj.pojo.entity.ContestPrint;
import top.hcode.hoj.pojo.entity.ContestRecord;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.service.ContestRecordService;
import top.hcode.hoj.service.impl.ContestPrintServiceImpl;
import top.hcode.hoj.service.impl.ContestServiceImpl;
import top.hcode.hoj.utils.Constants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @Author: Himit_ZH
 * @Date: 2021/9/20 13:15
 * @Description: 处理比赛管理模块的相关数据请求
 */
@RestController
@RequestMapping("/api")
public class ContestAdminController {


    @Autowired
    private ContestServiceImpl contestService;

    @Autowired
    private ContestRecordService contestRecordService;

    @Autowired
    private ContestPrintServiceImpl contestPrintService;

    /**
     * @MethodName getContestACInfo
     * @Params * @param null
     * @Description 获取各个用户的ac情况，仅限于比赛管理者可查看
     * @Return
     * @Since 2021/1/17
     */
    @GetMapping("/get-contest-ac-info")
    @RequiresAuthentication
    public CommonResult getContestACInfo(@RequestParam("cid") Long cid,
                                         @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                         @RequestParam(value = "limit", required = false) Integer limit,
                                         HttpServletRequest request) {
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        // 获取本场比赛的状态
        Contest contest = contestService.getById(cid);

        // 超级管理员或者该比赛的创建者，则为比赛管理者
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        if (!isRoot && !contest.getUid().equals(userRolesVo.getUid())) {
            return CommonResult.errorResponse("对不起，你无权查看！", CommonResult.STATUS_FORBIDDEN);
        }

        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 30;

        // 获取当前比赛的，状态为ac，未被校验的排在签名
        IPage<ContestRecord> contestRecords = contestRecordService.getACInfo(currentPage,
                limit, Constants.Contest.RECORD_AC.getCode(), cid, contest.getUid());

        return CommonResult.successResponse(contestRecords, "查询成功");
    }


    /**
     * @MethodName checkContestACInfo
     * @Params * @param null
     * @Description 比赛管理员确定该次提交的ac情况
     * @Return
     * @Since 2021/1/17
     */
    @PutMapping("/check-contest-ac-info")
    @RequiresAuthentication
    public CommonResult checkContestACInfo(@RequestBody CheckACDto checkACDto,
                                           HttpServletRequest request) {

        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        // 获取本场比赛的状态
        Contest contest = contestService.getById(checkACDto.getCid());

        // 超级管理员或者该比赛的创建者，则为比赛管理者
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        if (!isRoot && !contest.getUid().equals(userRolesVo.getUid())) {
            return CommonResult.errorResponse("对不起，你无权操作！", CommonResult.STATUS_FORBIDDEN);
        }

        boolean result = contestRecordService.updateById(
                new ContestRecord().setChecked(checkACDto.getChecked()).setId(checkACDto.getId()));

        if (result) {
            return CommonResult.successResponse(null, "修改校验确定成功！");
        } else {
            return CommonResult.errorResponse("修改校验确定失败！");
        }

    }


    @GetMapping("/get-contest-print")
    @RequiresAuthentication
    public CommonResult getContestPrint(@RequestParam("cid") Long cid,
                                        @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                        @RequestParam(value = "limit", required = false) Integer limit,
                                        HttpServletRequest request) {
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        // 获取本场比赛的状态
        Contest contest = contestService.getById(cid);

        // 超级管理员或者该比赛的创建者，则为比赛管理者
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        if (!isRoot && !contest.getUid().equals(userRolesVo.getUid())) {
            return CommonResult.errorResponse("对不起，你无权查看！", CommonResult.STATUS_FORBIDDEN);
        }

        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 30;

        // 获取当前比赛的，未被确定的排在签名

        IPage<ContestPrint> contestPrintIPage = new Page<>(currentPage,limit);

        QueryWrapper<ContestPrint> contestPrintQueryWrapper = new QueryWrapper<>();
        contestPrintQueryWrapper.select("id","cid","username","realname","status","gmt_create")
                .eq("cid", cid)
                .orderByAsc("status")
                .orderByDesc("gmt_create");

        IPage<ContestPrint> contestPrintList = contestPrintService.page(contestPrintIPage, contestPrintQueryWrapper);

        return CommonResult.successResponse(contestPrintList, "查询成功");
    }

    /**
     * @param id
     * @param cid
     * @param request
     * @MethodName checkContestStatus
     * @Description 更新该打印为确定状态
     * @Return
     * @Since 2021/9/20
     */
    @PutMapping("/check-contest-print-status")
    @RequiresAuthentication
    public CommonResult checkContestStatus(@RequestParam("id") Long id,
                                           @RequestParam("cid") Long cid,
                                           HttpServletRequest request) {

        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        // 获取本场比赛的状态
        Contest contest = contestService.getById(cid);

        // 超级管理员或者该比赛的创建者，则为比赛管理者
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        if (!isRoot && !contest.getUid().equals(userRolesVo.getUid())) {
            return CommonResult.errorResponse("对不起，你无权操作！", CommonResult.STATUS_FORBIDDEN);
        }

        boolean result = contestPrintService.updateById(new ContestPrint().setId(id).setStatus(1));

        if (result) {
            return CommonResult.successResponse(null, "确定成功！");
        } else {
            return CommonResult.errorResponse("确定失败！");
        }
    }

}
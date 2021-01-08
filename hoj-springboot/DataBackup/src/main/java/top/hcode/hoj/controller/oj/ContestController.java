package top.hcode.hoj.controller.oj;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.dao.AnnouncementMapper;
import top.hcode.hoj.dao.ContestRegisterMapper;
import top.hcode.hoj.pojo.entity.ContestAnnouncement;
import top.hcode.hoj.pojo.entity.ContestRegister;
import top.hcode.hoj.pojo.vo.AnnouncementVo;
import top.hcode.hoj.pojo.vo.ContestRecordVo;
import top.hcode.hoj.pojo.vo.ContestVo;
import top.hcode.hoj.dao.ContestProblemMapper;
import top.hcode.hoj.pojo.entity.ContestProblem;
import top.hcode.hoj.service.AnnouncementService;
import top.hcode.hoj.service.ContestRecordService;
import top.hcode.hoj.service.ContestService;
import top.hcode.hoj.service.impl.AnnouncementServiceImpl;
import top.hcode.hoj.service.impl.ContestProblemServiceImpl;
import top.hcode.hoj.service.impl.ContestRegisterServiceImpl;
import top.hcode.hoj.service.impl.ContestServiceImpl;

import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2020/10/27 21:40
 * @Description: 处理比赛模块的相关数据请求
 */
@RestController
@RequestMapping("/api")
public class ContestController {

    @Autowired
    private ContestServiceImpl contestService;

    @Autowired
    private ContestRecordService contestRecordService;

    @Autowired
    private ContestProblemServiceImpl contestProblemService;

    @Autowired
    private AnnouncementServiceImpl announcementService;

    @Autowired
    private ContestRegisterServiceImpl contestRegisterService;

    /**
     * @MethodName getContestList
     * @Params  * @param null
     * @Description 获取比赛列表分页数据
     * @Return CommonResult
     * @Since 2020/10/27
     */
    @GetMapping("/get-contest-list")
    public CommonResult getContestList(@RequestParam(value = "limit", required = false) Integer limit,
                                       @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                       @RequestParam(value = "status",required = false)Integer status,
                                       @RequestParam(value = "type",required = false)Integer type,
                                       @RequestParam(value = "keyword",required = false)String keyword){
        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) currentPage = 1;

        if (limit == null || limit < 1) limit = 10;

        Page<ContestVo> contestList = contestService.getContestList(limit, currentPage,type,status,keyword);

        if (contestList.getTotal() == 0) {
            return CommonResult.successResponse(contestList,"暂无数据");
        } else {
            return CommonResult.successResponse(contestList, "获取成功");
        }
    }

    /**
     * @MethodName getContestInfo
     * @Params  * @param null
     * @Description 获得指定比赛的详细信息
     * @Return
     * @Since 2020/10/28
     */
    @GetMapping("/get-contest-info")
    public CommonResult getContestInfo(@RequestParam(value = "cid",required = true) Long cid){
        ContestVo contestInfo = contestService.getContestInfoById(cid);
        if (contestInfo == null) {
            return CommonResult.errorResponse("暂无数据", CommonResult.STATUS_NOT_FOUND);
        } else {
            return CommonResult.successResponse(contestInfo, "获取成功");
        }
    }


    /**
     * @MethodName getContestProblem
     * @Params  * @param null
     * @Description 获得指定比赛的题目列表
     * @Return
     * @Since 2020/10/28
     */
    @GetMapping("/get-contest-problem")
    public CommonResult getContestProblem(@RequestParam(value = "cid",required = true) Long cid){
        QueryWrapper<ContestProblem> wrapper = new QueryWrapper<ContestProblem>().eq("cid", cid)
                                                                                 .orderByAsc("id");

        List<ContestProblem> contestProblemList = contestProblemService.list(wrapper);
        if (contestProblemList.size()==0) {
            return CommonResult.errorResponse("暂无数据", CommonResult.STATUS_NOT_FOUND);
        } else {
            return CommonResult.successResponse(contestProblemList, "获取成功");
        }
    }


    /**
     * @MethodName getContestRank
     * @Params  * @param null
     * @Description 获得比赛做题记录以用来排名
     * @Return
     * @Since 2020/10/28
     */
    @GetMapping("/get-contest-rank")
    public CommonResult getContestRank(@RequestParam(value = "cid",required = true) Long cid){

        QueryWrapper<ContestProblem> wrapper = new QueryWrapper<ContestProblem>().eq("cid", cid).groupBy();

        List<ContestRecordVo> contestRecordList = contestRecordService.getContestRecord(cid);
        if (contestRecordList.size() == 0) {
            return CommonResult.errorResponse("暂无数据", CommonResult.STATUS_NOT_FOUND);
        } else {

            return CommonResult.successResponse(contestRecordList, "获取成功");
        }
    }


    /**
     * @MethodName getContestAnnouncement
     * @Params  * @param null
     * @Description 获得比赛的通知列表
     * @Return CommonResult
     * @Since 2020/10/28
     */
    @GetMapping("/get-contest-announcement")
    public CommonResult getContestAnnouncement(@RequestParam(value = "cid",required = true)Long cid,
                                               @RequestParam(value = "limit", required = false) Integer limit,
                                               @RequestParam(value = "currentPage", required = false) Integer currentPage){
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;

        IPage<AnnouncementVo> contestAnnouncementList = announcementService.getContestAnnouncement(cid,true,limit,currentPage);
        if (contestAnnouncementList.getTotal() == 0) {
            return CommonResult.errorResponse("暂无数据", CommonResult.STATUS_NOT_FOUND);
        } else {
            return CommonResult.successResponse(contestAnnouncementList, "获取成功");
        }

    }

    /**
     * @MethodName toRegisterContest
     * @Params  * @param null
     * @Description 注册比赛
     * @Return
     * @Since 2020/10/28
     */
    @PostMapping("/to-register-contest")
    @RequiresAuthentication
    public CommonResult toRegisterContest(@RequestBody long cid,
                                          @RequestBody String uid){
        QueryWrapper<ContestRegister> wrapper = new QueryWrapper<ContestRegister>().eq("cid", cid)
                                                                                    .eq("uid",uid);
        if (contestRegisterService.getOne(wrapper)!=null){
            return CommonResult.errorResponse("您已注册过该比赛，请勿重复注册！", CommonResult.STATUS_ACCESS_DENIED);
        }

        boolean result = contestRegisterService.saveOrUpdate(new ContestRegister().setCid(cid).setUid(uid));

        if (!result) {
            return CommonResult.errorResponse("注册比赛失败，请稍后再试", CommonResult.STATUS_FAIL);
        } else {
            return CommonResult.successResponse(null, "注册比赛成功");
        }
    }
}
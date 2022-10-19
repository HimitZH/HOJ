package top.hcode.hoj.controller.admin;


import com.baomidou.mybatisplus.core.metadata.IPage;

import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.AnnouncementDTO;
import top.hcode.hoj.pojo.dto.ContestProblemDTO;
import top.hcode.hoj.pojo.dto.ProblemDTO;
import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.pojo.entity.contest.ContestProblem;

import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.vo.AdminContestVO;
import top.hcode.hoj.pojo.vo.AnnouncementVO;

import top.hcode.hoj.service.admin.contest.AdminContestAnnouncementService;
import top.hcode.hoj.service.admin.contest.AdminContestProblemService;
import top.hcode.hoj.service.admin.contest.AdminContestService;

import javax.servlet.http.HttpServletRequest;
import java.util.*;


/**
 * @Author: Himit_ZH
 * @Date: 2020/12/19 22:28
 * @Description:
 */
@RestController
@RequestMapping("/api/admin/contest")
public class AdminContestController {


    @Autowired
    private AdminContestService adminContestService;

    @Autowired
    private AdminContestProblemService adminContestProblemService;

    @Autowired
    private AdminContestAnnouncementService adminContestAnnouncementService;

    @GetMapping("/get-contest-list")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<IPage<Contest>> getContestList(@RequestParam(value = "limit", required = false) Integer limit,
                                                       @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                                       @RequestParam(value = "keyword", required = false) String keyword) {

        return adminContestService.getContestList(limit, currentPage, keyword);
    }

    @GetMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<AdminContestVO> getContest(@RequestParam("cid") Long cid) {

        return adminContestService.getContest(cid);
    }

    @DeleteMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = "root") // 只有超级管理员能删除比赛
    public CommonResult<Void> deleteContest(@RequestParam("cid") Long cid) {

        return adminContestService.deleteContest(cid);
    }

    @PostMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> addContest(@RequestBody AdminContestVO adminContestVo) {

        return adminContestService.addContest(adminContestVo);
    }

    @GetMapping("/clone")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> cloneContest(@RequestParam("cid") Long cid) {
        return adminContestService.cloneContest(cid);
    }

    @PutMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> updateContest(@RequestBody AdminContestVO adminContestVo) {

        return adminContestService.updateContest(adminContestVo);
    }

    @PutMapping("/change-contest-visible")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> changeContestVisible(@RequestParam(value = "cid", required = true) Long cid,
                                                   @RequestParam(value = "uid", required = true) String uid,
                                                   @RequestParam(value = "visible", required = true) Boolean visible) {

        return adminContestService.changeContestVisible(cid, uid, visible);
    }

    /**
     * 以下为比赛的题目的增删改查操作接口
     */

    @GetMapping("/get-problem-list")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<HashMap<String, Object>> getProblemList(@RequestParam(value = "limit", required = false) Integer limit,
                                                                @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                                                @RequestParam(value = "keyword", required = false) String keyword,
                                                                @RequestParam(value = "cid", required = true) Long cid,
                                                                @RequestParam(value = "problemType", required = false) Integer problemType,
                                                                @RequestParam(value = "oj", required = false) String oj) {

        return adminContestProblemService.getProblemList(limit, currentPage, keyword, cid, problemType, oj);
    }

    @GetMapping("/problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Problem> getProblem(@RequestParam("pid") Long pid, HttpServletRequest request) {
        return adminContestProblemService.getProblem(pid);
    }

    @DeleteMapping("/problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "problem_admin"}, logical = Logical.OR)
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> deleteProblem(@RequestParam("pid") Long pid,
                                            @RequestParam(value = "cid", required = false) Long cid) {
        return adminContestProblemService.deleteProblem(pid, cid);
    }

    @PostMapping("/problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Map<Object, Object>> addProblem(@RequestBody ProblemDTO problemDto) {

        return adminContestProblemService.addProblem(problemDto);
    }

    @PutMapping("/problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> updateProblem(@RequestBody ProblemDTO problemDto) {

        return adminContestProblemService.updateProblem(problemDto);
    }

    @GetMapping("/contest-problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<ContestProblem> getContestProblem(@RequestParam(value = "cid", required = true) Long cid,
                                                          @RequestParam(value = "pid", required = true) Long pid) {

        return adminContestProblemService.getContestProblem(cid, pid);
    }

    @PutMapping("/contest-problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<ContestProblem> setContestProblem(@RequestBody ContestProblem contestProblem) {

        return adminContestProblemService.setContestProblem(contestProblem);
    }

    @PostMapping("/add-problem-from-public")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> addProblemFromPublic(@RequestBody ContestProblemDTO contestProblemDto) {

        return adminContestProblemService.addProblemFromPublic(contestProblemDto);
    }

    @GetMapping("/import-remote-oj-problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> importContestRemoteOJProblem(@RequestParam("name") String name,
                                                           @RequestParam("problemId") String problemId,
                                                           @RequestParam("cid") Long cid,
                                                           @RequestParam("displayId") String displayId) {

        return adminContestProblemService.importContestRemoteOJProblem(name, problemId, cid, displayId);
    }

    /**
     * 以下处理比赛公告的操作请求
     */

    @GetMapping("/announcement")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<IPage<AnnouncementVO>> getAnnouncementList(@RequestParam(value = "limit", required = false) Integer limit,
                                                                   @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                                                   @RequestParam(value = "cid", required = true) Long cid) {

        return adminContestAnnouncementService.getAnnouncementList(limit, currentPage, cid);
    }

    @DeleteMapping("/announcement")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> deleteAnnouncement(@RequestParam("aid") Long aid) {

        return adminContestAnnouncementService.deleteAnnouncement(aid);
    }

    @PostMapping("/announcement")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> addAnnouncement(@RequestBody AnnouncementDTO announcementDto) {

        return adminContestAnnouncementService.addAnnouncement(announcementDto);
    }

    @PutMapping("/announcement")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> updateAnnouncement(@RequestBody AnnouncementDTO announcementDto) {

        return adminContestAnnouncementService.updateAnnouncement(announcementDto);
    }
}
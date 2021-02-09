package top.hcode.hoj.controller.admin;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.AnnouncementDto;
import top.hcode.hoj.pojo.dto.ProblemDto;
import top.hcode.hoj.pojo.entity.*;
import top.hcode.hoj.pojo.vo.AnnouncementVo;
import top.hcode.hoj.service.impl.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


/**
 * @Author: Himit_ZH
 * @Date: 2020/12/19 22:28
 * @Description:
 */
@RestController
@RequestMapping("/admin/contest")
public class AdminContestController {

    @Autowired
    private ContestServiceImpl contestService;

    @Autowired
    private ProblemServiceImpl problemService;

    @Autowired
    private ContestProblemServiceImpl contestProblemService;

    @Autowired
    private ContestAnnouncementServiceImpl contestAnnouncementService;

    @Autowired
    private AnnouncementServiceImpl announcementService;

    @GetMapping("/get-contest-list")
    @RequiresAuthentication
    @RequiresRoles(value = {"root","admin"},logical = Logical.OR)
    public CommonResult getContestList(@RequestParam(value = "limit", required = false) Integer limit,
                                       @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                       @RequestParam(value = "keyword", required = false) String keyword){

        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;
        IPage<Contest> iPage = new Page<>(currentPage,limit);
        QueryWrapper<Contest> queryWrapper = new QueryWrapper<>();

        if (!StringUtils.isEmpty(keyword)) {
            queryWrapper
                    .like("title", keyword).or()
                    .like("id", keyword);
        }

        queryWrapper.orderByDesc("start_time");

        IPage<Contest> contestList = contestService.page(iPage, queryWrapper);
        if (contestList.getTotal() == 0) { // 未查询到一条数据
            return CommonResult.successResponse(contestList,"暂无数据");
        } else {
            return CommonResult.successResponse(contestList, "获取成功");
        }
    }

    @GetMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root","admin"},logical = Logical.OR)
    public CommonResult getContest(@Valid @RequestParam("cid")Long cid){
        Contest contest = contestService.getById(cid);
        if (contest !=null) { // 查询成功
            return CommonResult.successResponse(contest,"查询成功！");
        } else {
            return CommonResult.errorResponse("查询失败！",CommonResult.STATUS_FAIL);
        }
    }

    @DeleteMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root","admin"},logical = Logical.OR)
    public CommonResult deleteContest(@Valid @RequestParam("cid")Long cid){
        boolean result = contestService.removeById(cid);
        /*
        contest的id为其他表的外键的表中的对应数据都会被一起删除！
         */
        if (result) { // 删除成功
            return CommonResult.successResponse(null,"删除成功！");
        } else {
            return CommonResult.errorResponse("删除失败！",CommonResult.STATUS_FAIL);
        }
    }

    @PostMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root","admin"},logical = Logical.OR)
    public CommonResult addContest(@RequestBody Contest contest){
        boolean result = contestService.save(contest);
        if (result) { // 添加成功
            return CommonResult.successResponse(null,"添加成功！");
        } else {
            return CommonResult.errorResponse("添加失败",CommonResult.STATUS_FAIL);
        }
    }

    @PutMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root","admin"},logical = Logical.OR)
    public CommonResult updateContest(@RequestBody Contest contest){
        boolean result = contestService.saveOrUpdate(contest);
        if (result) { // 添加成功
            return CommonResult.successResponse(null,"修改成功！");
        } else {
            return CommonResult.errorResponse("修改失败",CommonResult.STATUS_FAIL);
        }
    }

    /**
    * 以下为比赛的题目的增删改查操作接口
     */

    @GetMapping("/get-problem-list")
    @RequiresAuthentication
    @RequiresRoles(value = {"root","admin"},logical = Logical.OR)
    @Transactional
    public CommonResult getProblemList(@RequestParam(value = "limit", required = false) Integer limit,
                                       @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                       @RequestParam(value = "keyword", required = false) String keyword,
                                       @RequestParam(value = "cid", required = false) Long cid,
                                       @RequestParam(value = "problem_type",required = false) Integer problem_type){
        if(cid == null){
            return CommonResult.errorResponse("参数错误！", CommonResult.STATUS_NOT_FOUND);
        }

        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;
        IPage<Problem> iPage = new Page<>(currentPage,limit);
        IPage<Problem> problemList = null;

        // 根据cid在ContestProblem表中查询到对应pid集合
        QueryWrapper<ContestProblem> contestProblemQueryWrapper = new QueryWrapper<>();
        contestProblemQueryWrapper.select("pid").eq("cid", cid);
        List<Long> pidList = new LinkedList<>();
        contestProblemService.list(contestProblemQueryWrapper).forEach(contestProblem -> {pidList.add(contestProblem.getPid());});

        if(pidList.size() == 0&&problem_type==null){ // 该比赛原本就无题目数据
            return CommonResult.successResponse(null, "获取成功");
        }

        QueryWrapper<Problem> problemQueryWrapper = new QueryWrapper<>();

        if(problem_type!=null){ // 必备条件 私有的不可取来做比赛题目
            problemQueryWrapper.eq("type", problem_type).ne("auth", 2);
        }

        // 逻辑判断，如果是查询已有的就应该是in，如果是查询不要重复的，使用not in
        if(pidList.size()>0) {
            if (problem_type != null) {
                // 同时需要与比赛相同类型的题目，权限需要是公开的（私有，比赛中不可加入！）
                problemQueryWrapper.notIn("id", pidList);
            } else {
                problemQueryWrapper.in("id", pidList);
            }
        }

        if (!StringUtils.isEmpty(keyword)) {
            problemQueryWrapper
                    .like("title", keyword).or()
                    .like("author", keyword);
        }

        problemList = problemService.page(iPage, problemQueryWrapper);
        if (problemList.getTotal() == 0) { // 未查询到一条数据
            return CommonResult.successResponse(problemList,"暂无数据" );
        } else {
            return CommonResult.successResponse(problemList, "获取成功");
        }
    }

    @GetMapping("/problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root","admin"},logical = Logical.OR)
    public CommonResult getProblem(@Valid @RequestParam("id")Long pid){
        Problem problem = problemService.getById(pid);
        if (problem !=null) { // 查询成功
            return CommonResult.successResponse(problem,"查询成功！");
        } else {
            return CommonResult.errorResponse("查询失败！",CommonResult.STATUS_FAIL);
        }
    }

    @DeleteMapping("/problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root","admin"},logical = Logical.OR)
    public CommonResult deleteProblem(@Valid @RequestParam("pid")Long pid){
        boolean result = problemService.removeById(pid);
        /*
        problem的id为其他表的外键的表中的对应数据都会被一起删除！
         */
        if (result) { // 删除成功
            return CommonResult.successResponse(null,"删除成功！");
        } else {
            return CommonResult.errorResponse("删除失败！",CommonResult.STATUS_FAIL);
        }
    }

    @PostMapping("/problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root","admin"},logical = Logical.OR)
    @Transactional
    public CommonResult addProblem(@RequestBody ProblemDto problemDto){
        boolean result = problemService.adminAddProblem(problemDto);
        if (result) { // 添加成功
            // 顺便返回新的题目id，好下一步添加外键操作
            return CommonResult.successResponse(MapUtil.builder().put("pid", problemDto.getProblem().getId()).map(),"添加成功！");
        } else {
            return CommonResult.errorResponse("添加失败",CommonResult.STATUS_FAIL);
        }

    }

    @PutMapping("/problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root","admin"},logical = Logical.OR)
    @Transactional
    public CommonResult updateProblem(@RequestBody ProblemDto problemDto){
        boolean result = problemService.adminUpdateProblem(problemDto);
        if (result) { // 更新成功
            return CommonResult.successResponse(null,"修改成功！");
        } else {
            return CommonResult.errorResponse("修改失败",CommonResult.STATUS_FAIL);
        }
    }

    @GetMapping("/contest-problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root","admin"},logical = Logical.OR)
    public CommonResult getContestProblem(@RequestParam(value = "cid", required = true) Long cid,
                                          @RequestParam(value = "pid", required = true) Long pid){
        QueryWrapper<ContestProblem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cid", cid).eq("pid", cid);
        ContestProblem contestProblem = contestProblemService.getOne(queryWrapper);
        if (contestProblem!=null){
            return CommonResult.successResponse(contestProblem,"查询成功！");
        }else{
            return CommonResult.errorResponse("查询失败",CommonResult.STATUS_FAIL);
        }
    }

    @PutMapping("/contest-problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root","admin"},logical = Logical.OR)
    public CommonResult setContestProblem(@RequestBody ContestProblem contestProblem){
        boolean result = contestProblemService.saveOrUpdate(contestProblem);
        if (result){
            return CommonResult.successResponse(contestProblem,"更新成功！");
        }else{
            return CommonResult.errorResponse("更新失败",CommonResult.STATUS_FAIL);
        }
    }

    @PutMapping("/change-problem-auth")
    @RequiresAuthentication
    @RequiresRoles(value = {"root","admin"},logical = Logical.OR)
    public CommonResult changeProblemAuth(@RequestBody Problem problem){
        boolean result = problemService.saveOrUpdate(problem);
        if (result) { // 更新成功
            return CommonResult.successResponse(null,"修改成功！");
        } else {
            return CommonResult.errorResponse("修改失败",CommonResult.STATUS_FAIL);
        }
    }

    @PostMapping("/add-problem-from-public")
    @RequiresAuthentication
    @RequiresRoles(value = {"root","admin"},logical = Logical.OR)
    public CommonResult addProblemFromPublic(@RequestBody HashMap<String,String> params){

        String pidStr = params.get("pid");
        String cidStr = params.get("cid");
        String displayId = params.get("displayId");
        if (StringUtils.isEmpty(pidStr) || StringUtils.isEmpty(cidStr)||StringUtils.isEmpty(displayId)){
            return CommonResult.errorResponse("参数错误！",CommonResult.STATUS_FAIL);
        }

        Long pid = Long.valueOf(pidStr);
        Long cid = Long.valueOf(cidStr);

        // 比赛中题目显示默认为原标题
        String displayName = problemService.getById(pid).getTitle();

        boolean result = contestProblemService.saveOrUpdate(new ContestProblem()
                .setCid(cid).setPid(pid).setDisplayTitle(displayName).setDisplayId(displayId));
        if (result) { // 添加成功
            return CommonResult.successResponse(null,"添加成功！");
        } else {
            return CommonResult.errorResponse("添加失败",CommonResult.STATUS_FAIL);
        }
    }


    /**
     *  以下处理比赛公告的操作请求
     */

    @GetMapping("/announcement")
    @RequiresAuthentication
    @RequiresRoles(value = {"root","admin"},logical = Logical.OR)
    public CommonResult getAnnouncementList(@RequestParam(value = "limit", required = false) Integer limit,
                                            @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                            @RequestParam(value = "cid", required = true) Long cid){
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;

        IPage<AnnouncementVo> announcementList = announcementService.getContestAnnouncement(cid,false,limit,currentPage);
        if (announcementList.getTotal() == 0) { // 未查询到一条数据
            return CommonResult.successResponse(announcementList,"暂无数据");
        } else {
            return CommonResult.successResponse(announcementList, "获取成功");
        }
    }

    @DeleteMapping("/announcement")
    @RequiresAuthentication
    @RequiresRoles(value = {"root","admin"},logical = Logical.OR)
    public CommonResult deleteAnnouncement(@Valid @RequestParam("aid")Long aid){
        boolean result = announcementService.removeById(aid);
        if (result) { // 删除成功
            return CommonResult.successResponse(null,"删除成功！");
        } else {
            return CommonResult.errorResponse("删除失败！",CommonResult.STATUS_FAIL);
        }
    }

    @PostMapping("/announcement")
    @RequiresAuthentication
    @RequiresRoles(value = {"root","admin"},logical = Logical.OR)
    @Transactional
    public CommonResult addAnnouncement(@RequestBody AnnouncementDto announcementDto){
        boolean result1 = announcementService.save(announcementDto.getAnnouncement());
        boolean result2 = contestAnnouncementService.saveOrUpdate(new ContestAnnouncement().setAid(announcementDto.getAnnouncement().getId())
                .setCid(announcementDto.getCid()));
        if (result1&&result2) { // 添加成功
            return CommonResult.successResponse(null,"添加成功！");
        } else {
            return CommonResult.errorResponse("添加失败",CommonResult.STATUS_FAIL);
        }
    }

    @PutMapping("/announcement")
    @RequiresAuthentication
    @RequiresRoles(value = {"root","admin"},logical = Logical.OR)
    public CommonResult updateAnnouncement(@RequestBody AnnouncementDto announcementDto){
        boolean result = announcementService.saveOrUpdate(announcementDto.getAnnouncement());
        if (result) { // 更新成功
            return CommonResult.successResponse(null,"修改成功！");
        } else {
            return CommonResult.errorResponse("修改失败",CommonResult.STATUS_FAIL);
        }
    }
}
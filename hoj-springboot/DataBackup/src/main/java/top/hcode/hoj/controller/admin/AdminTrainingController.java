package top.hcode.hoj.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.TrainingDto;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.training.Training;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.service.problem.impl.ProblemServiceImpl;
import top.hcode.hoj.service.training.impl.TrainingProblemServiceImpl;
import top.hcode.hoj.service.training.impl.TrainingServiceImpl;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;

/**
 * @Author: Himit_ZH
 * @Date: 2021/11/22 20:57
 * @Description:
 */
@RestController
@RequestMapping("/api/admin/training")
public class AdminTrainingController {

    @Resource
    private TrainingServiceImpl trainingService;

    @Resource
    private TrainingProblemServiceImpl trainingProblemService;

    @Resource
    private ProblemServiceImpl problemService;

    @GetMapping("/list")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult getTrainingList(@RequestParam(value = "limit", required = false) Integer limit,
                                        @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                        @RequestParam(value = "keyword", required = false) String keyword) {

        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;
        IPage<Training> iPage = new Page<>(currentPage, limit);
        QueryWrapper<Training> queryWrapper = new QueryWrapper<>();

        if (!StringUtils.isEmpty(keyword)) {
            keyword = keyword.trim();
            queryWrapper
                    .like("title", keyword).or()
                    .like("id", keyword);
        }

        queryWrapper.orderByAsc("rank");

        IPage<Training> TrainingPager = trainingService.page(iPage, queryWrapper);
        if (TrainingPager.getTotal() == 0) { // 未查询到一条数据
            return CommonResult.successResponse(TrainingPager, "暂无数据");
        } else {
            return CommonResult.successResponse(TrainingPager, "获取成功");
        }
    }

    @GetMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult getTraining(@RequestParam("tid") Long tid, HttpServletRequest request) {

        // 获取本场训练的信息
        Training training = trainingService.getById(tid);
        if (training == null) { // 查询不存在
            return CommonResult.errorResponse("查询失败：该训练不存在,请检查参数tid是否准确！");
        }

        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        // 是否为超级管理员
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");
        // 只有超级管理员和训练拥有者才能操作
        if (!isRoot && !userRolesVo.getUsername().equals(training.getAuthor())) {
            return CommonResult.errorResponse("对不起，你无权限操作！", CommonResult.STATUS_FORBIDDEN);
        }
        return CommonResult.successResponse(training, "查询成功！");
    }

    @DeleteMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = "root") // 只有超级管理员能删除训练
    public CommonResult deleteTraining(@RequestParam("cid") Long cid) {
        boolean result = trainingService.removeById(cid);
        /*
        Training的id为其他表的外键的表中的对应数据都会被一起删除！
         */
        if (result) { // 删除成功
            return CommonResult.successResponse(null, "删除成功！");
        } else {
            return CommonResult.errorResponse("删除失败！", CommonResult.STATUS_FAIL);
        }
    }

    @PostMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult addTraining(@RequestBody TrainingDto trainingDto) {

        boolean result = trainingService.addTraining(trainingDto);
        if (result) { // 添加成功
            return CommonResult.successResponse(null, "添加成功！");
        } else {
            return CommonResult.errorResponse("添加失败", CommonResult.STATUS_FAIL);
        }
    }

    @PutMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult updateTraining(@RequestBody TrainingDto trainingDto, HttpServletRequest request) {

        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        // 是否为超级管理员
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");
        // 只有超级管理员和比赛拥有者才能操作
        if (!isRoot && !userRolesVo.getUsername().equals(trainingDto.getTraining().getAuthor())) {
            return CommonResult.errorResponse("对不起，你无权限操作！", CommonResult.STATUS_FORBIDDEN);
        }

        boolean result = trainingService.updateTraining(trainingDto);
        if (result) {
            return CommonResult.successResponse(null, "修改成功！");
        } else {
            return CommonResult.errorResponse("修改失败", CommonResult.STATUS_FAIL);
        }
    }

    @GetMapping("/get-problem-list")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    @Transactional(rollbackFor = Exception.class)
    public CommonResult getProblemList(@RequestParam(value = "limit", required = false) Integer limit,
                                       @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                       @RequestParam(value = "keyword", required = false) String keyword,
                                       @RequestParam(value = "queryExisted", defaultValue = "false") Boolean queryExisted,
                                       @RequestParam(value = "tid", required = true) Long tid) {

        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;
        HashMap<String, Object> trainingProblemMap = trainingProblemService.getAdminTrainingProblemList(limit, currentPage, keyword, tid, queryExisted);
        return CommonResult.successResponse(trainingProblemMap, "获取成功");
    }

    @GetMapping("/problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult getProblem(@Valid @RequestParam("pid") Long pid) {
        Problem problem = problemService.getById(pid);
        if (problem != null) { // 查询成功
            return CommonResult.successResponse(problem, "查询成功！");
        } else {
            return CommonResult.errorResponse("查询失败！", CommonResult.STATUS_FAIL);
        }
    }


}
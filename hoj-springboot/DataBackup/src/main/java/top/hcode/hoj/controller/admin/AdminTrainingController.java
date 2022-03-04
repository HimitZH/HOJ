package top.hcode.hoj.controller.admin;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
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
import top.hcode.hoj.crawler.problem.ProblemStrategy;
import top.hcode.hoj.pojo.dto.TrainingDto;
import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.training.Training;
import top.hcode.hoj.pojo.entity.training.TrainingProblem;
import top.hcode.hoj.pojo.entity.training.TrainingRecord;
import top.hcode.hoj.pojo.vo.TrainingVo;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.service.problem.impl.ProblemServiceImpl;
import top.hcode.hoj.service.training.impl.TrainingProblemServiceImpl;
import top.hcode.hoj.service.training.impl.TrainingRecordServiceImpl;
import top.hcode.hoj.service.training.impl.TrainingRegisterServiceImpl;
import top.hcode.hoj.service.training.impl.TrainingServiceImpl;
import top.hcode.hoj.utils.Constants;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.File;
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

    @Resource
    private TrainingRecordServiceImpl trainingRecordService;

    @Resource
    private TrainingRegisterServiceImpl trainingRegisterService;

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
        // 过滤密码
        queryWrapper.select(Training.class, info -> !info.getColumn().equals("private_pwd"));
        if (!StringUtils.isEmpty(keyword)) {
            keyword = keyword.trim();
            queryWrapper
                    .like("title", keyword).or()
                    .like("id", keyword).or()
                    .like("`rank`", keyword);
        }

        queryWrapper.orderByAsc("`rank`");

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
        return trainingService.getAdminTrainingDto(tid, request);
    }

    @DeleteMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = "root") // 只有超级管理员能删除训练
    public CommonResult deleteTraining(@RequestParam("tid") Long tid) {
        boolean result = trainingService.removeById(tid);
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
        // 只有超级管理员和训练拥有者才能操作
        if (!isRoot && !userRolesVo.getUsername().equals(trainingDto.getTraining().getAuthor())) {
            return CommonResult.errorResponse("对不起，你无权限操作！", CommonResult.STATUS_FORBIDDEN);
        }
        boolean result = trainingService.updateTraining(trainingDto);
        if (result) {
            trainingRecordService.checkSyncRecord(trainingDto.getTraining());
            return CommonResult.successResponse(null, "修改成功！");
        } else {
            return CommonResult.errorResponse("修改失败", CommonResult.STATUS_FAIL);
        }
    }


    @PutMapping("/change-training-status")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult changeTrainingStatus(@RequestParam(value = "tid", required = true) Long tid,
                                             @RequestParam(value = "author", required = true) String author,
                                             @RequestParam(value = "status", required = true) Boolean status,
                                             HttpServletRequest request) {

        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        // 是否为超级管理员
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");
        // 只有超级管理员和训练拥有者才能操作
        if (!isRoot && !userRolesVo.getUsername().equals(author)) {
            return CommonResult.errorResponse("对不起，你无权限操作！", CommonResult.STATUS_FORBIDDEN);
        }

        boolean result = trainingService.saveOrUpdate(new Training().setId(tid).setStatus(status));
        if (result) { // 添加成功
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


    @PutMapping("/problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "problem_admin"}, logical = Logical.OR)
    public CommonResult updateProblem(@RequestBody TrainingProblem trainingProblem) {
        boolean isOk = trainingProblemService.saveOrUpdate(trainingProblem);
        if (isOk) { // 删除成功
            return CommonResult.successResponse(null, "修改成功！");
        } else {
            return CommonResult.errorResponse("修改失败！", CommonResult.STATUS_FAIL);
        }
    }

    @DeleteMapping("/problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "problem_admin"}, logical = Logical.OR)
    public CommonResult deleteProblem(@RequestParam("pid") Long pid,
                                      @RequestParam(value = "tid", required = false) Long tid) {

        boolean result = false;
        //  训练id不为null，表示就是从比赛列表移除而已
        if (tid != null) {
            QueryWrapper<TrainingProblem> trainingProblemQueryWrapper = new QueryWrapper<>();
            trainingProblemQueryWrapper.eq("tid", tid).eq("pid", pid);
            result = trainingProblemService.remove(trainingProblemQueryWrapper);

        } else {
             /*
                problem的id为其他表的外键的表中的对应数据都会被一起删除！
              */
            result = problemService.removeById(pid);
        }


        if (result) { // 删除成功
            if (tid == null) {
                FileUtil.del(Constants.File.TESTCASE_BASE_FOLDER.getPath() + File.separator + "problem_" + pid);
            }
            return CommonResult.successResponse(null, "删除成功！");
        } else {
            return CommonResult.errorResponse("删除失败！", CommonResult.STATUS_FAIL);
        }
    }

    @PostMapping("/add-problem-from-public")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult addProblemFromPublic(@RequestBody HashMap<String, String> params) {

        String pidStr = params.get("pid");
        String tidStr = params.get("tid");
        String displayId = params.get("displayId");
        if (StringUtils.isEmpty(pidStr) || StringUtils.isEmpty(tidStr) || StringUtils.isEmpty(displayId)) {
            return CommonResult.errorResponse("参数错误！", CommonResult.STATUS_FAIL);
        }

        Long pid = Long.valueOf(pidStr);
        Long tid = Long.valueOf(tidStr);

        QueryWrapper<TrainingProblem> trainingProblemQueryWrapper = new QueryWrapper<>();
        trainingProblemQueryWrapper.eq("tid", tid)
                .and(wrapper -> wrapper.eq("pid", pid)
                        .or()
                        .eq("display_id", displayId));
        TrainingProblem trainingProblem = trainingProblemService.getOne(trainingProblemQueryWrapper, false);
        if (trainingProblem != null) {
            return CommonResult.errorResponse("添加失败，该题目已添加或者题目的训练展示ID已存在！", CommonResult.STATUS_FAIL);
        }

        TrainingProblem newTProblem = new TrainingProblem();
        boolean result = trainingProblemService.saveOrUpdate(newTProblem
                .setTid(tid).setPid(pid).setDisplayId(displayId));
        if (result) { // 添加成功
            trainingRegisterService.syncAlreadyRegisterUserRecord(tid, pid, newTProblem.getId());
            return CommonResult.successResponse(null, "添加成功！");
        } else {
            return CommonResult.errorResponse("添加失败", CommonResult.STATUS_FAIL);
        }
    }

    @GetMapping("/import-remote-oj-problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    @Transactional(rollbackFor = Exception.class)
    public CommonResult importTrainingRemoteOJProblem(@RequestParam("name") String name,
                                                      @RequestParam("problemId") String problemId,
                                                      @RequestParam("tid") Long tid,
                                                      HttpServletRequest request) {

        QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("problem_id", name.toUpperCase() + "-" + problemId);
        Problem problem = problemService.getOne(queryWrapper, false);

        // 如果该题目不存在，需要先导入
        if (problem == null) {
            HttpSession session = request.getSession();
            UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
            try {
                ProblemStrategy.RemoteProblemInfo otherOJProblemInfo = problemService.getOtherOJProblemInfo(name.toUpperCase(), problemId, userRolesVo.getUsername());
                if (otherOJProblemInfo != null) {
                    problem = problemService.adminAddOtherOJProblem(otherOJProblemInfo, name);
                    if (problem == null) {
                        return CommonResult.errorResponse("导入新题目失败！请重新尝试！");
                    }
                } else {
                    return CommonResult.errorResponse("导入新题目失败！原因：可能是与该OJ链接超时或题号格式错误！");
                }
            } catch (Exception e) {
                return CommonResult.errorResponse(e.getMessage());
            }
        }

        QueryWrapper<TrainingProblem> trainingProblemQueryWrapper = new QueryWrapper<>();
        Problem finalProblem = problem;
        trainingProblemQueryWrapper.eq("tid", tid)
                .and(wrapper -> wrapper.eq("pid", finalProblem.getId())
                        .or()
                        .eq("display_id", finalProblem.getProblemId()));
        TrainingProblem trainingProblem = trainingProblemService.getOne(trainingProblemQueryWrapper, false);
        if (trainingProblem != null) {
            return CommonResult.errorResponse("添加失败，该题目已添加或者题目的训练展示ID已存在！", CommonResult.STATUS_FAIL);
        }

        TrainingProblem newTProblem = new TrainingProblem();
        boolean result = trainingProblemService.saveOrUpdate(newTProblem
                .setTid(tid).setPid(problem.getId()).setDisplayId(problem.getProblemId()));
        if (result) { // 添加成功
            trainingRegisterService.syncAlreadyRegisterUserRecord(tid, problem.getId(), newTProblem.getId());
            return CommonResult.successResponse(null, "添加成功！");
        } else {
            return CommonResult.errorResponse("添加失败", CommonResult.STATUS_FAIL);
        }
    }

}
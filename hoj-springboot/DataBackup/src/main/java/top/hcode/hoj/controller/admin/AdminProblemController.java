package top.hcode.hoj.controller.admin;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.crawler.problem.ProblemStrategy;
import top.hcode.hoj.judge.Dispatcher;
import top.hcode.hoj.pojo.dto.ProblemDto;
import top.hcode.hoj.pojo.entity.*;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.service.impl.*;
import top.hcode.hoj.utils.Constants;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.File;
import java.util.*;

/**
 * @Author: Himit_ZH
 * @Date: 2020/12/11 21:45
 * @Description:
 */
@RestController
@RefreshScope
@RequestMapping("/api/admin/problem")
public class AdminProblemController {

    @Autowired
    private ProblemServiceImpl problemService;

    @Autowired
    private ProblemCaseServiceImpl problemCaseService;

    @Autowired
    private Dispatcher dispatcher;

    @Value("${hoj.judge.token}")
    private String judgeToken;


    @GetMapping("/get-problem-list")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult getProblemList(@RequestParam(value = "limit", required = false) Integer limit,
                                       @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                       @RequestParam(value = "keyword", required = false) String keyword,
                                       @RequestParam(value = "oj", required = false) String oj) {
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;
        IPage<Problem> iPage = new Page<>(currentPage, limit);
        IPage<Problem> problemList;

        QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("gmt_create");

        if (oj != null && oj.equals("Mine")) {
            queryWrapper.eq("is_remote", false);
        }

        if (!StringUtils.isEmpty(keyword)) {
            final String key = keyword.trim();
            queryWrapper.and(wrapper -> wrapper.like("title", key).or()
                    .like("author", key).or()
                    .like("problem_id", key));
            problemList = problemService.page(iPage, queryWrapper);
        } else {
            problemList = problemService.page(iPage, queryWrapper);
        }

        if (problemList.getTotal() == 0) { // 未查询到一条数据
            return CommonResult.successResponse(problemList, "暂无数据");
        } else {
            return CommonResult.successResponse(problemList, "获取成功");
        }
    }

    @GetMapping("")
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

    @DeleteMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "problem_admin"}, logical = Logical.OR)
    public CommonResult deleteProblem(@Valid @RequestParam("pid") Long pid) {
        boolean result = problemService.removeById(pid);
        /*
        problem的id为其他表的外键的表中的对应数据都会被一起删除！
         */
        if (result) { // 删除成功
            FileUtil.del(Constants.File.TESTCASE_BASE_FOLDER.getPath() + File.separator + "problem_" + pid);
            return CommonResult.successResponse(null, "删除成功！");
        } else {
            return CommonResult.errorResponse("删除失败！", CommonResult.STATUS_FAIL);
        }
    }

    @PostMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult addProblem(@RequestBody ProblemDto problemDto) {

        QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("problem_id", problemDto.getProblem().getProblemId().toUpperCase());
        Problem problem = problemService.getOne(queryWrapper);
        if (problem != null) {
            return CommonResult.errorResponse("该题目的Problem ID已存在，请更换！", CommonResult.STATUS_FAIL);
        }

        boolean result = problemService.adminAddProblem(problemDto);
        if (result) { // 添加成功
            return CommonResult.successResponse(null, "添加成功！");
        } else {
            return CommonResult.errorResponse("添加失败", CommonResult.STATUS_FAIL);
        }

    }

    @PutMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    @Transactional
    public CommonResult updateProblem(@RequestBody ProblemDto problemDto, HttpServletRequest request) {

        QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("problem_id", problemDto.getProblem().getProblemId().toUpperCase());
        Problem problem = problemService.getOne(queryWrapper);

        // 如果problem_id不是原来的且已存在该problem_id，则修改失败！
        if (problem != null && problem.getId().longValue() != problemDto.getProblem().getId()) {
            return CommonResult.errorResponse("当前的Problem ID 已被使用，请重新更换新的！", CommonResult.STATUS_FAIL);
        }
        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        // 记录修改题目的用户
        problemDto.getProblem().setModifiedUser(userRolesVo.getUsername());

        boolean result = problemService.adminUpdateProblem(problemDto);
        if (result) { // 更新成功
            return CommonResult.successResponse(null, "修改成功！");
        } else {
            return CommonResult.errorResponse("修改失败", CommonResult.STATUS_FAIL);
        }
    }

    @GetMapping("/get-problem-cases")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult getProblemCases(@Valid @RequestParam("pid") Long pid) {
        Map<String, Object> map = new HashMap<>();
        map.put("pid", pid);
        map.put("status", 0);
        List<ProblemCase> problemCases = (List<ProblemCase>) problemCaseService.listByMap(map);
        if (problemCases != null && problemCases.size() > 0) {
            return CommonResult.successResponse(problemCases, "获取该题目的评测样例列表成功！");
        } else {
            return CommonResult.successResponse(null, "获取该题目的评测样例列表为空！");
        }
    }

    @PostMapping("/compile-spj")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult compileSpj(@RequestBody CompileSpj compileSpj) {

        if (StringUtils.isEmpty(compileSpj.getSpjSrc()) ||
                StringUtils.isEmpty(compileSpj.getSpjLanguage())) {
            return CommonResult.errorResponse("参数不能为空！");
        }

        compileSpj.setToken(judgeToken);
        return dispatcher.dispatcher("compile", "/compile-spj", compileSpj);
    }

    @GetMapping("/import-remote-oj-problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    @Transactional
    public CommonResult importRemoteOJProblem(@RequestParam("name") String name,
                                              @RequestParam("problemId") String problemId,
                                              HttpServletRequest request) {

        QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("problem_id", name.toUpperCase() + "-" + problemId);
        Problem problem = problemService.getOne(queryWrapper);
        if (problem != null) {
            return CommonResult.errorResponse("该题目已添加，请勿重复添加！", CommonResult.STATUS_FAIL);
        }

        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        try {
            ProblemStrategy.RemoteProblemInfo otherOJProblemInfo = problemService.getOtherOJProblemInfo(name.toUpperCase(), problemId, userRolesVo.getUsername());
            if (otherOJProblemInfo != null) {
                Problem importProblem = problemService.adminAddOtherOJProblem(otherOJProblemInfo, name);
                if (importProblem == null) {
                    return CommonResult.errorResponse("导入新题目失败！请重新尝试！");
                }
            } else {
                return CommonResult.errorResponse("导入新题目失败！原因：可能是与该OJ链接超时或题号格式错误！");
            }
        } catch (Exception e) {
            return CommonResult.errorResponse(e.getMessage());
        }
        return CommonResult.successResponse(null, "导入新题目成功！");
    }


}
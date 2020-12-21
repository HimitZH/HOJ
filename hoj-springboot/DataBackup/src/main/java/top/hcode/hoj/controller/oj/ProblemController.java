package top.hcode.hoj.controller.oj;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.vo.ProblemVo;
import top.hcode.hoj.pojo.entity.Problem;
import top.hcode.hoj.service.impl.ProblemServiceImpl;


/**
 * @Author: Himit_ZH
 * @Date: 2020/10/27 13:24
 * @Description: 问题数据控制类，处理题目列表请求，题目内容请求。
 */
@RestController
@RequestMapping("/api")
public class ProblemController {

    @Autowired
    private ProblemServiceImpl problemService;


    /**
     * @MethodName getProblemList
     * @Params * @param null
     * @Description 获取题目列表分页
     * @Return CommonResult
     * @Since 2020/10/27
     */
    @RequestMapping(value = "/get-problem-list", method = RequestMethod.GET)
    public CommonResult getProblemList(@RequestParam(value = "limit", required = false) Integer limit,
                                       @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                       @RequestParam(value = "searchPid", required = false) Long searchPid,
                                       @RequestParam(value = "searchTitle", required = false) String searchTitle) {
        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 15;

        // 设置无传参的空值设定
        if (searchPid == null) {
            searchPid = 0L;
        }

        Page<ProblemVo> problemList = problemService.getProblemList(limit, currentPage, searchPid, searchTitle);

        if (problemList.getTotal() == 0) { // 未查询到一条数据
            return CommonResult.errorResponse("暂无数据", CommonResult.STATUS_NOT_FOUND);
        } else {
            return CommonResult.successResponse(problemList, "获取成功");
        }
    }


    /**
     * @MethodName getProblemInfo
     * @Params * @param null
     * @Description 获取指定题目的详情信息
     * @Return CommonResult
     * @Since 2020/10/27
     */
    @RequestMapping(value = "/get-problem-info", method = RequestMethod.GET)
    public CommonResult getProblemInfo(@RequestParam(value = "pid", required = true) Long pid,
                                       @RequestParam(value = "cid", required = true) Long cid) {
        boolean isContestingProblem = false;
        if (cid != 0) {
            isContestingProblem = true;
        }
        QueryWrapper<Problem> wrapper = new QueryWrapper<Problem>().eq("id", pid)
                .eq(isContestingProblem,"auth", 3)
                .eq(!isContestingProblem, "auth",1);

        Problem problem = problemService.getOne(wrapper);
        if (problem == null) {
            return CommonResult.errorResponse("该题号对应的题目不存在或暂时不能访问", CommonResult.STATUS_NOT_FOUND);
        }
        return CommonResult.successResponse(problem, "获取成功");
    }


}
package top.hcode.hoj.controller.oj;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.PidListDTO;
import top.hcode.hoj.pojo.vo.*;
import top.hcode.hoj.service.oj.ProblemService;

import java.util.*;


/**
 * @Author: Himit_ZH
 * @Date: 2020/10/27 13:24
 * @Description: 问题数据控制类，处理题目列表请求，题目内容请求。
 */
@RestController
@RequestMapping("/api")
public class ProblemController {

    @Autowired
    private ProblemService problemService;

    /**
     * @param currentPage
     * @param keyword
     * @param tagId
     * @param difficulty
     * @param oj
     * @MethodName getProblemList
     * @Description 获取题目列表分页
     * @Return CommonResult
     * @Since 2020/10/27
     */
    @RequestMapping(value = "/get-problem-list", method = RequestMethod.GET)
    public CommonResult<Page<ProblemVO>> getProblemList(@RequestParam(value = "limit", required = false) Integer limit,
                                                        @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                                        @RequestParam(value = "keyword", required = false) String keyword,
                                                        @RequestParam(value = "tagId", required = false) List<Long> tagId,
                                                        @RequestParam(value = "difficulty", required = false) Integer difficulty,
                                                        @RequestParam(value = "oj", required = false) String oj) {

        return problemService.getProblemList(limit, currentPage, keyword, tagId, difficulty, oj);
    }

    /**
     * @MethodName getRandomProblem
     * @Description 随机选取一道题目
     * @Return CommonResult
     * @Since 2020/10/27
     */
    @GetMapping("/get-random-problem")
    public CommonResult<RandomProblemVO> getRandomProblem() {
        return problemService.getRandomProblem();
    }

    /**
     * @param pidListDto
     * @MethodName getUserProblemStatus
     * @Description 获取用户对应该题目列表中各个题目的做题情况
     * @Return CommonResult
     * @Since 2020/12/29
     */
    @RequiresAuthentication
    @PostMapping("/get-user-problem-status")
    public CommonResult<HashMap<Long, Object>> getUserProblemStatus(@Validated @RequestBody PidListDTO pidListDto) {
        return problemService.getUserProblemStatus(pidListDto);
    }

    /**
     * @param problemId
     * @MethodName getProblemInfo
     * @Description 获取指定题目的详情信息，标签，所支持语言，做题情况（只能查询公开题目 也就是auth为1）
     * @Return CommonResult
     * @Since 2020/10/27
     */
    @RequestMapping(value = "/get-problem-detail", method = RequestMethod.GET)
    public CommonResult<ProblemInfoVO> getProblemInfo(@RequestParam(value = "problemId", required = true) String problemId,
                                                      @RequestParam(value = "gid", required = false) Long gid) {
        return problemService.getProblemInfo(problemId,gid);
    }


}
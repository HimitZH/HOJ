package top.hcode.hoj.controller.oj;

import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.ContestRankDto;
import top.hcode.hoj.pojo.vo.*;
import top.hcode.hoj.service.oj.ContestScoreboardService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/11 22:11
 * @Description: 处理比赛外榜的相关请求
 */

@RestController
@RequestMapping("/api")
public class ContestScoreboardController {

    @Resource
    private ContestScoreboardService contestScoreboardService;

    /**
     * @param cid 比赛id
     * @MethodName getContestOutsideInfo
     * @Description 提供比赛外榜所需的比赛信息和题目信息
     * @Return
     * @Since 2021/12/8
     */
    @GetMapping("/get-contest-outsize-info")
    public CommonResult<ContestOutsideInfo> getContestOutsideInfo(@RequestParam(value = "cid", required = true) Long cid) {
        return contestScoreboardService.getContestOutsideInfo(cid);
    }

    /**
     * @MethodName getContestScoreBoard
     * @Description 提供比赛外榜排名数据
     * @Return
     * @Since 2021/12/07
     */
    @PostMapping("/get-contest-outside-scoreboard")
    public CommonResult<List> getContestOutsideScoreboard(@RequestBody ContestRankDto contestRankDto) {
        return contestScoreboardService.getContestOutsideScoreboard(contestRankDto);
    }
}
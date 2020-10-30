package top.hcode.hoj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.vo.RankVo;
import top.hcode.hoj.service.UserRecordService;

/**
 * @Author: Himit_ZH
 * @Date: 2020/10/27 20:53
 * @Description: 处理排行榜数据
 */
@RestController
@RequestMapping("/api")
public class RankController {

    @Autowired
    private UserRecordService userRecordService;


    /**
     * @MethodName get-rank-list
     * @Params  * @param null
     * @Description 获取排行榜数据
     * @Return CommonResult
     * @Since 2020/10/27
     */
    @GetMapping("/get-rank-list")
    public CommonResult getRankList(@RequestParam(value = "limit", required = false) Integer limit,
                                    @RequestParam(value = "currentPage", required = false) Integer currentPage) {

        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 30;

        Page<RankVo> rankList = userRecordService.getRankList(limit, currentPage);

        if (rankList.getTotal() == 0) {
            return CommonResult.errorResponse("暂无数据", CommonResult.STATUS_NOT_FOUND);
        } else {
            return CommonResult.successResponse(rankList, "获取成功");
        }
    }
}
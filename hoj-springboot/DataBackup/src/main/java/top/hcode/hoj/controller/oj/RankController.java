package top.hcode.hoj.controller.oj;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.vo.ACMRankVo;
import top.hcode.hoj.service.impl.UserRecordServiceImpl;
import top.hcode.hoj.utils.Constants;

/**
 * @Author: Himit_ZH
 * @Date: 2020/10/27 20:53
 * @Description: 处理排行榜数据
 */
@RestController
@RequestMapping("/api")
public class RankController {

    @Autowired
    private UserRecordServiceImpl userRecordService;


    /**
     * @MethodName get-rank-list
     * @Params * @param null
     * @Description 获取排行榜数据
     * @Return CommonResult
     * @Since 2020/10/27
     */
    @GetMapping("/get-rank-list")
    public CommonResult getRankList(@RequestParam(value = "limit", required = false) Integer limit,
                                    @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                    @RequestParam(value = "type", required = true) Integer type) {

        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 30;

        Page rankList = null;
        // 根据type查询不同类型的排行榜
        if (type.intValue() == Constants.Contest.TYPE_ACM.getCode()) {
            rankList = userRecordService.getACMRankList(limit, currentPage);
        } else if (type.intValue() == Constants.Contest.TYPE_OI.getCode()) {
            rankList = userRecordService.getOIRankList(limit, currentPage);
        } else {
            return CommonResult.errorResponse("比赛类型代码不正确！");
        }

        if (rankList != null && rankList.getTotal() == 0) {
            return CommonResult.successResponse(rankList, "暂无数据");
        } else {
            return CommonResult.successResponse(rankList, "获取成功");
        }
    }
}
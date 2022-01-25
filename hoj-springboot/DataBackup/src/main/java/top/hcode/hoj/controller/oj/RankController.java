package top.hcode.hoj.controller.oj;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.user.UserInfo;
import top.hcode.hoj.service.user.impl.UserInfoServiceImpl;
import top.hcode.hoj.service.user.impl.UserRecordServiceImpl;
import top.hcode.hoj.utils.Constants;

import java.util.List;
import java.util.stream.Collectors;

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

    @Autowired
    private UserInfoServiceImpl userInfoService;


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
                                    @RequestParam(value = "searchUser", required = false) String searchUser,
                                    @RequestParam(value = "type", required = true) Integer type) {

        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 30;

        List<String> uidList = null;
        if (!StringUtils.isEmpty(searchUser)) {
            QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
            userInfoQueryWrapper.and(wrapper -> wrapper
                    .like("username", searchUser)
                    .or()
                    .like("nickname", searchUser)
                    .or()
                    .like("realname", searchUser));

            userInfoQueryWrapper.eq("status", 0);

            uidList = userInfoService.list(userInfoQueryWrapper)
                    .stream()
                    .map(UserInfo::getUuid)
                    .collect(Collectors.toList());
        }

        IPage rankList = null;
        // 根据type查询不同类型的排行榜
        if (type.intValue() == Constants.Contest.TYPE_ACM.getCode()) {
            rankList = userRecordService.getACMRankList(limit, currentPage, uidList);
        } else if (type.intValue() == Constants.Contest.TYPE_OI.getCode()) {
            rankList = userRecordService.getOIRankList(limit, currentPage, uidList);
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
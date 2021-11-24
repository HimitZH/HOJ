package top.hcode.hoj.controller.oj;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.UnicodeUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.dao.ContestMapper;
import top.hcode.hoj.pojo.entity.common.File;
import top.hcode.hoj.pojo.vo.ACMRankVo;
import top.hcode.hoj.pojo.vo.AnnouncementVo;
import top.hcode.hoj.pojo.vo.ConfigVo;
import top.hcode.hoj.pojo.vo.ContestVo;
import top.hcode.hoj.service.common.impl.AnnouncementServiceImpl;
import top.hcode.hoj.service.common.impl.FileServiceImpl;
import top.hcode.hoj.service.user.impl.UserRecordServiceImpl;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.RedisUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: Himit_ZH
 * @Date: 2020/12/26 14:12
 * @Description: 处理客户端主页的请求
 */
@RestController
@RequestMapping("/api")
public class HomeController {

    @Autowired
    private ContestMapper contestDao;

    @Autowired
    private ConfigVo configVo;

    @Autowired
    private AnnouncementServiceImpl announcementDao;

    @Autowired

    private UserRecordServiceImpl userRecordService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private FileServiceImpl fileService;

    /**
     * @MethodName getRecentContest
     * @Params * @param null
     * @Description 获取最近14天的比赛信息列表
     * @Return CommonResult
     * @Since 2020/12/29
     */

    @GetMapping("/get-recent-contest")
    public CommonResult getRecentContest() {
        List<ContestVo> contests = contestDao.getWithinNext14DaysContests();
        return CommonResult.successResponse(contests);
    }



    /**
     * @MethodName getHomeCarousel
     * @Params
     * @Description 获取主页轮播图
     * @Return
     * @Since 2021/9/4
     */
    @GetMapping("/home-carousel")
    public CommonResult getHomeCarousel() {
        List<File> fileList = fileService.queryCarouselFileList();
        List<HashMap<String, Object>> apiList = fileList.stream().map(f -> {
            HashMap<String, Object> param = new HashMap<>(2);
            param.put("id", f.getId());
            param.put("url", Constants.File.IMG_API.getPath() + f.getName());
            return param;
        }).collect(Collectors.toList());
        return CommonResult.successResponse(apiList);
    }



    /**
     * @MethodName getRecentSevenACRank
     * @Params * @param null
     * @Description 获取最近7天用户做题榜单
     * @Return
     * @Since 2021/1/15
     */
    @GetMapping("/get-recent-seven-ac-rank")
    public CommonResult getRecentSevenACRank() {
        List<ACMRankVo> recent7ACRank = userRecordService.getRecent7ACRank();

        return CommonResult.successResponse(recent7ACRank, "获取成功！");
    }



    /**
     * @MethodName getRecentOtherContest
     * @Params * @param null
     * @Description 获取最近其他OJ的比赛信息列表
     * @Return CommonResult
     * @Since 2020/1/15
     */

    @GetMapping("/get-recent-other-contest")
    public CommonResult getRecentOtherContest() {
        String redisKey = Constants.Schedule.RECENT_OTHER_CONTEST.getCode();
        List<HashMap<String, Object>> contestsList;
        // 从redis获取比赛列表
        contestsList = (ArrayList<HashMap<String, Object>>) redisUtils.get(redisKey);

        return CommonResult.successResponse(contestsList,"获取其它OJ最近比赛列表成功");
    }


    /**
     * @MethodName getCommonAnnouncement
     * @Params * @param null
     * @Description 获取主页公告列表
     * @Return CommonResult
     * @Since 2020/12/29
     */

    @GetMapping("/get-common-announcement")

    public CommonResult getCommonAnnouncement(@RequestParam(value = "limit", required = false) Integer limit,
                                              @RequestParam(value = "currentPage", required = false) Integer currentPage) {
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;
        IPage<AnnouncementVo> announcementList = announcementDao.getAnnouncementList(limit, currentPage, true);
        return CommonResult.successResponse(announcementList);
    }

    /**
     * @MethodName getWebConfig
     * @Params * @param null
     * @Description 获取网站的基础配置。例如名字，缩写名字等等。
     * @Return CommonResult
     * @Since 2020/12/29
     */

    @GetMapping("/get-website-config")
    public CommonResult getWebConfig() {

        return CommonResult.successResponse(
                MapUtil.builder().put("baseUrl", UnicodeUtil.toString(configVo.getBaseUrl()))
                        .put("name", UnicodeUtil.toString(configVo.getName()))
                        .put("shortName", UnicodeUtil.toString(configVo.getShortName()))
                        .put("register", configVo.getRegister())
                        .put("recordName", UnicodeUtil.toString(configVo.getRecordName()))
                        .put("recordUrl", UnicodeUtil.toString(configVo.getRecordUrl()))
                        .put("description", UnicodeUtil.toString(configVo.getDescription()))
                        .put("email", UnicodeUtil.toString(configVo.getEmailUsername()))
                        .put("projectName", UnicodeUtil.toString(configVo.getProjectName()))
                        .put("projectUrl", UnicodeUtil.toString(configVo.getProjectUrl())).map()
        );
    }

}
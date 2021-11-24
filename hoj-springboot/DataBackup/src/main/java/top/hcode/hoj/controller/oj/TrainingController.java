package top.hcode.hoj.controller.oj;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.training.Training;
import top.hcode.hoj.pojo.entity.training.TrainingCategory;
import top.hcode.hoj.pojo.entity.training.TrainingRegister;
import top.hcode.hoj.pojo.vo.ProblemVo;
import top.hcode.hoj.pojo.vo.TrainingRankVo;
import top.hcode.hoj.pojo.vo.TrainingVo;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.service.training.impl.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2021/11/19 21:42
 * @Description: 处理训练题单的请求
 */

@RestController
@RequestMapping("/api")
public class TrainingController {

    @Resource
    private TrainingServiceImpl trainingService;

    @Resource
    private TrainingRegisterServiceImpl trainingRegisterService;

    @Resource
    private TrainingCategoryServiceImpl trainingCategoryService;

    @Resource
    private TrainingProblemServiceImpl trainingProblemService;

    @Resource
    private TrainingRecordServiceImpl trainingRecordService;

    /**
     * @param limit
     * @param currentPage
     * @param keyword
     * @param categoryId
     * @param auth
     * @MethodName getTrainingList
     * @Description 获取训练题单列表，可根据关键词、类别、权限、类型过滤
     * @Return
     * @Since 2021/11/20
     */
    @GetMapping("/get-training-list")
    public CommonResult getTrainingList(@RequestParam(value = "limit", required = false) Integer limit,
                                        @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                        @RequestParam(value = "keyword", required = false) String keyword,
                                        @RequestParam(value = "categoryId", required = false) Long categoryId,
                                        @RequestParam(value = "auth", required = false) String auth) {

        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 30;
        IPage<TrainingVo> trainingList = trainingService.getTrainingList(limit, currentPage, categoryId, auth, keyword);
        return CommonResult.successResponse(trainingList, "success");
    }


    /**
     * @param tid
     * @param request
     * @MethodName getTraining
     * @Description 根据tid获取指定训练详情
     * @Return
     * @Since 2021/11/20
     */
    @GetMapping("/get-training-detail")
    public CommonResult getTraining(@RequestParam(value = "tid") Long tid, HttpServletRequest request) {

        Training training = trainingService.getById(tid);
        if (training == null || !training.getStatus()) {
            return CommonResult.errorResponse("该训练不存在或不允许显示！");
        }

        CommonResult result = trainingRegisterService.checkTrainingAuth(training, request);
        if (result != null) {
            return result;
        }

        TrainingVo trainingVo = BeanUtil.copyProperties(training, TrainingVo.class);
        TrainingCategory trainingCategory = trainingCategoryService.getTrainingCategoryByTrainingId(training.getId());
        trainingVo.setCategoryName(trainingCategory.getName());
        trainingVo.setCategoryColor(trainingCategory.getColor());
        trainingVo.setProblemCount(trainingProblemService.getTrainingProblemCount(training.getId()));

        return CommonResult.successResponse(trainingVo, "success");
    }

    /**
     * @param tid
     * @param request
     * @MethodName getTrainingProblemList
     * @Description 根据tid获取指定训练的题单题目列表
     * @Return
     * @Since 2021/11/20
     */
    @GetMapping("/get-training-problem-list")
    public CommonResult getTrainingProblemList(@RequestParam(value = "tid") Long tid, HttpServletRequest request) {

        Training training = trainingService.getById(tid);
        if (training == null || !training.getStatus()) {
            return CommonResult.errorResponse("该训练不存在或不允许显示！");
        }

        CommonResult result = trainingRegisterService.checkTrainingAuth(training, request);
        if (result != null) {
            return result;
        }
        List<ProblemVo> trainingProblemList = trainingProblemService.getTrainingProblemList(tid);
        return CommonResult.successResponse(trainingProblemList, "success");
    }

    /**
     * @param params
     * @param request
     * @MethodName toRegisterTraining
     * @Description 注册校验私有权限的训练
     * @Return
     * @Since 2021/11/20
     */
    @PostMapping("/register-training")
    @RequiresAuthentication
    public CommonResult toRegisterTraining(@RequestBody HashMap<String, Object> params, HttpServletRequest request) {
        String tidStr = (String) params.get("tid");
        String password = (String) params.get("password");
        if (StringUtils.isEmpty(tidStr) || StringUtils.isEmpty(password)) {
            return CommonResult.errorResponse("请求参数不能为空！");
        }
        Long tid = Long.valueOf(tidStr);

        return trainingRegisterService.toRegisterTraining(tid, password, request);
    }


    /**
     * @param tid
     * @param request
     * @MethodName getTrainingAccess
     * @Description 私有权限的训练需要获取当前用户是否有进入训练的权限
     * @Return
     * @Since 2021/11/20
     */
    @RequiresAuthentication
    @GetMapping("/get-training-access")
    public CommonResult getTrainingAccess(@RequestParam(value = "tid") Long tid, HttpServletRequest request) {

        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        QueryWrapper<TrainingRegister> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tid", tid).eq("uid", userRolesVo.getUid());
        TrainingRegister trainingRegister = trainingRegisterService.getOne(queryWrapper, false);
        HashMap<String, Object> result = new HashMap<>();
        result.put("access", trainingRegister != null);
        return CommonResult.successResponse(result);
    }


    /**
     * @MethodName getTrainingRank
     * @param tid
     * @param limit
     * @param currentPage
     * @param request
     * @Description 获取训练的排行榜分页
     * @Return
     * @Since 2021/11/22
     */
    @GetMapping("/get-training-rank")
    @RequiresAuthentication
    public CommonResult getTrainingRank(@RequestParam(value = "tid", required = true) Long tid,
                                        @RequestParam(value = "limit", required = false) Integer limit,
                                        @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                        HttpServletRequest request) {

        Training training = trainingService.getById(tid);
        if (training == null || !training.getStatus()) {
            return CommonResult.errorResponse("该训练不存在或不允许显示！");
        }

        CommonResult result = trainingRegisterService.checkTrainingAuth(training, request);
        if (result != null) {
            return result;
        }

        // 页数，每页数若为空，设置默认值
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 30;
        IPage<TrainingRankVo> trainingRankPager = trainingRecordService.getTrainingRank(tid, currentPage, limit);
        return CommonResult.successResponse(trainingRankPager, "success");
    }

}
package top.hcode.hoj.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.CommonResult;
import top.hcode.hoj.common.ResultStatus;
import top.hcode.hoj.common.exception.SystemError;
import top.hcode.hoj.dao.JudgeServerEntityService;
import top.hcode.hoj.pojo.dto.CompileDTO;
import top.hcode.hoj.pojo.dto.TestJudgeReq;
import top.hcode.hoj.pojo.dto.TestJudgeRes;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.dto.ToJudgeDTO;
import top.hcode.hoj.service.JudgeService;

import java.util.HashMap;
import java.util.Objects;


/**
 * @Author: Himit_ZH
 * @Date: 2020/10/29 22:22
 * @Description: 处理代码提交
 */
@RestController
@RefreshScope
public class JudgeController {

    @Autowired
    private JudgeService judgeService;

    @Value("${hoj.judge.token}")
    private String judgeToken;

    @Value("${hoj-judge-server.remote-judge.open}")
    private Boolean openRemoteJudge;

    @Autowired
    private JudgeServerEntityService judgeServerEntityService;

    @RequestMapping("/version")
    public CommonResult<HashMap<String, Object>> getVersion() {
        return CommonResult.successResponse(judgeServerEntityService.getJudgeServerInfo(), "运行正常");
    }

    @PostMapping(value = "/judge")
    public CommonResult<Void> submitProblemJudge(@RequestBody ToJudgeDTO toJudgeDTO) {

        if (!Objects.equals(toJudgeDTO.getToken(), judgeToken)) {
            return CommonResult.errorResponse("对不起！您使用的判题服务调用凭证不正确！访问受限！", ResultStatus.ACCESS_DENIED);
        }

        Judge judge = toJudgeDTO.getJudge();

        if (judge == null || judge.getSubmitId() == null || judge.getUid() == null || judge.getPid() == null) {
            return CommonResult.errorResponse("调用参数错误！请检查您的调用参数！");
        }

        judgeService.judge(judge);

        return CommonResult.successResponse("判题机评测完成！");
    }


    @PostMapping(value = "/test-judge")
    public CommonResult<TestJudgeRes> submitProblemTestJudge(@RequestBody TestJudgeReq testJudgeReq) {

        if (testJudgeReq == null
                || StringUtils.isEmpty(testJudgeReq.getCode())
                || StringUtils.isEmpty(testJudgeReq.getLanguage())
                || StringUtils.isEmpty(testJudgeReq.getUniqueKey())
                || testJudgeReq.getTimeLimit() == null
                || testJudgeReq.getMemoryLimit() == null
                || testJudgeReq.getStackLimit() == null) {
            return CommonResult.errorResponse("调用参数错误！请检查您的调用参数！");
        }

        if (!Objects.equals(testJudgeReq.getToken(), judgeToken)) {
            return CommonResult.errorResponse("对不起！您使用的判题服务调用凭证不正确！访问受限！", ResultStatus.ACCESS_DENIED);
        }
        return CommonResult.successResponse(judgeService.testJudge(testJudgeReq));
    }


    @PostMapping(value = "/compile-spj")
    public CommonResult<Void> compileSpj(@RequestBody CompileDTO compileDTO) {

        if (!Objects.equals(compileDTO.getToken(), judgeToken)) {
            return CommonResult.errorResponse("对不起！您使用的判题服务调用凭证不正确！访问受限！", ResultStatus.ACCESS_DENIED);
        }

        try {
            judgeService.compileSpj(compileDTO.getCode(), compileDTO.getPid(), compileDTO.getLanguage(), compileDTO.getExtraFiles());
            return CommonResult.successResponse(null, "编译成功！");
        } catch (SystemError systemError) {
            return CommonResult.errorResponse(systemError.getStderr(), ResultStatus.SYSTEM_ERROR);
        }
    }

    @PostMapping(value = "/compile-interactive")
    public CommonResult<Void> compileInteractive(@RequestBody CompileDTO compileDTO) {

        if (!Objects.equals(compileDTO.getToken(), judgeToken)) {
            return CommonResult.errorResponse("对不起！您使用的判题服务调用凭证不正确！访问受限！", ResultStatus.ACCESS_DENIED);
        }

        try {
            judgeService.compileInteractive(compileDTO.getCode(), compileDTO.getPid(), compileDTO.getLanguage(), compileDTO.getExtraFiles());
            return CommonResult.successResponse(null, "编译成功！");
        } catch (SystemError systemError) {
            return CommonResult.errorResponse(systemError.getStderr(), ResultStatus.SYSTEM_ERROR);
        }
    }

    @PostMapping(value = "/remote-judge")
    public CommonResult<Void> remoteJudge(@RequestBody ToJudgeDTO toJudgeDTO) {

        if (!openRemoteJudge) {
            return CommonResult.errorResponse("对不起！该判题服务器未开启远程虚拟判题功能！", ResultStatus.ACCESS_DENIED);
        }

        if (!Objects.equals(toJudgeDTO.getToken(), judgeToken)) {
            return CommonResult.errorResponse("对不起！您使用的判题服务调用凭证不正确！访问受限！", ResultStatus.ACCESS_DENIED);
        }


        if (toJudgeDTO.getJudge() == null) {
            return CommonResult.errorResponse("请求参数不能为空！");
        }

        judgeService.remoteJudge(toJudgeDTO);

        return CommonResult.successResponse("提交成功");
    }
}
package top.hcode.hoj.controller;


import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import top.hcode.hoj.common.CommonResult;
import top.hcode.hoj.common.exception.CompileError;
import top.hcode.hoj.common.exception.SystemError;
import top.hcode.hoj.judge.SandboxRun;
import top.hcode.hoj.pojo.entity.*;
import top.hcode.hoj.remoteJudge.RemoteJudgeToSubmit;
import top.hcode.hoj.service.impl.*;
import top.hcode.hoj.util.Constants;

import java.util.Date;
import java.util.HashMap;


/**
 * @Author: Himit_ZH
 * @Date: 2020/10/29 22:22
 * @Description: 处理代码提交
 */
@RestController
@Slf4j(topic = "hoj")
@RefreshScope
public class JudgeController {

    @Autowired
    private JudgeServiceImpl judgeService;

    @Autowired
    private ProblemServiceImpl problemService;

    @Autowired
    private RemoteJudgeToSubmit remoteJudgeToSubmit;


    @Value("${hoj.judge.token}")
    private String judgeToken;

    @Value("${hoj-judge-server.max-task-num}")
    private Integer maxTaskNum;

    @Value("${hoj-judge-server.remote-judge.open}")
    private Boolean isOpenRemoteJudge;

    @Value("${hoj-judge-server.remote-judge.max-task-num}")
    private Integer RemoteJudgeMaxTaskNum;

    @Value("${hoj-judge-server.name}")
    private String name;

    @Value("${hoj-judge-server.remote-judge.open}")
    private Boolean openRemoteJudge;


    @RequestMapping("/version")
    public CommonResult getVersion() {

        HashMap<String, Object> res = new HashMap<>();

        res.put("version", "1.2.0");
        res.put("currentTime", new Date());
        res.put("judgeServerName", name);
        res.put("cpu", Runtime.getRuntime().availableProcessors());

        if (maxTaskNum == -1) {
            res.put("maxTaskNum", Runtime.getRuntime().availableProcessors() * 2);
        } else {
            res.put("maxTaskNum", maxTaskNum);
        }
        if (isOpenRemoteJudge) {
            res.put("isOpenRemoteJudge", true);
            if (RemoteJudgeMaxTaskNum == -1) {
                res.put("remoteJudgeMaxTaskNum", Runtime.getRuntime().availableProcessors() * 4);
            } else {
                res.put("remoteJudgeMaxTaskNum", RemoteJudgeMaxTaskNum);
            }
        }

        String versionResp;

        try {
            versionResp = SandboxRun.getRestTemplate().getForObject(SandboxRun.getSandboxBaseUrl() + "/version", String.class);
        } catch (Exception e) {
            res.put("SandBoxMsg", MapUtil.builder().put("error", e.getMessage()).map());
            return CommonResult.successResponse(res, "判题服务器正常，安全沙盒异常，请检查！");
        }

        res.put("SandBoxMsg", JSONUtil.parseObj(versionResp));

        return CommonResult.successResponse(res, "运行正常");
    }

    @PostMapping(value = "/judge")
    public CommonResult submitProblemJudge(@RequestBody ToJudge toJudge) {

        if (!toJudge.getToken().equals(judgeToken)) {
            return CommonResult.errorResponse("对不起！您使用的判题服务调用凭证不正确！访问受限！", CommonResult.STATUS_ACCESS_DENIED);
        }

        Judge judge = toJudge.getJudge();

        if (judge == null || judge.getSubmitId() == null || judge.getUid() == null || judge.getPid() == null) {
            return CommonResult.errorResponse("调用参数错误！请检查您的调用参数！", CommonResult.STATUS_FAIL);
        }

        judge.setStatus(Constants.Judge.STATUS_COMPILING.getStatus()); // 标志该判题过程进入编译阶段
        // 写入当前判题服务的名字
        judge.setJudger(name);
        boolean updateResult = judgeService.saveOrUpdate(judge);
        if (!updateResult) { // 出错并不影响主要业务逻辑，可以稍微记录一下即可。
            log.error("修改Judge表失效--------->{}", "修改提交评判为编译中出错");
        }
        // 进行判题操作
        Problem problem = problemService.getById(judge.getPid());

        Judge finalJudge = judgeService.Judge(problem, judge);

        // 更新该次提交
        judgeService.updateById(finalJudge);

        if (finalJudge.getStatus().intValue() != Constants.Judge.STATUS_SUBMITTED_FAILED.getStatus()) {
            // 更新其它表
            judgeService.updateOtherTable(finalJudge.getSubmitId(),
                    finalJudge.getStatus(),
                    finalJudge.getCid(),
                    finalJudge.getUid(),
                    finalJudge.getPid(),
                    finalJudge.getScore());
        }


        return CommonResult.successResponse(finalJudge, "判题机评测完成！");
    }


    @PostMapping(value = "/compile-spj")
    public CommonResult compileSpj(@RequestBody CompileSpj compileSpj) {

        if (!compileSpj.getToken().equals(judgeToken)) {
            return CommonResult.errorResponse("对不起！您使用的判题服务调用凭证不正确！访问受限！", CommonResult.STATUS_ACCESS_DENIED);
        }

        try {
            judgeService.compileSpj(compileSpj.getSpjSrc(), compileSpj.getPid(), compileSpj.getSpjLanguage());
            return CommonResult.successResponse(null, "编译成功！");
        } catch (CompileError compileError) {
            return CommonResult.errorResponse(compileError.getStderr(), CommonResult.STATUS_FAIL);
        } catch (SystemError systemError) {
            return CommonResult.errorResponse(systemError.getStderr(), CommonResult.STATUS_ERROR);
        }
    }

    @PostMapping(value = "/remote-judge")
    public CommonResult remoteJudge(@RequestBody ToJudge toJudge) {

        if (!openRemoteJudge) {
            return CommonResult.errorResponse("对不起！该判题服务器未开启远程虚拟判题功能！",CommonResult.STATUS_ACCESS_DENIED);
        }

        if (!toJudge.getToken().equals(judgeToken)) {
            return CommonResult.errorResponse("对不起！您使用的判题服务调用凭证不正确！访问受限！", CommonResult.STATUS_ACCESS_DENIED);
        }


        if (toJudge.getJudge() == null) {
            return CommonResult.errorResponse("请求参数不能为空！");
        }

        Long submitId = toJudge.getJudge().getSubmitId();
        String uid = toJudge.getJudge().getUid();
        Long cid = toJudge.getJudge().getCid();
        Long pid = toJudge.getJudge().getPid();
        String username = toJudge.getUsername();
        String password = toJudge.getPassword();
        String[] source = toJudge.getRemoteJudge().split("-");
        String remotePid = source[1];
        String remoteJudge = source[0];
        String userCode = toJudge.getJudge().getCode();
        String language = toJudge.getJudge().getLanguage();

        // 调用远程判题
        remoteJudgeToSubmit.sendTask(username, password, remoteJudge, remotePid, submitId, uid, cid, pid, language, userCode);
        return CommonResult.successResponse(null, "提交成功");
    }
}
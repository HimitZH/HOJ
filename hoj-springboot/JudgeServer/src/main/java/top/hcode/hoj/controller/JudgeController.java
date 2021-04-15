package top.hcode.hoj.controller;


import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.CommonResult;
import top.hcode.hoj.common.exception.CompileError;
import top.hcode.hoj.common.exception.SystemError;
import top.hcode.hoj.pojo.entity.*;
import top.hcode.hoj.remoteJudge.RemoteJudgeToSubmit;
import top.hcode.hoj.service.impl.*;
import top.hcode.hoj.util.Constants;

import java.util.Date;


/**
 * @Author: Himit_ZH
 * @Date: 2020/10/29 22:22
 * @Description: 处理代码提交
 */
@RestController
@Slf4j
@RefreshScope
public class JudgeController {

    @Autowired
    private JudgeServiceImpl judgeService;

    @Autowired
    private ProblemServiceImpl problemService;

    @Autowired
    private SystemConfigServiceImpl systemConfigService;

    @Autowired
    private RemoteJudgeToSubmit remoteJudgeToSubmit;


    @Value("${hoj.judge.token}")
    private String judgeToken;

    @Value("${hoj-judge-server.max-task-num}")
    private Integer maxTaskNum;

    @Value("${hoj-judge-server.name}")
    private String name;

    @RequestMapping("/version")
    public CommonResult getVersion() {

        return CommonResult.successResponse(MapUtil.builder()
                        .put("version", "1.1.0")
                        .put("currentTime", new Date())
                        .put("judgeServerName", name)
                        .put("cpu", Runtime.getRuntime().availableProcessors())
                        .put("maxTaskNum", maxTaskNum)
                        .map(),
                "运行正常"
        );
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

        // 更新其它表
        judgeService.updateOtherTable(finalJudge.getSubmitId(),
                finalJudge.getStatus(),
                finalJudge.getCid(),
                finalJudge.getUid(),
                finalJudge.getPid(),
                finalJudge.getScore());


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
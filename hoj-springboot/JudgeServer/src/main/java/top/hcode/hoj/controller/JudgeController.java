package top.hcode.hoj.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.CommonResult;
import top.hcode.hoj.common.exception.SystemError;
import top.hcode.hoj.pojo.entity.judge.CompileDTO;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.entity.judge.ToJudge;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.remoteJudge.RemoteJudgeContext;
import top.hcode.hoj.service.impl.*;
import top.hcode.hoj.util.Constants;


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
    private RemoteJudgeContext remoteJudgeContext;

    @Value("${hoj.judge.token}")
    private String judgeToken;

    @Value("${hoj-judge-server.name}")
    private String name;

    @Value("${hoj-judge-server.remote-judge.open}")
    private Boolean openRemoteJudge;

    @Autowired
    private JudgeServerServiceImpl judgeServerService;

    @RequestMapping("/version")
    public CommonResult getVersion() {
        return CommonResult.successResponse(judgeServerService.getJudgeServerInfo(), "运行正常");
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
        judgeService.updateById(judge);
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
                    finalJudge.getScore(),
                    finalJudge.getTime());
        }


        return CommonResult.successResponse(finalJudge, "判题机评测完成！");
    }


    @PostMapping(value = "/compile-spj")
    public CommonResult compileSpj(@RequestBody CompileDTO compileDTO) {

        if (!compileDTO.getToken().equals(judgeToken)) {
            return CommonResult.errorResponse("对不起！您使用的判题服务调用凭证不正确！访问受限！", CommonResult.STATUS_ACCESS_DENIED);
        }

        try {
            judgeService.compileSpj(compileDTO.getCode(), compileDTO.getPid(), compileDTO.getLanguage(), compileDTO.getExtraFiles());
            return CommonResult.successResponse(null, "编译成功！");
        } catch (SystemError systemError) {
            return CommonResult.errorResponse(systemError.getStderr(), CommonResult.STATUS_ERROR);
        }
    }

    @PostMapping(value = "/compile-interactive")
    public CommonResult compileInteractive(@RequestBody CompileDTO compileDTO) {

        if (!compileDTO.getToken().equals(judgeToken)) {
            return CommonResult.errorResponse("对不起！您使用的判题服务调用凭证不正确！访问受限！", CommonResult.STATUS_ACCESS_DENIED);
        }

        try {
            judgeService.compileInteractive(compileDTO.getCode(), compileDTO.getPid(), compileDTO.getLanguage(), compileDTO.getExtraFiles());
            return CommonResult.successResponse(null, "编译成功！");
        } catch (SystemError systemError) {
            return CommonResult.errorResponse(systemError.getStderr(), CommonResult.STATUS_ERROR);
        }
    }

    @PostMapping(value = "/remote-judge")
    public CommonResult remoteJudge(@RequestBody ToJudge toJudge) {

        if (!openRemoteJudge) {
            return CommonResult.errorResponse("对不起！该判题服务器未开启远程虚拟判题功能！", CommonResult.STATUS_ACCESS_DENIED);
        }

        if (!toJudge.getToken().equals(judgeToken)) {
            return CommonResult.errorResponse("对不起！您使用的判题服务调用凭证不正确！访问受限！", CommonResult.STATUS_ACCESS_DENIED);
        }


        if (toJudge.getJudge() == null) {
            return CommonResult.errorResponse("请求参数不能为空！");
        }

        remoteJudgeContext.judge(toJudge);

        return CommonResult.successResponse(null, "提交成功");
    }
}
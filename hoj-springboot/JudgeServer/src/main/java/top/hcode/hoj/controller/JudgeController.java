package top.hcode.hoj.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.CommonResult;
import top.hcode.hoj.common.exception.CompileError;
import top.hcode.hoj.common.exception.SystemError;
import top.hcode.hoj.pojo.entity.*;
import top.hcode.hoj.service.impl.*;
import top.hcode.hoj.util.Constants;
import top.hcode.hoj.util.IpUtils;


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
    private ProblemCountServiceImpl problemCountService;

    @Autowired
    private UserAcproblemServiceImpl userAcproblemService;

    @Autowired
    private UserRecordServiceImpl userRecordService;

    @Autowired
    private ContestRecordServiceImpl contestRecordService;

    @Autowired
    private ContestServiceImpl contestService;

    @Autowired
    private SystemConfigServiceImpl systemConfigService;

    @Value("${hoj.judge.token}")
    private String judgeToken;

    @Value("${hoj-judger.ip}")
    private String ip;

    @PostMapping(value = "/judge")
    public CommonResult submitProblemJudge(@RequestBody ToJudge toJudge) {
        if (!toJudge.getToken().equals(judgeToken)) {
            return CommonResult.errorResponse("对不起！您使用的判题服务调用凭证不正确！访问受限！", CommonResult.STATUS_ACCESS_DENIED);
        }

        Judge judge = toJudge.getJudge();
        if (judge == null || judge.getSubmitId() == null || judge.getUid() == null || judge.getPid() == null) {
            return CommonResult.errorResponse("参数错误！");
        }

        // 当前判题任务数+1
        systemConfigService.updateJudgeTaskNum(true);

        judge.setStatus(Constants.Judge.STATUS_COMPILING.getStatus()); // 标志该判题过程进入编译阶段
        // 获取当前判题系统所在ip写入数据库
        if (ip.equals("-1")) {
            judge.setJudger(IpUtils.getLocalIpv4Address());
        } else {
            judge.setJudger(ip);
        }
        boolean updateResult = judgeService.saveOrUpdate(judge);
        if (!updateResult) { // 出错并不影响主要业务逻辑，可以稍微记录一下即可。
            log.error("修改Judge表失效--------->{}", "修改提交评判为编译中出错");
        }
        // 进行判题操作
        Problem problem = problemService.getById(judge.getPid());

        Judge finalJudge = judgeService.Judge(problem, judge);

        // 更新该次提交
        judgeService.updateById(finalJudge);

        if (finalJudge.getCid() == 0) { // 非比赛提交

            // 如果是AC,就更新user_acproblem表,
            if (finalJudge.getStatus().intValue() == Constants.Judge.STATUS_ACCEPTED.getStatus()) {
                userAcproblemService.saveOrUpdate(new UserAcproblem()
                        .setPid(finalJudge.getPid())
                        .setUid(finalJudge.getUid())
                        .setSubmitId(finalJudge.getSubmitId())
                );
            }
            // 比赛的提交不纳入，更新该提交对应题目的数据
            problemCountService.updateCount(finalJudge.getStatus(), finalJudge);


            // 如果是非比赛提交，且为OI题目的提交，需要判断是否更新用户得分
            if (finalJudge.getScore() != null) {
                userRecordService.updateRecord(finalJudge);
            }

        } else { //如果是比赛提交

            Contest contest = contestService.getById(finalJudge.getCid());
            if (contest == null) {
                log.error("判题机出错----------->{}", "该比赛不存在");
                return CommonResult.errorResponse("该比赛不存在");
            }
            // 每个提交都得记录到contest_record里面,同时需要判断是否为比赛时的提交
            if (contest.getStatus().intValue() == Constants.Contest.STATUS_RUNNING.getCode()) {
                contestRecordService.UpdateContestRecord(finalJudge);
            }

        }
        // 当前判题任务数-1
        systemConfigService.updateJudgeTaskNum(false);
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
            return CommonResult.errorResponse(systemError.getMessage(), CommonResult.STATUS_ERROR);
        }
    }

    @PostMapping(value = "/remote-judge")
    public CommonResult remoteJudge(@RequestBody ToJudge toJudge){
        /**
         * 在此调用远程判题
         */
    }
}
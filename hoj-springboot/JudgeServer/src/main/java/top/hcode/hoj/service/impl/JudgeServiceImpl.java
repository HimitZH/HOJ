package top.hcode.hoj.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.CompileError;
import top.hcode.hoj.common.exception.SystemError;
import top.hcode.hoj.dao.JudgeMapper;
import top.hcode.hoj.judge.*;
import top.hcode.hoj.pojo.entity.Contest;
import top.hcode.hoj.pojo.entity.Judge;
import top.hcode.hoj.pojo.entity.Problem;
import top.hcode.hoj.pojo.entity.UserAcproblem;
import top.hcode.hoj.service.JudgeService;
import top.hcode.hoj.util.Constants;

import java.util.HashMap;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
@Service
@Slf4j
public class JudgeServiceImpl extends ServiceImpl<JudgeMapper, Judge> implements JudgeService {

    public String test() {
        return "test";
    }

    @Autowired
    private JudgeStrategy judgeStrategy;

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

    @Override
    public Judge Judge(Problem problem, Judge judge) {

        // c和c++为一倍时间和空间，其它语言为2倍时间和空间
        if (!judge.getLanguage().equals("C++") && !judge.getLanguage().equals("C")) {
            problem.setTimeLimit(problem.getTimeLimit() * 2);
            problem.setMemoryLimit(problem.getMemoryLimit() * 2);
        }

        HashMap<String, Object> judgeResult = judgeStrategy.judge(problem, judge);

        // 如果是编译失败或者系统错误就有错误提醒
        if (judgeResult.get("code") == Constants.Judge.STATUS_COMPILE_ERROR.getStatus() ||
                judgeResult.get("code") == Constants.Judge.STATUS_SYSTEM_ERROR.getStatus() ||
                judgeResult.get("code") == Constants.Judge.STATUS_RUNTIME_ERROR.getStatus()) {
            judge.setErrorMessage((String) judgeResult.getOrDefault("errMsg", ""));
        }
        // 设置最终结果状态码
        judge.setStatus((Integer) judgeResult.get("code"));
        // 设置最大时间和最大空间不超过题目限制时间和空间
        // kb
        Long memory = (Long) judgeResult.get("memory");
        judge.setMemory(Math.min(memory.intValue(), problem.getMemoryLimit() * 1024));
        // ms
        Long time = (Long) judgeResult.get("time");
        judge.setTime(Math.min(time.intValue(), problem.getTimeLimit()));
        // score
        judge.setScore((Integer) judgeResult.getOrDefault("score", null));

        return judge;
    }

    public Boolean compileSpj(String code, Long pid, String spjLanguage) throws CompileError, SystemError {
        return judgeStrategy.compileSpj(code, pid, spjLanguage);
    }

    @Override
    public void updateOtherTable(Long submitId, Integer status, Long cid, String uid, Long pid, Integer score) {

        if (cid == 0) { // 非比赛提交
            // 如果是AC,就更新user_acproblem表,
            if (status.intValue() == Constants.Judge.STATUS_ACCEPTED.getStatus()) {
                userAcproblemService.saveOrUpdate(new UserAcproblem()
                        .setPid(pid)
                        .setUid(uid)
                        .setSubmitId(submitId)
                );
            }
            // 比赛的提交不纳入，更新该提交对应题目的数据
            problemCountService.updateCount(status, pid);


            // 如果是非比赛提交，且为OI题目的提交，需要判断是否更新用户得分
            if (score != null) {
                userRecordService.updateRecord(uid, score);
            }

        } else { //如果是比赛提交

            Contest contest = contestService.getById(cid);
            if (contest == null) {
                log.error("判题机出错----------->{}", "该比赛不存在");
            }
            // 每个提交都得记录到contest_record里面,同时需要判断是否为比赛时的提交
            if (contest.getStatus().intValue() == Constants.Contest.STATUS_RUNNING.getCode()) {
                contestRecordService.UpdateContestRecord(uid,score,status,submitId, cid);
            }

        }
    }
}

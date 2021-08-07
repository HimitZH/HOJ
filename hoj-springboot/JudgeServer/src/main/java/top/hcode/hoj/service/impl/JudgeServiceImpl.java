package top.hcode.hoj.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import top.hcode.hoj.common.exception.CompileError;
import top.hcode.hoj.common.exception.SystemError;
import top.hcode.hoj.dao.JudgeMapper;
import top.hcode.hoj.judge.*;
import top.hcode.hoj.judge.Compiler;
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
@Slf4j(topic = "hoj")
public class JudgeServiceImpl extends ServiceImpl<JudgeMapper, Judge> implements JudgeService {


    @Autowired
    private JudgeStrategy judgeStrategy;

    @Autowired
    private UserAcproblemServiceImpl userAcproblemService;


    @Autowired
    private ContestRecordServiceImpl contestRecordService;


    @Override
    public Judge Judge(Problem problem, Judge judge) {

        // c和c++为一倍时间和空间，其它语言为2倍时间和空间
        if (!judge.getLanguage().equals("C++") && !judge.getLanguage().equals("C") &&
                !judge.getLanguage().equals("C++ With O2") && !judge.getLanguage().equals("C With O2")) {
            problem.setTimeLimit(problem.getTimeLimit() * 2);
            problem.setMemoryLimit(problem.getMemoryLimit() * 2);
        }

        HashMap<String, Object> judgeResult = judgeStrategy.judge(problem, judge);

        // 如果是编译失败、提交错误或者系统错误就有错误提醒
        if (judgeResult.get("code") == Constants.Judge.STATUS_COMPILE_ERROR.getStatus() ||
                judgeResult.get("code") == Constants.Judge.STATUS_SYSTEM_ERROR.getStatus() ||
                judgeResult.get("code") == Constants.Judge.STATUS_RUNTIME_ERROR.getStatus() ||
                judgeResult.get("code") == Constants.Judge.STATUS_SUBMITTED_FAILED.getStatus()) {
            judge.setErrorMessage((String) judgeResult.getOrDefault("errMsg", ""));
        }
        // 设置最终结果状态码
        judge.setStatus((Integer) judgeResult.get("code"));
        // 设置最大时间和最大空间不超过题目限制时间和空间
        // kb
        Integer memory = (Integer) judgeResult.get("memory");
        judge.setMemory(Math.min(memory, problem.getMemoryLimit() * 1024));
        // ms
        Integer time = (Integer) judgeResult.get("time");
        judge.setTime(Math.min(time, problem.getTimeLimit()));
        // score
        judge.setScore((Integer) judgeResult.getOrDefault("score", null));
        // oi_rank_score
        judge.setOiRankScore((Integer) judgeResult.getOrDefault("oiRankScore", null));

        return judge;
    }

    public Boolean compileSpj(String code, Long pid, String spjLanguage) throws CompileError, SystemError {
        return Compiler.compileSpj(code, pid, spjLanguage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void updateOtherTable(Long submitId, Integer status, Long cid, String uid, Long pid, Integer score, Integer useTime) {

        if (cid == 0) { // 非比赛提交
            // 如果是AC,就更新user_acproblem表,
            if (status.intValue() == Constants.Judge.STATUS_ACCEPTED.getStatus()) {
                userAcproblemService.saveOrUpdate(new UserAcproblem()
                        .setPid(pid)
                        .setUid(uid)
                        .setSubmitId(submitId)
                );
            }

        } else { //如果是比赛提交

            contestRecordService.UpdateContestRecord(uid, score, status, submitId, cid, useTime);
        }
    }

}

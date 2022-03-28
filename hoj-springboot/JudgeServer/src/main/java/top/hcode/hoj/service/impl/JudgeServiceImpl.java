package top.hcode.hoj.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.SystemError;
import top.hcode.hoj.dao.JudgeEntityService;
import top.hcode.hoj.dao.ProblemEntityService;
import top.hcode.hoj.judge.JudgeContext;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.entity.judge.ToJudge;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.remoteJudge.RemoteJudgeContext;
import top.hcode.hoj.service.JudgeService;
import top.hcode.hoj.util.Constants;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/12 15:54
 * @Description:
 */
@Service
@RefreshScope
public class JudgeServiceImpl implements JudgeService {

    @Value("${hoj-judge-server.name}")
    private String name;

    @Resource
    private JudgeEntityService judgeEntityService;

    @Resource
    private ProblemEntityService problemEntityService;

    @Resource
    private JudgeContext judgeContext;

    @Autowired
    private RemoteJudgeContext remoteJudgeContext;

    @Override
    public void judge(Judge judge) {
        judge.setStatus(Constants.Judge.STATUS_COMPILING.getStatus()); // 标志该判题过程进入编译阶段
        // 写入当前判题服务的名字
        judge.setJudger(name);
        judgeEntityService.updateById(judge);
        // 进行判题操作
        Problem problem = problemEntityService.getById(judge.getPid());
        Judge finalJudge = judgeContext.Judge(problem, judge);

        // 更新该次提交
        judgeEntityService.updateById(finalJudge);

        if (finalJudge.getStatus().intValue() != Constants.Judge.STATUS_SUBMITTED_FAILED.getStatus()) {
            // 更新其它表
            judgeContext.updateOtherTable(finalJudge.getSubmitId(),
                    finalJudge.getStatus(),
                    finalJudge.getCid(),
                    finalJudge.getUid(),
                    finalJudge.getPid(),
                    finalJudge.getGid(),
                    finalJudge.getScore(),
                    finalJudge.getTime());
        }
    }

    @Override
    public void remoteJudge(ToJudge toJudge) {
        remoteJudgeContext.judge(toJudge);
    }

    @Override
    public Boolean compileSpj(String code, Long pid, String spjLanguage, HashMap<String, String> extraFiles) throws SystemError {
        return judgeContext.compileSpj(code, pid, spjLanguage, extraFiles);
    }

    @Override
    public Boolean compileInteractive(String code, Long pid, String interactiveLanguage, HashMap<String, String> extraFiles) throws SystemError {
        return judgeContext.compileInteractive(code, pid, interactiveLanguage, extraFiles);
    }
}
package top.hcode.hoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.SystemError;
import top.hcode.hoj.dao.JudgeEntityService;
import top.hcode.hoj.dao.ProblemEntityService;
import top.hcode.hoj.judge.JudgeContext;
import top.hcode.hoj.pojo.dto.TestJudgeReq;
import top.hcode.hoj.pojo.dto.TestJudgeRes;
import top.hcode.hoj.pojo.dto.ToJudgeDTO;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.remoteJudge.RemoteJudgeContext;
import top.hcode.hoj.service.JudgeService;
import top.hcode.hoj.util.Constants;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Objects;

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
        // 标志该判题过程进入编译阶段
        // 写入当前判题服务的名字
        UpdateWrapper<Judge> judgeUpdateWrapper = new UpdateWrapper<>();
        judgeUpdateWrapper.set("status", Constants.Judge.STATUS_COMPILING.getStatus())
                .set("judger", name)
                .eq("submit_id", judge.getSubmitId())
                .ne("status", Constants.Judge.STATUS_CANCELLED.getStatus());
        boolean isUpdatedOk = judgeEntityService.update(judgeUpdateWrapper);

        // 没更新成功，则可能表示该评测被取消 或者 judge记录被删除了，则结束评测
        if (!isUpdatedOk){
            judgeContext.updateOtherTable(judge.getSubmitId(),
                    Constants.Judge.STATUS_CANCELLED.getStatus(),
                    judge.getCid(),
                    judge.getUid(),
                    judge.getPid(),
                    judge.getGid(),
                    null,
                    null);
            return;
        }

        // 进行判题操作
        QueryWrapper<Problem> problemQueryWrapper = new QueryWrapper<>();
        problemQueryWrapper.select("id",
                        "type",
                        "io_score",
                        "difficulty",
                        "judge_mode",
                        "judge_case_mode",
                        "time_limit",
                        "memory_limit",
                        "stack_limit",
                        "user_extra_file",
                        "judge_extra_file",
                        "case_version",
                        "spj_code",
                        "spj_language",
                        "problem_id",
                        "is_remove_end_blank",
                        "is_file_io",
                        "io_read_file_name",
                        "io_write_file_name")
                .eq("id", judge.getPid());
        Problem problem = problemEntityService.getOne(problemQueryWrapper);
        Judge finalJudgeRes = judgeContext.Judge(problem, judge);

        // 更新该次提交
        judgeEntityService.updateById(finalJudgeRes);

        if (!Objects.equals(finalJudgeRes.getStatus(), Constants.Judge.STATUS_SUBMITTED_FAILED.getStatus())) {
            // 更新其它表
            judgeContext.updateOtherTable(finalJudgeRes.getSubmitId(),
                    finalJudgeRes.getStatus(),
                    judge.getCid(),
                    judge.getUid(),
                    judge.getPid(),
                    judge.getGid(),
                    finalJudgeRes.getScore(),
                    finalJudgeRes.getTime());
        }
    }

    @Override
    public TestJudgeRes testJudge(TestJudgeReq testJudgeReq) {
        return judgeContext.testJudge(testJudgeReq);
    }


    @Override
    public void remoteJudge(ToJudgeDTO toJudgeDTO) {
        Judge judge = toJudgeDTO.getJudge();
        UpdateWrapper<Judge> judgeUpdateWrapper = new UpdateWrapper<>();
        judgeUpdateWrapper.set("status", Constants.Judge.STATUS_PENDING.getStatus())
                .set("judger", name)
                .eq("submit_id", judge.getSubmitId())
                .ne("status", Constants.Judge.STATUS_CANCELLED.getStatus());
        boolean isUpdatedOk = judgeEntityService.update(judgeUpdateWrapper);
        // 没更新成功，则可能表示该评测被取消 或者 judge记录被删除了，则结束评测
        if (!isUpdatedOk){
            judgeContext.updateOtherTable(judge.getSubmitId(),
                    Constants.Judge.STATUS_CANCELLED.getStatus(),
                    judge.getCid(),
                    judge.getUid(),
                    judge.getPid(),
                    judge.getGid(),
                    null,
                    null);
            return;
        }
        remoteJudgeContext.judge(toJudgeDTO);
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
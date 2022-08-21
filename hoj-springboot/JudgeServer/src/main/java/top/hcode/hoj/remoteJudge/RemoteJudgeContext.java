package top.hcode.hoj.remoteJudge;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.JudgeEntityService;
import top.hcode.hoj.pojo.dto.ToJudgeDTO;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.remoteJudge.entity.RemoteJudgeDTO;
import top.hcode.hoj.remoteJudge.task.RemoteJudgeFactory;
import top.hcode.hoj.remoteJudge.task.RemoteJudgeStrategy;
import top.hcode.hoj.util.Constants;

import javax.annotation.Resource;

/**
 * @Author: Himit_ZH
 * @Date: 2022/1/29 13:17
 * @Description:
 */
@Service
@Slf4j(topic = "hoj")
public class RemoteJudgeContext {

    @Resource
    private RemoteJudgeToSubmit remoteJudgeToSubmit;

    @Resource
    private RemoteJudgeGetResult remoteJudgeGetResult;

    @Resource
    private JudgeEntityService judgeEntityService;

    public static final boolean openCodeforcesFixServer = false;

    @Async
    public void judge(ToJudgeDTO toJudgeDTO) {
        String[] source = toJudgeDTO.getRemoteJudgeProblem().split("-");
        String remoteOj = source[0];
        String remoteProblemId = source[1];

        RemoteJudgeDTO remoteJudgeDTO = RemoteJudgeDTO.builder()
                .judgeId(toJudgeDTO.getJudge().getSubmitId())
                .uid(toJudgeDTO.getJudge().getUid())
                .cid(toJudgeDTO.getJudge().getCid())
                .pid(toJudgeDTO.getJudge().getPid())
                .gid(toJudgeDTO.getJudge().getGid())
                .username(toJudgeDTO.getUsername())
                .password(toJudgeDTO.getPassword())
                .oj(remoteOj)
                .completeProblemId(remoteProblemId)
                .userCode(toJudgeDTO.getJudge().getCode())
                .language(toJudgeDTO.getJudge().getLanguage())
                .serverIp(toJudgeDTO.getJudgeServerIp())
                .serverPort(toJudgeDTO.getJudgeServerPort())
                .submitId(toJudgeDTO.getJudge().getVjudgeSubmitId())
                .build();

        initProblemId(remoteJudgeDTO);

        Boolean isHasSubmitIdRemoteReJudge = toJudgeDTO.getIsHasSubmitIdRemoteReJudge();

        RemoteJudgeStrategy remoteJudgeStrategy = buildJudgeStrategy(remoteJudgeDTO);
        if (remoteJudgeStrategy != null) {
            if (isHasSubmitIdRemoteReJudge != null && isHasSubmitIdRemoteReJudge) {
                // 拥有远程oj的submitId远程判题的重判
                remoteJudgeGetResult.process(remoteJudgeStrategy);
            } else {
                // 调用远程判题
                boolean isSubmitOk = remoteJudgeToSubmit.process(remoteJudgeStrategy);
                if (isSubmitOk) {
                    remoteJudgeGetResult.process(remoteJudgeStrategy);
                }
            }
        }
    }

    private void initProblemId(RemoteJudgeDTO remoteJudgeDTO){
        switch (remoteJudgeDTO.getOj()){
            case "GYM":
            case "CF":
                if (NumberUtil.isInteger(remoteJudgeDTO.getCompleteProblemId())) {
                    remoteJudgeDTO.setContestId(ReUtil.get("([0-9]+)[0-9]{2}", remoteJudgeDTO.getCompleteProblemId(), 1));
                    remoteJudgeDTO.setProblemNum(ReUtil.get("[0-9]+([0-9]{2})", remoteJudgeDTO.getCompleteProblemId(), 1));
                } else {
                    remoteJudgeDTO.setContestId(ReUtil.get("([0-9]+)[A-Z]{1}[0-9]{0,1}", remoteJudgeDTO.getCompleteProblemId(), 1));
                    remoteJudgeDTO.setProblemNum(ReUtil.get("[0-9]+([A-Z]{1}[0-9]{0,1})", remoteJudgeDTO.getCompleteProblemId(), 1));
                }
                break;
            case "AC":
                String[] arr = remoteJudgeDTO.getCompleteProblemId().split("_");
                remoteJudgeDTO.setContestId(arr[0]);
                remoteJudgeDTO.setProblemNum(arr[1]);
                break;
        }
    }

    private RemoteJudgeStrategy buildJudgeStrategy(RemoteJudgeDTO remoteJudgeDTO) {
        RemoteJudgeStrategy remoteJudgeStrategy = RemoteJudgeFactory.selectJudge(remoteJudgeDTO.getOj());
        if (remoteJudgeStrategy == null) {
            // 更新此次提交状态为系统失败！
            UpdateWrapper<Judge> judgeUpdateWrapper = new UpdateWrapper<>();
            judgeUpdateWrapper.set("status", Constants.Judge.STATUS_SYSTEM_ERROR.getStatus())
                    .set("error_message", "The judge server does not support this oj:" + remoteJudgeDTO.getOj())
                    .eq("submit_id", remoteJudgeDTO.getJudgeId());
            judgeEntityService.update(judgeUpdateWrapper);
            return null;
        }
        remoteJudgeStrategy.setRemoteJudgeDTO(remoteJudgeDTO);
        return remoteJudgeStrategy;
    }

}
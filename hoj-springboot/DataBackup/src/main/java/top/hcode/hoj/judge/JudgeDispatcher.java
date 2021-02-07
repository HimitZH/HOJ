package top.hcode.hoj.judge;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.hcode.hoj.dao.JudgeMapper;
import top.hcode.hoj.pojo.entity.ContestRecord;
import top.hcode.hoj.pojo.entity.Judge;
import top.hcode.hoj.pojo.entity.ProblemCount;
import top.hcode.hoj.pojo.entity.ToJudge;
import top.hcode.hoj.service.impl.ContestRecordServiceImpl;
import top.hcode.hoj.service.impl.JudgeServiceImpl;
import top.hcode.hoj.service.impl.ProblemCountServiceImpl;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.RedisUtils;

/**
 * @Author: Himit_ZH
 * @Date: 2021/2/5 16:44
 * @Description:
 */
@Component
@Slf4j
public class JudgeDispatcher {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private JudgeMapper judgeMapper;

    @Autowired
    private ProblemCountServiceImpl problemCountService;

    @Autowired
    private ContestRecordServiceImpl contestRecordService;

    public void sendTask(Long submitId, Long pid, String token, Boolean isContest) {

        JSONObject task = new JSONObject();
        task.set("submitId", submitId);
        task.set("pid", pid);
        task.set("token", token);
        task.set("isContest", isContest);

        try {
            redisUtils.sendMessage(Constants.Judge.STATUS_JUDGE_WAITING.getName(), JSONUtil.toJsonStr(task));
        } catch (Exception e) {
            log.error("调用redis消息发布异常,此次判题任务判为系统错误--------------->{}", e.getMessage());

            UpdateWrapper<Judge> judgeUpdateWrapper = new UpdateWrapper<>();
            judgeUpdateWrapper.eq("submit_id", submitId)
                    .set("error_message", "The something has gone wrong with the data Backup server. Please report this to administrator.")
                    .set("status", Constants.Judge.STATUS_SYSTEM_ERROR.getStatus());
            judgeMapper.update(null, judgeUpdateWrapper);
            // 更新problem_count 表
            if (!isContest) {
                QueryWrapper<ProblemCount> problemCountQueryWrapper = new QueryWrapper<ProblemCount>();
                problemCountQueryWrapper.eq("pid", pid);
                ProblemCount problemCount = problemCountService.getOne(problemCountQueryWrapper);
                problemCount.setSe(problemCount.getSe() + 1);
                problemCountService.saveOrUpdate(problemCount);
            } else {
                // 更新contest_record表
                UpdateWrapper<ContestRecord> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("submit_id", submitId) // submit_id一定只有一个
                        .set("first_blood", false)
                        .set("status", Constants.Contest.RECORD_NOT_AC_NOT_PENALTY.getCode());
                contestRecordService.update(updateWrapper);
            }
        }
    }
}
package top.hcode.hoj.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.hcode.hoj.dao.JudgeMapper;
import top.hcode.hoj.judger.*;
import top.hcode.hoj.pojo.entity.Judge;
import top.hcode.hoj.pojo.entity.Problem;
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
public class JudgeServiceImpl extends ServiceImpl<JudgeMapper, Judge> implements JudgeService {

    public String test() {
        return "test";
    }

    @Autowired
    private JudgeStrategy judgeStrategy;

    @Override
    public Judge Judge(Problem problem, Judge judge) {

        HashMap<String, Object> judgeResult = judgeStrategy.judge(problem, judge);

        if (judgeResult.get("code") == Constants.Judge.STATUS_COMPILE_ERROR.getStatus() ||
                judgeResult.get("code") == Constants.Judge.STATUS_SYSTEM_ERROR.getStatus()) {
            judge.setStatus(Constants.Judge.STATUS_COMPILE_ERROR.getStatus());
            judge.setErrorMessage((String) judgeResult.get("errMsg"));
        } else {
            judge.setStatus((Integer) judgeResult.get("code"));
            Long memory = (Long) judgeResult.get("memory");
            judge.setMemory(memory.intValue());
            // ms
            Long time = (Long) judgeResult.get("time");
            judge.setTime(time.intValue());
            if (problem.getType() == 0) {
                judge.setScore((Integer) judgeResult.getOrDefault("score", 0));
            }
        }
        return judge;
    }
}

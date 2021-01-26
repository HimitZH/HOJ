package top.hcode.hoj.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
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

    @Override
    public Judge Judge(Problem problem, Judge judge) {

        JudgeStrategy needJudge = getJudge(problem, judge);
        if (needJudge == null) {
            judge.setErrorMessage("Unknown Language!");
            judge.setStatus(Constants.Judge.STATUS_COMPILE_ERROR.getStatus());
            return judge;
        }

        JudgeContext judgeContext = new JudgeContext(needJudge);
        HashMap<String, Object> result = judgeContext.judge();
        if (result.get("code") == Constants.Judge.STATUS_COMPILE_ERROR.getStatus() ||
                result.get("code") == Constants.Judge.STATUS_SYSTEM_ERROR.getStatus()) {
            judge.setStatus(Constants.Judge.STATUS_COMPILE_ERROR.getStatus());
            judge.setErrorMessage((String) result.get("msg"));
        } else {
            judge.setStatus((Integer) result.get("code"));
            Long memory = (Long)result.get("memory"); // 单位为long类型的b
            judge.setMemory(memory.intValue());
            Long time =  (Long)result.get("time");
            judge.setTime(time.intValue());
            if (problem.getType() == 0) {
                judge.setScore((Integer) result.getOrDefault("score", 0));
            }
        }
        return judge;
    }

    public JudgeStrategy getJudge(Problem problem, Judge judge) {
        switch (judge.getLanguage()) {
            case "C":
                return new JudgeC(problem, judge);
            case "C++":
                return new JudgeCPP(problem, judge);
            case "Python3":
                return new JudgePython3(problem, judge);
            case "Python2":
                return new JudgePython2(problem, judge);
            case "Java":
                return new JudgeJava(problem, judge);
            default:
                return null;
        }
    }

}

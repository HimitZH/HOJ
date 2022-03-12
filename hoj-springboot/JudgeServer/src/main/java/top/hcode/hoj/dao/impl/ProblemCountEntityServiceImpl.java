package top.hcode.hoj.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import top.hcode.hoj.mapper.ProblemCountMapper;
import top.hcode.hoj.pojo.entity.problem.ProblemCount;
import top.hcode.hoj.dao.ProblemCountEntityService;
import top.hcode.hoj.util.Constants;


/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
@Service
public class ProblemCountEntityServiceImpl extends ServiceImpl<ProblemCountMapper, ProblemCount> implements ProblemCountEntityService {

    @Autowired
    private ProblemCountMapper problemCountMapper;

    // 默认的事务隔离等级可重复读会产生幻读，读不到新的version数据，所以需要更换等级为读已提交
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    @Async
    public void updateCount(int status, Long pid) {

        // 更新problem_count 表
        QueryWrapper<ProblemCount> problemCountQueryWrapper = new QueryWrapper<ProblemCount>();
        problemCountQueryWrapper.eq("pid", pid);
        ProblemCount problemCount = problemCountMapper.selectOne(problemCountQueryWrapper);
        ProblemCount newProblemCount = getNewProblemCount(status, problemCount);
        newProblemCount.setVersion(problemCount.getVersion());
        int num = problemCountMapper.updateById(newProblemCount);


        if (num == 1) {
            return;
        } else {
            // 进行重试操作
            tryAgainUpdate(status, pid);
        }

    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public boolean tryAgainUpdate(int status, Long pid) {
        boolean retryable;
        int attemptNumber = 0;
        do {
            // 查询最新版本号
            QueryWrapper<ProblemCount> problemCountQueryWrapper = new QueryWrapper<ProblemCount>();
            problemCountQueryWrapper.eq("pid", pid);
            ProblemCount problemCount = problemCountMapper.selectOne(problemCountQueryWrapper);

            // 更新
            ProblemCount newProblemCount = getNewProblemCount(status, problemCount);
            newProblemCount.setVersion(problemCount.getVersion());
            boolean success = problemCountMapper.updateById(newProblemCount) == 1;


            if (success) {
                return true;
            } else {
                attemptNumber++;
                retryable = attemptNumber < 8;
                if (attemptNumber == 8) {
                    log.error("超过最大重试次数");
                    break;
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                }
            }
        } while (retryable);

        return false;
    }

    public ProblemCount getNewProblemCount(int status, ProblemCount oldProblemCount) {

        ProblemCount newProblemCount = new ProblemCount();
        newProblemCount.setPid(oldProblemCount.getPid()).setTotal(oldProblemCount.getTotal() + 1);
        Constants.Judge type = Constants.Judge.getTypeByStatus(status);
        switch (type) {
            case STATUS_ACCEPTED:
                newProblemCount.setAc(oldProblemCount.getAc() + 1);
                break;
            case STATUS_MEMORY_LIMIT_EXCEEDED:
                newProblemCount.setMle(oldProblemCount.getMle() + 1);
                break;
            case STATUS_TIME_LIMIT_EXCEEDED:
                newProblemCount.setTle(oldProblemCount.getTle() + 1);
                break;
            case STATUS_RUNTIME_ERROR:
                newProblemCount.setRe(oldProblemCount.getRe() + 1);
                break;
            case STATUS_PRESENTATION_ERROR:
                newProblemCount.setPe(oldProblemCount.getPe() + 1);
                break;
            case STATUS_COMPILE_ERROR:
                newProblemCount.setCe(oldProblemCount.getCe() + 1);
                break;
            case STATUS_WRONG_ANSWER:
                newProblemCount.setWa(oldProblemCount.getWa() + 1);
                break;
            case STATUS_SYSTEM_ERROR:
                newProblemCount.setSe(oldProblemCount.getSe() + 1);
                break;
            case STATUS_PARTIAL_ACCEPTED:
                newProblemCount.setPa(oldProblemCount.getPa() + 1);
                break;
            default:
                break;
        }
        return newProblemCount;
    }

}

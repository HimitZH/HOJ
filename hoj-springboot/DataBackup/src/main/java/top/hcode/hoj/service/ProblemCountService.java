package top.hcode.hoj.service;

import top.hcode.hoj.pojo.entity.ProblemCount;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
public interface ProblemCountService extends IService<ProblemCount> {
    ProblemCount getContestProblemCount(Long pid, Long cpid, Long cid);
}

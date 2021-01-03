package top.hcode.hoj.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.ProblemCount;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
public interface ProblemCountService extends IService<ProblemCount> {

    void updateCount(int status,long pid);

}

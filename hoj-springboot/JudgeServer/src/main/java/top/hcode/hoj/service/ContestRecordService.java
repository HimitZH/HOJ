package top.hcode.hoj.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.ContestRecord;
import top.hcode.hoj.pojo.entity.Judge;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
public interface ContestRecordService extends IService<ContestRecord> {
    void UpdateContestRecord(Judge judge);
}

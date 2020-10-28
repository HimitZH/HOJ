package top.hcode.hoj.service;

import top.hcode.hoj.pojo.vo.ContestRecordVo;
import top.hcode.hoj.pojo.entity.ContestRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
public interface ContestRecordService extends IService<ContestRecord> {
    List<ContestRecordVo> getContestRecord(long cid);
}

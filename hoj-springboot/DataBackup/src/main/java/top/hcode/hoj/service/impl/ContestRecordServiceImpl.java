package top.hcode.hoj.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import top.hcode.hoj.pojo.vo.ContestRecordVo;
import top.hcode.hoj.pojo.entity.ContestRecord;
import top.hcode.hoj.dao.ContestRecordMapper;
import top.hcode.hoj.service.ContestRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
@Service
public class ContestRecordServiceImpl extends ServiceImpl<ContestRecordMapper, ContestRecord> implements ContestRecordService {

    @Autowired
    private ContestRecordMapper contestRecordMapper;

    @Override
    public List<ContestRecordVo> getContestRecord(long cid) {
        return contestRecordMapper.getContestRecord(cid);
    }
}

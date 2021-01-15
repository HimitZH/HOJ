package top.hcode.hoj.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import top.hcode.hoj.pojo.vo.ACMRankVo;
import top.hcode.hoj.pojo.entity.UserRecord;
import top.hcode.hoj.dao.UserRecordMapper;
import top.hcode.hoj.pojo.vo.OIRankVo;
import top.hcode.hoj.pojo.vo.UserHomeVo;
import top.hcode.hoj.service.UserRecordService;
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
public class UserRecordServiceImpl extends ServiceImpl<UserRecordMapper, UserRecord> implements UserRecordService {

    @Autowired
    private UserRecordMapper userRecordMapper;

    @Override
    public Page<ACMRankVo> getACMRankList(int limit, int currentPage) {

        //新建分页
        Page<ACMRankVo> page =new Page<>(currentPage,limit);

        return page.setRecords(userRecordMapper.getACMRankList(page));
    }

    @Override
    public List<ACMRankVo> getRecent7ACRank() {
        return userRecordMapper.getRecent7ACRank();
    }

    @Override
    public Page<OIRankVo> getOIRankList(int limit, int currentPage) {
        //新建分页
        Page<OIRankVo> page =new Page<>(currentPage,limit);

        return page.setRecords(userRecordMapper.getOIRankList(page));
    }

    @Override
    public UserHomeVo getUserHomeInfo(String uid) {
        return userRecordMapper.getUserHomeInfo(uid);
    }
}

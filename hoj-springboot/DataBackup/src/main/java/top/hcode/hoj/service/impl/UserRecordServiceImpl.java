package top.hcode.hoj.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import top.hcode.hoj.pojo.vo.RankVo;
import top.hcode.hoj.pojo.entity.UserRecord;
import top.hcode.hoj.dao.UserRecordMapper;
import top.hcode.hoj.service.UserRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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
    public Page<RankVo> getRankList(int limit, int currentPage) {

        //新建分页
        Page<RankVo> page =new Page<>(currentPage,limit);

        return page.setRecords(userRecordMapper.getRankList(page));
    }
}

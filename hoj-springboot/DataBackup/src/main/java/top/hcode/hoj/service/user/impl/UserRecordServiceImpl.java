package top.hcode.hoj.service.user.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import top.hcode.hoj.pojo.vo.ACMRankVo;
import top.hcode.hoj.pojo.entity.user.UserRecord;
import top.hcode.hoj.dao.UserRecordMapper;
import top.hcode.hoj.pojo.vo.OIRankVo;
import top.hcode.hoj.pojo.vo.UserHomeVo;
import top.hcode.hoj.service.user.UserRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.RedisUtils;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
@Service
public class UserRecordServiceImpl extends ServiceImpl<UserRecordMapper, UserRecord> implements UserRecordService {

    @Autowired
    private UserRecordMapper userRecordMapper;

    @Autowired
    private RedisUtils redisUtils;

    // 排行榜缓存时间 60s
    private static final long cacheRankSecond = 60;

    @Override
    public IPage<ACMRankVo> getACMRankList(int limit, int currentPage, List<String> uidList) {

        IPage<ACMRankVo> data = null;
        if (!CollectionUtils.isEmpty(uidList)) {
            Page<ACMRankVo> page = new Page<>(currentPage, limit);
            data = userRecordMapper.getACMRankList(page, uidList);
        } else {
            String key = Constants.Account.ACM_RANK_CACHE.getCode() + "_" + limit + "_" + currentPage;
            data = (IPage<ACMRankVo>) redisUtils.get(key);
            if (data == null) {
                Page<ACMRankVo> page = new Page<>(currentPage, limit);
                data = userRecordMapper.getACMRankList(page, null);
                redisUtils.set(key, data, cacheRankSecond);
            }
        }

        return data;
    }


    @Override
    public List<ACMRankVo> getRecent7ACRank() {
        return userRecordMapper.getRecent7ACRank();
    }

    @Override
    public IPage<OIRankVo> getOIRankList(int limit, int currentPage,List<String> uidList) {

        IPage<OIRankVo> data = null;
        if (!CollectionUtils.isEmpty(uidList)){
            Page<OIRankVo> page = new Page<>(currentPage, limit);
            data = userRecordMapper.getOIRankList(page, uidList);
        }else {
            String key = Constants.Account.OI_RANK_CACHE.getCode() + "_" + limit + "_" + currentPage;
            data = (IPage<OIRankVo>) redisUtils.get(key);
            if (data == null) {
                Page<OIRankVo> page = new Page<>(currentPage, limit);
                data = userRecordMapper.getOIRankList(page, null);
                redisUtils.set(key, data, cacheRankSecond);
            }
        }

        return data;
    }

    @Override
    public UserHomeVo getUserHomeInfo(String uid, String username) {
        return userRecordMapper.getUserHomeInfo(uid, username);
    }


}

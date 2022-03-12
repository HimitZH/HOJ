package top.hcode.hoj.dao.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import top.hcode.hoj.pojo.vo.ACMRankVo;
import top.hcode.hoj.pojo.entity.user.UserRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.vo.OIRankVo;
import top.hcode.hoj.pojo.vo.UserHomeVo;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
public interface UserRecordEntityService extends IService<UserRecord> {

    List<ACMRankVo> getRecent7ACRank();

    UserHomeVo getUserHomeInfo(String uid, String username);

    IPage<OIRankVo> getOIRankList(Page<OIRankVo> page, List<String> uidList);

    IPage<ACMRankVo> getACMRankList(Page<ACMRankVo> page, List<String> uidList);

}

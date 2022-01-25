package top.hcode.hoj.service.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
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
public interface UserRecordService extends IService<UserRecord> {

    IPage<ACMRankVo> getACMRankList(int limit, int currentPage, List<String> uidList);

    List<ACMRankVo> getRecent7ACRank();

    IPage<OIRankVo> getOIRankList(int limit, int currentPage,List<String> uidList);

    UserHomeVo getUserHomeInfo(String uid, String username);

}

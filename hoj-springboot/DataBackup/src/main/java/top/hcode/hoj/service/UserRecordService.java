package top.hcode.hoj.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import top.hcode.hoj.pojo.vo.RankVo;
import top.hcode.hoj.pojo.entity.UserRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
public interface UserRecordService extends IService<UserRecord> {

    Page<RankVo> getRankList(int limit, int currentPage);

}

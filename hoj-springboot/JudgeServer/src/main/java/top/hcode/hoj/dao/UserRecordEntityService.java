package top.hcode.hoj.dao;


import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.user.UserRecord;


/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
public interface UserRecordEntityService extends IService<UserRecord> {
    void updateRecord(String uid, Long submitId, Long pid, Integer score);
}

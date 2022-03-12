package top.hcode.hoj.dao.user;

import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.user.Session;

public interface SessionEntityService extends IService<Session> {

    public void checkRemoteLogin(String uid);

}

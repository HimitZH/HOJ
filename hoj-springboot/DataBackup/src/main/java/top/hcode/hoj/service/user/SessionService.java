package top.hcode.hoj.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.user.Session;

public interface SessionService extends IService<Session> {
    public void checkRemoteLogin(String uid);
}

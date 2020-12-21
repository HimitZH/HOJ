package top.hcode.hoj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.SessionMapper;
import top.hcode.hoj.pojo.entity.Session;
import top.hcode.hoj.service.SessionService;

/**
 * @Author: Himit_ZH
 * @Date: 2020/12/3 22:46
 * @Description:
 */
@Service
public class SessionServiceImpl extends ServiceImpl<SessionMapper, Session> implements SessionService {
}
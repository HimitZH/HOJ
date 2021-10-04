package top.hcode.hoj.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import top.hcode.hoj.dao.SessionMapper;
import top.hcode.hoj.pojo.entity.AdminSysNotice;
import top.hcode.hoj.pojo.entity.Session;
import top.hcode.hoj.pojo.entity.UserSysNotice;
import top.hcode.hoj.service.AdminSysNoticeService;
import top.hcode.hoj.service.SessionService;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2020/12/3 22:46
 * @Description:
 */
@Service
public class SessionServiceImpl extends ServiceImpl<SessionMapper, Session> implements SessionService {

    @Resource
    private SessionMapper sessionMapper;

    @Resource
    private AdminSysNoticeServiceImpl adminSysNoticeService;

    @Resource
    private UserSysNoticeServiceImpl userSysNoticeService;

    @Override
    @Async
    @Transactional
    public void checkRemoteLogin(String uid) {
        QueryWrapper<Session> sessionQueryWrapper = new QueryWrapper<>();
        sessionQueryWrapper.eq("uid", uid)
                .orderByDesc("gmt_create")
                .last("limit 2");
        List<Session> sessionList = sessionMapper.selectList(sessionQueryWrapper);
        if (sessionList.size() < 2) {
            return;
        }
        Session nowSession = sessionList.get(0);
        Session lastSession = sessionList.get(1);
        // 如果两次登录的ip不相同，需要发通知给用户
        if (!nowSession.getIp().equals(lastSession.getIp())) {
            AdminSysNotice adminSysNotice = new AdminSysNotice();
            adminSysNotice
                    .setType("Single")
                    .setContent(getRemoteLoginContent(nowSession.getIp(), nowSession.getGmtCreate()))
                    .setTitle("账号异地登录通知(Account Remote Login Notice)")
                    .setAdminId("1")
                    .setState(false)
                    .setRecipientId(uid);
            boolean isSaveOk = adminSysNoticeService.save(adminSysNotice);
            if (isSaveOk) {
                UserSysNotice userSysNotice = new UserSysNotice();
                userSysNotice.setType("Sys")
                        .setSysNoticeId(adminSysNotice.getId())
                        .setRecipientId(uid)
                        .setState(false);
                boolean isOk = userSysNoticeService.save(userSysNotice);
                if (isOk) {
                    adminSysNotice.setState(true);
                    adminSysNoticeService.saveOrUpdate(adminSysNotice);
                }
            }
        }
    }

    private String getRemoteLoginContent(String newIp, Date loginDate) {
        String dateStr = DateUtil.format(loginDate, "yyyy-MM-dd HH:mm:ss");
        StringBuilder sb = new StringBuilder();
        sb.append("亲爱的用户，您好！您的账号于").append(dateStr);
        String addr = null;
        try {
            String res = HttpUtil.get("https://whois.pconline.com.cn/ipJson.jsp?ip=" + newIp + "&json=true");
            JSONObject resJson = JSONUtil.parseObj(res);
            addr = resJson.getStr("addr");
        } catch (Exception ignored) {
        }
        if (!StringUtils.isEmpty(addr)) {
            sb.append("在【")
                    .append(addr)
                    .append("】");
        }
        sb.append("登录，登录IP为：【")
                .append(newIp)
                .append("】，若非本人操作，请立即修改密码。")
                .append("\n\n")
                .append("Hello! Dear user, Your account was logged in in");

        if (!StringUtils.isEmpty(addr)) {
            sb.append(" 【")
                    .append(addr)
                    .append("】 on ")
                    .append(dateStr)
                    .append(". If you do not operate by yourself, please change your password immediately.");
        }

        return sb.toString();
    }
}
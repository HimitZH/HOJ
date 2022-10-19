package top.hcode.hoj.manager.admin.system;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.hcode.hoj.pojo.entity.user.Session;
import top.hcode.hoj.pojo.vo.UserRolesVO;
import top.hcode.hoj.dao.contest.ContestEntityService;
import top.hcode.hoj.dao.judge.JudgeEntityService;
import top.hcode.hoj.dao.user.SessionEntityService;
import top.hcode.hoj.dao.user.UserInfoEntityService;

import java.util.List;
import java.util.Map;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/9 21:44
 * @Description:
 */
@Component
public class DashboardManager {

    @Autowired
    private ContestEntityService contestEntityService;

    @Autowired
    private JudgeEntityService judgeEntityService;

    @Autowired
    private UserInfoEntityService userInfoEntityService;

    @Autowired
    private SessionEntityService sessionEntityService;

    public Session getRecentSession(){
        // 需要获取一下该token对应用户的数据
        org.apache.shiro.session.Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        QueryWrapper<Session> wrapper = new QueryWrapper<Session>().eq("uid",userRolesVo.getUid()).orderByDesc("gmt_create");
        List<Session> sessionList = sessionEntityService.list(wrapper);
        if (sessionList.size()>1){
            return sessionList.get(1);
        }else{
            return sessionList.get(0);
        }
    }

    public Map<Object, Object> getDashboardInfo(){
        int userNum = userInfoEntityService.count();
        int recentContestNum = contestEntityService.getWithinNext14DaysContests().size();
        int todayJudgeNum = judgeEntityService.getTodayJudgeNum();
        return MapUtil.builder()
                        .put("userNum",userNum)
                        .put("recentContestNum",recentContestNum)
                        .put("todayJudgeNum",todayJudgeNum).map();
    }
}
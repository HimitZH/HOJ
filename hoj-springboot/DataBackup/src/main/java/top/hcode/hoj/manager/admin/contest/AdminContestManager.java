package top.hcode.hoj.manager.admin.contest;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.exception.StatusSystemErrorException;
import top.hcode.hoj.dao.contest.ContestEntityService;
import top.hcode.hoj.dao.contest.ContestRegisterEntityService;
import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.pojo.entity.contest.ContestRegister;
import top.hcode.hoj.pojo.vo.AdminContestVo;
import top.hcode.hoj.pojo.vo.ContestAwardConfigVo;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.utils.Constants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/9 11:20
 * @Description:
 */
@Component
public class AdminContestManager {

    @Autowired
    private ContestEntityService contestEntityService;

    @Autowired
    private ContestRegisterEntityService contestRegisterEntityService;

    public IPage<Contest> getContestList(Integer limit, Integer currentPage, String keyword) {

        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;
        IPage<Contest> iPage = new Page<>(currentPage, limit);
        QueryWrapper<Contest> queryWrapper = new QueryWrapper<>();
        // 过滤密码
        queryWrapper.select(Contest.class, info -> !info.getColumn().equals("pwd"));
        if (!StringUtils.isEmpty(keyword)) {
            keyword = keyword.trim();
            queryWrapper
                    .like("title", keyword).or()
                    .like("id", keyword);
        }
        queryWrapper.eq("is_group", false).orderByDesc("start_time");
        return contestEntityService.page(iPage, queryWrapper);
    }

    public AdminContestVo getContest(Long cid) throws StatusFailException, StatusForbiddenException {
        // 获取本场比赛的状态
        Contest contest = contestEntityService.getById(cid);
        if (contest == null) { // 查询不存在
            throw new StatusFailException("查询失败：该比赛不存在,请检查参数cid是否准确！");
        }
        // 获取当前登录的用户
        UserRolesVo userRolesVo = (UserRolesVo) SecurityUtils.getSubject().getSession().getAttribute("userInfo");

        // 是否为超级管理员
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        // 只有超级管理员和比赛拥有者才能操作
        if (!isRoot && !userRolesVo.getUid().equals(contest.getUid())) {
            throw new StatusForbiddenException("对不起，你无权限操作！");
        }
        AdminContestVo adminContestVo = BeanUtil.copyProperties(contest, AdminContestVo.class, "starAccount");
        if (StringUtils.isEmpty(contest.getStarAccount())) {
            adminContestVo.setStarAccount(new ArrayList<>());
        } else {
            try {
                JSONObject jsonObject = JSONUtil.parseObj(contest.getStarAccount());
                List<String> starAccount = jsonObject.get("star_account", List.class);
                adminContestVo.setStarAccount(starAccount);
            } catch (Exception e) {
                adminContestVo.setStarAccount(new ArrayList<>());
            }
        }

        if (contest.getAwardType() != null && contest.getAwardType() != 0) {
            try {
                JSONObject jsonObject = JSONUtil.parseObj(contest.getAwardConfig());
                List<ContestAwardConfigVo> awardConfigList = jsonObject.get("config", List.class);
                adminContestVo.setAwardConfigList(awardConfigList);
            } catch (Exception e) {
                adminContestVo.setAwardConfigList(new ArrayList<>());
            }
        } else {
            adminContestVo.setAwardConfigList(new ArrayList<>());
        }

        return adminContestVo;
    }

    public void deleteContest(Long cid) throws StatusFailException {
        boolean isOk = contestEntityService.removeById(cid);
        /*
        contest的id为其他表的外键的表中的对应数据都会被一起删除！
         */
        if (!isOk) { // 删除成功
            throw new StatusFailException("删除失败");
        }
    }

    public void addContest(AdminContestVo adminContestVo) throws StatusFailException {
        Contest contest = BeanUtil.copyProperties(adminContestVo, Contest.class, "starAccount");
        JSONObject accountJson = new JSONObject();
        if (adminContestVo.getStarAccount() == null) {
            accountJson.set("star_account", new ArrayList<>());
        } else {
            accountJson.set("star_account", adminContestVo.getStarAccount());
        }
        contest.setStarAccount(accountJson.toString());

        if (adminContestVo.getAwardType() != null && adminContestVo.getAwardType() != 0) {
            JSONObject awardConfigJson = new JSONObject();
            List<ContestAwardConfigVo> awardConfigList = adminContestVo.getAwardConfigList();
            awardConfigList.sort(Comparator.comparingInt(ContestAwardConfigVo::getPriority));
            awardConfigJson.set("config", awardConfigList);
            contest.setAwardConfig(awardConfigJson.toString());
        }

        boolean isOk = contestEntityService.save(contest);
        if (!isOk) { // 删除成功
            throw new StatusFailException("添加失败");
        }
    }

    public void cloneContest(Long cid) throws StatusSystemErrorException {
        Contest contest = contestEntityService.getById(cid);
        if (contest == null) {
            throw new StatusSystemErrorException("该比赛不存在，无法克隆！");
        }
        // 获取当前登录的用户
        UserRolesVo userRolesVo = (UserRolesVo) SecurityUtils.getSubject().getSession().getAttribute("userInfo");
        contest.setUid(userRolesVo.getUid())
                .setAuthor(userRolesVo.getUsername())
                .setSource(cid.intValue())
                .setId(null)
                .setGmtCreate(null)
                .setGmtModified(null);
        contest.setTitle(contest.getTitle() + " [Cloned]");
        contestEntityService.save(contest);
    }

    public void updateContest(AdminContestVo adminContestVo) throws StatusForbiddenException, StatusFailException {
        // 获取当前登录的用户
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        // 是否为超级管理员
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");
        // 只有超级管理员和比赛拥有者才能操作
        if (!isRoot && !userRolesVo.getUid().equals(adminContestVo.getUid())) {
            throw new StatusForbiddenException("对不起，你无权限操作！");
        }
        Contest contest = BeanUtil.copyProperties(adminContestVo, Contest.class, "starAccount");

        JSONObject accountJson = new JSONObject();
        accountJson.set("star_account", adminContestVo.getStarAccount());
        contest.setStarAccount(accountJson.toString());

        if (adminContestVo.getAwardType() != null && adminContestVo.getAwardType() != 0) {
            List<ContestAwardConfigVo> awardConfigList = adminContestVo.getAwardConfigList();
            awardConfigList.sort(Comparator.comparingInt(ContestAwardConfigVo::getPriority));
            JSONObject awardConfigJson = new JSONObject();
            awardConfigJson.set("config", awardConfigList);
            contest.setAwardConfig(awardConfigJson.toString());
        }


        Contest oldContest = contestEntityService.getById(contest.getId());
        boolean isOk = contestEntityService.saveOrUpdate(contest);
        if (isOk) {
            if (!contest.getAuth().equals(Constants.Contest.AUTH_PUBLIC.getCode())) {
                if (!Objects.equals(oldContest.getPwd(), contest.getPwd())) { // 改了比赛密码则需要删掉已有的注册比赛用户
                    UpdateWrapper<ContestRegister> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("cid", contest.getId());
                    contestRegisterEntityService.remove(updateWrapper);
                }
            }
        } else {
            throw new StatusFailException("修改失败");
        }
    }

    public void changeContestVisible(Long cid, String uid, Boolean visible) throws StatusFailException, StatusForbiddenException {
        // 获取当前登录的用户
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        // 是否为超级管理员
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");
        // 只有超级管理员和比赛拥有者才能操作
        if (!isRoot && !userRolesVo.getUid().equals(uid)) {
            throw new StatusForbiddenException("对不起，你无权限操作！");
        }

        boolean isOK = contestEntityService.saveOrUpdate(new Contest().setId(cid).setVisible(visible));

        if (!isOK) {
            throw new StatusFailException("修改失败");
        }
    }

}
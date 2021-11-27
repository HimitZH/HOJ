package top.hcode.hoj.service.contest.impl;

import cn.hutool.core.util.ReUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.contest.ContestRegister;
import top.hcode.hoj.pojo.vo.ContestVo;
import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.dao.ContestMapper;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.service.contest.ContestService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.utils.Constants;

import java.util.Date;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
@Service
public class ContestServiceImpl extends ServiceImpl<ContestMapper, Contest> implements ContestService {

    @Autowired
    private ContestMapper contestMapper;

    @Autowired
    private ContestRegisterServiceImpl contestRegisterService;

    @Override
    public Page<ContestVo> getContestList(Integer limit, Integer currentPage, Integer type, Integer status, String keyword) {
        //新建分页
        Page<ContestVo> page = new Page<>(currentPage, limit);

        return page.setRecords(contestMapper.getContestList(page, type, status, keyword));
    }

    @Override
    public ContestVo getContestInfoById(long cid) {
        return contestMapper.getContestInfoById(cid);
    }


    /**
     * @param contest
     * @param userRolesVo
     * @param isRoot
     * @return CommonResult
     * @MethodName checkContestAuth
     * @Description 需要对该比赛做判断，是否处于开始或结束状态才可以获取，同时若是私有赛需要判断是否已注册（比赛管理员包括超级管理员可以直接获取）
     * @Since 2021/1/17
     */
    @Override
    public CommonResult checkContestAuth(Contest contest, UserRolesVo userRolesVo, Boolean isRoot) {

        if (contest == null || !contest.getVisible()) {
            return CommonResult.errorResponse("对不起，该比赛不存在！");
        }

        if (!isRoot && !contest.getUid().equals(userRolesVo.getUid())) { // 若不是比赛管理者

            // 判断一下比赛的状态，还未开始不能查看题目。
            if (contest.getStatus().intValue() != Constants.Contest.STATUS_RUNNING.getCode() &&
                    contest.getStatus().intValue() != Constants.Contest.STATUS_ENDED.getCode()) {
                return CommonResult.errorResponse("比赛还未开始，您无权访问该比赛！", CommonResult.STATUS_FORBIDDEN);
            } else { // 如果是处于比赛正在进行阶段，需要判断该场比赛是否为私有赛，私有赛需要判断该用户是否已注册
                if (contest.getAuth().intValue() == Constants.Contest.AUTH_PRIVATE.getCode()) {
                    QueryWrapper<ContestRegister> registerQueryWrapper = new QueryWrapper<>();
                    registerQueryWrapper.eq("cid", contest.getId()).eq("uid", userRolesVo.getUid());
                    ContestRegister register = contestRegisterService.getOne(registerQueryWrapper);
                    if (register == null) { // 如果数据为空，表示未注册私有赛，不可访问
                        return CommonResult.errorResponse("对不起，请先到比赛首页输入比赛密码进行注册！", CommonResult.STATUS_FORBIDDEN);
                    }

                    if (contest.getOpenAccountLimit()
                            && !checkAccountRule(contest.getAccountLimitRule(), userRolesVo.getUsername())) {
                        return CommonResult.errorResponse("对不起！本次比赛只允许特定账号规则的用户参赛！", CommonResult.STATUS_ACCESS_DENIED);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Boolean isSealRank(String uid, Contest contest, Boolean forceRefresh, Boolean isRoot) {
        // 如果是管理员同时选择强制刷新榜单，则封榜无效
        if (forceRefresh && (isRoot || uid.equals(contest.getUid()))) {
            return false;
        } else if (contest.getSealRank() && contest.getSealRankTime() != null) { // 该比赛开启封榜模式
            Date now = new Date();
            // 如果现在时间处于封榜开始到比赛结束之间，不可刷新榜单
            if (now.after(contest.getSealRankTime()) && now.before(contest.getEndTime())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public CommonResult checkJudgeAuth(Contest contest, String uid) {

        if (contest.getAuth().intValue() == Constants.Contest.AUTH_PRIVATE.getCode() ||
                contest.getAuth().intValue() == Constants.Contest.AUTH_PROTECT.getCode()) {
            QueryWrapper<ContestRegister> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("cid", contest.getId()).eq("uid", uid);
            ContestRegister register = contestRegisterService.getOne(queryWrapper, false);
            // 如果还没注册
            if (register == null) {
                return CommonResult.errorResponse("对不起，请你先注册该比赛，提交代码失败！", CommonResult.STATUS_FAIL);
            }
        }
        return null;
    }

    @Override
    public boolean checkAccountRule(String accountRule, String username) {

        String prefix = ReUtil.get("<prefix>([\\s\\S]*?)</prefix>",
                accountRule, 1);
        String suffix = ReUtil.get("<suffix>([\\s\\S]*?)</suffix>",
                accountRule, 1);
        String start = ReUtil.get("<start>([\\s\\S]*?)</start>",
                accountRule, 1);
        String end = ReUtil.get("<end>([\\s\\S]*?)</end>",
                accountRule, 1);
        String extra = ReUtil.get("<extra>([\\s\\S]*?)</extra>",
                accountRule, 1);

        int startNum = Integer.parseInt(start);
        int endNum = Integer.parseInt(end);

        for (int i = startNum; i <= endNum; i++) {
            if (username.equals(prefix + i + suffix)) {
                return true;
            }
        }
        // 额外账号列表
        if (!StringUtils.isEmpty(extra)) {
            String[] accountList = extra.trim().split(" ");
            for (String account : accountList) {
                if (username.equals(account)){
                    return true;
                }
            }
        }

        return false;
    }
}

package top.hcode.hoj.validator;

import cn.hutool.core.util.ReUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.dao.contest.ContestRegisterEntityService;
import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.pojo.entity.contest.ContestRegister;
import top.hcode.hoj.pojo.vo.AdminContestVO;
import top.hcode.hoj.shiro.AccountProfile;
import top.hcode.hoj.utils.Constants;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/11 20:06
 * @Description:
 */
@Component
public class ContestValidator {

    @Resource
    private ContestRegisterEntityService contestRegisterEntityService;

    @Autowired
    private GroupValidator groupValidator;

    @Resource
    private CommonValidator commonValidator;

    public void validateContest(AdminContestVO adminContestVO) throws StatusFailException {
        commonValidator.validateContent(adminContestVO.getTitle(), "比赛标题", 500);
        commonValidator.validateContentLength(adminContestVO.getDescription(), "比赛描述", 65535);

        if (!Objects.equals(Constants.Contest.TYPE_OI.getCode(), adminContestVO.getType())
                && !Objects.equals(Constants.Contest.TYPE_ACM.getCode(), adminContestVO.getType())) {
            throw new StatusFailException("比赛的赛制必须为ACM(0)、OI(1)！");
        }

        if (Objects.equals(Constants.Contest.TYPE_OI.getCode(), adminContestVO.getType())) {
            if (!Objects.equals(Constants.Contest.OI_RANK_RECENT_SCORE.getName(), adminContestVO.getOiRankScoreType())
                    && !Objects.equals(Constants.Contest.OI_RANK_HIGHEST_SCORE.getName(), adminContestVO.getOiRankScoreType())) {
                throw new StatusFailException("OI比赛排行榜得分类型必须为最近得分(Recent)、最高得分(Highest)！");
            }
        }

        if (!Objects.equals(Constants.Contest.AUTH_PUBLIC.getCode(), adminContestVO.getAuth())
                && !Objects.equals(Constants.Contest.AUTH_PRIVATE.getCode(), adminContestVO.getAuth())
                && !Objects.equals(Constants.Contest.AUTH_PROTECT.getCode(), adminContestVO.getAuth())) {
            throw new StatusFailException("比赛的权限必须为公开赛(0)、私有赛(1)、保护赛(2)！");
        }
    }

    public boolean isSealRank(String uid, Contest contest, Boolean forceRefresh, Boolean isRoot) {
        if (!contest.getSealRank()) {
            return false;
        }
        // 如果是管理员同时选择强制刷新榜单，则封榜无效
        Long gid = contest.getGid();
        boolean isContestAdmin = isRoot || contest.getUid().equals(uid);
        if (forceRefresh && (isContestAdmin || (contest.getIsGroup() && groupValidator.isGroupRoot(uid, gid)))) {
            return false;
        } else if (contest.getSealRank() && contest.getSealRankTime() != null) { // 该比赛开启封榜模式
            Date now = new Date();
            // 如果现在时间处于封榜开始到比赛结束之间
            if (now.after(contest.getSealRankTime()) && now.before(contest.getEndTime())) {
                return true;
            }
            // 或者没有开启赛后自动解除封榜，不可刷新榜单
            return !contest.getAutoRealRank() && now.after(contest.getEndTime());
        }
        return false;
    }


    /**
     * @param contest
     * @param userRolesVo
     * @param isRoot
     * @MethodName validateContestAuth
     * @Description 需要对该比赛做判断，是否处于开始或结束状态才可以获取，同时若是私有赛需要判断是否已注册（比赛管理员包括超级管理员可以直接获取）
     * @Since 2021/1/17
     */
    public void validateContestAuth(Contest contest, AccountProfile userRolesVo, Boolean isRoot) throws StatusFailException, StatusForbiddenException {

        if (contest == null || !contest.getVisible()) {
            throw new StatusFailException("对不起，该比赛不存在！");
        }

        boolean isContestAdmin = isRoot || contest.getUid().equals(userRolesVo.getUid());
        Long gid = contest.getGid();
        // 若是比赛管理者
        if (isContestAdmin || (contest.getIsGroup() && groupValidator.isGroupRoot(userRolesVo.getUid(), gid))) {
            return;
        }

        // 判断一下比赛的状态，还未开始不能查看题目。
        if (contest.getStatus().intValue() != Constants.Contest.STATUS_RUNNING.getCode() &&
                contest.getStatus().intValue() != Constants.Contest.STATUS_ENDED.getCode()) {
            throw new StatusForbiddenException("比赛还未开始，您无权访问该比赛！");
        } else {

            if (contest.getIsGroup() && !groupValidator.isGroupMember(userRolesVo.getUid(), gid)) {
                throw new StatusForbiddenException("对不起，您并非团队内的成员无法参加该团队内的比赛！");
            }

            // 如果是处于比赛正在进行阶段，需要判断该场比赛是否为私有赛，私有赛需要判断该用户是否已注册
            if (contest.getAuth().intValue() == Constants.Contest.AUTH_PRIVATE.getCode()) {
                QueryWrapper<ContestRegister> registerQueryWrapper = new QueryWrapper<>();
                registerQueryWrapper.eq("cid", contest.getId()).eq("uid", userRolesVo.getUid());
                ContestRegister register = contestRegisterEntityService.getOne(registerQueryWrapper);
                if (register == null) { // 如果数据为空，表示未注册私有赛，不可访问
                    throw new StatusForbiddenException("对不起，请先到比赛首页输入比赛密码进行注册！");
                }

                if (contest.getOpenAccountLimit()
                        && !validateAccountRule(contest.getAccountLimitRule(), userRolesVo.getUsername())) {
                    throw new StatusForbiddenException("对不起！本次比赛只允许特定账号规则的用户参赛！");
                }
            }
        }

    }


    public void validateJudgeAuth(Contest contest, String uid) throws StatusForbiddenException {

        if (contest.getAuth().intValue() == Constants.Contest.AUTH_PRIVATE.getCode() ||
                contest.getAuth().intValue() == Constants.Contest.AUTH_PROTECT.getCode()) {
            QueryWrapper<ContestRegister> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("cid", contest.getId()).eq("uid", uid);
            ContestRegister register = contestRegisterEntityService.getOne(queryWrapper, false);
            // 如果还没注册
            if (register == null) {
                throw new StatusForbiddenException("对不起，请你先注册该比赛，提交代码失败！");
            }
        }
    }


    public boolean validateAccountRule(String accountRule, String username) {

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
                if (username.equals(account)) {
                    return true;
                }
            }
        }

        return false;
    }
}
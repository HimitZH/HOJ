package top.hcode.hoj.manager.oj;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.stereotype.Component;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.exception.StatusNotFoundException;
import top.hcode.hoj.dao.contest.ContestEntityService;
import top.hcode.hoj.dao.contest.ContestProblemEntityService;
import top.hcode.hoj.pojo.dto.ContestRankDTO;
import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.pojo.entity.contest.ContestProblem;
import top.hcode.hoj.pojo.vo.ContestOutsideInfoVO;
import top.hcode.hoj.pojo.vo.ContestVO;
import top.hcode.hoj.pojo.vo.UserRolesVO;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.validator.ContestValidator;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/11 20:02
 * @Description:
 */
@Component
public class ContestScoreboardManager {

    @Resource
    private ContestEntityService contestEntityService;

    @Resource
    private ContestProblemEntityService contestProblemEntityService;

    @Resource
    private ContestValidator contestValidator;

    @Resource
    private ContestRankManager contestRankManager;

    public ContestOutsideInfoVO getContestOutsideInfo(Long cid) throws StatusNotFoundException, StatusForbiddenException {

        ContestVO contestInfo = contestEntityService.getContestInfoById(cid);

        if (contestInfo == null) {
            throw new StatusNotFoundException("访问错误：该比赛不存在！");
        }

        if (!contestInfo.getOpenRank()) {
            throw new StatusForbiddenException("本场比赛未开启外榜，禁止访问外榜！");
        }

        // 获取本场比赛的状态
        if (contestInfo.getStatus().equals(Constants.Contest.STATUS_SCHEDULED.getCode())) {
            throw new StatusForbiddenException("本场比赛正在筹备中，禁止访问外榜！");
        }

        contestInfo.setNow(new Date());
        ContestOutsideInfoVO contestOutsideInfoVO = new ContestOutsideInfoVO();
        contestOutsideInfoVO.setContest(contestInfo);

        QueryWrapper<ContestProblem> contestProblemQueryWrapper = new QueryWrapper<>();
        contestProblemQueryWrapper.eq("cid", cid).orderByAsc("display_id");
        List<ContestProblem> contestProblemList = contestProblemEntityService.list(contestProblemQueryWrapper);
        contestOutsideInfoVO.setProblemList(contestProblemList);

        return contestOutsideInfoVO;
    }


    public IPage getContestOutsideScoreboard(ContestRankDTO contestRankDto) throws StatusFailException, StatusForbiddenException {

        Long cid = contestRankDto.getCid();
        List<String> concernedList = contestRankDto.getConcernedList();
        Boolean removeStar = contestRankDto.getRemoveStar();
        Boolean forceRefresh = contestRankDto.getForceRefresh();

        if (cid == null) {
            throw new StatusFailException("错误：比赛id不能为空");
        }
        if (removeStar == null) {
            removeStar = false;
        }
        if (forceRefresh == null) {
            forceRefresh = false;
        }

        // 获取本场比赛的状态
        Contest contest = contestEntityService.getById(cid);

        if (contest == null) {
            throw new StatusFailException("访问错误：该比赛不存在！");
        }

        if (!contest.getOpenRank()) {
            throw new StatusForbiddenException("本场比赛未开启外榜，禁止访问外榜！");
        }

        if (contest.getStatus().equals(Constants.Contest.STATUS_SCHEDULED.getCode())) {
            throw new StatusForbiddenException("本场比赛正在筹备中，禁止访问外榜！");
        }

        // 获取当前登录的用户
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        // 超级管理员或者该比赛的创建者，则为比赛管理者
        boolean isRoot = false;
        String currentUid = null;

        if (userRolesVo != null) {
            currentUid = userRolesVo.getUid();
            isRoot = SecurityUtils.getSubject().hasRole("root");
            // 不是比赛创建者或者超管无权限开启强制实时榜单
            if (!isRoot && !contest.getUid().equals(currentUid)) {
                forceRefresh = false;
            }
        }


        Integer currentPage = contestRankDto.getCurrentPage();
        Integer limit = contestRankDto.getLimit();
        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 50;

        // 校验该比赛是否开启了封榜模式，超级管理员和比赛创建者可以直接看到实际榜单
        boolean isOpenSealRank = contestValidator.isSealRank(currentUid, contest, forceRefresh, isRoot);

        if (contest.getType().intValue() == Constants.Contest.TYPE_ACM.getCode()) {


            // 获取ACM比赛排行榜外榜
            return contestRankManager.getACMContestScoreboard(isOpenSealRank,
                    removeStar,
                    contest,
                    null,
                    concernedList,
                    contestRankDto.getExternalCidList(),
                    currentPage,
                    limit,
                    contestRankDto.getKeyword(),
                    !forceRefresh,
                    15L); // 默认15s缓存

        } else {
            // 获取OI比赛排行榜外榜
            return contestRankManager.getOIContestScoreboard(isOpenSealRank,
                    removeStar,
                    contest,
                    null,
                    concernedList,
                    contestRankDto.getExternalCidList(),
                    currentPage,
                    limit,
                    contestRankDto.getKeyword(),
                    !forceRefresh,
                    15L); // 默认15s缓存
        }
    }
}
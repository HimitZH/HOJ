package top.hcode.hoj.service.contest;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.vo.ContestVo;
import top.hcode.hoj.pojo.entity.contest.Contest;
import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.vo.UserRolesVo;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
public interface ContestService extends IService<Contest> {

    List<ContestVo> getWithinNext14DaysContests();

    Page<ContestVo> getContestList(Integer limit, Integer currentPage, Integer type, Integer status, String keyword);

    ContestVo getContestInfoById(long cid);

    CommonResult checkContestAuth(Contest contest, UserRolesVo userRolesVo, Boolean isRoot);

    Boolean isSealRank(String uid, Contest contest, Boolean forceRefresh, Boolean isRoot);

    CommonResult checkJudgeAuth(Contest contest, String uid);

    boolean checkAccountRule(String accountRule, String username);
}

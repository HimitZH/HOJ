package top.hcode.hoj.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.ToJudgeDto;
import top.hcode.hoj.pojo.vo.ContestVo;
import top.hcode.hoj.pojo.entity.Contest;
import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.vo.UserRolesVo;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
public interface ContestService extends IService<Contest> {
    Page<ContestVo> getContestList(Integer limit, Integer currentPage, Integer type, Integer status, String keyword);

    ContestVo getContestInfoById(long cid);

    CommonResult checkContestAuth(Contest contest, UserRolesVo userRolesVo, Boolean isRoot);

    Boolean isSealRank(String uid, Contest contest, Boolean forceRefresh, Boolean isRoot);

    CommonResult checkJudgeAuth(String protectContestPwd, Contest contest, String uid);
}

package top.hcode.hoj.service.group.contest;

import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.pojo.vo.AdminContestVo;
import top.hcode.hoj.pojo.vo.ContestVo;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
public interface GroupContestService {

    public CommonResult<IPage<ContestVo>> getContestList(Integer limit, Integer currentPage, Long gid);

    public CommonResult<IPage<Contest>> getAdminContestList(Integer limit, Integer currentPage, Long gid);

    public CommonResult<AdminContestVo> getContest(Long cid);

    public CommonResult<Void> addContest(AdminContestVo adminContestVo);

    public CommonResult<Void> updateContest(AdminContestVo adminContestVo);

    public CommonResult<Void> deleteContest(Long cid);

    public CommonResult<Void> changeContestVisible(Long cid, Boolean visible);

}

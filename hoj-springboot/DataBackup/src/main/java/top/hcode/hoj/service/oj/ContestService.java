package top.hcode.hoj.service.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.ContestPrintDto;
import top.hcode.hoj.pojo.dto.ContestRankDto;
import top.hcode.hoj.pojo.dto.RegisterContestDto;
import top.hcode.hoj.pojo.dto.UserReadContestAnnouncementDto;
import top.hcode.hoj.pojo.entity.common.Announcement;
import top.hcode.hoj.pojo.vo.*;

import java.util.List;

public interface ContestService {

    public CommonResult<IPage<ContestVo>> getContestList(Integer limit, Integer currentPage, Integer status, Integer type, String keyword);

    public CommonResult<ContestVo> getContestInfo(Long cid);

    public CommonResult<Void> toRegisterContest(RegisterContestDto registerContestDto);

    public CommonResult<AccessVo> getContestAccess(Long cid);

    public CommonResult<List<ContestProblemVo>> getContestProblem(Long cid);

    public CommonResult<ProblemInfoVo> getContestProblemDetails(Long cid, String displayId);

    public CommonResult<IPage<JudgeVo>> getContestSubmissionList(Integer limit,
                                                                 Integer currentPage,
                                                                 Boolean onlyMine,
                                                                 String displayId,
                                                                 Integer searchStatus,
                                                                 String searchUsername,
                                                                 Long searchCid,
                                                                 Boolean beforeContestSubmit,
                                                                 Boolean completeProblemID);

    public CommonResult<IPage> getContestRank(ContestRankDto contestRankDto);

    public CommonResult<IPage<AnnouncementVo>> getContestAnnouncement(Long cid, Integer limit, Integer currentPage);

    public CommonResult<List<Announcement>> getContestUserNotReadAnnouncement(UserReadContestAnnouncementDto userReadContestAnnouncementDto);

    public CommonResult<Void> submitPrintText(ContestPrintDto contestPrintDto);

}

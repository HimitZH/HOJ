package top.hcode.hoj.service.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.ContestPrintDTO;
import top.hcode.hoj.pojo.dto.ContestRankDTO;
import top.hcode.hoj.pojo.dto.RegisterContestDTO;
import top.hcode.hoj.pojo.dto.UserReadContestAnnouncementDTO;
import top.hcode.hoj.pojo.entity.common.Announcement;
import top.hcode.hoj.pojo.vo.*;

import java.util.List;

public interface ContestService {

    public CommonResult<IPage<ContestVO>> getContestList(Integer limit, Integer currentPage, Integer status, Integer type, String keyword);

    public CommonResult<ContestVO> getContestInfo(Long cid);

    public CommonResult<Void> toRegisterContest(RegisterContestDTO registerContestDto);

    public CommonResult<AccessVO> getContestAccess(Long cid);

    public CommonResult<List<ContestProblemVO>> getContestProblem(Long cid, Boolean isContainsContestEndJudge);

    public CommonResult<ProblemInfoVO> getContestProblemDetails(Long cid, String displayId, Boolean isContainsContestEndJudge);

    public CommonResult<IPage<JudgeVO>> getContestSubmissionList(Integer limit,
                                                                 Integer currentPage,
                                                                 Boolean onlyMine,
                                                                 String displayId,
                                                                 Integer searchStatus,
                                                                 String searchUsername,
                                                                 Long searchCid,
                                                                 Boolean beforeContestSubmit,
                                                                 Boolean completeProblemID,
                                                                 Boolean isContainsContestEndJudge);

    public CommonResult<IPage> getContestRank(ContestRankDTO contestRankDto);

    public CommonResult<IPage<AnnouncementVO>> getContestAnnouncement(Long cid, Integer limit, Integer currentPage);

    public CommonResult<List<Announcement>> getContestUserNotReadAnnouncement(UserReadContestAnnouncementDTO userReadContestAnnouncementDto);

    public CommonResult<Void> submitPrintText(ContestPrintDTO contestPrintDto);

}

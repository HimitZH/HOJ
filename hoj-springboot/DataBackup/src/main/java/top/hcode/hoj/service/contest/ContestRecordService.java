package top.hcode.hoj.service.contest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.ToJudgeDto;
import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.entity.user.UserInfo;
import top.hcode.hoj.pojo.vo.ACMContestRankVo;
import top.hcode.hoj.pojo.entity.contest.ContestRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.vo.ContestRecordVo;
import top.hcode.hoj.pojo.vo.OIContestRankVo;
import top.hcode.hoj.pojo.vo.UserRolesVo;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
public interface ContestRecordService extends IService<ContestRecord> {

    CommonResult submitContestProblem(ToJudgeDto judgeDto, UserRolesVo userRolesVo, Judge judge);

    IPage<ContestRecord> getACInfo(Integer currentPage,
                                   Integer limit,
                                   Integer status,
                                   Long cid,
                                   String contestCreatorId);

    IPage<ACMContestRankVo> getContestACMRank(Boolean isOpenSealRank,
                                              Boolean removeStar,
                                              String currentUserId,
                                              List<String> concernedList,
                                              Contest contest,
                                              int currentPage,
                                              int limit);

    IPage<OIContestRankVo> getContestOIRank(Boolean isOpenSealRank,
                                            Boolean removeStar,
                                            String currentUserId,
                                            List<String> concernedList,
                                            Contest contest,
                                            int currentPage,
                                            int limit);

    List<ContestRecordVo> getOIContestRecord(Long cid,
                                             String contestAuthor,
                                             Boolean isOpenSealRank,
                                             Date sealTime,
                                             Date startTime,
                                             Date endTime,
                                             String oiRankScoreType);

    List<ContestRecordVo> getACMContestRecord(String username, Long cid);

    List<UserInfo> getSuperAdminList();

    List<ACMContestRankVo> getACMContestScoreboard(Boolean isOpenSealRank, Boolean removeStar,
                                                   Contest contest, String currentUserId,
                                                   List<String> concernedList);

    List<OIContestRankVo> getOIContestScoreboard(Boolean isOpenSealRank, Boolean removeStar,
                                                 Contest contest, String currentUserId,
                                                 List<String> concernedList);

}

package top.hcode.hoj.dao.contest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.pojo.entity.contest.ContestRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.vo.ContestRecordVO;

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
public interface ContestRecordEntityService extends IService<ContestRecord> {

    IPage<ContestRecord> getACInfo(Integer currentPage,
                                   Integer limit,
                                   Integer status,
                                   Long cid,
                                   String contestCreatorId,
                                   Date startTime,
                                   Date endTime);

    List<ContestRecordVO> getOIContestRecord(Contest contest, List<Integer> externalCidList,
                                             Boolean isOpenSealRank, Boolean isContainsAfterContestJudge);

    List<ContestRecordVO> getACMContestRecord(String contestCreatorUid, Long cid, List<Integer> externalCidList, Date startTime);

}

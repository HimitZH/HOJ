package top.hcode.hoj.dao.contest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.pojo.vo.ContestVo;
import top.hcode.hoj.pojo.entity.contest.Contest;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
public interface ContestEntityService extends IService<Contest> {

    List<ContestVo> getWithinNext14DaysContests();

    IPage<ContestVo> getContestList(Integer limit, Integer currentPage, Integer type, Integer status, String keyword);

    ContestVo getContestInfoById(long cid);
}

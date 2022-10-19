package top.hcode.hoj.dao.contest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.pojo.vo.ContestVO;
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

    List<ContestVO> getWithinNext14DaysContests();

    IPage<ContestVO> getContestList(Integer limit, Integer currentPage, Integer type, Integer status, String keyword);

    ContestVO getContestInfoById(long cid);
}

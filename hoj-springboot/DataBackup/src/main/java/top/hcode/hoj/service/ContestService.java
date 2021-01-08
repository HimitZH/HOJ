package top.hcode.hoj.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import top.hcode.hoj.pojo.vo.ContestVo;
import top.hcode.hoj.pojo.entity.Contest;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
public interface ContestService extends IService<Contest> {
    Page<ContestVo> getContestList(Integer limit, Integer currentPage,Integer type,Integer status,String keyword);
    ContestVo getContestInfoById(long cid);
}

package top.hcode.hoj.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import top.hcode.hoj.pojo.vo.ProblemVo;
import top.hcode.hoj.pojo.entity.Problem;
import com.baomidou.mybatisplus.extension.service.IService;


/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */

public interface ProblemService extends IService<Problem> {
    Page<ProblemVo> getProblemList(int limit,int currentPage,long pid,String title);
}

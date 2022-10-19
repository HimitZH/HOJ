package top.hcode.hoj.dao.group;

import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.vo.ProblemVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
public interface GroupProblemEntityService extends IService<Problem> {

    IPage<ProblemVO> getProblemList(int limit, int currentPage, Long gid);

    IPage<Problem> getAdminProblemList(int limit, int currentPage, Long gid);

}

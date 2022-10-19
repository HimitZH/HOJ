package top.hcode.hoj.service.admin.problem;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.ChangeGroupProblemProgressDTO;
import top.hcode.hoj.pojo.entity.problem.Problem;

/**
 * @Author Himit_ZH
 * @Date 2022/4/13
 */
public interface AdminGroupProblemService {

    public CommonResult<IPage<Problem>> getProblemList(Integer currentPage, Integer limit, String keyword, Long gid);

    public CommonResult<Void> changeProgress(ChangeGroupProblemProgressDTO changeGroupProblemProgressDto);
}

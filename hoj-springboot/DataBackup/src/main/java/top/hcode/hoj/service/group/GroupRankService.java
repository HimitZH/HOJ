package top.hcode.hoj.service.group;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.vo.OIRankVO;

public interface GroupRankService {

    public CommonResult<IPage<OIRankVO>> getGroupRankList(Integer limit,
                                                          Integer currentPage,
                                                          String searchUser,
                                                          Integer type,
                                                          Long gid);
}

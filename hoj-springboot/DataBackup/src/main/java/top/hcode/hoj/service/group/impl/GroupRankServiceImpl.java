package top.hcode.hoj.service.group.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.manager.group.GroupRankManager;
import top.hcode.hoj.pojo.vo.OIRankVO;
import top.hcode.hoj.service.group.GroupRankService;

import javax.annotation.Resource;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/10 20:56
 * @Description:
 */
@Service
public class GroupRankServiceImpl implements GroupRankService {

    @Resource
    private GroupRankManager groupRankManager;

    @Override
    public CommonResult<IPage<OIRankVO>> getGroupRankList(Integer limit,
                                                          Integer currentPage,
                                                          String searchUser,
                                                          Integer type,
                                                          Long gid) {
        try {
            return CommonResult.successResponse(groupRankManager.getGroupRankList(limit, currentPage, searchUser, type, gid));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }
}
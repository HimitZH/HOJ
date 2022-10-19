package top.hcode.hoj.service.group.impl;

import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.exception.StatusNotFoundException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.manager.group.GroupManager;
import top.hcode.hoj.pojo.entity.group.Group;
import top.hcode.hoj.pojo.vo.AccessVO;
import top.hcode.hoj.pojo.vo.GroupVO;
import top.hcode.hoj.service.group.GroupService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
@Service
public class GroupServiceImpl implements GroupService {

    @Autowired
    private GroupManager groupManager;

    @Override
    public CommonResult<IPage<GroupVO>> getGroupList(Integer limit, Integer currentPage, String keyword, Integer auth, Boolean onlyMine) {
        return CommonResult.successResponse(groupManager.getGroupList(limit, currentPage, keyword, auth, onlyMine));
    }

    @Override
    public CommonResult<Group> getGroup(Long gid) {
        try {
            return CommonResult.successResponse(groupManager.getGroup(gid));
        } catch (StatusNotFoundException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.NOT_FOUND);
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<AccessVO> getGroupAccess(Long gid) {
        try {
            return CommonResult.successResponse(groupManager.getGroupAccess(gid));
        } catch (StatusNotFoundException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.NOT_FOUND);
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FAIL);
        }
    }

    @Override
    public CommonResult<Integer> getGroupAuth(Long gid) {
        return CommonResult.successResponse(groupManager.getGroupAuth(gid));
    }

    @Override
    public CommonResult<Void> addGroup(Group group) {
        try {
            groupManager.addGroup(group);
            return CommonResult.successResponse();
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FAIL);
        }
    }

    @Override
    public CommonResult<Void> updateGroup(Group group) {
        try {
            groupManager.updateGroup(group);
            return CommonResult.successResponse();
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FAIL);
        }
    }

    @Override
    public CommonResult<Void> deleteGroup(Long gid) {
        try {
            groupManager.deleteGroup(gid);
            return CommonResult.successResponse();
        } catch (StatusNotFoundException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.NOT_FOUND);
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FAIL);
        }
    }
}

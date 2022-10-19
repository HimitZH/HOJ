package top.hcode.hoj.service.group.member.impl;

import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.exception.StatusNotFoundException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.manager.group.member.GroupMemberManager;
import top.hcode.hoj.pojo.entity.group.GroupMember;
import top.hcode.hoj.pojo.vo.GroupMemberVO;
import top.hcode.hoj.service.group.member.GroupMemberService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
@Service
public class GroupMemberServiceImpl implements GroupMemberService {

    @Autowired
    private GroupMemberManager groupMemberManager;

    @Override
    public CommonResult<IPage<GroupMemberVO>> getMemberList(Integer limit, Integer currentPage, String keyword, Integer auth, Long gid) {
        try {
            return CommonResult.successResponse(groupMemberManager.getMemberList(limit, currentPage, keyword, auth, gid));
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        } catch (StatusNotFoundException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.NOT_FOUND);
        }
    }

    @Override
    public CommonResult<IPage<GroupMemberVO>> getApplyList(Integer limit, Integer currentPage, String keyword, Integer auth, Long gid) {
        try {
            return CommonResult.successResponse(groupMemberManager.getApplyList(limit, currentPage, keyword, auth, gid));
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        } catch (StatusNotFoundException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.NOT_FOUND);
        }
    }

    @Override
    public CommonResult<Void> addMember(Long gid, String code, String reason) {
        try {
            groupMemberManager.addMember(gid, code, reason);
            return CommonResult.successResponse();
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FAIL);
        } catch (StatusNotFoundException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.NOT_FOUND);
        }
    }

    @Override
    public CommonResult<Void> updateMember(GroupMember groupMember) {
        try {
            groupMemberManager.updateMember(groupMember);
            return CommonResult.successResponse();
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FAIL);
        } catch (StatusNotFoundException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.NOT_FOUND);
        }
    }


    @Override
    public CommonResult<Void> deleteMember(String uid, Long gid) {
        try {
            groupMemberManager.deleteMember(uid, gid);
            return CommonResult.successResponse();
        } catch (StatusNotFoundException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.NOT_FOUND);
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FAIL);
        }
    }


    @Override
    public CommonResult<Void> exitGroup(Long gid) {
        try {
            groupMemberManager.exitGroup(gid);
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

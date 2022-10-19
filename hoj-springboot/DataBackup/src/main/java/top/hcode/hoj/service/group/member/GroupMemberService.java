package top.hcode.hoj.service.group.member;

import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.group.GroupMember;
import top.hcode.hoj.pojo.vo.GroupMemberVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
public interface GroupMemberService {

    public CommonResult<IPage<GroupMemberVO>> getMemberList(Integer limit, Integer currentPage, String keyword, Integer auth, Long gid);

    public CommonResult<IPage<GroupMemberVO>> getApplyList(Integer limit, Integer currentPage, String keyword, Integer auth, Long gid);

    public CommonResult<Void> addMember(Long gid, String code, String reason);

    public CommonResult<Void> updateMember(GroupMember groupMember);

    public CommonResult<Void> deleteMember(String uid, Long gid);

    public CommonResult<Void> exitGroup(Long gid);
}

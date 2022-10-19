package top.hcode.hoj.manager.group.member;

import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.exception.StatusNotFoundException;
import top.hcode.hoj.dao.group.GroupEntityService;
import top.hcode.hoj.dao.group.GroupMemberEntityService;
import top.hcode.hoj.pojo.entity.group.GroupMember;
import top.hcode.hoj.pojo.entity.group.Group;
import top.hcode.hoj.pojo.vo.GroupMemberVO;
import top.hcode.hoj.pojo.vo.UserRolesVO;
import top.hcode.hoj.validator.GroupValidator;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
@Component
public class GroupMemberManager {

    @Autowired
    private GroupEntityService groupEntityService;

    @Autowired
    private GroupMemberEntityService groupMemberEntityService;


    @Autowired
    private GroupValidator groupValidator;

    public IPage<GroupMemberVO> getMemberList(Integer limit, Integer currentPage, String keyword, Integer auth, Long gid) throws StatusNotFoundException, StatusForbiddenException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }

        if (!groupValidator.isGroupMember(userRolesVo.getUid(), gid) && !isRoot) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;
        if (auth == null || auth < 1) auth = 0;

        if (!StringUtils.isEmpty(keyword)) {
            keyword = keyword.trim();
        }

        return groupMemberEntityService.getMemberList(limit, currentPage, keyword, auth, gid);
    }

    public IPage<GroupMemberVO> getApplyList(Integer limit, Integer currentPage, String keyword, Integer auth, Long gid) throws StatusNotFoundException, StatusForbiddenException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }

        if (!groupValidator.isGroupAdmin(userRolesVo.getUid(), gid) && !isRoot) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;
        if (auth == null || auth < 1) auth = 0;

        if (!StringUtils.isEmpty(keyword)) {
            keyword = keyword.trim();
        }

        return groupMemberEntityService.getApplyList(limit, currentPage, keyword, auth, gid);
    }

    public void addMember(Long gid, String code, String reason) throws StatusFailException, StatusNotFoundException, StatusForbiddenException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Group group = groupEntityService.getById(gid);

        if (group == null) {
            throw new StatusNotFoundException("该团队不存在!");
        }

        if (!isRoot && (group.getStatus() == 1 || !group.getVisible())) {
            throw new StatusNotFoundException("该团队已被封禁或未公开显示！");
        }

        if (group.getAuth() == 3 && !code.equals(group.getCode())) {
            throw new StatusFailException("邀请码错误，请重新尝试！");
        }

        if (group.getAuth() != 1 && !StringUtils.isEmpty(reason) && (reason.length() < 5 || reason.length() > 100)) {
            throw new StatusFailException("申请理由的长度应为 5 到 100！");
        }

        QueryWrapper<GroupMember> groupMemberQueryWrapper = new QueryWrapper<>();
        groupMemberQueryWrapper.eq("uid", userRolesVo.getUid()).eq("gid", gid);

        GroupMember groupMember = groupMemberEntityService.getOne(groupMemberQueryWrapper);

        if (groupMember != null) {
            if (groupMember.getAuth() == 1) {
                throw new StatusForbiddenException("您已申请过，请勿重复申请！");
            } else if (groupMember.getAuth() >= 3) {
                throw new StatusForbiddenException("您已经加入了该团队，请勿再申请！！");
            }
        }

        GroupMember newGroupMember = new GroupMember();
        newGroupMember.setUid(userRolesVo.getUid()).setGid(gid).setReason(reason);
        if (group.getAuth() == 1) {
            newGroupMember.setAuth(3);
        } else {
            newGroupMember.setAuth(1);
        }

        boolean isOk = groupMemberEntityService.save(newGroupMember);

        if (!isOk) {
            throw new StatusFailException("申请失败，请重新尝试！");
        } else {
            if (group.getAuth() == 1) {
                groupMemberEntityService.addWelcomeNoticeToGroupNewMember(gid, group.getName(), userRolesVo.getUid());
            } else {
                groupMemberEntityService.addApplyNoticeToGroupRoot(gid, group.getName(), userRolesVo.getUid());
            }
        }
    }

    public void updateMember(GroupMember groupMemberDto) throws StatusFailException, StatusForbiddenException, StatusNotFoundException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Long gid = groupMemberDto.getGid();

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }

        if (group.getUid().equals(groupMemberDto.getUid())) {
            throw new StatusNotFoundException("对不起，不允许操作团队的Owner权限！");
        }

        boolean isAgreedNewMember = false;

        QueryWrapper<GroupMember> groupMemberQueryWrapper = new QueryWrapper<>();
        groupMemberQueryWrapper.eq("gid", gid)
                .eq("uid", userRolesVo.getUid())
                .in("auth", 4, 5);

        GroupMember currentGroupMember = groupMemberEntityService.getOne(groupMemberQueryWrapper);

        if (!isRoot && currentGroupMember == null) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        QueryWrapper<GroupMember> changeGroupMemberQueryWrapper = new QueryWrapper<>();
        changeGroupMemberQueryWrapper.eq("gid", gid).eq("uid", groupMemberDto.getUid());

        GroupMember changeGroupMember = groupMemberEntityService.getOne(changeGroupMemberQueryWrapper);

        if (changeGroupMember == null) {
            throw new StatusNotFoundException("该用户不在团队中！");
        }

        if (!isRoot && (changeGroupMember.getAuth() >= currentGroupMember.getAuth()
                || groupMemberDto.getAuth() >= currentGroupMember.getAuth())) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        boolean isOk = groupMemberEntityService.updateById(groupMemberDto);
        if (!isOk) {
            throw new StatusFailException("更新失败，请重新尝试！");
        } else {
            if (changeGroupMember.getAuth() <= 2) { // 之前是申请中，则之后通过审批就要发消息
                groupMemberEntityService.addWelcomeNoticeToGroupNewMember(gid, group.getName(), groupMemberDto.getUid());
            }
        }
    }

    public void deleteMember(String uid, Long gid) throws StatusFailException, StatusNotFoundException, StatusForbiddenException {

        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }

        if (userRolesVo.getUid().equals(uid)) {
            throw new StatusNotFoundException("对不起，您无法删除自己！");
        }

        if (group.getUid().equals(uid)) {
            throw new StatusNotFoundException("对不起，不允许删除团队的Owner！");
        }

        QueryWrapper<GroupMember> groupMemberQueryWrapper = new QueryWrapper<>();
        groupMemberQueryWrapper.eq("gid", gid)
                .eq("uid", userRolesVo.getUid())
                .in("auth", 4, 5);

        GroupMember currentGroupMember = groupMemberEntityService.getOne(groupMemberQueryWrapper);

        if (currentGroupMember == null && !isRoot) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }


        QueryWrapper<GroupMember> changeGroupMemberQueryWrapper = new QueryWrapper<>();
        changeGroupMemberQueryWrapper.eq("gid", gid).eq("uid", uid);

        GroupMember changeGroupMember = groupMemberEntityService.getOne(changeGroupMemberQueryWrapper);

        if (changeGroupMember == null) {
            throw new StatusNotFoundException("该用户不在团队中！");
        }

        if (!isRoot && currentGroupMember.getAuth() <= changeGroupMember.getAuth()) {
            throw new StatusNotFoundException("对不起，您无权限操作！");
        }

        boolean isOk = groupMemberEntityService.remove(changeGroupMemberQueryWrapper);
        if (!isOk) {
            throw new StatusFailException("删除失败，请重新尝试！");
        } else {
            groupMemberEntityService.addRemoveNoticeToGroupMember(gid, group.getName(), userRolesVo.getUsername(), uid);
        }
    }

    public void exitGroup(Long gid) throws StatusNotFoundException, StatusForbiddenException, StatusFailException {

        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }

        if (userRolesVo.getUid().equals(group.getUid())) {
            throw new StatusNotFoundException("对不起，作为该团队Owner，您只可以解散团队，不允许退出团队！");
        }

        QueryWrapper<GroupMember> groupMemberQueryWrapper = new QueryWrapper<>();
        groupMemberQueryWrapper.eq("gid", gid)
                .eq("uid", userRolesVo.getUid());

        GroupMember currentGroupMember = groupMemberEntityService.getOne(groupMemberQueryWrapper);

        if (currentGroupMember == null || currentGroupMember.getAuth() == null || currentGroupMember.getAuth() <= 2) {
            throw new StatusForbiddenException("对不起，您并未在当前团队，无法退出！");
        }

        boolean isOk = groupMemberEntityService.remove(groupMemberQueryWrapper);
        if (!isOk) {
            throw new StatusFailException("退出团队失败，请重新尝试！");
        }
    }

}

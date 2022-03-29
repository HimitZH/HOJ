package top.hcode.hoj.manager.group.member;

import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.exception.StatusNotFoundException;
import top.hcode.hoj.dao.group.GroupEntityService;
import top.hcode.hoj.dao.group.GroupMemberEntityService;
import top.hcode.hoj.pojo.entity.group.GroupMember;
import top.hcode.hoj.pojo.entity.group.Group;
import top.hcode.hoj.pojo.vo.GroupMemberVo;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.validator.GroupValidator;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

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

    public IPage<GroupMemberVo> getMemberList(Integer limit, Integer currentPage, String keyword, Integer auth, Long gid) throws StatusNotFoundException, StatusForbiddenException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

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

    public IPage<GroupMemberVo> getApplyList(Integer limit, Integer currentPage, String keyword, Integer auth, Long gid) throws StatusNotFoundException, StatusForbiddenException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

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

    public void addMember(String uid, Long gid, String code, String reason) throws StatusFailException, StatusNotFoundException, StatusForbiddenException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1
                && !isRoot || !group.getVisible()
                && !groupValidator.isGroupMember(userRolesVo.getUid(), gid) && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁或未公开显示！");
        }

        QueryWrapper<GroupMember> groupMemberQueryWrapper = new QueryWrapper<>();
        groupMemberQueryWrapper.eq("uid", uid).eq("gid", gid);

        GroupMember groupMember = groupMemberEntityService.getOne(groupMemberQueryWrapper);

        if (groupMember != null && groupMember.getAuth() == 1) {
            throw new StatusForbiddenException("您已申请过，请勿重复申请！");
        }

        if (groupValidator.isGroupMember(uid, gid)) {
            throw new StatusForbiddenException("您已经加入了该团队！");
        }

        if (group.getAuth() == 3 && !code.equals(group.getCode())) {
            throw new StatusFailException("邀请码错误，请重新尝试！");
        }

        if (group.getAuth() != 1 && !StringUtils.isEmpty(reason) && (reason.length() < 5 || reason.length() > 100)) {
            throw new StatusFailException("申请理由的长度应为 5 到 100！");
        }

        boolean isOk = false;
        if (groupMember != null) {
            UpdateWrapper<GroupMember> groupMemberUpdateWrapper = new UpdateWrapper<>();
            groupMemberUpdateWrapper.eq("uid", uid).eq("gid", gid).set("reason", reason);
            if (group.getAuth() == 1) {
                groupMemberUpdateWrapper.set("auth", 3);
            } else {
                groupMemberUpdateWrapper.set("auth", 1);
            }
            isOk = groupMemberEntityService.update(groupMemberUpdateWrapper);
        } else {
            GroupMember newGroupMember = new GroupMember();
            newGroupMember.setUid(uid).setGid(gid).setReason(reason);
            if (group.getAuth() == 1) {
                newGroupMember.setAuth(3);
            } else {
                newGroupMember.setAuth(1);
            }
            isOk = groupMemberEntityService.save(newGroupMember);
        }
        if (!isOk) {
            throw new StatusFailException("申请失败，请重新尝试！");
        } else {
            groupMemberEntityService.addApplyNoticeToGroupRoot(gid, group.getName(), uid);
        }
    }

    public void updateMember(GroupMember groupMemberDto) throws StatusFailException, StatusForbiddenException, StatusNotFoundException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Long gid = groupMemberDto.getGid();

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }

        QueryWrapper<GroupMember> groupMemberQueryWrapper = new QueryWrapper<>();
        groupMemberQueryWrapper.eq("gid", gid).eq("uid", userRolesVo.getUid()).in("auth", 4, 5);

        GroupMember currentGroupMember = groupMemberEntityService.getOne(groupMemberQueryWrapper);

        if (currentGroupMember == null || (!isRoot && !groupValidator.isGroupOwner(userRolesVo.getUid(), gid))) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        QueryWrapper<GroupMember> changeGroupMemberQueryWrapper = new QueryWrapper<>();
        changeGroupMemberQueryWrapper.eq("gid", gid).eq("uid", groupMemberDto.getUid());

        GroupMember changeGroupMember = groupMemberEntityService.getOne(changeGroupMemberQueryWrapper);

        if (changeGroupMember == null) {
            throw new StatusNotFoundException("该用户不在团队中！");
        }

        if (!isRoot && !groupValidator.isGroupOwner(userRolesVo.getUid(), gid)) {
            if (changeGroupMember.getAuth() >= currentGroupMember.getAuth() || groupMemberDto.getAuth() >= currentGroupMember.getAuth() || groupValidator.isGroupOwner(groupMemberDto.getUid(), gid)) {
                throw new StatusForbiddenException("对不起，您无权限操作！");
            }
        }

        boolean isOk = groupMemberEntityService.updateById(groupMemberDto);
        if (!isOk) {
            throw new StatusFailException("更新失败，请重新尝试！");
        }
    }

    public void deleteMember(String uid, Long gid) throws StatusFailException, StatusNotFoundException, StatusForbiddenException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }

        if (userRolesVo.getUsername().equals(group.getOwner())) {
            if (userRolesVo.getUid().equals(uid)) {
                QueryWrapper<GroupMember> groupMemberQueryWrapper = new QueryWrapper<>();
                groupMemberQueryWrapper.eq("gid", gid).in("auth", 3, 4, 5);
                List<GroupMember> groupMemberList = groupMemberEntityService.list(groupMemberQueryWrapper);
                List<String> groupMemberUidList = groupMemberList.stream()
                        .map(groupMember -> group.getUid())
                        .collect(Collectors.toList());
                boolean isOk = groupEntityService.removeById(gid);
                if (!isOk) {
                    throw new StatusFailException("删除失败，请重新尝试！");
                } else {
                    groupMemberEntityService.addDissolutionNoticeToGroupMember(gid,
                            group.getName(),
                            groupMemberUidList,
                            userRolesVo.getUsername());
                }
                return;
            } else {
                throw new StatusForbiddenException("对不起，您无权移除团队的Owner来解散团队！");
            }
        }

        QueryWrapper<GroupMember> groupMemberQueryWrapper = new QueryWrapper<>();
        groupMemberQueryWrapper.eq("gid", gid).eq("uid", userRolesVo.getUid()).in("auth", 3, 4, 5);

        GroupMember currentGroupMember = groupMemberEntityService.getOne(groupMemberQueryWrapper);


        if (currentGroupMember == null || (!isRoot && !groupValidator.isGroupOwner(userRolesVo.getUid(), gid))) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }


        QueryWrapper<GroupMember> changeGroupMemberQueryWrapper = new QueryWrapper<>();
        changeGroupMemberQueryWrapper.eq("gid", gid).eq("uid", uid);

        GroupMember changeGroupMember = groupMemberEntityService.getOne(changeGroupMemberQueryWrapper);

        if (changeGroupMember == null) {
            throw new StatusNotFoundException("该用户不在团队中！");
        }

        if (!isRoot && !groupValidator.isGroupOwner(userRolesVo.getUid(), gid) && !userRolesVo.getUid().equals(uid)) {
            if (!userRolesVo.getUid().equals(uid) && (changeGroupMember.getAuth() >= currentGroupMember.getAuth()
                    || groupValidator.isGroupOwner(uid, gid))) {
                throw new StatusForbiddenException("对不起，您无权限操作！");
            }
        }

        boolean isOk = groupMemberEntityService.remove(changeGroupMemberQueryWrapper);
        if (!isOk) {
            throw new StatusFailException("删除失败，请重新尝试！");
        } else {
            groupMemberEntityService.addRemoveNoticeToGroupMember(gid, group.getName(), userRolesVo.getUsername(), uid);
        }
    }
}

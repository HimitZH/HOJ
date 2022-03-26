package top.hcode.hoj.validator;

import top.hcode.hoj.dao.group.GroupEntityService;
import top.hcode.hoj.dao.group.GroupMemberEntityService;
import top.hcode.hoj.dao.user.UserInfoEntityService;
import top.hcode.hoj.pojo.entity.group.Group;
import top.hcode.hoj.pojo.entity.group.GroupMember;

import top.hcode.hoj.pojo.entity.user.UserInfo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
@Component
public class GroupValidator {

    @Autowired
    private GroupMemberEntityService groupMemberEntityService;

    @Autowired
    private GroupEntityService groupEntityService;

    @Autowired
    private UserInfoEntityService userInfoEntityService;

    public boolean isGroupMember(String uid, Long gid) {
        QueryWrapper<GroupMember> groupMemberQueryWrapper = new QueryWrapper<>();
        groupMemberQueryWrapper.eq("gid", gid).eq("uid", uid).in("auth", 3, 4, 5);

        GroupMember groupMember = groupMemberEntityService.getOne(groupMemberQueryWrapper);

        return groupMember != null || isGroupOwner(uid, gid);
    }

    public boolean isGroupAdmin(String uid, Long gid) {
        QueryWrapper<GroupMember> groupMemberQueryWrapper = new QueryWrapper<>();
        groupMemberQueryWrapper.eq("gid", gid).eq("uid", uid).in("auth", 4, 5);

        GroupMember groupMember = groupMemberEntityService.getOne(groupMemberQueryWrapper);

        return groupMember != null || isGroupOwner(uid, gid);
    }

    public boolean isGroupRoot(String uid, Long gid) {

        QueryWrapper<GroupMember> groupMemberQueryWrapper = new QueryWrapper<>();
        groupMemberQueryWrapper.eq("gid", gid).eq("uid", uid).in("auth", 5);

        GroupMember groupMember = groupMemberEntityService.getOne(groupMemberQueryWrapper);

        return groupMember != null || isGroupOwner(uid, gid);
    }

    public boolean isGroupOwner(String uid, Long gid) {
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq("uuid", uid);

        UserInfo userInfo = userInfoEntityService.getOne(userInfoQueryWrapper);

        Group group = groupEntityService.getById(gid);

        return group != null && userInfo.getUsername().equals(group.getOwner());
    }
}
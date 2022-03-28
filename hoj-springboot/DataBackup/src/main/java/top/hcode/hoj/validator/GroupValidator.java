package top.hcode.hoj.validator;

import top.hcode.hoj.dao.group.GroupEntityService;
import top.hcode.hoj.dao.group.GroupMemberEntityService;
import top.hcode.hoj.pojo.entity.group.Group;
import top.hcode.hoj.pojo.entity.group.GroupMember;

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

    public boolean isGroupMember(String uid, Long gid) {
        QueryWrapper<GroupMember> groupMemberQueryWrapper = new QueryWrapper<>();
        groupMemberQueryWrapper.eq("gid", gid).eq("uid", uid).in("auth", 3, 4, 5);

        GroupMember groupMember = groupMemberEntityService.getOne(groupMemberQueryWrapper,false);

        return groupMember != null || isGroupOwner(uid, gid);
    }

    public boolean isGroupAdmin(String uid, Long gid) {
        QueryWrapper<GroupMember> groupMemberQueryWrapper = new QueryWrapper<>();
        groupMemberQueryWrapper.eq("gid", gid).eq("uid", uid).in("auth", 4, 5);

        GroupMember groupMember = groupMemberEntityService.getOne(groupMemberQueryWrapper,false);

        return groupMember != null || isGroupOwner(uid, gid);
    }

    public boolean isGroupRoot(String uid, Long gid) {

        QueryWrapper<GroupMember> groupMemberQueryWrapper = new QueryWrapper<>();
        groupMemberQueryWrapper.eq("gid", gid).eq("uid", uid).eq("auth", 5);

        GroupMember groupMember = groupMemberEntityService.getOne(groupMemberQueryWrapper,false);

        return groupMember != null;
    }

    public boolean isGroupOwner(String uid, Long gid) {

        Group group = groupEntityService.getById(gid);

        return group != null && uid.equals(group.getUid());
    }
}
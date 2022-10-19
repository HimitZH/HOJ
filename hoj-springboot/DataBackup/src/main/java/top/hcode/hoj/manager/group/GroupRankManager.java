package top.hcode.hoj.manager.group;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.dao.group.GroupMemberEntityService;
import top.hcode.hoj.dao.user.UserInfoEntityService;
import top.hcode.hoj.dao.user.UserRecordEntityService;
import top.hcode.hoj.pojo.entity.group.GroupMember;
import top.hcode.hoj.pojo.entity.user.UserInfo;
import top.hcode.hoj.pojo.vo.OIRankVO;
import top.hcode.hoj.utils.Constants;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author Himit_ZH
 * @Date 2022/4/22
 */
@Component
public class GroupRankManager {

    @Autowired
    private UserRecordEntityService userRecordEntityService;

    @Autowired
    private UserInfoEntityService userInfoEntityService;

    @Autowired
    private GroupMemberEntityService groupMemberEntityService;


    public IPage<OIRankVO> getGroupRankList(Integer limit,
                                            Integer currentPage,
                                            String searchUser,
                                            Integer type,
                                            Long gid) throws StatusFailException {

        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 30;

        // 根据type查询不同 进行不同排序方式
        String rankType;
        if (type.intValue() == Constants.Contest.TYPE_ACM.getCode()) {
            rankType = Constants.Contest.TYPE_ACM.getName();
        } else if (type.intValue() == Constants.Contest.TYPE_OI.getCode()) {
            rankType = Constants.Contest.TYPE_OI.getName();
        } else {
            throw new StatusFailException("排行榜类型代码不正确，请使用0(ACM),1(OI)！");
        }

        QueryWrapper<GroupMember> groupMemberQueryWrapper = new QueryWrapper<>();
        groupMemberQueryWrapper.select("uid")
                .eq("gid", gid).in("auth", 3, 4, 5);

        List<String> groupMemberUidList = groupMemberEntityService.list(groupMemberQueryWrapper)
                .stream()
                .map(GroupMember::getUid)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(groupMemberUidList)) {
            return new Page<>(currentPage, limit);
        }

        if (!StringUtils.isEmpty(searchUser)) {
            QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
            userInfoQueryWrapper.select("uuid")
                    .eq("status", 0)
                    .in("uuid", groupMemberUidList)
                    .and(wrapper -> wrapper
                            .like("username", searchUser)
                            .or()
                            .like("nickname", searchUser)
                            .or()
                            .like("realname", searchUser));

            List<String> uidList = userInfoEntityService.list(userInfoQueryWrapper)
                    .stream()
                    .map(UserInfo::getUuid)
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(uidList)) {
                return new Page<>(currentPage, limit);
            }
            Page<OIRankVO> page = new Page<>(currentPage, limit);
            return userRecordEntityService.getGroupRankList(page, gid, uidList, rankType, false);
        } else {
            Page<OIRankVO> page = new Page<>(currentPage, limit);
            return userRecordEntityService.getGroupRankList(page, gid, groupMemberUidList, rankType, true);
        }

    }
}

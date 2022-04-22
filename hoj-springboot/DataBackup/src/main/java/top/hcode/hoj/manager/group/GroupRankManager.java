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
import top.hcode.hoj.pojo.vo.OIRankVo;
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


    public IPage<OIRankVo> getGroupRankList(Integer limit,
                                            Integer currentPage,
                                            String searchUser,
                                            Integer type,
                                            Long gid) throws StatusFailException {

        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 30;

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

        IPage<OIRankVo> groupRankList;
        Page<OIRankVo> page = new Page<>(currentPage, limit);
        // 根据type查询不同 进行不同排序方式
        if (type.intValue() == Constants.Contest.TYPE_ACM.getCode()) {
            groupRankList = userRecordEntityService.getGroupRankList(page, gid, groupMemberUidList, Constants.Contest.TYPE_ACM.getName());
        } else if (type.intValue() == Constants.Contest.TYPE_OI.getCode()) {
            groupRankList = userRecordEntityService.getGroupRankList(page, gid, groupMemberUidList, Constants.Contest.TYPE_OI.getName());
        } else {
            throw new StatusFailException("比赛类型代码不正确！");
        }

        if (!StringUtils.isEmpty(searchUser)) {
            QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
            userInfoQueryWrapper.select("uuid")
                    .eq("status", 0)
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
                return groupRankList;
            }
            List<OIRankVo> records = groupRankList.getRecords()
                    .stream()
                    .filter(OIRankVo -> uidList.contains(OIRankVo.getUid()))
                    .collect(Collectors.toList());

            groupRankList.setRecords(records)
                    .setTotal(records.size());
            return groupRankList;
        } else {
            return groupRankList;
        }

    }
}

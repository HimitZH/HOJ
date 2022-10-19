package top.hcode.hoj.service.group;

import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.group.Group;
import top.hcode.hoj.pojo.vo.AccessVO;
import top.hcode.hoj.pojo.vo.GroupVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
public interface GroupService {

    public CommonResult<IPage<GroupVO>> getGroupList(Integer limit, Integer currentPage, String keyword, Integer auth, Boolean onlyMine);

    public CommonResult<Group> getGroup(Long gid);

    public CommonResult<AccessVO> getGroupAccess(Long gid);

    public CommonResult<Integer> getGroupAuth(Long gid);

    public CommonResult<Void> addGroup(Group group);

    public CommonResult<Void> updateGroup(Group group);

    public CommonResult<Void> deleteGroup(Long gid);
}

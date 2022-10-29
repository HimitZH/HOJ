package top.hcode.hoj.controller.group;

import top.hcode.hoj.annotation.AnonApi;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.group.Group;
import top.hcode.hoj.pojo.vo.AccessVO;
import top.hcode.hoj.pojo.vo.GroupVO;
import top.hcode.hoj.service.group.GroupService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
@RestController
@RequestMapping("/api")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @GetMapping("/get-group-list")
    @AnonApi
    public CommonResult<IPage<GroupVO>> getGroupList(@RequestParam(value = "limit", required = false) Integer limit,
                                                     @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                                     @RequestParam(value = "keyword", required = false) String keyword,
                                                     @RequestParam(value = "auth", required = false) Integer auth,
                                                     @RequestParam(value = "onlyMine", required = false) Boolean onlyMine) {
        return groupService.getGroupList(limit, currentPage, keyword, auth, onlyMine);
    }

    @GetMapping("/get-group-detail")
    @RequiresAuthentication
    public CommonResult<Group> getGroup(@RequestParam(value = "gid", required = true) Long gid) {
        return groupService.getGroup(gid);
    }

    @RequiresAuthentication
    @GetMapping("/get-group-access")
    public CommonResult<AccessVO> getGroupAccess(@RequestParam(value = "gid", required = true) Long gid) {
        return groupService.getGroupAccess(gid);
    }

    @RequiresAuthentication
    @GetMapping("/get-group-auth")
    public CommonResult<Integer> getGroupAuth(@RequestParam(value = "gid", required = true) Long gid) {
        return groupService.getGroupAuth(gid);
    }

    @PostMapping("/group")
    @RequiresAuthentication
    @RequiresPermissions("group_add")
    public CommonResult<Void> addGroup(@RequestBody Group group) {
        return groupService.addGroup(group);
    }

    @PutMapping("/group")
    @RequiresAuthentication
    public CommonResult<Void> updateGroup(@RequestBody Group group) {
        return groupService.updateGroup(group);
    }

    @DeleteMapping("/group")
    @RequiresAuthentication
    @RequiresPermissions("group_del")
    public CommonResult<Void> deleteGroup(@RequestParam(value = "gid", required = true) Long gid) {
        return groupService.deleteGroup(gid);
    }
}

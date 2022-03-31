package top.hcode.hoj.controller.group;

import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.discussion.Discussion;
import top.hcode.hoj.service.group.discussion.GroupDiscussionService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
@RestController
@RequiresAuthentication
@RequestMapping("/api/group")
public class GroupDiscussionController {

    @Autowired
    private GroupDiscussionService groupDiscussionService;

    @GetMapping("/get-discussion-list")
    public CommonResult<IPage<Discussion>> getDiscussionList(@RequestParam(value = "limit", required = false) Integer limit,
                                                             @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                                             @RequestParam(value = "gid", required = true) Long gid,
                                                             @RequestParam(value = "pid", required = false) String pid) {
        return groupDiscussionService.getDiscussionList(limit, currentPage, gid, pid);
    }

    @GetMapping("/get-admin-discussion-list")
    public CommonResult<IPage<Discussion>> getAdminDiscussionList(@RequestParam(value = "limit", required = false) Integer limit,
                                                                  @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                                                  @RequestParam(value = "gid", required = true) Long gid) {
        return groupDiscussionService.getAdminDiscussionList(limit, currentPage, gid);
    }

    @PostMapping("/discussion")
    public CommonResult<Void> addDiscussion(@RequestBody Discussion discussion) {
        return groupDiscussionService.addDiscussion(discussion);
    }

    @PutMapping("/discussion")
    public CommonResult<Void> updateDiscussion(@RequestBody Discussion discussion) {
        return groupDiscussionService.updateDiscussion(discussion);
    }

    @DeleteMapping("/discussion")
    public CommonResult<Void> deleteDiscussion(@RequestParam(value = "did", required = true) Long did) {
        return groupDiscussionService.deleteDiscussion(did);
    }

}

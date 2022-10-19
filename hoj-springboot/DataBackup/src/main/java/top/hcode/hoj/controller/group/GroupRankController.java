package top.hcode.hoj.controller.group;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.vo.OIRankVO;
import top.hcode.hoj.service.group.GroupRankService;

/**
 * @Author Himit_ZH
 * @Date 2022/4/22
 */
@RestController
@RequestMapping("/api")
public class GroupRankController {

    @Autowired
    private GroupRankService groupRankService;

    @GetMapping("/get-group-rank-list")
    public CommonResult<IPage<OIRankVO>> getRankList(@RequestParam(value = "limit", required = false) Integer limit,
                                                     @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                                     @RequestParam(value = "searchUser", required = false) String searchUser,
                                                     @RequestParam(value = "gid", required = true) Long gid,
                                                     @RequestParam(value = "type", required = true) Integer type) {
        return groupRankService.getGroupRankList(limit, currentPage, searchUser, type, gid);
    }
}

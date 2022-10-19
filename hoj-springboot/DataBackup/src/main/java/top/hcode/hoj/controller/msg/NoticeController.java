package top.hcode.hoj.controller.msg;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.vo.SysMsgVO;
import top.hcode.hoj.service.msg.NoticeService;

import javax.annotation.Resource;

/**
 * @Author: Himit_ZH
 * @Date: 2021/10/1 20:42
 * @Description: 负责用户的 系统消息模块、我的消息模块
 */
@RestController
@RequestMapping("/api/msg")
public class NoticeController {

    @Resource
    private NoticeService noticeService;

    @RequestMapping(value = "/sys", method = RequestMethod.GET)
    @RequiresAuthentication
    public CommonResult<IPage<SysMsgVO>> getSysNotice(@RequestParam(value = "limit", required = false) Integer limit,
                                                      @RequestParam(value = "currentPage", required = false) Integer currentPage) {

        return noticeService.getSysNotice(limit, currentPage);
    }


    @RequestMapping(value = "/mine", method = RequestMethod.GET)
    @RequiresAuthentication
    public CommonResult<IPage<SysMsgVO>> getMineNotice(@RequestParam(value = "limit", required = false) Integer limit,
                                                       @RequestParam(value = "currentPage", required = false) Integer currentPage) {

        return noticeService.getMineNotice(limit, currentPage);
    }
}
package top.hcode.hoj.service.msg;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.msg.AdminSysNotice;
import top.hcode.hoj.pojo.vo.AdminSysNoticeVo;

import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2021/10/1 20:33
 * @Description:
 */
public interface AdminSysNoticeService extends IService<AdminSysNotice> {
    public IPage<AdminSysNoticeVo> getSysNotice(int limit,int currentPage,String type);

    public void syncNoticeToNewRegisterUser(String uid);

    public void syncNoticeToNewRegisterBatchUser(List<String> uidList);

    public void addSingleNoticeToUser(String adminId, String recipientId, String title, String content,String type);
}
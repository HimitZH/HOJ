package top.hcode.hoj.dao.msg;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.msg.UserSysNotice;
import top.hcode.hoj.pojo.vo.SysMsgVO;

public interface UserSysNoticeEntityService extends IService<UserSysNotice> {

    IPage<SysMsgVO> getSysNotice(int limit, int currentPage, String uid);

    IPage<SysMsgVO> getMineNotice(int limit, int currentPage, String uid);
}
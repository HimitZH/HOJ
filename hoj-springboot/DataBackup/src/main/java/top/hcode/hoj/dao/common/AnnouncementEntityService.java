package top.hcode.hoj.dao.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.pojo.entity.common.Announcement;
import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.vo.AnnouncementVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
public interface AnnouncementEntityService extends IService<Announcement> {

    IPage<AnnouncementVo> getAnnouncementList(int limit, int currentPage, Boolean notAdmin);

    IPage<AnnouncementVo> getContestAnnouncement(Long cid,Boolean notAdmin,int limit, int currentPage);
}

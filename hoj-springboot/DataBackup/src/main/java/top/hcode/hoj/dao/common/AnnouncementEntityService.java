package top.hcode.hoj.dao.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.pojo.entity.common.Announcement;
import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.vo.AnnouncementVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
public interface AnnouncementEntityService extends IService<Announcement> {

    IPage<AnnouncementVO> getAnnouncementList(int limit, int currentPage, Boolean notAdmin);

    IPage<AnnouncementVO> getContestAnnouncement(Long cid, Boolean notAdmin, int limit, int currentPage);
}

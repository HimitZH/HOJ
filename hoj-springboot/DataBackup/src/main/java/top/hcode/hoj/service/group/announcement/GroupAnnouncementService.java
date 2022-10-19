package top.hcode.hoj.service.group.announcement;

import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.common.Announcement;
import top.hcode.hoj.pojo.vo.AnnouncementVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
public interface GroupAnnouncementService {

    public CommonResult<IPage<AnnouncementVO>> getAnnouncementList(Integer limit, Integer currentPage, Long gid);

    public CommonResult<IPage<AnnouncementVO>> getAdminAnnouncementList(Integer limit, Integer currentPage, Long gid);

    public CommonResult<Void> addAnnouncement(Announcement announcement);

    public CommonResult<Void> updateAnnouncement(Announcement announcement);

    public CommonResult<Void> deleteAnnouncement(Long aid);

}

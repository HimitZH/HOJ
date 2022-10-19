package top.hcode.hoj.service.admin.announcement;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.common.Announcement;
import top.hcode.hoj.pojo.vo.AnnouncementVO;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/9 22:11
 * @Description:
 */
public interface AdminAnnouncementService {

    public CommonResult<IPage<AnnouncementVO>> getAnnouncementList(Integer limit, Integer currentPage);

    public CommonResult<Void> deleteAnnouncement(Long aid);

    public CommonResult<Void> addAnnouncement(Announcement announcement);

    public CommonResult<Void> updateAnnouncement(Announcement announcement);
}
package top.hcode.hoj.service.admin.contest;


import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.AnnouncementDTO;
import top.hcode.hoj.pojo.vo.AnnouncementVO;


public interface AdminContestAnnouncementService {

    public CommonResult<IPage<AnnouncementVO>> getAnnouncementList(Integer limit, Integer currentPage, Long cid);

    public CommonResult<Void> deleteAnnouncement(Long aid);

    public CommonResult<Void> addAnnouncement(AnnouncementDTO announcementDto);

    public CommonResult<Void> updateAnnouncement(AnnouncementDTO announcementDto);
}

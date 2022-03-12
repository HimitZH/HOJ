package top.hcode.hoj.service.admin.contest;


import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.AnnouncementDto;
import top.hcode.hoj.pojo.vo.AnnouncementVo;


public interface AdminContestAnnouncementService {

    public CommonResult<IPage<AnnouncementVo>> getAnnouncementList(Integer limit, Integer currentPage, Long cid);

    public CommonResult<Void> deleteAnnouncement(Long aid);

    public CommonResult<Void> addAnnouncement(AnnouncementDto announcementDto);

    public CommonResult<Void> updateAnnouncement(AnnouncementDto announcementDto);
}

package top.hcode.hoj.service.group.contest;

import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.AnnouncementDTO;
import top.hcode.hoj.pojo.vo.AnnouncementVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
public interface GroupContestAnnouncementService {

    public CommonResult<IPage<AnnouncementVO>> getContestAnnouncementList(Integer limit, Integer currentPage, Long cid);

    public CommonResult<Void> addContestAnnouncement(AnnouncementDTO announcementDto);

    public CommonResult<Void> updateContestAnnouncement(AnnouncementDTO announcementDto);

    public CommonResult<Void> deleteContestAnnouncement(Long aid, Long cid);

}

package top.hcode.hoj.dao.group;

import top.hcode.hoj.pojo.entity.common.Announcement;
import top.hcode.hoj.pojo.vo.AnnouncementVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
public interface GroupAnnouncementEntityService extends IService<Announcement> {

    IPage<AnnouncementVo> getAnnouncementList(int limit, int currentPage, Long gid);

    IPage<AnnouncementVo> getAdminAnnouncementList(int limit, int currentPage, Long gid);

}

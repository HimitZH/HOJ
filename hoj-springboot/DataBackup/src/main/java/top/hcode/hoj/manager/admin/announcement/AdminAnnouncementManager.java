package top.hcode.hoj.manager.admin.announcement;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.pojo.entity.common.Announcement;
import top.hcode.hoj.pojo.vo.AnnouncementVO;
import top.hcode.hoj.dao.common.AnnouncementEntityService;
import top.hcode.hoj.pojo.vo.UserRolesVO;


/**
 * @Author: Himit_ZH
 * @Date: 2022/3/9 21:40
 * @Description:
 */
@Component
@Slf4j
public class AdminAnnouncementManager {

    @Autowired
    private AnnouncementEntityService announcementEntityService;

    public IPage<AnnouncementVO> getAnnouncementList(Integer limit, Integer currentPage) {

        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;
        return announcementEntityService.getAnnouncementList(limit, currentPage, false);

    }

    public void deleteAnnouncement(long aid) throws StatusFailException {
        boolean isOk = announcementEntityService.removeById(aid);
        if (!isOk) {
            throw new StatusFailException("删除失败");
        }
        // 获取当前登录的用户
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");
        log.info("[{}],[{}],id:[{}],operatorUid:[{}],operatorUsername:[{}]",
                "Admin_Announcement", "Delete", aid, userRolesVo.getUid(), userRolesVo.getUsername());
    }


    public void addAnnouncement(Announcement announcement) throws StatusFailException {
        boolean isOk = announcementEntityService.save(announcement);
        if (!isOk) {
            throw new StatusFailException("添加失败");
        }
    }

    public void updateAnnouncement(Announcement announcement) throws StatusFailException {
        boolean isOk = announcementEntityService.saveOrUpdate(announcement);
        if (!isOk) {
            throw new StatusFailException("修改失败");
        }
    }
}
package top.hcode.hoj.manager.group.contest;

import org.springframework.transaction.annotation.Transactional;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.exception.StatusNotFoundException;
import top.hcode.hoj.dao.common.AnnouncementEntityService;
import top.hcode.hoj.dao.contest.ContestAnnouncementEntityService;
import top.hcode.hoj.dao.contest.ContestEntityService;
import top.hcode.hoj.dao.group.GroupEntityService;
import top.hcode.hoj.pojo.dto.AnnouncementDto;
import top.hcode.hoj.pojo.entity.common.Announcement;
import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.pojo.entity.contest.ContestAnnouncement;
import top.hcode.hoj.pojo.entity.group.Group;
import top.hcode.hoj.pojo.vo.AnnouncementVo;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.validator.GroupValidator;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
@Component
public class GroupContestAnnouncementManager {

    @Autowired
    private GroupEntityService groupEntityService;

    @Autowired
    private ContestEntityService contestEntityService;

    @Autowired
    private AnnouncementEntityService announcementEntityService;

    @Autowired
    private ContestAnnouncementEntityService contestAnnouncementEntityService;

    @Autowired
    private GroupValidator groupValidator;

    public IPage<AnnouncementVo> getContestAnnouncementList(Integer limit, Integer currentPage, Long cid) throws StatusNotFoundException, StatusForbiddenException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Contest contest = contestEntityService.getById(cid);

        if (contest == null) {
            throw new StatusNotFoundException("?????????????????????");
        }

        Long gid = contest.getGid();

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("????????????????????????????????????");
        }

        if (!userRolesVo.getUid().equals(contest.getUid()) && !isRoot
                && !groupValidator.isGroupRoot(userRolesVo.getUid(), gid)) {
            throw new StatusForbiddenException("?????????????????????????????????");
        }

        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;
        return announcementEntityService.getContestAnnouncement(cid, false, limit, currentPage);
    }

    @Transactional(rollbackFor = Exception.class)
    public void addContestAnnouncement(AnnouncementDto announcementDto) throws StatusNotFoundException, StatusForbiddenException, StatusFailException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Long cid = announcementDto.getCid();

        Contest contest = contestEntityService.getById(cid);

        if (contest == null) {
            throw new StatusNotFoundException("?????????????????????");
        }

        Long gid = contest.getGid();

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("????????????????????????????????????");
        }

        if (!userRolesVo.getUid().equals(contest.getUid()) && !isRoot
                && !groupValidator.isGroupRoot(userRolesVo.getUid(), gid)) {
            throw new StatusForbiddenException("?????????????????????????????????");
        }

        announcementDto.getAnnouncement().setGid(gid);

        boolean isOk = announcementEntityService.save(announcementDto.getAnnouncement());
        if (isOk) {
            contestAnnouncementEntityService.saveOrUpdate(new ContestAnnouncement()
                    .setAid(announcementDto.getAnnouncement().getId())
                    .setCid(announcementDto.getCid()));
        } else {
            throw new StatusFailException("???????????????");
        }
    }

    public void updateContestAnnouncement(AnnouncementDto announcementDto) throws StatusNotFoundException, StatusForbiddenException, StatusFailException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Long cid = announcementDto.getCid();

        Contest contest = contestEntityService.getById(cid);

        if (contest == null) {
            throw new StatusNotFoundException("?????????????????????");
        }

        Long gid = contest.getGid();

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("????????????????????????????????????");
        }

        if (!userRolesVo.getUid().equals(contest.getUid()) && !isRoot
                && !groupValidator.isGroupRoot(userRolesVo.getUid(), gid)) {
            throw new StatusForbiddenException("?????????????????????????????????");
        }

        boolean isOk = announcementEntityService.updateById(announcementDto.getAnnouncement());
        if (!isOk) {
            throw new StatusFailException("???????????????");
        }
    }

    public void deleteContestAnnouncement(Long aid, Long cid) throws StatusNotFoundException, StatusForbiddenException, StatusFailException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Contest contest = contestEntityService.getById(cid);

        if (contest == null) {
            throw new StatusNotFoundException("?????????????????????");
        }

        Long gid = contest.getGid();

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("????????????????????????????????????");
        }

        Announcement announcement = announcementEntityService.getById(aid);

        if (announcement == null) {
            throw new StatusNotFoundException("?????????????????????");
        }

        if (!userRolesVo.getUid().equals(contest.getUid()) && !isRoot
                && !groupValidator.isGroupRoot(userRolesVo.getUid(), gid)) {
            throw new StatusForbiddenException("?????????????????????????????????");
        }

        boolean isOk = announcementEntityService.removeById(aid);
        if (!isOk) {
            throw new StatusFailException("???????????????");
        }
    }
}

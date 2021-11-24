package top.hcode.hoj.service.contest.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.ContestAnnouncementMapper;
import top.hcode.hoj.pojo.entity.contest.ContestAnnouncement;
import top.hcode.hoj.service.contest.ContestAnnouncementService;

/**
 * @Author: Himit_ZH
 * @Date: 2020/12/21 22:59
 * @Description:
 */
@Service
public class ContestAnnouncementServiceImpl extends ServiceImpl<ContestAnnouncementMapper, ContestAnnouncement> implements ContestAnnouncementService {
}
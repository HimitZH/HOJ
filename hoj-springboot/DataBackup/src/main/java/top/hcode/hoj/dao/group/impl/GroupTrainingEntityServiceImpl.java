package top.hcode.hoj.dao.group.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.group.GroupTrainingEntityService;
import top.hcode.hoj.dao.training.TrainingProblemEntityService;
import top.hcode.hoj.mapper.GroupTrainingMapper;
import top.hcode.hoj.pojo.entity.training.Training;
import top.hcode.hoj.pojo.entity.training.TrainingProblem;
import top.hcode.hoj.pojo.vo.TrainingVo;
import top.hcode.hoj.pojo.vo.UserRolesVo;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
@Service
public class GroupTrainingEntityServiceImpl extends ServiceImpl<GroupTrainingMapper, Training> implements GroupTrainingEntityService {

    @Autowired
    private GroupTrainingMapper groupTrainingMapper;

    @Resource
    private TrainingProblemEntityService trainingProblemEntityService;

    @Override
    public IPage<TrainingVo> getTrainingList(int limit, int currentPage, Long gid) {
        IPage<TrainingVo> iPage = new Page<>(currentPage, limit);

        List<TrainingVo> trainingList = groupTrainingMapper.getTrainingList(iPage, gid);

        // 当前用户有登录，且训练列表不为空，则查询用户对于每个训练的做题进度
        if (trainingList.size() > 0) {

            Session session = SecurityUtils.getSubject().getSession();
            UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

            List<Long> tidList = trainingList.stream().map(TrainingVo::getId).collect(Collectors.toList());
            List<TrainingProblem> trainingProblemList = trainingProblemEntityService.getGroupTrainingListAcceptedCountByUid(tidList, gid, userRolesVo.getUid());
            HashMap<Long, Integer> tidMapCount = new HashMap<>(trainingList.size());
            for (TrainingProblem trainingProblem : trainingProblemList) {
                int count = tidMapCount.getOrDefault(trainingProblem.getTid(), 0);
                count++;
                tidMapCount.put(trainingProblem.getTid(), count);
            }

            for (TrainingVo trainingVo : trainingList) {
                Integer count = tidMapCount.getOrDefault(trainingVo.getId(), 0);
                trainingVo.setAcCount(count);
            }
        }

        return iPage.setRecords(trainingList);
    }

    @Override
    public IPage<Training> getAdminTrainingList(int limit, int currentPage, Long gid) {
        IPage<Training> iPage = new Page<>(currentPage, limit);

        List<Training> trainingList = groupTrainingMapper.getAdminTrainingList(iPage, gid);

        return iPage.setRecords(trainingList);
    }

}

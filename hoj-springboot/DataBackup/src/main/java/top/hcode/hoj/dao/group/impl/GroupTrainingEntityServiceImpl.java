package top.hcode.hoj.dao.group.impl;

import top.hcode.hoj.dao.group.GroupTrainingEntityService;
import top.hcode.hoj.mapper.GroupTrainingMapper;
import top.hcode.hoj.pojo.entity.training.Training;
import top.hcode.hoj.pojo.vo.TrainingVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
@Service
public class GroupTrainingEntityServiceImpl extends ServiceImpl<GroupTrainingMapper, Training> implements GroupTrainingEntityService {

    @Autowired
    private GroupTrainingMapper groupTrainingMapper;

    @Override
    public IPage<TrainingVo> getTrainingList(int limit, int currentPage, Long gid) {
        IPage<TrainingVo> iPage = new Page<>(currentPage, limit);

        List<TrainingVo> trainingList = groupTrainingMapper.getTrainingList(iPage, gid);

        return iPage.setRecords(trainingList);
    }

    @Override
    public IPage<Training> getAdminTrainingList(int limit, int currentPage, Long gid) {
        IPage<Training> iPage = new Page<>(currentPage, limit);

        List<Training> trainingList = groupTrainingMapper.getAdminTrainingList(iPage, gid);

        return iPage.setRecords(trainingList);
    }

}

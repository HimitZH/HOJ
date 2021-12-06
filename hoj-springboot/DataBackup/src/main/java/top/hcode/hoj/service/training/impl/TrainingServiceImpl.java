package top.hcode.hoj.service.training.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.dao.MappingTrainingCategoryMapper;
import top.hcode.hoj.dao.TrainingMapper;
import top.hcode.hoj.pojo.dto.TrainingDto;
import top.hcode.hoj.pojo.entity.training.MappingTrainingCategory;
import top.hcode.hoj.pojo.entity.training.Training;
import top.hcode.hoj.pojo.entity.training.TrainingCategory;
import top.hcode.hoj.pojo.vo.TrainingVo;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.service.training.TrainingService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2021/11/19 22:01
 * @Description:
 */
@Service
public class TrainingServiceImpl extends ServiceImpl<TrainingMapper, Training> implements TrainingService {

    @Resource
    private TrainingMapper trainingMapper;

    @Resource
    private TrainingCategoryServiceImpl trainingCategoryService;

    @Resource
    private MappingTrainingCategoryMapper mappingTrainingCategoryMapper;

    @Override
    public IPage<TrainingVo> getTrainingList(int limit, int currentPage, Long categoryId, String auth, String keyword) {
        List<TrainingVo> trainingList = trainingMapper.getTrainingList(categoryId, auth, keyword);
        Page<TrainingVo> page = new Page<>(currentPage, limit);
        int count = trainingList.size();
        List<TrainingVo> pageList = new ArrayList<>();
        //计算当前页第一条数据的下标
        int currId = currentPage > 1 ? (currentPage - 1) * limit : 0;
        for (int i = 0; i < limit && i < count - currId; i++) {
            pageList.add(trainingList.get(currId + i));
        }
        page.setSize(limit);
        page.setCurrent(currentPage);
        page.setTotal(count);
        page.setRecords(pageList);
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addTraining(TrainingDto trainingDto) {
        Training training = trainingDto.getTraining();
        trainingMapper.insert(training);
        TrainingCategory trainingCategory = trainingDto.getTrainingCategory();
        if (trainingCategory.getId() == null) {
            try {
                trainingCategoryService.save(trainingCategory);
            } catch (Exception ignored) {
                QueryWrapper<TrainingCategory> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("name", trainingCategory.getName());
                trainingCategory = trainingCategoryService.getOne(queryWrapper, false);
            }
        }

        int insert = mappingTrainingCategoryMapper.insert(new MappingTrainingCategory()
                .setTid(training.getId())
                .setCid(trainingCategory.getId()));
        return insert > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTraining(TrainingDto trainingDto) {

        Training training = trainingDto.getTraining();
        trainingMapper.updateById(training);
        TrainingCategory trainingCategory = trainingDto.getTrainingCategory();
        if (trainingCategory.getId() == null) {
            try {
                trainingCategoryService.save(trainingCategory);
            } catch (Exception ignored) {
                QueryWrapper<TrainingCategory> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("name", trainingCategory.getName());
                trainingCategory = trainingCategoryService.getOne(queryWrapper, false);
            }
        }

        MappingTrainingCategory mappingTrainingCategory = mappingTrainingCategoryMapper.selectOne(new QueryWrapper<MappingTrainingCategory>()
                .eq("tid", training.getId()));

        if (mappingTrainingCategory == null) {
            mappingTrainingCategoryMapper.insert(new MappingTrainingCategory()
                    .setTid(training.getId()).setCid(trainingCategory.getId()));
        } else {
            if (!mappingTrainingCategory.getCid().equals(trainingCategory.getId())) {
                UpdateWrapper<MappingTrainingCategory> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("tid", training.getId()).set("cid", trainingCategory.getId());
                int update = mappingTrainingCategoryMapper.update(null, updateWrapper);
                return update > 0;
            }
        }
        return true;
    }

    @Override
    public CommonResult getAdminTrainingDto(Long tid, HttpServletRequest request) {
        // 获取本场训练的信息
        Training training = trainingMapper.selectById(tid);
        if (training == null) { // 查询不存在
            return CommonResult.errorResponse("查询失败：该训练不存在,请检查参数tid是否准确！");
        }

        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        // 是否为超级管理员
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");
        // 只有超级管理员和训练拥有者才能操作
        if (!isRoot && !userRolesVo.getUsername().equals(training.getAuthor())) {
            return CommonResult.errorResponse("对不起，你无权限操作！", CommonResult.STATUS_FORBIDDEN);
        }

        TrainingDto trainingDto = new TrainingDto();
        trainingDto.setTraining(training);

        QueryWrapper<MappingTrainingCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tid", tid);
        MappingTrainingCategory mappingTrainingCategory = mappingTrainingCategoryMapper.selectOne(queryWrapper);
        TrainingCategory trainingCategory = null;
        if (mappingTrainingCategory != null) {
            trainingCategory = trainingCategoryService.getById(mappingTrainingCategory.getCid());
        }
        trainingDto.setTrainingCategory(trainingCategory);
        return CommonResult.successResponse(trainingDto, "查询成功！");
    }
}
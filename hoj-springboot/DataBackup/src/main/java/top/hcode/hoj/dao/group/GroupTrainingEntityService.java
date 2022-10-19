package top.hcode.hoj.dao.group;

import top.hcode.hoj.pojo.entity.training.Training;
import top.hcode.hoj.pojo.vo.TrainingVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
public interface GroupTrainingEntityService extends IService<Training> {

    IPage<TrainingVO> getTrainingList(int limit, int currentPage, Long gid);

    IPage<Training> getAdminTrainingList(int limit, int currentPage, Long gid);

}

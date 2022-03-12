package top.hcode.hoj.dao.contest.impl;

import top.hcode.hoj.pojo.entity.contest.ContestScore;
import top.hcode.hoj.mapper.ContestScoreMapper;
import top.hcode.hoj.dao.contest.ContestScoreEntityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
@Service
public class ContestScoreEntityServiceImpl extends ServiceImpl<ContestScoreMapper, ContestScore> implements ContestScoreEntityService {

}

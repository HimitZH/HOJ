package top.hcode.hoj.dao.problem.impl;

import top.hcode.hoj.pojo.entity.problem.Tag;
import top.hcode.hoj.mapper.TagMapper;
import top.hcode.hoj.dao.problem.TagEntityService;
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
public class TagEntityServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagEntityService {

}

package top.hcode.hoj.service.problem.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.CategoryMapper;
import top.hcode.hoj.pojo.entity.problem.Category;
import top.hcode.hoj.service.problem.CategoryService;

/**
 * @Author: Himit_ZH
 * @Date: 2021/5/4 22:30
 * @Description:
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
}
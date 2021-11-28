package top.hcode.hoj.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.problem.Tag;
import top.hcode.hoj.pojo.entity.training.TrainingCategory;
import top.hcode.hoj.service.training.impl.TrainingCategoryServiceImpl;

import javax.annotation.Resource;

/**
 * @Author: Himit_ZH
 * @Date: 2021/11/27 15:11
 * @Description:
 */

@RestController
@RequestMapping("/api/admin/training/category")
public class AdminTrainingCategoryController {

    @Resource
    private TrainingCategoryServiceImpl trainingCategoryService;

    @PostMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "problem_admin"}, logical = Logical.OR)
    public CommonResult addTrainingCategory(@RequestBody TrainingCategory trainingCategory) {

        QueryWrapper<TrainingCategory> trainingCategoryQueryWrapper = new QueryWrapper<>();
        trainingCategoryQueryWrapper.eq("name", trainingCategory.getName());
        TrainingCategory existedTrainingCategory = trainingCategoryService.getOne(trainingCategoryQueryWrapper, false);

        if (existedTrainingCategory != null) {
            return CommonResult.errorResponse("该分类名称已存在！请勿重复添加！", CommonResult.STATUS_FAIL);
        }

        boolean result = trainingCategoryService.save(trainingCategory);
        if (result) { // 添加成功
            return CommonResult.successResponse(trainingCategory, "添加成功！");
        } else {
            return CommonResult.errorResponse("添加失败", CommonResult.STATUS_FAIL);
        }
    }

    @PutMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "problem_admin"}, logical = Logical.OR)
    public CommonResult updateTrainingCategory(@RequestBody TrainingCategory trainingCategory) {
        boolean result = trainingCategoryService.updateById(trainingCategory);
        if (result) { // 更新成功
            return CommonResult.successResponse(null, "更新成功！");
        } else {
            return CommonResult.errorResponse("更新失败！", CommonResult.STATUS_FAIL);
        }
    }

    @DeleteMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "problem_admin"}, logical = Logical.OR)
    public CommonResult deleteTrainingCategory(@RequestParam("cid") Long cid) {
        boolean result = trainingCategoryService.removeById(cid);
        if (result) { // 删除成功
            return CommonResult.successResponse(null, "删除成功！");
        } else {
            return CommonResult.errorResponse("删除失败！", CommonResult.STATUS_FAIL);
        }
    }
}
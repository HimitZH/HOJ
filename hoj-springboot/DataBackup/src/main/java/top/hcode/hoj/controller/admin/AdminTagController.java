package top.hcode.hoj.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.Tag;
import top.hcode.hoj.service.impl.TagServiceImpl;

import javax.annotation.Resource;

/**
 * @Author: Himit_ZH
 * @Date: 2021/11/2 23:24
 * @Description: 处理tag的增删改
 */

@RestController
@RequestMapping("/api/admin/tag")
public class AdminTagController {


    @Resource
    private TagServiceImpl tagService;

    @PostMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "problem_admin"}, logical = Logical.OR)
    public CommonResult addProblem(@RequestBody Tag tag) {

        QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
        tagQueryWrapper.eq("name", tag.getName());
        Tag existTag = tagService.getOne(tagQueryWrapper, false);

        if (existTag != null) {
            return CommonResult.errorResponse("该标签名称已存在！请勿重复添加！", CommonResult.STATUS_FAIL);
        }

        boolean result = tagService.save(tag);
        if (result) { // 添加成功
            return CommonResult.successResponse(tag, "添加成功！");
        } else {
            return CommonResult.errorResponse("添加失败", CommonResult.STATUS_FAIL);
        }

    }

    @PutMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "problem_admin"}, logical = Logical.OR)
    public CommonResult updateTag(@RequestBody Tag tag) {
        boolean result = tagService.updateById(tag);
        if (result) { // 更新成功
            return CommonResult.successResponse(null, "更新成功！");
        } else {
            return CommonResult.errorResponse("更新失败！", CommonResult.STATUS_FAIL);
        }
    }

    @DeleteMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "problem_admin"}, logical = Logical.OR)
    public CommonResult deleteTag(@RequestParam("tid") Long tid) {
        boolean result = tagService.removeById(tid);
        if (result) { // 删除成功
            return CommonResult.successResponse(null, "删除成功！");
        } else {
            return CommonResult.errorResponse("删除失败！", CommonResult.STATUS_FAIL);
        }
    }

}
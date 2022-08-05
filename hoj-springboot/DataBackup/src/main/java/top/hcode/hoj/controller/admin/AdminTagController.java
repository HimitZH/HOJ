package top.hcode.hoj.controller.admin;

import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.problem.Tag;
import top.hcode.hoj.pojo.entity.problem.TagClassification;
import top.hcode.hoj.service.admin.tag.AdminTagService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2021/11/2 23:24
 * @Description: 处理tag的增删改
 */

@RestController
@RequestMapping("/api/admin/tag")
public class AdminTagController {


    @Resource
    private AdminTagService adminTagService;

    @PostMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Tag> addTag(@RequestBody Tag tag) {
        return adminTagService.addTag(tag);
    }

    @PutMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> updateTag(@RequestBody Tag tag) {
        return adminTagService.updateTag(tag);
    }

    @DeleteMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> deleteTag(@RequestParam("tid") Long tid) {
        return adminTagService.deleteTag(tid);
    }


    @GetMapping("/classification")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "problem_admin"}, logical = Logical.OR)
    public CommonResult<List<TagClassification>> getTagClassification(@RequestParam(value = "oj", defaultValue = "ME") String oj) {
        return adminTagService.getTagClassification(oj);
    }

    @PostMapping("/classification")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "problem_admin"}, logical = Logical.OR)
    public CommonResult<TagClassification> addTagClassification(@RequestBody TagClassification tagClassification) {
        return adminTagService.addTagClassification(tagClassification);
    }

    @PutMapping("/classification")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> updateTagClassification(@RequestBody TagClassification tagClassification) {
        return adminTagService.updateTagClassification(tagClassification);
    }

    @DeleteMapping("/classification")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> deleteTagClassification(@RequestParam("tcid") Long tcid) {
        return adminTagService.deleteTagClassification(tcid);
    }
}
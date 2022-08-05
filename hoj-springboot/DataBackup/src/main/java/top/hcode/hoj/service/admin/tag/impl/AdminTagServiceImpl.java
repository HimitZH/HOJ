package top.hcode.hoj.service.admin.tag.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.manager.admin.tag.AdminTagManager;
import top.hcode.hoj.pojo.entity.problem.Tag;
import top.hcode.hoj.pojo.entity.problem.TagClassification;
import top.hcode.hoj.service.admin.tag.AdminTagService;

import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/9 17:46
 * @Description:
 */

@Service
public class AdminTagServiceImpl implements AdminTagService {

    @Autowired
    private AdminTagManager adminTagManager;

    @Override
    public CommonResult<Tag> addTag(Tag tag) {
        try {
            return CommonResult.successResponse(adminTagManager.addTag(tag));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<Void> updateTag(Tag tag) {
        try {
            adminTagManager.updateTag(tag);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<Void> deleteTag(Long tid) {
        try {
            adminTagManager.deleteTag(tid);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<List<TagClassification>> getTagClassification(String oj) {
        return CommonResult.successResponse(adminTagManager.getTagClassification(oj));
    }

    @Override
    public CommonResult<TagClassification> addTagClassification(TagClassification tagClassification) {
        try {
            return CommonResult.successResponse(adminTagManager.addTagClassification(tagClassification));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<Void> updateTagClassification(TagClassification tagClassification) {
        try {
            adminTagManager.updateTagClassification(tagClassification);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<Void> deleteTagClassification(Long tcid) {
        try {
            adminTagManager.deleteTagClassification(tcid);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }
}
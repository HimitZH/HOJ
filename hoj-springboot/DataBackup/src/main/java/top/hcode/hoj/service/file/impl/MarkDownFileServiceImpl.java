package top.hcode.hoj.service.file.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.exception.StatusSystemErrorException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.manager.file.MarkDownFileManager;
import top.hcode.hoj.service.file.MarkDownFileService;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/10 15:18
 * @Description:
 */
@Service
public class MarkDownFileServiceImpl implements MarkDownFileService {

    @Resource
    private MarkDownFileManager markDownFileManager;

    @Override
    public CommonResult<Map<Object, Object>> uploadMDImg(MultipartFile image, Long gid) {
        try {
            return CommonResult.successResponse(markDownFileManager.uploadMDImg(image, gid));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusSystemErrorException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.SYSTEM_ERROR);
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Void> deleteMDImg(Long fileId) {
        try {
            markDownFileManager.deleteMDImg(fileId);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Map<Object, Object>> uploadMd(MultipartFile file, Long gid) {
        try {
            return CommonResult.successResponse(markDownFileManager.uploadMd(file, gid));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusSystemErrorException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.SYSTEM_ERROR);
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }
}
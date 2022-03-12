package top.hcode.hoj.service.file.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusSystemErrorException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.manager.file.ImportQDUOJProblemManager;
import top.hcode.hoj.service.file.ImportQDUOJProblemService;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/10 15:24
 * @Description:
 */
@Service
public class ImportQDUOJProblemServiceImpl implements ImportQDUOJProblemService {

    @Autowired
    private ImportQDUOJProblemManager importQDUOJProblemManager;

    @Override
    public CommonResult<Void> importQDOJProblem(MultipartFile file) {
        try {
            importQDUOJProblemManager.importQDOJProblem(file);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusSystemErrorException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.SYSTEM_ERROR);
        }
    }
}
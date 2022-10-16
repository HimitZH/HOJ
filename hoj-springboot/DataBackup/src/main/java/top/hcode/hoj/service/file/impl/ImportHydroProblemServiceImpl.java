package top.hcode.hoj.service.file.impl;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusSystemErrorException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.manager.file.ImportHydroProblemManager;
import top.hcode.hoj.service.file.ImportHydroProblemService;

import javax.annotation.Resource;

/**
 * @Author Himit_ZH
 * @Date 2022/10/16
 */
@Component
public class ImportHydroProblemServiceImpl implements ImportHydroProblemService {

    @Resource
    private ImportHydroProblemManager importHydroProblemManager;

    @Override
    public CommonResult<Void> importHydroProblem(MultipartFile file) {
        try {
            importHydroProblemManager.importHydroProblem(file);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusSystemErrorException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.SYSTEM_ERROR);
        }
    }
}

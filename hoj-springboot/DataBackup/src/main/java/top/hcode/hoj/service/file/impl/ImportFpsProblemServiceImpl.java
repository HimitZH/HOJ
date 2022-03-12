package top.hcode.hoj.service.file.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.manager.file.ImportFpsProblemManager;
import top.hcode.hoj.service.file.ImportFpsProblemService;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/10 15:35
 * @Description:
 */
@Service
public class ImportFpsProblemServiceImpl implements ImportFpsProblemService {

    @Resource
    private ImportFpsProblemManager importFpsProblemManager;

    @Override
    public CommonResult<Void> importFPSProblem(MultipartFile file) {
        try {
            importFpsProblemManager.importFPSProblem(file);
            return CommonResult.successResponse();
        } catch (IOException | StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }
}
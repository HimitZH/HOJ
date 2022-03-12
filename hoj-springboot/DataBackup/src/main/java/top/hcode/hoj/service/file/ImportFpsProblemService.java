package top.hcode.hoj.service.file;

import org.springframework.web.multipart.MultipartFile;
import top.hcode.hoj.common.result.CommonResult;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/10 15:32
 * @Description:
 */
public interface ImportFpsProblemService {

    public CommonResult<Void> importFPSProblem(MultipartFile file);
}
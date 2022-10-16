package top.hcode.hoj.service.file;

import org.springframework.web.multipart.MultipartFile;
import top.hcode.hoj.common.result.CommonResult;

/**
 * @Author Himit_ZH
 * @Date 2022/10/16
 */
public interface ImportHydroProblemService {

    public CommonResult<Void> importHydroProblem(MultipartFile file);
}

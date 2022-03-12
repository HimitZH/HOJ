package top.hcode.hoj.service.file;

import org.springframework.web.multipart.MultipartFile;
import top.hcode.hoj.common.result.CommonResult;

public interface ImportQDUOJProblemService {

    public CommonResult<Void> importQDOJProblem(MultipartFile file);
}

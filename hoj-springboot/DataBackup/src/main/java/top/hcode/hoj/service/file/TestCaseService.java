package top.hcode.hoj.service.file;

import top.hcode.hoj.common.exception.StatusForbiddenException;
import org.springframework.web.multipart.MultipartFile;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.result.CommonResult;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/10 15:08
 * @Description:
 */
public interface TestCaseService {

    public CommonResult<Map<Object, Object>> uploadTestcaseZip(MultipartFile file, Long gid, String mode);

    public void downloadTestcase(Long pid, HttpServletResponse response) throws StatusFailException, StatusForbiddenException;
}
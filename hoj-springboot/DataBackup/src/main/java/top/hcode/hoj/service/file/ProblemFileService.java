package top.hcode.hoj.service.file;

import org.springframework.web.multipart.MultipartFile;
import top.hcode.hoj.common.result.CommonResult;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface ProblemFileService {

    public CommonResult<Void> importProblem(MultipartFile file);

    public void exportProblem(List<Long> pidList, HttpServletResponse response);
}

package top.hcode.hoj.service.file;

import org.springframework.web.multipart.MultipartFile;
import top.hcode.hoj.common.result.CommonResult;

import java.util.Map;

public interface ImageService {

    public CommonResult<Map<Object,Object>> uploadAvatar(MultipartFile image);

    public CommonResult<Map<Object,Object>> uploadGroupAvatar(MultipartFile image, Long gid);

    public CommonResult<Map<Object,Object>> uploadCarouselImg(MultipartFile image);
}

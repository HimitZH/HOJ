package top.hcode.hoj.controller.file;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.service.file.MarkDownFileService;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @Author: Himit_ZH
 * @Date: 2021/10/5 20:01
 * @Description:
 */
@Controller
@RequestMapping("/api/file")
public class MarkDownFileController {


    @Resource
    private MarkDownFileService markDownFileService;

    @RequestMapping(value = "/upload-md-img", method = RequestMethod.POST)
    @RequiresAuthentication
    @ResponseBody
    public CommonResult<Map<Object, Object>> uploadMDImg(@RequestParam("image") MultipartFile image,
                                                         @RequestParam(value = "gid", required = false) Long gid) {
        return markDownFileService.uploadMDImg(image, gid);
    }


    @RequestMapping(value = "/delete-md-img", method = RequestMethod.GET)
    @RequiresAuthentication
    @ResponseBody
    public CommonResult<Void> deleteMDImg(@RequestParam("fileId") Long fileId) {
        return markDownFileService.deleteMDImg(fileId);
    }


    @RequestMapping(value = "/upload-md-file", method = RequestMethod.POST)
    @RequiresAuthentication
    @ResponseBody
    public CommonResult<Map<Object, Object>> uploadMd(@RequestParam("file") MultipartFile file,
                                                      @RequestParam(value = "gid", required = false) Long gid) {
        return markDownFileService.uploadMd(file, gid);
    }

}
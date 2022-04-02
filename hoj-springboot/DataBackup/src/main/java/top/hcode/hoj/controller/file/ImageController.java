package top.hcode.hoj.controller.file;


import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.group.Group;
import top.hcode.hoj.service.file.ImageService;

import java.util.Map;

/**
 * @Author: Himit_ZH
 * @Date: 2021/10/5 19:46
 * @Description:
 */
@Controller
@RequestMapping("/api/file")
public class ImageController {

    @Autowired
    private ImageService imageService;


    @RequestMapping(value = "/upload-avatar", method = RequestMethod.POST)
    @RequiresAuthentication
    @ResponseBody
    public CommonResult<Map<Object, Object>> uploadAvatar(@RequestParam("image") MultipartFile image) {
        return imageService.uploadAvatar(image);
    }

    @RequestMapping(value = "/upload-group-avatar", method = RequestMethod.POST)
    @RequiresAuthentication
    @ResponseBody
    public CommonResult<Group> uploadGroupAvatar(@RequestParam(value = "image", required = true) MultipartFile image,
                                                 @RequestParam(value = "gid", required = true) Long gid) {
        return imageService.uploadGroupAvatar(image, gid);
    }

    @RequestMapping(value = "/upload-carouse-img", method = RequestMethod.POST)
    @RequiresAuthentication
    @ResponseBody
    @RequiresRoles("root")
    public CommonResult<Map<Object, Object>> uploadCarouselImg(@RequestParam("file") MultipartFile image) {
        return imageService.uploadCarouselImg(image);
    }

}
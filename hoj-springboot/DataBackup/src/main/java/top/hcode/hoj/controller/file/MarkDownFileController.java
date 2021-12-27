package top.hcode.hoj.controller.file;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.service.common.impl.FileServiceImpl;
import top.hcode.hoj.utils.Constants;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;

/**
 * @Author: Himit_ZH
 * @Date: 2021/10/5 20:01
 * @Description:
 */
@Controller
@RequestMapping("/api/file")
@Slf4j(topic = "hoj")
public class MarkDownFileController {


    @Resource
    private FileServiceImpl fileService;

    @RequestMapping(value = "/upload-md-img", method = RequestMethod.POST)
    @RequiresAuthentication
    @ResponseBody
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult uploadMDImg(@RequestParam("image") MultipartFile image, HttpServletRequest request) {
        if (image == null) {
            return CommonResult.errorResponse("上传的图片不能为空！");
        }
        if (image.getSize() > 1024 * 1024 * 4) {
            return CommonResult.errorResponse("上传的图片文件大小不能大于4M！");
        }
        //获取文件后缀
        String suffix = image.getOriginalFilename().substring(image.getOriginalFilename().lastIndexOf(".") + 1);
        if (!"jpg,jpeg,gif,png,webp".toUpperCase().contains(suffix.toUpperCase())) {
            return CommonResult.errorResponse("请选择jpg,jpeg,gif,png,webp格式的图片！");
        }

        //若不存在该目录，则创建目录
        FileUtil.mkdir(Constants.File.MARKDOWN_FILE_FOLDER.getPath());

        //通过UUID生成唯一文件名
        String filename = IdUtil.simpleUUID() + "." + suffix;
        try {
            //将文件保存指定目录
            image.transferTo(FileUtil.file(Constants.File.MARKDOWN_FILE_FOLDER.getPath() + File.separator + filename));
        } catch (Exception e) {
            log.error("图片文件上传异常-------------->", e);
            return CommonResult.errorResponse("服务器异常：图片文件上传失败！", CommonResult.STATUS_ERROR);
        }

        // 获取当前登录用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        top.hcode.hoj.pojo.entity.common.File file = new top.hcode.hoj.pojo.entity.common.File();
        file.setFolderPath(Constants.File.MARKDOWN_FILE_FOLDER.getPath())
                .setName(filename)
                .setFilePath(Constants.File.MARKDOWN_FILE_FOLDER.getPath() + File.separator + filename)
                .setSuffix(suffix)
                .setType("md")
                .setUid(userRolesVo.getUid());
        fileService.save(file);

        return CommonResult.successResponse(MapUtil.builder()
                        .put("link", Constants.File.IMG_API.getPath() + filename)
                        .put("fileId", file.getId()).map(),
                "上传图片成功！");

    }


    @RequestMapping(value = "/delete-md-img", method = RequestMethod.GET)
    @RequiresAuthentication
    @ResponseBody
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult deleteMDImg(@RequestParam("fileId") Long fileId, HttpServletRequest request) {

        // 获取当前登录用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        top.hcode.hoj.pojo.entity.common.File file = fileService.getById(fileId);

        if (file == null) {
            return CommonResult.errorResponse("错误：文件不存在！");
        }

        if (!file.getType().equals("md")) {
            return CommonResult.errorResponse("错误：不支持删除！", CommonResult.STATUS_FORBIDDEN);
        }

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");
        boolean isProblemAdmin = SecurityUtils.getSubject().hasRole("problem_admin");
        boolean isAdmin = SecurityUtils.getSubject().hasRole("admin");

        if (!file.getUid().equals(userRolesVo.getUid()) && !isRoot && !isAdmin && !isProblemAdmin) {
            return CommonResult.errorResponse("错误：无权删除他人文件！", CommonResult.STATUS_FORBIDDEN);
        }

        boolean result = FileUtil.del(file.getFilePath());
        if (result) {
            fileService.removeById(fileId);
            return CommonResult.successResponse(null, "删除成功");
        } else {
            return CommonResult.errorResponse("删除失败");
        }

    }


    @RequestMapping(value = "/upload-md-file", method = RequestMethod.POST)
    @RequiresAuthentication
    @ResponseBody
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult uploadMd(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file == null) {
            return CommonResult.errorResponse("上传的文件不能为空！");
        }
        if (file.getSize() >= 1024 * 1024 * 128) {
            return CommonResult.errorResponse("上传的文件大小不能大于128M！");
        }
        //获取文件后缀
        String suffix = "";
        String filename = "";
        if (file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")) {
            suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
            //通过UUID生成唯一文件名
            filename = IdUtil.simpleUUID() + "." + suffix;
        } else {
            filename = IdUtil.simpleUUID();
        }
        //若不存在该目录，则创建目录
        FileUtil.mkdir(Constants.File.MARKDOWN_FILE_FOLDER.getPath());

        try {
            //将文件保存指定目录
            file.transferTo(FileUtil.file(Constants.File.MARKDOWN_FILE_FOLDER.getPath() + File.separator + filename));
        } catch (Exception e) {
            log.error("文件上传异常-------------->", e);
            return CommonResult.errorResponse("服务器异常：文件上传失败！", CommonResult.STATUS_ERROR);
        }

        return CommonResult.successResponse(MapUtil.builder()
                        .put("link", Constants.File.FILE_API.getPath() + filename).map(),
                "上传文件成功！");
    }

}
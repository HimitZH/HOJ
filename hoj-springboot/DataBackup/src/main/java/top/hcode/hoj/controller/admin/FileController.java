package top.hcode.hoj.controller.admin;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.Role;
import top.hcode.hoj.pojo.entity.UserInfo;
import top.hcode.hoj.pojo.vo.ExcelUserVo;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.service.impl.FileServiceImpl;
import top.hcode.hoj.service.impl.UserInfoServiceImpl;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.RedisUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @Author: Himit_ZH
 * @Date: 2020/12/10 16:18
 * @Description:
 */
@Controller
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private FileServiceImpl fileService;

    @Autowired
    private UserInfoServiceImpl userInfoService;

    @RequestMapping("/generate-user-excel")
    @RequiresAuthentication
    @RequiresRoles("root")
    public void generateUserExcel(@RequestParam("key") String key, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode(key, "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        response.setHeader("Content-Type", "application/xlsx");
        EasyExcel.write(response.getOutputStream(), ExcelUserVo.class).sheet("用户数据").doWrite(getGenerateUsers(key));
    }

    public List<ExcelUserVo> getGenerateUsers(String key){
        List<ExcelUserVo> result = new LinkedList<>();
        Map<Object, Object> userInfo = redisUtils.hmget(key);
        for (Object hashKey:userInfo.keySet()){
            String username = (String) hashKey;
            String password = (String) userInfo.get(hashKey);
            result.add(new ExcelUserVo().setUsername(username).setPassword(password));
        }
        return result;
    }

    @RequestMapping(value = "/upload-avatar",method = RequestMethod.POST)
    @RequiresAuthentication
    @ResponseBody
    @Transactional
    public CommonResult uploadAvatar(@RequestParam("image") MultipartFile image, HttpServletRequest request){
        if (image == null) {
            return CommonResult.errorResponse("上传的头像图片文件不能为空！");
        }
        if (image.getSize() > 1024 * 1024 * 2) {
            return CommonResult.errorResponse("上传的头像图片文件大小不能大于2M！");
        }
        //获取文件后缀
        String suffix = image.getOriginalFilename().substring(image.getOriginalFilename().lastIndexOf(".") + 1);
        if (!"jpg,jpeg,gif,png,webp".toUpperCase().contains(suffix.toUpperCase())) {
            return CommonResult.errorResponse("请选择jpg,jpeg,gif,png,webp格式的头像图片！");
        }
        File savePathFile = new File(Constants.File.USER_AVATAR_FOLDER.getPath());
        if (!savePathFile.exists()) {
            //若不存在该目录，则创建目录
            savePathFile.mkdir();
        }
        //通过UUID生成唯一文件名
        String filename = IdUtil.simpleUUID()+"."+suffix;
        try {
            //将文件保存指定目录
            image.transferTo(new File(Constants.File.USER_AVATAR_FOLDER.getPath()+ filename));
        } catch (Exception e) {
            log.error("头像文件上传异常-------------->{}", e.getMessage());
            return CommonResult.errorResponse("服务器异常：头像上传失败！",CommonResult.STATUS_ERROR);
        }

        // 获取当前登录用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");


        // 将当前用户所属的file表中avatar类型的实体的delete设置为1；
        fileService.updateFileToDeleteByUidAndType(userRolesVo.getUid(),"avatar");

        //更新user_info里面的avatar
        UpdateWrapper<UserInfo> userInfoUpdateWrapper = new UpdateWrapper<>();
        userInfoUpdateWrapper.set("avatar", Constants.File.USER_FILE_HOST.getPath()+Constants.File.USER_AVATAR_API.getPath()+filename)
                .eq("uuid", userRolesVo.getUid());
        userInfoService.update(userInfoUpdateWrapper);

        // 插入file表记录
        top.hcode.hoj.pojo.entity.File imgFile = new top.hcode.hoj.pojo.entity.File();
        imgFile.setName(filename).setFolderPath(Constants.File.USER_AVATAR_FOLDER.getPath())
                .setFilePath(Constants.File.USER_AVATAR_FOLDER.getPath()+ filename)
                .setSuffix(suffix)
                .setType("avatar")
                .setUid(userRolesVo.getUid());
        fileService.saveOrUpdate(imgFile);
        return CommonResult.successResponse(MapUtil.builder()
                .put("uid", userRolesVo.getUid())
                .put("username", userRolesVo.getUsername())
                .put("nickname", userRolesVo.getNickname())
                .put("avatar", Constants.File.USER_FILE_HOST.getPath()+Constants.File.USER_AVATAR_API.getPath()+filename)
                .put("email", userRolesVo.getEmail())
                .put("number", userRolesVo.getNumber())
                .put("school", userRolesVo.getSchool())
                .put("course", userRolesVo.getCourse())
                .put("signature", userRolesVo.getSignature())
                .put("realname", userRolesVo.getRealname())
                .put("github", userRolesVo.getGithub())
                .put("blog", userRolesVo.getBlog())
                .put("cfUsername", userRolesVo.getCfUsername())
                .put("roleList", userRolesVo.getRoles().stream().map(Role::getRole))
                .map(), "设置新头像成功！");
    }

}
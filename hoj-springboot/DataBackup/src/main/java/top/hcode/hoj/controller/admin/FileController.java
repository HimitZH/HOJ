package top.hcode.hoj.controller.admin;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.core.io.file.FileWriter;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.ProblemCase;
import top.hcode.hoj.pojo.entity.Role;
import top.hcode.hoj.pojo.entity.UserInfo;
import top.hcode.hoj.pojo.vo.ExcelUserVo;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.service.impl.FileServiceImpl;
import top.hcode.hoj.service.impl.ProblemCaseServiceImpl;
import top.hcode.hoj.service.impl.UserInfoServiceImpl;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.RedisUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;

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

    @Autowired
    private ProblemCaseServiceImpl problemCaseService;

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

    public List<ExcelUserVo> getGenerateUsers(String key) {
        List<ExcelUserVo> result = new LinkedList<>();
        Map<Object, Object> userInfo = redisUtils.hmget(key);
        for (Object hashKey : userInfo.keySet()) {
            String username = (String) hashKey;
            String password = (String) userInfo.get(hashKey);
            result.add(new ExcelUserVo().setUsername(username).setPassword(password));
        }
        return result;
    }

    @RequestMapping(value = "/upload-avatar", method = RequestMethod.POST)
    @RequiresAuthentication
    @ResponseBody
    @Transactional
    public CommonResult uploadAvatar(@RequestParam("image") MultipartFile image, HttpServletRequest request) {
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
        String filename = IdUtil.simpleUUID() + "." + suffix;
        try {
            //将文件保存指定目录
            image.transferTo(new File(Constants.File.USER_AVATAR_FOLDER.getPath() + filename));
        } catch (Exception e) {
            log.error("头像文件上传异常-------------->{}", e.getMessage());
            return CommonResult.errorResponse("服务器异常：头像上传失败！", CommonResult.STATUS_ERROR);
        }

        // 获取当前登录用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");


        // 将当前用户所属的file表中avatar类型的实体的delete设置为1；
        fileService.updateFileToDeleteByUidAndType(userRolesVo.getUid(), "avatar");

        //更新user_info里面的avatar
        UpdateWrapper<UserInfo> userInfoUpdateWrapper = new UpdateWrapper<>();
        userInfoUpdateWrapper.set("avatar", Constants.File.USER_FILE_HOST.getPath() + Constants.File.USER_AVATAR_API.getPath() + filename)
                .eq("uuid", userRolesVo.getUid());
        userInfoService.update(userInfoUpdateWrapper);

        // 插入file表记录
        top.hcode.hoj.pojo.entity.File imgFile = new top.hcode.hoj.pojo.entity.File();
        imgFile.setName(filename).setFolderPath(Constants.File.USER_AVATAR_FOLDER.getPath())
                .setFilePath(Constants.File.USER_AVATAR_FOLDER.getPath() + filename)
                .setSuffix(suffix)
                .setType("avatar")
                .setUid(userRolesVo.getUid());
        fileService.saveOrUpdate(imgFile);
        return CommonResult.successResponse(MapUtil.builder()
                .put("uid", userRolesVo.getUid())
                .put("username", userRolesVo.getUsername())
                .put("nickname", userRolesVo.getNickname())
                .put("avatar", Constants.File.USER_FILE_HOST.getPath() + Constants.File.USER_AVATAR_API.getPath() + filename)
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


    @PostMapping("/upload-testcase-zip")
    @ResponseBody
    public CommonResult uploadTestcaseZip(@RequestParam("file") MultipartFile file) {
        //获取文件后缀
        String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
        if (!"zip".toUpperCase().contains(suffix.toUpperCase())) {
            return CommonResult.errorResponse("请上传zip格式的测试数据压缩包！");
        }
        String fileDirId = IdUtil.simpleUUID();
        String fileDir = Constants.File.TESTCASE_BASE_FOLDER.getPath() + File.separator + fileDirId;
        String filePath = fileDir + File.separator + file.getOriginalFilename();
        // 文件夹不存在就新建
        FileUtil.mkdir(fileDir);
        try {
            file.transferTo(new File(filePath));
        } catch (IOException e) {
            log.error("评测数据文件上传异常-------------->{}", e.getMessage());
            return CommonResult.errorResponse("服务器异常：评测数据上传失败！", CommonResult.STATUS_ERROR);
        }

        // 将压缩包压缩到指定文件夹
        ZipUtil.unzip(filePath, fileDir);
        // 删除zip文件
        FileUtil.del(filePath);
        // 检查文件是否存在
        File testCaseFileList = new File(fileDir);
        File[] files = testCaseFileList.listFiles();
        if (files == null || files.length == 0) {
            FileUtil.del(fileDir);
            return CommonResult.errorResponse("评测数据压缩包里文件不能为空！");
        }

        HashMap<String, String> inputData = new HashMap<>();
        HashMap<String, String> outputData = new HashMap<>();

        // 遍历读取与检查是否in和out文件一一对应，否则报错
        for (File tmp : files) {
            String tmpPreName = tmp.getName().substring(0, tmp.getName().lastIndexOf("."));
            if (tmp.getName().endsWith("in")) {
                inputData.put(tmpPreName, tmp.getName());
            } else if (tmp.getName().endsWith("out")) {
                outputData.put(tmpPreName, tmp.getName());
            }
        }

        // 进行数据对应检查,同时生成返回数据
        List<HashMap<String, String>> problemCaseList = new LinkedList<>();
        for (String key : inputData.keySet()) {
            // 若有名字不对应，直接返回失败
            if (outputData.getOrDefault(key, null) == null) {
                FileUtil.del(fileDir);
                return CommonResult.errorResponse("请检查数据压缩包里面的in和out文件是否一一对应！");
            }
            HashMap<String, String> testcaseMap = new HashMap<>();
            testcaseMap.put("input", inputData.get(key));
            testcaseMap.put("output", outputData.get(key));
            problemCaseList.add(testcaseMap);
        }

        return CommonResult.successResponse(MapUtil.builder()
                        .put("fileList", problemCaseList)
                        .put("fileListDir", fileDir)
                        .map()
                , "上传测试数据成功！");
    }

    @GetMapping("/download-testcase")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin"}, logical = Logical.OR)
    public void downloadTestcase(@RequestParam("pid") Long pid, HttpServletResponse response) throws IOException {
        QueryWrapper<ProblemCase> problemCaseQueryWrapper = new QueryWrapper<>();
        problemCaseQueryWrapper.eq("pid", pid);
        List<ProblemCase> problemCaseList = problemCaseService.list(problemCaseQueryWrapper);
        Assert.notEmpty(problemCaseList, "对不起，该题目的评测数据为空！");
        String workDir = Constants.File.TESTCASE_DOWNLOAD_TMP_FOLDER.getPath() + File.separator + IdUtil.simpleUUID();
        FileUtil.mkdir(workDir);
        // 写入本地
        for (int i = 0; i < problemCaseList.size(); i++) {
            String filePreName = workDir + File.separator + (i + 1);
            String inputName = filePreName + ".in";
            String outputName = filePreName + ".out";
            FileWriter infileWriter = new FileWriter(inputName);
            infileWriter.write(problemCaseList.get(i).getInput());
            FileWriter outfileWriter = new FileWriter(outputName);
            outfileWriter.write(problemCaseList.get(i).getOutput());
        }
        String fileName = "problem_" + pid + "_testcase_"+System.currentTimeMillis()+".zip";
        // 将对应文件夹的文件压缩成zip
        ZipUtil.zip(workDir, Constants.File.TESTCASE_DOWNLOAD_TMP_FOLDER.getPath() + File.separator + fileName);
        // 将zip变成io流返回给前端
        FileReader fileReader = new FileReader(Constants.File.TESTCASE_DOWNLOAD_TMP_FOLDER.getPath() + File.separator + fileName);
        BufferedInputStream bins = new BufferedInputStream(fileReader.getInputStream());//放到缓冲流里面
        OutputStream outs = response.getOutputStream();//获取文件输出IO流
        BufferedOutputStream bouts = new BufferedOutputStream(outs);
        response.setContentType("application/x-download");
        response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
        int bytesRead = 0;
        byte[] buffer = new byte[8192];
        //开始向网络传输文件流
        while ((bytesRead = bins.read(buffer, 0, 8192)) != -1) {
            bouts.write(buffer, 0, bytesRead);
        }
        bouts.flush();
        bins.close();
        outs.close();
        bouts.close();
        // 清空临时文件
        FileUtil.del(workDir);
        FileUtil.del(Constants.File.TESTCASE_DOWNLOAD_TMP_FOLDER.getPath() + File.separator + fileName);
    }

}
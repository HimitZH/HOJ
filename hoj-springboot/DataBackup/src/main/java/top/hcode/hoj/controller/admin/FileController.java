package top.hcode.hoj.controller.admin;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
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
import top.hcode.hoj.pojo.entity.*;
import top.hcode.hoj.pojo.vo.ACMContestRankVo;
import top.hcode.hoj.pojo.vo.ExcelUserVo;
import top.hcode.hoj.pojo.vo.OIContestRankVo;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.service.impl.*;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.RedisUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.io.File;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @Author: Himit_ZH
 * @Date: 2020/12/10 16:18
 * @Description:
 */
@Controller
@RequestMapping("/api/file")
@Slf4j(topic = "hoj")
public class FileController {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private FileServiceImpl fileService;

    @Autowired
    private UserInfoServiceImpl userInfoService;

    @Autowired
    private ProblemCaseServiceImpl problemCaseService;

    @Autowired
    private ContestServiceImpl contestService;

    @Autowired
    private ContestRecordServiceImpl contestRecordService;

    @Autowired
    private ContestProblemServiceImpl contestProblemService;

    @Autowired
    private JudgeServiceImpl judgeService;

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
        //若不存在该目录，则创建目录
        FileUtil.mkdir(Constants.File.USER_AVATAR_FOLDER.getPath());
        //通过UUID生成唯一文件名
        String filename = IdUtil.simpleUUID() + "." + suffix;
        try {
            //将文件保存指定目录
            image.transferTo(FileUtil.file(Constants.File.USER_AVATAR_FOLDER.getPath() + File.separator + filename));
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
        userInfoUpdateWrapper.set("avatar", Constants.File.IMG_API.getPath() + filename)
                .eq("uuid", userRolesVo.getUid());
        userInfoService.update(userInfoUpdateWrapper);

        // 插入file表记录
        top.hcode.hoj.pojo.entity.File imgFile = new top.hcode.hoj.pojo.entity.File();
        imgFile.setName(filename).setFolderPath(Constants.File.USER_AVATAR_FOLDER.getPath())
                .setFilePath(Constants.File.USER_AVATAR_FOLDER.getPath() + File.separator + filename)
                .setSuffix(suffix)
                .setType("avatar")
                .setUid(userRolesVo.getUid());
        fileService.saveOrUpdate(imgFile);
        return CommonResult.successResponse(MapUtil.builder()
                .put("uid", userRolesVo.getUid())
                .put("username", userRolesVo.getUsername())
                .put("nickname", userRolesVo.getNickname())
                .put("avatar", Constants.File.IMG_API.getPath() + filename)
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
        String fileDir = Constants.File.TESTCASE_TMP_FOLDER.getPath() + File.separator + fileDirId;
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
            } else if (tmp.getName().endsWith("out") || tmp.getName().endsWith("ans")) {
                outputData.put(tmpPreName, tmp.getName());
            }
        }

        // 进行数据对应检查,同时生成返回数据
        List<HashMap<String, String>> problemCaseList = new LinkedList<>();
        for (String key : inputData.keySet()) {
            // 若有名字不对应，直接返回失败
            if (outputData.getOrDefault(key, null) == null) {
                FileUtil.del(fileDir);
                return CommonResult.errorResponse("请检查数据压缩包里面的in和out、ans文件是否一一对应！");
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
    public void downloadTestcase(@RequestParam("pid") Long pid, HttpServletResponse response) {

        String workDir = Constants.File.TESTCASE_BASE_FOLDER.getPath() + "problem_" + pid;
        File file = new File(workDir);
        if (!file.exists()) { // 本地为空 尝试去数据库查找
            QueryWrapper<ProblemCase> problemCaseQueryWrapper = new QueryWrapper<>();
            problemCaseQueryWrapper.eq("pid", pid);
            List<ProblemCase> problemCaseList = problemCaseService.list(problemCaseQueryWrapper);
            Assert.notEmpty(problemCaseList, "对不起，该题目的评测数据为空！");
            workDir = Constants.File.TESTCASE_DOWNLOAD_TMP_FOLDER.getPath() + File.separator + IdUtil.simpleUUID();
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
        }

        String fileName = "problem_" + pid + "_testcase_" + System.currentTimeMillis() + ".zip";
        // 将对应文件夹的文件压缩成zip
        ZipUtil.zip(workDir, Constants.File.TESTCASE_DOWNLOAD_TMP_FOLDER.getPath() + File.separator + fileName);
        // 将zip变成io流返回给前端
        FileReader fileReader = new FileReader(Constants.File.TESTCASE_DOWNLOAD_TMP_FOLDER.getPath() + File.separator + fileName);
        BufferedInputStream bins = new BufferedInputStream(fileReader.getInputStream());//放到缓冲流里面
        OutputStream outs = null;//获取文件输出IO流
        BufferedOutputStream bouts = null;
        try {
            outs = response.getOutputStream();
            bouts = new BufferedOutputStream(outs);
            response.setContentType("application/x-download");
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            int bytesRead = 0;
            byte[] buffer = new byte[1024 * 10];
            //开始向网络传输文件流
            while ((bytesRead = bins.read(buffer, 0, 1024 * 10)) != -1) {
                bouts.write(buffer, 0, bytesRead);
            }
            bouts.flush();
        } catch (IOException e) {
            log.error("下载题目测试数据的压缩文件异常------------>{}", e.getMessage());
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            Map<String, Object> map = new HashMap<>();
            map.put("status", CommonResult.STATUS_ERROR);
            map.put("msg", "下载文件失败，请重新尝试！");
            map.put("data", null);
            try {
                response.getWriter().println(JSONUtil.toJsonStr(map));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } finally {
            try {
                bins.close();
                if (outs != null) {
                    outs.close();
                }
                if (bouts != null) {
                    bouts.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 清空临时文件
            FileUtil.del(workDir);
            FileUtil.del(Constants.File.TESTCASE_DOWNLOAD_TMP_FOLDER.getPath() + File.separator + fileName);
        }
    }


    @GetMapping("/download-contest-rank")
    @RequiresAuthentication
    public void downloadContestRank(@RequestParam("cid") Long cid,
                                    @RequestParam("forceRefresh") Boolean forceRefresh,
                                    HttpServletRequest request,
                                    HttpServletResponse response) throws IOException {
        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        // 获取本场比赛的状态
        Contest contest = contestService.getById(cid);

        // 是否为超级管理员
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        // 检查比赛权限
        CommonResult commonResult = contestService.checkContestAuth(contest, userRolesVo, isRoot);

        // 不为空表示权限不足
        if (commonResult != null) {
            throw new IllegalArgumentException("对不起，你现在没有权限下载该比赛排行榜数据！");
        }

        // 检查是否需要开启封榜模式
        Boolean isOpenSealRank = contestService.isSealRank(userRolesVo.getUid(), contest, forceRefresh, isRoot);
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("contest_" + contest.getId() + "_rank", "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        response.setHeader("Content-Type", "application/xlsx");

        // 获取题目displayID列表
        QueryWrapper<ContestProblem> contestProblemQueryWrapper = new QueryWrapper<>();
        contestProblemQueryWrapper.eq("cid", contest.getId()).select("display_id");
        List<String> contestProblemDisplayIDList = contestProblemService.list(contestProblemQueryWrapper)
                .stream().map(ContestProblem::getDisplayId).collect(Collectors.toList());

        if (contest.getType().intValue() == Constants.Contest.TYPE_ACM.getCode()) { // ACM比赛

            QueryWrapper<ContestRecord> wrapper = new QueryWrapper<ContestRecord>().eq("cid", cid)
                    .isNotNull("status")
                    // 如果已经开启了封榜模式
                    .notBetween(isOpenSealRank, "submit_time", contest.getSealRankTime(), contest.getEndTime())
                    .orderByAsc("time");
            List<ContestRecord> contestRecordList = contestRecordService.list(wrapper);
            Assert.notEmpty(contestRecordList, "比赛暂无排行榜记录！");
            List<ACMContestRankVo> acmContestRankVoList = contestRecordService.calcACMRank(contestRecordList);
            EasyExcel.write(response.getOutputStream())
                    .head(fileService.getContestRankExcelHead(contestProblemDisplayIDList, true))
                    .sheet("rank")
                    .doWrite(fileService.changeACMContestRankToExcelRowList(acmContestRankVoList, contestProblemDisplayIDList));
        } else {
            List<ContestRecord> oiContestRecord = contestRecordService.getOIContestRecord(cid, isOpenSealRank, contest.getSealRankTime(),contest.getStartTime(), contest.getEndTime());
            Assert.notEmpty(oiContestRecord, "比赛暂无排行榜记录！");
            List<OIContestRankVo> oiContestRankVoList = contestRecordService.calcOIRank(oiContestRecord);
            EasyExcel.write(response.getOutputStream())
                    .head(fileService.getContestRankExcelHead(contestProblemDisplayIDList, false))
                    .sheet("rank")
                    .doWrite(fileService.changOIContestRankToExcelRowList(oiContestRankVoList, contestProblemDisplayIDList));
        }
    }

    private static final ThreadLocal<SimpleDateFormat> threadLocal = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        }
    };

    @GetMapping("/download-contest-ac-submission")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin"}, logical = Logical.OR)
    public void downloadContestACSubmission(@RequestParam("cid") Long cid,
                                            @RequestParam(value = "excludeAdmin", defaultValue = "false") Boolean excludeAdmin,
                                            HttpServletResponse response) {

        Contest contest = contestService.getById(cid);
        boolean isACM = contest.getType().intValue() == Constants.Contest.TYPE_ACM.getCode();

        // cpid-->displayId
        QueryWrapper<ContestProblem> contestProblemQueryWrapper = new QueryWrapper<>();
        contestProblemQueryWrapper.eq("cid", contest.getId());
        List<ContestProblem> contestProblemList = contestProblemService.list(contestProblemQueryWrapper);
        HashMap<Long, String> cpIdMap = new HashMap<>();
        for (ContestProblem contestProblem : contestProblemList) {
            cpIdMap.put(contestProblem.getId(), contestProblem.getDisplayId());
        }

        QueryWrapper<Judge> judgeQueryWrapper = new QueryWrapper<>();
        judgeQueryWrapper.eq("cid", cid)
                .eq(isACM, "status", Constants.Judge.STATUS_ACCEPTED.getStatus())
                .isNotNull(!isACM, "score") // OI模式取得分不为null的
                .between("submit_time", contest.getStartTime(), contest.getEndTime())
                .ne(excludeAdmin, "uid", contest.getUid())
                .orderByDesc("submit_time");

        List<Judge> judgeList = judgeService.list(judgeQueryWrapper);

        List<String> usernameList = judgeList.stream()
                .filter(distinctByKey(Judge::getUsername)) // 根据用户名过滤唯一
                .map(Judge::getUsername).collect(Collectors.toList()); // 映射出用户名列表


        // 打包文件的临时路径 -> username为文件夹名字
        String tmpFilesDir = Constants.File.CONTEST_AC_SUBMISSION_TMP_FOLDER.getPath() + File.separator + IdUtil.fastSimpleUUID();
        FileUtil.mkdir(tmpFilesDir);
        for (String username : usernameList) {
            // 对于每个用户生成对应的文件夹
            String userDir = tmpFilesDir + File.separator + username;
            FileUtil.mkdir(userDir);
            // 如果是ACM模式，则所有提交代码都要生成，如果同一题多次提交AC，加上提交时间秒后缀 ---> A_(666666).c
            // 如果是OI模式就生成最近一次提交即可，且带上分数 ---> A_(666666)_100.c
            List<Judge> userSubmissionList = judgeList.stream()
                    .filter(judge -> judge.getUsername().equals(username)) // 过滤出对应用户的提交
                    .sorted(Comparator.comparing(Judge::getSubmitTime).reversed()) // 根据提交时间进行降序
                    .collect(Collectors.toList());

            for (Judge judge : userSubmissionList) {
                String filePath = userDir + File.separator + cpIdMap.getOrDefault(judge.getCpid(), "null")
                        + "_(" + threadLocal.get().format(judge.getSubmitTime()) + ")";

                // OI模式只取最后一次提交
                if (!isACM) {
                    filePath += "_" + judge.getScore() + "." + languageToFileSuffix(judge.getLanguage().toLowerCase());
                    FileWriter fileWriter = new FileWriter(filePath);
                    fileWriter.write(judge.getCode());
                    break;
                } else {
                    filePath += "." + languageToFileSuffix(judge.getLanguage().toLowerCase());
                    FileWriter fileWriter = new FileWriter(filePath);
                    fileWriter.write(judge.getCode());
                }

            }
        }
        String zipFileName = "contest_" + contest.getId() + "_" + System.currentTimeMillis() + ".zip";
        String zipPath = Constants.File.CONTEST_AC_SUBMISSION_TMP_FOLDER.getPath() + File.separator + zipFileName;
        ZipUtil.zip(tmpFilesDir, zipPath);
        // 将zip变成io流返回给前端
        FileReader zipFileReader = new FileReader(zipPath);
        BufferedInputStream bins = new BufferedInputStream(zipFileReader.getInputStream());//放到缓冲流里面
        OutputStream outs = null;//获取文件输出IO流
        BufferedOutputStream bouts = null;
        try {
            outs = response.getOutputStream();
            bouts = new BufferedOutputStream(outs);
            response.setContentType("application/x-download");
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(zipFileName, "UTF-8"));
            int bytesRead = 0;
            byte[] buffer = new byte[1024 * 10];
            //开始向网络传输文件流
            while ((bytesRead = bins.read(buffer, 0, 1024 * 10)) != -1) {
                bouts.write(buffer, 0, bytesRead);
            }
            // 刷新缓存
            bouts.flush();
        } catch (IOException e) {
            log.error("下载比赛AC提交代码的压缩文件异常------------>{}", e.getMessage());
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            Map<String, Object> map = new HashMap<>();
            map.put("status", CommonResult.STATUS_ERROR);
            map.put("msg", "下载文件失败，请重新尝试！");
            map.put("data", null);
            try {
                response.getWriter().println(JSONUtil.toJsonStr(map));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } finally {
            try {
                bins.close();
                if (outs != null) {
                    outs.close();
                }
                if (bouts != null) {
                    bouts.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileUtil.del(tmpFilesDir);
        FileUtil.del(zipPath);

    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    private static String languageToFileSuffix(String language) {

        List<String> CLang = Arrays.asList("c", "gcc", "clang");
        List<String> CPPLang = Arrays.asList("c++", "g++", "clang++");
        List<String> PythonLang = Arrays.asList("python", "pypy");

        for (String lang : CPPLang) {
            if (language.contains(lang)) {
                return "cpp";
            }
        }

        if (language.contains("c#")) {
            return "cs";
        }

        for (String lang : CLang) {
            if (language.contains(lang)) {
                return "c";
            }
        }

        for (String lang : PythonLang) {
            if (language.contains(lang)) {
                return "py";
            }
        }

        if (language.contains("java")) {
            return "java";
        }

        if (language.contains("pascal")) {
            return "pas";
        }

        if (language.contains("go")) {
            return "go";
        }

        return "txt";
    }


    @RequestMapping(value = "/upload-md-img", method = RequestMethod.POST)
    @RequiresAuthentication
    @ResponseBody
    @Transactional
    @RequiresRoles(value = {"root", "admin"}, logical = Logical.OR)
    public CommonResult uploadMDImg(@RequestParam("image") MultipartFile image) {
        if (image == null) {
            return CommonResult.errorResponse("图片为空！");
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
        FileUtil.mkdir(Constants.File.MARKDOWN_IMG_FOLDER.getPath());

        //通过UUID生成唯一文件名
        String filename = IdUtil.simpleUUID() + "." + suffix;
        try {
            //将文件保存指定目录
            image.transferTo(FileUtil.file(Constants.File.MARKDOWN_IMG_FOLDER.getPath() + File.separator + filename));
        } catch (Exception e) {
            log.error("图片文件上传异常-------------->{}", e.getMessage());
            return CommonResult.errorResponse("服务器异常：图片文件上传失败！", CommonResult.STATUS_ERROR);
        }

        return CommonResult.successResponse(MapUtil.builder()
                        .put("link", Constants.File.IMG_API.getPath() + filename)
                        .put("filePath", Constants.File.MARKDOWN_IMG_FOLDER.getPath() + File.separator + filename).map(),
                "上传图片成功！");

    }


    @RequestMapping(value = "/delete-md-img", method = RequestMethod.GET)
    @RequiresAuthentication
    @ResponseBody
    @Transactional
    @RequiresRoles(value = {"root", "admin"}, logical = Logical.OR)
    public CommonResult uploadMDImg(@RequestParam("filePath") String filePath) {
        boolean result = FileUtil.del(filePath);
        if (result) {
            return CommonResult.successResponse(null, "删除成功");
        } else {
            return CommonResult.errorResponse("删除失败");
        }

    }

}
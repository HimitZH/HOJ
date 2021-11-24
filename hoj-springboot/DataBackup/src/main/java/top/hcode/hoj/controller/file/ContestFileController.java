package top.hcode.hoj.controller.file;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.pojo.entity.contest.ContestPrint;
import top.hcode.hoj.pojo.entity.contest.ContestProblem;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.vo.ACMContestRankVo;
import top.hcode.hoj.pojo.vo.ContestRecordVo;
import top.hcode.hoj.pojo.vo.OIContestRankVo;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.service.common.impl.FileServiceImpl;
import top.hcode.hoj.service.contest.impl.ContestPrintServiceImpl;
import top.hcode.hoj.service.contest.impl.ContestProblemServiceImpl;
import top.hcode.hoj.service.contest.impl.ContestRecordServiceImpl;
import top.hcode.hoj.service.contest.impl.ContestServiceImpl;
import top.hcode.hoj.service.judge.impl.JudgeServiceImpl;
import top.hcode.hoj.utils.Constants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.io.File;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @Author: Himit_ZH
 * @Date: 2021/10/5 19:55
 * @Description:
 */
@Controller
@RequestMapping("/api/file")
@Slf4j(topic = "hoj")
public class ContestFileController {

    @Autowired
    private ContestServiceImpl contestService;

    @Autowired
    private ContestRecordServiceImpl contestRecordService;

    @Autowired
    private ContestProblemServiceImpl contestProblemService;

    @Autowired
    private ContestPrintServiceImpl contestPrintService;

    @Autowired
    private FileServiceImpl fileService;

    @Autowired
    private JudgeServiceImpl judgeService;

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

            List<ContestRecordVo> contestRecordList = contestRecordService.getACMContestRecord(contest.getAuthor(), contest.getId());
            Assert.notEmpty(contestRecordList, "比赛暂无排行榜记录！");
            List<ACMContestRankVo> acmContestRankVoList = contestRecordService.calcACMRank(isOpenSealRank, contest, contestRecordList);
            EasyExcel.write(response.getOutputStream())
                    .head(fileService.getContestRankExcelHead(contestProblemDisplayIDList, true))
                    .sheet("rank")
                    .doWrite(fileService.changeACMContestRankToExcelRowList(acmContestRankVoList, contestProblemDisplayIDList));
        } else {
            List<ContestRecordVo> oiContestRecord = contestRecordService.getOIContestRecord(cid, contest.getAuthor(), isOpenSealRank, contest.getSealRankTime(), contest.getStartTime(), contest.getEndTime());
            Assert.notEmpty(oiContestRecord, "比赛暂无排行榜记录！");
            List<OIContestRankVo> oiContestRankVoList = contestRecordService.calcOIRank(oiContestRecord);
            EasyExcel.write(response.getOutputStream())
                    .head(fileService.getContestRankExcelHead(contestProblemDisplayIDList, false))
                    .sheet("rank")
                    .doWrite(fileService.changOIContestRankToExcelRowList(oiContestRankVoList, contestProblemDisplayIDList));
        }
    }

    @GetMapping("/download-contest-ac-submission")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public void downloadContestACSubmission(@RequestParam("cid") Long cid,
                                            @RequestParam(value = "excludeAdmin", defaultValue = "false") Boolean excludeAdmin,
                                            HttpServletRequest request,
                                            HttpServletResponse response) {

        Contest contest = contestService.getById(cid);

        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");
        // 除非是root 其它管理员只能下载自己的比赛ac记录
        if (!userRolesVo.getUid().equals(contest.getUid()) && !isRoot) {
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            Map<String, Object> map = new HashMap<>();
            map.put("status", CommonResult.STATUS_FORBIDDEN);
            map.put("msg", "对不起，你无权限下载！");
            map.put("data", null);
            try {
                response.getWriter().println(JSONUtil.toJsonStr(map));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            return;
        }

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
                .ne(excludeAdmin, "uid", contest.getUid()) // 排除比赛创建者和root
                .ne(excludeAdmin, "username", "root")
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
                        + "_(" + threadLocalTime.get().format(judge.getSubmitTime()) + ")";

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
            log.error("下载比赛AC提交代码的压缩文件异常------------>", e);
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


    @GetMapping("/download-contest-print-text")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public void downloadContestPrintText(@RequestParam("id") Long id,
                                         HttpServletResponse response) {
        ContestPrint contestPrint = contestPrintService.getById(id);
        String filename = contestPrint.getUsername() + "_Contest_Print.txt";
        String filePath = Constants.File.CONTEST_TEXT_PRINT_FOLDER.getPath() + File.separator + id + File.separator + filename;
        if (!FileUtil.exist(filePath)) {

            FileWriter fileWriter = new FileWriter(filePath);
            fileWriter.write(contestPrint.getContent());
        }

        FileReader zipFileReader = new FileReader(filePath);
        BufferedInputStream bins = new BufferedInputStream(zipFileReader.getInputStream());//放到缓冲流里面
        OutputStream outs = null;//获取文件输出IO流
        BufferedOutputStream bouts = null;
        try {
            outs = response.getOutputStream();
            bouts = new BufferedOutputStream(outs);
            response.setContentType("application/x-download");
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
            int bytesRead = 0;
            byte[] buffer = new byte[1024 * 10];
            //开始向网络传输文件流
            while ((bytesRead = bins.read(buffer, 0, 1024 * 10)) != -1) {
                bouts.write(buffer, 0, bytesRead);
            }
            // 刷新缓存
            bouts.flush();
        } catch (IOException e) {
            log.error("下载比赛打印文本文件异常------------>", e);
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
    }


    private static final ThreadLocal<SimpleDateFormat> threadLocalTime = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        }
    };

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

}
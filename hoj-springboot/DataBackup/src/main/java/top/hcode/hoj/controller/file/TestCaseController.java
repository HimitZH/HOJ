package top.hcode.hoj.controller.file;

import top.hcode.hoj.common.exception.StatusForbiddenException;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.service.file.TestCaseService;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @Author: Himit_ZH
 * @Date: 2021/10/5 19:51
 * @Description:
 */
@Controller
@RequestMapping("/api/file")
public class TestCaseController {

    @Autowired
    private TestCaseService testCaseService;


    @PostMapping("/upload-testcase-zip")
    @ResponseBody
    public CommonResult<Map<Object, Object>> uploadTestcaseZip(@RequestParam("file") MultipartFile file,
                                                               @RequestParam(value = "gid", required = false) Long gid) {
        return testCaseService.uploadTestcaseZip(file, gid);
    }


    @GetMapping("/download-testcase")
    @RequiresAuthentication
    public void downloadTestcase(@RequestParam("pid") Long pid, HttpServletResponse response) throws StatusFailException, StatusForbiddenException {
        testCaseService.downloadTestcase(pid, response);
    }

    @GetMapping("/download-wrongcase-in")
    @RequiresAuthentication
    public void downloadwrongcasein(@RequestParam("pid") Long pid,@RequestParam("in") String in, HttpServletResponse response) throws StatusFailException, StatusForbiddenException {
        testCaseService.downloadwrongcasein(pid, in, response);
    }

    @GetMapping("/download-wrongcase-out")
    @RequiresAuthentication
    public void downloadwrongcaseout(@RequestParam("pid") Long pid,@RequestParam("out") String out, HttpServletResponse response) throws StatusFailException, StatusForbiddenException {
        testCaseService.downloadwrongcaseout(pid, out, response);
    }
}
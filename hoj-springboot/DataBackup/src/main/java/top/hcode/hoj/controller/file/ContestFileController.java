package top.hcode.hoj.controller.file;


import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.service.file.ContestFileService;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * @Author: Himit_ZH
 * @Date: 2021/10/5 19:55
 * @Description:
 */
@Controller
@RequestMapping("/api/file")
public class ContestFileController {

    @Autowired
    private ContestFileService contestFileService;


    @GetMapping("/download-contest-rank")
    @RequiresAuthentication
    public void downloadContestRank(@RequestParam("cid") Long cid,
                                    @RequestParam("forceRefresh") Boolean forceRefresh,
                                    @RequestParam(value = "removeStar", defaultValue = "false") Boolean removeStar,
                                    @RequestParam(value = "containEnd", defaultValue = "false") Boolean isContainsAfterContestJudge,
                                    HttpServletResponse response) throws StatusFailException, IOException, StatusForbiddenException {
        contestFileService.downloadContestRank(cid, forceRefresh, removeStar, isContainsAfterContestJudge, response);
    }

    @GetMapping("/download-contest-ac-submission")
    @RequiresAuthentication
    public void downloadContestACSubmission(@RequestParam("cid") Long cid,
                                            @RequestParam(value = "excludeAdmin", defaultValue = "false") Boolean excludeAdmin,
                                            @RequestParam(value = "splitType", defaultValue = "user") String splitType,
                                            HttpServletResponse response) throws StatusFailException, StatusForbiddenException {

        contestFileService.downloadContestACSubmission(cid, excludeAdmin, splitType, response);
    }


    @GetMapping("/download-contest-print-text")
    @RequiresAuthentication
    public void downloadContestPrintText(@RequestParam("id") Long id, HttpServletResponse response) throws StatusForbiddenException {
        contestFileService.downloadContestPrintText(id, response);
    }


}
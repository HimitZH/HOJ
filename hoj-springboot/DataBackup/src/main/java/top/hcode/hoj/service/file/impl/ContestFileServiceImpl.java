package top.hcode.hoj.service.file.impl;

import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.manager.file.ContestFileManager;
import top.hcode.hoj.service.file.ContestFileService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/10 15:53
 * @Description:
 */
@Service
public class ContestFileServiceImpl implements ContestFileService {

    @Resource
    private ContestFileManager contestFileManager;

    @Override
    public void downloadContestRank(Long cid, Boolean forceRefresh, Boolean removeStar, Boolean isContainsAfterContestJudge,
                                    HttpServletResponse response) throws StatusFailException, IOException, StatusForbiddenException {
        contestFileManager.downloadContestRank(cid, forceRefresh, removeStar, isContainsAfterContestJudge, response);
    }

    @Override
    public void downloadContestACSubmission(Long cid, Boolean excludeAdmin, String splitType, HttpServletResponse response) throws StatusFailException, StatusForbiddenException {
        contestFileManager.downloadContestACSubmission(cid, excludeAdmin, splitType, response);
    }

    @Override
    public void downloadContestPrintText(Long id, HttpServletResponse response) throws StatusForbiddenException {
        contestFileManager.downloadContestPrintText(id, response);
    }
}
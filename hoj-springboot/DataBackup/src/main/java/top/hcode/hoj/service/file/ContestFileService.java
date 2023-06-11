package top.hcode.hoj.service.file;


import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/10 15:43
 * @Description:
 */

public interface ContestFileService {

    public void downloadContestRank(Long cid, Boolean forceRefresh, Boolean removeStar, Boolean isContainsAfterContestJudge,
                                    HttpServletResponse response) throws StatusFailException, IOException, StatusForbiddenException;

    public void downloadContestACSubmission(Long cid, Boolean excludeAdmin, String splitType, HttpServletResponse response) throws StatusFailException, StatusForbiddenException;

    public void downloadContestPrintText(Long id, HttpServletResponse response) throws StatusForbiddenException;
}
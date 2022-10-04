package top.hcode.hoj.service.oj;

import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.vo.ContestScrollBoardInfoVo;
import top.hcode.hoj.pojo.vo.ContestScrollBoardSubmissionVo;

import java.util.List;

/**
 * @Author Himit_ZH
 * @Date 2022/10/3
 */
public interface ContestScrollBoardService{

    public CommonResult<ContestScrollBoardInfoVo> getContestScrollBoardInfo(Long cid);

    public CommonResult<List<ContestScrollBoardSubmissionVo>> getContestScrollBoardSubmission(Long cid);
}

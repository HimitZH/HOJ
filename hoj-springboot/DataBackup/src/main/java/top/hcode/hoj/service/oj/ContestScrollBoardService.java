package top.hcode.hoj.service.oj;

import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.vo.ContestScrollBoardInfoVO;
import top.hcode.hoj.pojo.vo.ContestScrollBoardSubmissionVO;

import java.util.List;

/**
 * @Author Himit_ZH
 * @Date 2022/10/3
 */
public interface ContestScrollBoardService{

    public CommonResult<ContestScrollBoardInfoVO> getContestScrollBoardInfo(Long cid);

    public CommonResult<List<ContestScrollBoardSubmissionVO>> getContestScrollBoardSubmission(Long cid);
}

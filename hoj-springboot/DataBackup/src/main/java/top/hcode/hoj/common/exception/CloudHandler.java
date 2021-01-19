package top.hcode.hoj.common.exception;

import org.springframework.stereotype.Component;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.Judge;
import top.hcode.hoj.service.ToJudgeService;


/**
 * @Author: Himit_ZH
 * @Date: 2020/10/30 10:21
 * @Description:
 */
@Component
public class CloudHandler implements ToJudgeService {

    @Override
    public CommonResult submitProblemJudge(Judge judge) {
        return CommonResult.errorResponse("判题机系统出错，提交进入重判队列，请等待管理员处理！", CommonResult.STATUS_ERROR);
    }
}
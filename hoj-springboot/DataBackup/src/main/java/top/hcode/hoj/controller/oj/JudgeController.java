package top.hcode.hoj.controller.oj;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.dao.JudgeCaseMapper;
import top.hcode.hoj.dao.JudgeMapper;
import top.hcode.hoj.pojo.dto.ToJudgeDto;
import top.hcode.hoj.pojo.entity.Judge;
import top.hcode.hoj.pojo.entity.JudgeCase;
import top.hcode.hoj.pojo.entity.Problem;
import top.hcode.hoj.pojo.vo.JudgeVo;
import top.hcode.hoj.service.JudgeService;
import top.hcode.hoj.service.ProblemService;
import top.hcode.hoj.service.ToJudgeService;
import top.hcode.hoj.utils.IpUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2020/10/27 20:52
 * @Description: 处理代码评判相关业务
 */
@RestController
@RequestMapping("/api")
public class JudgeController {

    @Autowired
    private JudgeService judgeService;

    @Autowired
    private JudgeMapper judgeMapper;

    @Autowired
    private JudgeCaseMapper judgeCaseMapper;

    @Autowired
    private ProblemService problemService;

    @Autowired
    private ToJudgeService toJudgeService;

//    @Autowired
//    private RestTemplate restTemplate;

//    @Value("${service-url.hoj-judge-server}") // restTemplate风格调用不使用
//    private String REST_URL_PREFIX;

//    @GetMapping("/submit-problem-judge")
//    public String list(){
//        return restTemplate.getForObject(REST_URL_PREFIX+"/hoj-judge-server/submit-problem-judge", String.class);
//    }

    /**
     * @MethodName submitProblemJudge
     * @Params  * @param null
     * @Description 核心方法 判题通过openfeign调用判题系统服务
     * @Return CommonResult
     * @Since 2020/10/30
     */
    @RequiresAuthentication
    @RequestMapping(value = "/submit-problem-judge",method = RequestMethod.POST)
    public CommonResult submitProblemJudge(@RequestBody ToJudgeDto judgeDto, HttpServletRequest request){
        // 将提交先写入数据库，准备调用判题服务器
        Judge judge = new Judge();

        int result = judgeMapper.insert(judge.setAuth(judgeDto.getAuth())
                .setCid(judgeDto.getCid())
                .setCode(judgeDto.getCode())
                .setCpid(judgeDto.getCpid())
                .setLanguage(judgeDto.getLanguage())
                .setPid(judgeDto.getPid())
                .setUid(judgeDto.getUid())
                .setStatus(-10) // 开始进入判题队列
                .setSubmitTime(new Date())
                .setIp(IpUtils.getUserIpAddr(request)));

        if (result !=1){
            return CommonResult.errorResponse("数据提交失败",CommonResult.STATUS_ERROR);
        }

        return toJudgeService.submitProblemJudge(judge);
    }

    /**
     * @MethodName getJudgeList
     * @Params * @param null
     * @Description 通用查询判题记录
     * @Return CommonResult
     * @Since 2020/10/29
     */
    @RequestMapping(value = "/get-judge-list",method = RequestMethod.GET)
    public CommonResult getJudgeList(@RequestParam(value = "limit", required = false) Integer limit,
                                     @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                     @RequestParam(value = "searchPid", required = false) Long searchPid,
                                     @RequestParam(value = "searchSource", required = false) String searchSource,
                                     @RequestParam(value = "searchLanguage", required = false) String searchLanguage,
                                     @RequestParam(value = "searchStatus", required = false) Integer searchStatus,
                                     @RequestParam(value = "searchUsername", required = false) String searchUsername,
                                     @RequestParam(value = "searchCid", required = true) Long searchCid) {
        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 30;

        // 设置无传参的空值设定
        if (searchPid == null) { // 表示无题号过滤查询
            searchPid = 0L;
        }
        if (searchStatus == null) { // 表示无结果过滤查询
            searchStatus = -100;
        }

        IPage<JudgeVo> commonJudgeList = judgeService.getCommonJudgeList(limit, currentPage, searchPid, searchSource, searchLanguage,
                searchStatus, searchUsername, searchCid);

        if (commonJudgeList.getTotal() == 0) { // 未查询到一条数据
            return CommonResult.errorResponse("暂无数据", CommonResult.STATUS_NOT_FOUND);
        } else {
            return CommonResult.successResponse(commonJudgeList, "获取成功");
        }
    }

    /**
     * @MethodName getJudgeCase
     * @Params * @param null
     * @Description 获得指定提交id的测试样例，前提是不能为比赛期间的题目！
     * @Return
     * @Since 2020/10/29
     */
    @GetMapping("/get-judge-case")
    public CommonResult getJudgeCase(@RequestParam(value = "submitId", required = true) Long submitId,
                                     @RequestParam(value = "pid", required = true) Long pid) {


        // 如果该题目是还属于比赛期间的题目，则禁止查看测试样例！
        Problem problem = problemService.getById(pid);

        if (problem.getAuth() ==3){ // 3为比赛期间的题目
            return CommonResult.errorResponse("对不起，该题测试样例不能查看！", CommonResult.STATUS_ACCESS_DENIED);
        }


        QueryWrapper<JudgeCase> wrapper = new QueryWrapper<JudgeCase>().eq("submit_id", submitId).orderByAsc("case_id");

        List<JudgeCase> judgeCaseList = judgeCaseMapper.selectList(wrapper);

        if (judgeCaseList.size() == 0) { // 未查询到一条数据
            return CommonResult.errorResponse("暂无数据", CommonResult.STATUS_NOT_FOUND);
        } else {
            return CommonResult.successResponse(judgeCaseList, "获取成功");
        }
    }
}
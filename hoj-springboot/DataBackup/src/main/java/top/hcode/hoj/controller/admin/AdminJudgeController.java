package top.hcode.hoj.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.Judge;
import top.hcode.hoj.pojo.entity.ProblemCount;
import top.hcode.hoj.pojo.entity.UserAcproblem;
import top.hcode.hoj.service.ToJudgeService;
import top.hcode.hoj.service.impl.JudgeServiceImpl;
import top.hcode.hoj.service.impl.ProblemCountServiceImpl;
import top.hcode.hoj.service.impl.UserAcproblemServiceImpl;
import top.hcode.hoj.utils.Constants;

/**
 * @Author: Himit_ZH
 * @Date: 2021/1/3 14:09
 * @Description:
 */

@RestController
@RequestMapping("/admin/judge")
@RequiresAuthentication
@RequiresRoles("root")  // 只有超级管理员能操作
public class AdminJudgeController {

    @Autowired
    private ToJudgeService toJudgeService;

    @Autowired
    private JudgeServiceImpl judgeService;

    @Autowired
    private ProblemCountServiceImpl problemCountService;

    @Autowired
    private UserAcproblemServiceImpl userAcproblemService;

    @GetMapping("/rejudge")
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public CommonResult rejudge(@RequestParam("submitId")Long submitId){
        Judge judge = judgeService.getById(submitId);

        // 如果是非比赛题目
        if (judge.getCid()==0&&judge.getCpid()==0) {
            // 重判前，需要将该题目对应记录表一并更新
            // 更新该题目的提交统计表problem_count
            String columnName = Constants.Judge.getTableColumnNameByStatus(judge.getStatus()); // 获取要修改的字段名
            if (columnName != null) {
                UpdateWrapper<ProblemCount> problemCountUpdateWrapper = new UpdateWrapper<>();
                // 重判需要减掉之前的判题结果
                problemCountUpdateWrapper.setSql(columnName + "=" + columnName + "-1,total=total-1")
                        .eq("pid", judge.getPid());
                problemCountService.update(problemCountUpdateWrapper);
            }
            // 如果该题已经是AC通过状态，更新该题目的用户ac做题表 user_acproblem
            if (judge.getStatus().intValue() == Constants.Judge.STATUS_ACCEPTED.getStatus().intValue()) {
                QueryWrapper<UserAcproblem> userAcproblemQueryWrapper = new QueryWrapper<>();
                userAcproblemQueryWrapper.eq("pid", judge.getPid()).eq("uid", judge.getUid());
                userAcproblemService.remove(userAcproblemQueryWrapper);
            }
        }

        // 设置默认值
        judge.setStatus(Constants.Judge.STATUS_PENDING.getStatus()); // 开始进入判题队列
        judge.setJudger(null).setTime(null).setMemory(null).setErrorMessage(null);
        boolean result = judgeService.updateById(judge);
        // 异步调用判题机
        toJudgeService.submitProblemJudge(judge);
        if (result) {
            return CommonResult.successResponse(judge, "重判成功！该提交已进入判题队列！");
        }else{
            return CommonResult.successResponse(judge, "重判失败！请重新尝试！");
        }
    }
}
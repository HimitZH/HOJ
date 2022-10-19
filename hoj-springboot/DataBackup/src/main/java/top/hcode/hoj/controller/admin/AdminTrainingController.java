package top.hcode.hoj.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.TrainingDTO;
import top.hcode.hoj.pojo.dto.TrainingProblemDTO;
import top.hcode.hoj.pojo.entity.training.Training;
import top.hcode.hoj.pojo.entity.training.TrainingProblem;
import top.hcode.hoj.service.admin.training.AdminTrainingProblemService;
import top.hcode.hoj.service.admin.training.AdminTrainingService;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * @Author Himit_ZH
 * @Since 2022/01/23 20:22
 * @Description
 */
@RestController
@RequestMapping("/api/admin/training")
public class AdminTrainingController {

    @Resource
    private AdminTrainingService adminTrainingService;

    @Resource
    private AdminTrainingProblemService adminTrainingProblemService;


    @GetMapping("/get-training-list")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<IPage<Training>> getTrainingList(@RequestParam(value = "limit", required = false) Integer limit,
                                                         @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                                         @RequestParam(value = "keyword", required = false) String keyword) {
        return adminTrainingService.getTrainingList(limit, currentPage, keyword);
    }

    @GetMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<TrainingDTO> getTraining(@RequestParam("tid") Long tid) {
        return adminTrainingService.getTraining(tid);
    }

    @DeleteMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = "root") // 只有超级管理员能删除训练
    public CommonResult<Void> deleteTraining(@RequestParam("tid") Long tid) {
        return adminTrainingService.deleteTraining(tid);
    }

    @PostMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> addTraining(@RequestBody TrainingDTO trainingDto) {
        return adminTrainingService.addTraining(trainingDto);
    }

    @PutMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> updateTraining(@RequestBody TrainingDTO trainingDto) {
        return adminTrainingService.updateTraining(trainingDto);
    }


    @PutMapping("/change-training-status")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> changeTrainingStatus(@RequestParam(value = "tid", required = true) Long tid,
                                                   @RequestParam(value = "author", required = true) String author,
                                                   @RequestParam(value = "status", required = true) Boolean status) {
        return adminTrainingService.changeTrainingStatus(tid, author, status);
    }

    @GetMapping("/get-problem-list")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<HashMap<String, Object>> getProblemList(@RequestParam(value = "limit", required = false) Integer limit,
                                                                @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                                                @RequestParam(value = "keyword", required = false) String keyword,
                                                                @RequestParam(value = "queryExisted", defaultValue = "false") Boolean queryExisted,
                                                                @RequestParam(value = "tid", required = true) Long tid) {
        return adminTrainingProblemService.getProblemList(limit, currentPage, keyword, queryExisted, tid);
    }


    @PutMapping("/problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> updateProblem(@RequestBody TrainingProblem trainingProblem) {
        return adminTrainingProblemService.updateProblem(trainingProblem);
    }

    @DeleteMapping("/problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> deleteProblem(@RequestParam("pid") Long pid,
                                            @RequestParam(value = "tid", required = false) Long tid) {

        return adminTrainingProblemService.deleteProblem(pid, tid);
    }

    @PostMapping("/add-problem-from-public")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult<Void> addProblemFromPublic(@RequestBody TrainingProblemDTO trainingProblemDto) {
        return adminTrainingProblemService.addProblemFromPublic(trainingProblemDto);
    }

    @GetMapping("/import-remote-oj-problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> importTrainingRemoteOJProblem(@RequestParam("name") String name,
                                                            @RequestParam("problemId") String problemId,
                                                            @RequestParam("tid") Long tid) {
        return adminTrainingProblemService.importTrainingRemoteOJProblem(name, problemId, tid);
    }

}
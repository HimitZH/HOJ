package top.hcode.hoj.manager.admin.problem;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.dao.problem.ProblemEntityService;
import top.hcode.hoj.pojo.dto.ChangeGroupProblemProgressDTO;
import top.hcode.hoj.pojo.entity.problem.Problem;

import javax.annotation.Resource;

/**
 * @Author Himit_ZH
 * @Date 2022/4/13
 */
@Component
public class AdminGroupProblemManager {

    @Resource
    private ProblemEntityService problemEntityService;

    public IPage<Problem> list(Integer currentPage, Integer limit, String keyword, Long gid) {
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;
        IPage<Problem> iPage = new Page<>(currentPage, limit);
        QueryWrapper<Problem> problemQueryWrapper = new QueryWrapper<>();
        problemQueryWrapper.select("id", "gid", "apply_public_progress", "problem_id", "title", "author", "type", "judge_mode")
                .eq(gid != null, "gid", gid)
                .isNotNull("gid")
                .isNotNull("apply_public_progress")
                .orderByAsc("apply_public_progress", "gid");

        if (!StringUtils.isEmpty(keyword)) {
            problemQueryWrapper.and(wrapper -> wrapper.like("title", keyword).or()
                    .like("author", keyword).or()
                    .like("problem_id", keyword));
        }

        return problemEntityService.page(iPage, problemQueryWrapper);
    }

    public void changeProgress(ChangeGroupProblemProgressDTO changeGroupProblemProgressDto) throws StatusFailException {
        Long pid = changeGroupProblemProgressDto.getPid();
        Integer progress = changeGroupProblemProgressDto.getProgress();
        if (pid == null || progress == null) {
            throw new StatusFailException("请求参数pid或者progress不能为空！");
        }
        QueryWrapper<Problem> problemQueryWrapper = new QueryWrapper<>();
        problemQueryWrapper.select("id", "is_group", "gid", "apply_public_progress").eq("id", pid);
        Problem problem = problemEntityService.getOne(problemQueryWrapper);
        if (problem == null) {
            throw new StatusFailException("错误：当前题目已不存在！");
        }
        problem.setApplyPublicProgress(progress);
        switch (progress) {
            case 1:
            case 3:
                problem.setIsGroup(true);
                break;
            case 2:
                problem.setIsGroup(false);
                break;
            default:
                throw new StatusFailException("请求参数错误：progress请使用1~3");
        }
        boolean isOk = problemEntityService.updateById(problem);
        if (!isOk) {
            throw new StatusFailException("修改失败！");
        }
    }
}

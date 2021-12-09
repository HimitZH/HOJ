package top.hcode.hoj.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.crawler.problem.ProblemStrategy;
import top.hcode.hoj.pojo.dto.AnnouncementDto;
import top.hcode.hoj.pojo.dto.ProblemDto;
import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.pojo.entity.contest.ContestAnnouncement;
import top.hcode.hoj.pojo.entity.contest.ContestProblem;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.vo.AdminContestVo;
import top.hcode.hoj.pojo.vo.AnnouncementVo;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.service.common.impl.AnnouncementServiceImpl;
import top.hcode.hoj.service.contest.impl.ContestAnnouncementServiceImpl;
import top.hcode.hoj.service.contest.impl.ContestProblemServiceImpl;
import top.hcode.hoj.service.contest.impl.ContestServiceImpl;
import top.hcode.hoj.service.judge.impl.JudgeServiceImpl;
import top.hcode.hoj.service.problem.impl.ProblemServiceImpl;
import top.hcode.hoj.utils.Constants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


/**
 * @Author: Himit_ZH
 * @Date: 2020/12/19 22:28
 * @Description:
 */
@RestController
@RequestMapping("/api/admin/contest")
public class AdminContestController {

    @Autowired
    private ContestServiceImpl contestService;

    @Autowired
    private ProblemServiceImpl problemService;

    @Autowired
    private ContestProblemServiceImpl contestProblemService;

    @Autowired
    private ContestAnnouncementServiceImpl contestAnnouncementService;

    @Autowired
    private AnnouncementServiceImpl announcementService;

    @Autowired
    private JudgeServiceImpl judgeService;

    @GetMapping("/get-contest-list")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult getContestList(@RequestParam(value = "limit", required = false) Integer limit,
                                       @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                       @RequestParam(value = "keyword", required = false) String keyword) {

        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;
        IPage<Contest> iPage = new Page<>(currentPage, limit);
        QueryWrapper<Contest> queryWrapper = new QueryWrapper<>();
        // 过滤密码
        queryWrapper.select(Contest.class, info -> !info.getColumn().equals("pwd"));
        if (!StringUtils.isEmpty(keyword)) {
            keyword = keyword.trim();
            queryWrapper
                    .like("title", keyword).or()
                    .like("id", keyword);
        }

        queryWrapper.orderByDesc("start_time");

        IPage<Contest> contestList = contestService.page(iPage, queryWrapper);
        if (contestList.getTotal() == 0) { // 未查询到一条数据
            return CommonResult.successResponse(contestList, "暂无数据");
        } else {
            return CommonResult.successResponse(contestList, "获取成功");
        }
    }

    @GetMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult getContest(@RequestParam("cid") Long cid, HttpServletRequest request) {

        // 获取本场比赛的状态
        Contest contest = contestService.getById(cid);
        if (contest == null) { // 查询不存在
            return CommonResult.errorResponse("查询失败：该比赛不存在,请检查参数cid是否准确！");
        }
        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        // 是否为超级管理员
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        // 只有超级管理员和比赛拥有者才能操作
        if (!isRoot && !userRolesVo.getUid().equals(contest.getUid())) {
            return CommonResult.errorResponse("对不起，你无权限操作！", CommonResult.STATUS_FORBIDDEN);
        }
        AdminContestVo adminContestVo = BeanUtil.copyProperties(contest, AdminContestVo.class, "starAccount");
        if (StringUtils.isEmpty(contest.getStarAccount())){
            adminContestVo.setStarAccount(new ArrayList<>());
        }else {
            JSONObject jsonObject = JSONUtil.parseObj(contest.getStarAccount());
            List<String> starAccount = jsonObject.get("star_account", List.class);
            adminContestVo.setStarAccount(starAccount);
        }
        return CommonResult.successResponse(adminContestVo, "查询成功！");
    }

    @DeleteMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = "root") // 只有超级管理员能删除比赛
    public CommonResult deleteContest(@RequestParam("cid") Long cid) {
        boolean result = contestService.removeById(cid);
        /*
        contest的id为其他表的外键的表中的对应数据都会被一起删除！
         */
        if (result) { // 删除成功
            return CommonResult.successResponse(null, "删除成功！");
        } else {
            return CommonResult.errorResponse("删除失败！", CommonResult.STATUS_FAIL);
        }
    }

    @PostMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult addContest(@RequestBody AdminContestVo adminContestVo) {
        Contest contest = BeanUtil.copyProperties(adminContestVo, Contest.class, "starAccount");
        JSONObject accountJson = new JSONObject();
        accountJson.set("star_account", adminContestVo.getStarAccount());
        contest.setStarAccount(accountJson.toString());
        boolean result = contestService.save(contest);
        if (result) { // 添加成功
            return CommonResult.successResponse(null, "添加成功！");
        } else {
            return CommonResult.errorResponse("添加失败", CommonResult.STATUS_FAIL);
        }
    }

    @PutMapping("")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult updateContest(@RequestBody AdminContestVo adminContestVo, HttpServletRequest request) {

        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        // 是否为超级管理员
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");
        // 只有超级管理员和比赛拥有者才能操作
        if (!isRoot && !userRolesVo.getUid().equals(adminContestVo.getUid())) {
            return CommonResult.errorResponse("对不起，你无权限操作！", CommonResult.STATUS_FORBIDDEN);
        }
        Contest contest = BeanUtil.copyProperties(adminContestVo, Contest.class, "starAccount");
        JSONObject accountJson = new JSONObject();
        accountJson.set("star_account", adminContestVo.getStarAccount());
        contest.setStarAccount(accountJson.toString());
        boolean result = contestService.saveOrUpdate(contest);
        if (result) {
            return CommonResult.successResponse(null, "修改成功！");
        } else {
            return CommonResult.errorResponse("修改失败", CommonResult.STATUS_FAIL);
        }
    }

    @PutMapping("/change-contest-visible")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult changeContestVisible(@RequestParam(value = "cid", required = true) Long cid,
                                             @RequestParam(value = "uid", required = true) String uid,
                                             @RequestParam(value = "visible", required = true) Boolean visible,
                                             HttpServletRequest request) {

        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        // 是否为超级管理员
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");
        // 只有超级管理员和比赛拥有者才能操作
        if (!isRoot && !userRolesVo.getUid().equals(uid)) {
            return CommonResult.errorResponse("对不起，你无权限操作！", CommonResult.STATUS_FORBIDDEN);
        }

        boolean result = contestService.saveOrUpdate(new Contest().setId(cid).setVisible(visible));
        if (result) { // 添加成功
            return CommonResult.successResponse(null, "修改成功！");
        } else {
            return CommonResult.errorResponse("修改失败", CommonResult.STATUS_FAIL);
        }
    }

    /**
     * 以下为比赛的题目的增删改查操作接口
     */

    @GetMapping("/get-problem-list")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    @Transactional(rollbackFor = Exception.class)
    public CommonResult getProblemList(@RequestParam(value = "limit", required = false) Integer limit,
                                       @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                       @RequestParam(value = "keyword", required = false) String keyword,
                                       @RequestParam(value = "cid", required = true) Long cid,
                                       @RequestParam(value = "problemType", required = false) Integer problemType,
                                       @RequestParam(value = "oj", required = false) String oj) {
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;
        IPage<Problem> iPage = new Page<>(currentPage, limit);
        // 根据cid在ContestProblem表中查询到对应pid集合
        QueryWrapper<ContestProblem> contestProblemQueryWrapper = new QueryWrapper<>();
        contestProblemQueryWrapper.eq("cid", cid);
        List<Long> pidList = new LinkedList<>();

        List<ContestProblem> contestProblemList = contestProblemService.list(contestProblemQueryWrapper);
        HashMap<Long, Object> contestProblemMap = new HashMap<>();
        contestProblemList.forEach(contestProblem -> {
            contestProblemMap.put(contestProblem.getPid(), contestProblem);
            pidList.add(contestProblem.getPid());
        });

        HashMap<String, Object> contestProblem = new HashMap<>();
        if (pidList.size() == 0 && problemType == null) { // 该比赛原本就无题目数据
            contestProblem.put("problemList", pidList);
            contestProblem.put("contestProblemMap", contestProblemMap);
            return CommonResult.successResponse(contestProblem, "获取成功");
        }

        QueryWrapper<Problem> problemQueryWrapper = new QueryWrapper<>();

        if (problemType != null) { // 必备条件 隐藏的不可取来做比赛题目
            problemQueryWrapper
                    // vj题目不限制赛制
                    .and(wrapper -> wrapper.eq("type", problemType)
                            .or().eq("is_remote", true))
                    .ne("auth", 2); // 同时需要与比赛相同类型的题目，权限需要是公开的（隐藏的不可加入！）
        }

        // 逻辑判断，如果是查询已有的就应该是in，如果是查询不要重复的，使用not in
        if (problemType != null) {
            problemQueryWrapper.notIn(pidList.size() > 0, "id", pidList);
        } else {
            problemQueryWrapper.in(pidList.size() > 0, "id", pidList);
        }


        // 根据oj筛选过滤
        if (oj != null && !"All".equals(oj)) {
            if (!Constants.RemoteOJ.isRemoteOJ(oj)) {
                problemQueryWrapper.eq("is_remote", false);
            } else {
                problemQueryWrapper.eq("is_remote", true).likeRight("problem_id", oj);
            }
        }

        if (!StringUtils.isEmpty(keyword)) {
            problemQueryWrapper.and(wrapper -> wrapper.like("title", keyword).or()
                    .like("problem_id", keyword).or()
                    .like("author", keyword));
        }

        IPage<Problem> problemList = problemService.page(iPage, problemQueryWrapper);
        contestProblem.put("problemList", problemList);
        contestProblem.put("contestProblemMap", contestProblemMap);

        return CommonResult.successResponse(contestProblem, "获取成功");

    }

    @GetMapping("/problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult getProblem(@Valid @RequestParam("pid") Long pid) {
        Problem problem = problemService.getById(pid);
        if (problem != null) { // 查询成功
            return CommonResult.successResponse(problem, "查询成功！");
        } else {
            return CommonResult.errorResponse("查询失败！", CommonResult.STATUS_FAIL);
        }
    }

    @DeleteMapping("/problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "problem_admin"}, logical = Logical.OR)
    @Transactional(rollbackFor = Exception.class)
    public CommonResult deleteProblem(@RequestParam("pid") Long pid,
                                      @RequestParam(value = "cid", required = false) Long cid) {

        //  比赛id不为null，表示就是从比赛列表移除而已
        if (cid != null) {
            QueryWrapper<ContestProblem> contestProblemQueryWrapper = new QueryWrapper<>();
            contestProblemQueryWrapper.eq("cid", cid).eq("pid", pid);
            contestProblemService.remove(contestProblemQueryWrapper);
            // 把该题目在比赛的提交全部删掉
            UpdateWrapper<Judge> judgeUpdateWrapper = new UpdateWrapper<>();
            judgeUpdateWrapper.eq("cid", cid).eq("pid", pid);
            judgeService.remove(judgeUpdateWrapper);
        } else {
             /*
                problem的id为其他表的外键的表中的对应数据都会被一起删除！
              */
            problemService.removeById(pid);
        }

        if (cid == null) {
            FileUtil.del(Constants.File.TESTCASE_BASE_FOLDER.getPath() + File.separator + "problem_" + pid);
        }
        return CommonResult.successResponse(null, "删除成功！");

    }

    @PostMapping("/problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    @Transactional(rollbackFor = Exception.class)
    public CommonResult addProblem(@RequestBody ProblemDto problemDto) {

        QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("problem_id", problemDto.getProblem().getProblemId().toUpperCase());
        Problem problem = problemService.getOne(queryWrapper);
        if (problem != null) {
            return CommonResult.errorResponse("该题目的Problem ID已存在，请更换！", CommonResult.STATUS_FAIL);
        }
        // 设置为比赛题目
        problemDto.getProblem().setAuth(3);
        boolean result = problemService.adminAddProblem(problemDto);
        if (result) { // 添加成功
            // 顺便返回新的题目id，好下一步添加外键操作
            return CommonResult.successResponse(MapUtil.builder().put("pid", problemDto.getProblem().getId()).map(), "添加成功！");
        } else {
            return CommonResult.errorResponse("添加失败", CommonResult.STATUS_FAIL);
        }

    }

    @PutMapping("/problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    @Transactional(rollbackFor = Exception.class)
    public CommonResult updateProblem(@RequestBody ProblemDto problemDto, HttpServletRequest request) {

        QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("problem_id", problemDto.getProblem().getProblemId().toUpperCase());
        Problem problem = problemService.getOne(queryWrapper);

        // 如果problem_id不是原来的且已存在该problem_id，则修改失败！
        if (problem != null && problem.getId().longValue() != problemDto.getProblem().getId()) {
            return CommonResult.errorResponse("当前的Problem ID 已被使用，请重新更换新的！", CommonResult.STATUS_FAIL);
        }
        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        // 记录修改题目的用户
        problemDto.getProblem().setModifiedUser(userRolesVo.getUsername());
        boolean result = problemService.adminUpdateProblem(problemDto);
        if (result) { // 更新成功
            return CommonResult.successResponse(null, "修改成功！");
        } else {
            return CommonResult.errorResponse("修改失败", CommonResult.STATUS_FAIL);
        }
    }

    @GetMapping("/contest-problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult getContestProblem(@RequestParam(value = "cid", required = true) Long cid,
                                          @RequestParam(value = "pid", required = true) Long pid) {
        QueryWrapper<ContestProblem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cid", cid).eq("pid", pid);
        ContestProblem contestProblem = contestProblemService.getOne(queryWrapper);
        if (contestProblem != null) {
            return CommonResult.successResponse(contestProblem, "查询成功！");
        } else {
            return CommonResult.errorResponse("查询失败", CommonResult.STATUS_FAIL);
        }
    }

    @PutMapping("/contest-problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult setContestProblem(@RequestBody ContestProblem contestProblem) {
        boolean result = contestProblemService.saveOrUpdate(contestProblem);
        if (result) {
            contestProblemService.syncContestRecord(contestProblem.getPid(), contestProblem.getCid(), contestProblem.getDisplayId());
            return CommonResult.successResponse(contestProblem, "更新成功！");
        } else {
            return CommonResult.errorResponse("更新失败", CommonResult.STATUS_FAIL);
        }
    }

    @PutMapping("/change-problem-auth")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "problem_admin", "admin"}, logical = Logical.OR)
    public CommonResult changeProblemAuth(@RequestBody Problem problem, HttpServletRequest request) {

        // 普通管理员只能将题目变成隐藏题目和比赛题目
        boolean root = SecurityUtils.getSubject().hasRole("root");

        boolean problemAdmin = SecurityUtils.getSubject().hasRole("problem_admin");

        if (!problemAdmin && !root && problem.getAuth() == 1) {
            return CommonResult.errorResponse("修改失败！你无权限公开题目！", CommonResult.STATUS_FORBIDDEN);
        }

        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        UpdateWrapper<Problem> problemUpdateWrapper = new UpdateWrapper<>();
        problemUpdateWrapper.eq("id", problem.getId())
                .set("auth", problem.getAuth())
                .set("modified_user", userRolesVo.getUsername());

        boolean result = problemService.update(problemUpdateWrapper);
        if (result) { // 更新成功
            return CommonResult.successResponse(null, "修改成功！");
        } else {
            return CommonResult.errorResponse("修改失败", CommonResult.STATUS_FAIL);
        }
    }

    @PostMapping("/add-problem-from-public")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult addProblemFromPublic(@RequestBody HashMap<String, String> params) {

        String pidStr = params.get("pid");
        String cidStr = params.get("cid");
        String displayId = params.get("displayId");
        if (StringUtils.isEmpty(pidStr) || StringUtils.isEmpty(cidStr) || StringUtils.isEmpty(displayId)) {
            return CommonResult.errorResponse("参数错误！", CommonResult.STATUS_FAIL);
        }

        Long pid = Long.valueOf(pidStr);
        Long cid = Long.valueOf(cidStr);

        QueryWrapper<ContestProblem> contestProblemQueryWrapper = new QueryWrapper<>();
        contestProblemQueryWrapper.eq("cid", cid)
                .and(wrapper -> wrapper.eq("pid", pid)
                        .or()
                        .eq("display_id", displayId));
        ContestProblem contestProblem = contestProblemService.getOne(contestProblemQueryWrapper, false);
        if (contestProblem != null) {
            return CommonResult.errorResponse("添加失败，该题目已添加或者题目的比赛展示ID已存在！", CommonResult.STATUS_FAIL);
        }

        // 比赛中题目显示默认为原标题
        Problem problem = problemService.getById(pid);
        String displayName = problem.getTitle();

        // 修改成比赛题目
        boolean updateProblem = problemService.saveOrUpdate(problem.setAuth(3));

        boolean result = contestProblemService.saveOrUpdate(new ContestProblem()
                .setCid(cid).setPid(pid).setDisplayTitle(displayName).setDisplayId(displayId));
        if (result && updateProblem) { // 添加成功
            return CommonResult.successResponse(null, "添加成功！");
        } else {
            return CommonResult.errorResponse("添加失败", CommonResult.STATUS_FAIL);
        }
    }

    @GetMapping("/import-remote-oj-problem")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    @Transactional(rollbackFor = Exception.class)
    public CommonResult importContestRemoteOJProblem(@RequestParam("name") String name,
                                                     @RequestParam("problemId") String problemId,
                                                     @RequestParam("cid") Long cid,
                                                     @RequestParam("displayId") String displayId,
                                                     HttpServletRequest request) {

        QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("problem_id", name.toUpperCase() + "-" + problemId);
        Problem problem = problemService.getOne(queryWrapper, false);

        // 如果该题目不存在，需要先导入
        if (problem == null) {
            HttpSession session = request.getSession();
            UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
            try {
                ProblemStrategy.RemoteProblemInfo otherOJProblemInfo = problemService.getOtherOJProblemInfo(name.toUpperCase(), problemId, userRolesVo.getUsername());
                if (otherOJProblemInfo != null) {
                    problem = problemService.adminAddOtherOJProblem(otherOJProblemInfo, name);
                    if (problem == null) {
                        return CommonResult.errorResponse("导入新题目失败！请重新尝试！");
                    }
                } else {
                    return CommonResult.errorResponse("导入新题目失败！原因：可能是与该OJ链接超时或题号格式错误！");
                }
            } catch (Exception e) {
                return CommonResult.errorResponse(e.getMessage());
            }
        }

        QueryWrapper<ContestProblem> contestProblemQueryWrapper = new QueryWrapper<>();
        Problem finalProblem = problem;
        contestProblemQueryWrapper.eq("cid", cid)
                .and(wrapper -> wrapper.eq("pid", finalProblem.getId())
                        .or()
                        .eq("display_id", displayId));
        ContestProblem contestProblem = contestProblemService.getOne(contestProblemQueryWrapper, false);
        if (contestProblem != null) {
            return CommonResult.errorResponse("添加失败，该题目已添加或者题目的比赛展示ID已存在！", CommonResult.STATUS_FAIL);
        }


        // 比赛中题目显示默认为原标题
        String displayName = problem.getTitle();

        // 修改成比赛题目
        boolean updateProblem = problemService.saveOrUpdate(problem.setAuth(3));

        boolean result = contestProblemService.saveOrUpdate(new ContestProblem()
                .setCid(cid).setPid(problem.getId()).setDisplayTitle(displayName).setDisplayId(displayId));

        if (result && updateProblem) { // 添加成功
            return CommonResult.successResponse(null, "添加成功！");
        } else {
            return CommonResult.errorResponse("添加失败", CommonResult.STATUS_FAIL);
        }
    }

    /**
     * 以下处理比赛公告的操作请求
     */

    @GetMapping("/announcement")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult getAnnouncementList(@RequestParam(value = "limit", required = false) Integer limit,
                                            @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                            @RequestParam(value = "cid", required = true) Long cid) {
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;

        IPage<AnnouncementVo> announcementList = announcementService.getContestAnnouncement(cid, false, limit, currentPage);
        if (announcementList.getTotal() == 0) { // 未查询到一条数据
            return CommonResult.successResponse(announcementList, "暂无数据");
        } else {
            return CommonResult.successResponse(announcementList, "获取成功");
        }
    }

    @DeleteMapping("/announcement")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult deleteAnnouncement(@Valid @RequestParam("aid") Long aid) {
        boolean result = announcementService.removeById(aid);
        if (result) { // 删除成功
            return CommonResult.successResponse(null, "删除成功！");
        } else {
            return CommonResult.errorResponse("删除失败！", CommonResult.STATUS_FAIL);
        }
    }

    @PostMapping("/announcement")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    @Transactional(rollbackFor = Exception.class)
    public CommonResult addAnnouncement(@RequestBody AnnouncementDto announcementDto) {
        boolean result1 = announcementService.save(announcementDto.getAnnouncement());
        boolean result2 = contestAnnouncementService.saveOrUpdate(new ContestAnnouncement().setAid(announcementDto.getAnnouncement().getId())
                .setCid(announcementDto.getCid()));
        if (result1 && result2) { // 添加成功
            return CommonResult.successResponse(null, "添加成功！");
        } else {
            return CommonResult.errorResponse("添加失败", CommonResult.STATUS_FAIL);
        }
    }

    @PutMapping("/announcement")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public CommonResult updateAnnouncement(@RequestBody AnnouncementDto announcementDto) {
        boolean result = announcementService.saveOrUpdate(announcementDto.getAnnouncement());
        if (result) { // 更新成功
            return CommonResult.successResponse(null, "修改成功！");
        } else {
            return CommonResult.errorResponse("修改失败", CommonResult.STATUS_FAIL);
        }
    }
}
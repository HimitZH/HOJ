package top.hcode.hoj.schedule;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.hcode.hoj.dao.common.FileEntityService;
import top.hcode.hoj.dao.judge.JudgeEntityService;
import top.hcode.hoj.dao.msg.AdminSysNoticeEntityService;
import top.hcode.hoj.dao.msg.UserSysNoticeEntityService;
import top.hcode.hoj.dao.problem.ProblemEntityService;
import top.hcode.hoj.dao.user.SessionEntityService;
import top.hcode.hoj.dao.user.UserInfoEntityService;
import top.hcode.hoj.dao.user.UserRecordEntityService;
import top.hcode.hoj.manager.msg.AdminNoticeManager;
import top.hcode.hoj.pojo.entity.common.File;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.entity.msg.AdminSysNotice;
import top.hcode.hoj.pojo.entity.msg.UserSysNotice;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.user.Session;
import top.hcode.hoj.pojo.entity.user.UserInfo;
import top.hcode.hoj.pojo.entity.user.UserRecord;
import top.hcode.hoj.service.admin.rejudge.RejudgeService;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.JsoupUtils;
import top.hcode.hoj.utils.RedisUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * 一个cron表达式有至少6个（也可能7个）有空格分隔的时间元素。按顺序依次为：
 * <p>
 * 字段	允许值	允许的特殊字符
 * 秒	0~59	, - * /
 * 分	0~59	, - * /
 * 小时	0~23	, - * /
 * 日期	1-31	, - * ? / L W C
 * 月份	1~12或者JAN~DEC	, - * /
 * 星期	1~7或者SUN~SAT	, - * ? / L C #
 * 年（可选）	留空，1970~2099	, - * /
 * <p>
 * “*”  字符代表所有可能的值
 * “-”  字符代表数字范围 例如1-5
 * “/”  字符用来指定数值的增量
 * “？” 字符仅被用于天（月）和天（星期）两个子表达式，表示不指定值。
 * 当2个子表达式其中之一被指定了值以后，为了避免冲突，需要将另一个子表达式的值设为“？”
 * “L” 字符仅被用于天（月）和天（星期）两个子表达式，它是单词“last”的缩写
 * 如果在“L”前有具体的内容，它就具有其他的含义了。
 * “W” 字符代表着平日(Mon-Fri)，并且仅能用于日域中。它用来指定离指定日的最近的一个平日。
 * 大部分的商业处理都是基于工作周的，所以 W 字符可能是非常重要的。
 * "C" 代表“Calendar”的意思。它的意思是计划所关联的日期，如果日期没有被关联，则相当于日历中所有日期。
 */
@Service
@Slf4j(topic = "hoj")
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private FileEntityService fileEntityService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private UserInfoEntityService userInfoEntityService;

    @Autowired
    private UserRecordEntityService userRecordEntityService;

    @Resource
    private SessionEntityService sessionEntityService;

    @Resource
    private AdminSysNoticeEntityService adminSysNoticeEntityService;

    @Resource
    private UserSysNoticeEntityService userSysNoticeEntityService;

    @Resource
    private JudgeEntityService judgeEntityService;

    @Resource
    private RejudgeService rejudgeService;

    @Resource
    private ProblemEntityService problemEntityService;

    @Resource
    private AdminNoticeManager adminNoticeManager;

    @Resource
    private ApplicationContext applicationContext;

    /**
     * @MethodName deleteAvatar
     * @Params * @param null
     * @Description 每天3点定时查询数据库字段并删除未引用的头像
     * @Return
     * @Since 2021/1/13
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Override
    public void deleteAvatar() {
        List<File> files = fileEntityService.queryDeleteAvatarList();
        // 如果查不到，直接结束
        if (files.isEmpty()) {
            return;
        }
        List<Long> idLists = new LinkedList<>();
        for (File file : files) {
            if (file.getDelete()) {
                boolean delSuccess = FileUtil.del(file.getFilePath());
                if (delSuccess) {
                    idLists.add(file.getId());
                }
            }
        }

        boolean isSuccess = fileEntityService.removeByIds(idLists);
        if (!isSuccess) {
            log.error("数据库file表删除头像数据失败----------------->sql语句执行失败");
        }
    }


    /**
     * @MethodName deleteTestCase
     * @Params * @param null
     * @Description 每天3点定时删除指定文件夹的上传测试数据
     * @Return
     * @Since 2021/2/7
     */
    @Scheduled(cron = "0 0 3 * * *")
//    @Scheduled(cron = "0/5 * * * * *")
    @Override
    public void deleteTestCase() {
        boolean result = FileUtil.del(Constants.File.TESTCASE_TMP_FOLDER.getPath());
        if (!result) {
            log.error("每日定时任务异常------------------------>{}", "清除本地的题目测试数据失败!");
        }
    }

    /**
     * @MethodName deleteContestPrintText
     * @Params * @param null
     * @Description 每天4点定时删除本地的比赛打印数据
     * @Return
     * @Since 2021/9/19
     */
    @Scheduled(cron = "0 0 4 * * *")
    @Override
    public void deleteContestPrintText() {
        boolean result = FileUtil.del(Constants.File.CONTEST_TEXT_PRINT_FOLDER.getPath());
        if (!result) {
            log.error("每日定时任务异常------------------------>{}", "清除本地的比赛打印数据失败!");
        }
    }

    /**
     * 每两小时获取其他OJ的比赛列表，并保存在redis里
     * 保存格式：
     * oj: "Codeforces",
     * title: "Codeforces Round #680 (Div. 1, based on VK Cup 2020-2021 - Final)",
     * beginTime: "2020-11-08T05:00:00Z",
     * endTime: "2020-11-08T08:00:00Z",
     */
    @Scheduled(cron = "0 0 0/2 * * *")
//    @Scheduled(cron = "0/5 * * * * *")
    @Override
    public void getOjContestsList() {
        // 待格式化的API，需要填充年月查询
        String nowcoderContestAPI = "https://ac.nowcoder.com/acm/calendar/contest?token=&month=%d-%d";
        // 将获取的比赛列表添加进这里
        List<Map<String, Object>> contestsList = new ArrayList<>();
        // 获取当前年月
        DateTime dateTime = DateUtil.date();
        // offsetMonth 增加的月份，只枚举最近3个月的比赛
        for (int offsetMonth = 0; offsetMonth <= 2; offsetMonth++) {
            // 月份增加i个月
            DateTime newDate = DateUtil.offsetMonth(dateTime, offsetMonth);
            // 格式化API 月份从0-11，所以要加一
            String contestAPI = String.format(nowcoderContestAPI, newDate.year(), newDate.month() + 1);
            try {
                // 连接api，获取json格式对象
                JSONObject resultObject = JsoupUtils.getJsonFromConnection(JsoupUtils.getConnectionFromUrl(contestAPI, null, null));
                // 比赛列表存放在data字段中
                JSONArray contestsArray = resultObject.getJSONArray("data");
                // 牛客比赛列表按时间顺序排序，所以从后向前取可以减少不必要的遍历
                for (int i = contestsArray.size() - 1; i >= 0; i--) {
                    JSONObject contest = contestsArray.getJSONObject(i);
                    // 如果比赛已经结束了，则直接结束
                    if (contest.getLong("endTime", 0L) < dateTime.getTime()) {
                        break;
                    }
                    // 把比赛列表信息添加在List里
                    contestsList.add(MapUtil.builder(new HashMap<String, Object>())
                            .put("oj", contest.getStr("ojName"))
                            .put("url", contest.getStr("link"))
                            .put("title", contest.getStr("contestName"))
                            .put("beginTime", new Date(contest.getLong("startTime")))
                            .put("endTime", new Date(contest.getLong("endTime"))).map());
                }
            } catch (Exception e) {
                log.error("爬虫爬取Nowcoder比赛异常----------------------->{}", e.getMessage());
            }
        }
        // 把比赛列表按照开始时间排序，方便查看
        contestsList.sort((o1, o2) -> {

            long beginTime1 = ((Date) o1.get("beginTime")).getTime();
            long beginTime2 = ((Date) o2.get("beginTime")).getTime();

            return Long.compare(beginTime1, beginTime2);
        });

        // 获取对应的redis key
        String redisKey = Constants.Schedule.RECENT_OTHER_CONTEST.getCode();
        // 缓存时间一天
        redisUtils.set(redisKey, contestsList, 60 * 60 * 24);
        // 增加log提示
        log.info("获取牛客API的比赛列表成功！共获取数据" + contestsList.size() + "条");
    }


    /**
     * 每天3点获取codeforces的rating分数
     */
    @Scheduled(cron = "0 0 3 * * *")
//    @Scheduled(cron = "0/5 * * * * *")
    @Override
    public void getCodeforcesRating() {
        String codeforcesUserInfoAPI = "https://codeforces.com/api/user.info?handles=%s";
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        // 查询cf_username不为空的数据
        userInfoQueryWrapper.isNotNull("cf_username");
        List<UserInfo> userInfoList = userInfoEntityService.list(userInfoQueryWrapper);
        for (UserInfo userInfo : userInfoList) {
            // 获取cf名字
            String cfUsername = userInfo.getCfUsername();
            // 获取uuid
            String uuid = userInfo.getUuid();
            // 格式化api
            String ratingAPI = String.format(codeforcesUserInfoAPI, cfUsername);
            try {
                // 连接api，获取json格式对象
                ScheduleServiceImpl service = applicationContext.getBean(ScheduleServiceImpl.class);
                JSONObject resultObject = service.getCFUserInfo(ratingAPI);
                // 获取状态码
                String status = resultObject.getStr("status");
                // 如果查无此用户，则跳过
                if ("FAILED".equals(status)) {
                    continue;
                }
                // 用户信息存放在result列表中的第0个
                JSONObject cfUserInfo = resultObject.getJSONArray("result").getJSONObject(0);
                // 获取cf的分数
                Integer cfRating = cfUserInfo.getInt("rating", null);
                UpdateWrapper<UserRecord> userRecordUpdateWrapper = new UpdateWrapper<>();
                // 将对应的cf分数修改
                userRecordUpdateWrapper.eq("uid", uuid).set("rating", cfRating);
                boolean result = userRecordEntityService.update(userRecordUpdateWrapper);
                if (!result) {
                    log.error("插入UserRecord表失败------------------------------->");
                }

            } catch (Exception e) {
                log.error("爬虫爬取Codeforces Rating分数异常----------------------->{}", e.getMessage());
            }
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.info("获取Codeforces Rating成功！");
    }

    @Retryable(value = Exception.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 1000, multiplier = 1.4))
    public JSONObject getCFUserInfo(String url) throws Exception {
        return JsoupUtils.getJsonFromConnection(JsoupUtils.getConnectionFromUrl(url, null, null));
    }


    /**
     * @MethodName deleteUserSession
     * @Params * @param null
     * @Description 每天3点定时删除用户半年的session表记录
     * @Return
     * @Since 2021/9/6
     */
    @Scheduled(cron = "0 0 3 * * *")
//    @Scheduled(cron = "0/5 * * * * *")
    @Override
    public void deleteUserSession() {
        QueryWrapper<Session> sessionQueryWrapper = new QueryWrapper<>();
        DateTime dateTime = DateUtil.offsetMonth(new Date(), -6);
        String strTime = DateFormatUtils.format(dateTime, "yyyy-MM-dd HH:mm:ss");
        sessionQueryWrapper.select("distinct uid");
        sessionQueryWrapper.apply("UNIX_TIMESTAMP(gmt_create) >= UNIX_TIMESTAMP('" + strTime + "')");
        List<Session> sessionList = sessionEntityService.list(sessionQueryWrapper);
        if (sessionList.size() > 0) {
            List<String> uidList = sessionList.stream().map(Session::getUid).collect(Collectors.toList());
            QueryWrapper<Session> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("uid", uidList)
                    .apply("UNIX_TIMESTAMP('" + strTime + "') > UNIX_TIMESTAMP(gmt_create)");
            List<Session> needDeletedSessionList = sessionEntityService.list(queryWrapper);
            if (needDeletedSessionList.size() > 0) {
                List<Long> needDeletedIdList = needDeletedSessionList.stream().map(Session::getId).collect(Collectors.toList());
                boolean isOk = sessionEntityService.removeByIds(needDeletedIdList);
                if (!isOk) {
                    log.error("=============数据库session表定时删除用户6个月前的记录失败===============");
                }
            }
        }
    }


    /**
     * @MethodName syncNoticeToUser
     * @Description 每一小时拉取系统通知表admin_sys_notice到表user_sys_notice(只推送给半年内有登录过的用户)
     * @Return
     * @Since 2021/10/3
     */
    @Override
    @Scheduled(cron = "0 0 0/1 * * *")
    public void syncNoticeToRecentHalfYearUser() {
        QueryWrapper<AdminSysNotice> adminSysNoticeQueryWrapper = new QueryWrapper<>();
        adminSysNoticeQueryWrapper.eq("state", false);
        List<AdminSysNotice> adminSysNotices = adminSysNoticeEntityService.list(adminSysNoticeQueryWrapper);
        if (adminSysNotices.size() == 0) {
            return;
        }

        QueryWrapper<Session> sessionQueryWrapper = new QueryWrapper<>();
        sessionQueryWrapper.select("DISTINCT uid");
        List<Session> sessionList = sessionEntityService.list(sessionQueryWrapper);
        List<String> userIds = sessionList.stream().map(Session::getUid).collect(Collectors.toList());

        for (AdminSysNotice adminSysNotice : adminSysNotices) {
            switch (adminSysNotice.getType()) {
                case "All":
                    List<UserSysNotice> userSysNoticeList = new ArrayList<>();
                    for (String uid : userIds) {
                        UserSysNotice userSysNotice = new UserSysNotice();
                        userSysNotice.setRecipientId(uid)
                                .setType("Sys")
                                .setSysNoticeId(adminSysNotice.getId());
                        userSysNoticeList.add(userSysNotice);
                    }
                    boolean isOk1 = userSysNoticeEntityService.saveOrUpdateBatch(userSysNoticeList);
                    if (isOk1) {
                        adminSysNotice.setState(true);
                    }
                    break;
                case "Single":
                    UserSysNotice userSysNotice = new UserSysNotice();
                    userSysNotice.setRecipientId(adminSysNotice.getRecipientId())
                            .setType("Mine")
                            .setSysNoticeId(adminSysNotice.getId());
                    boolean isOk2 = userSysNoticeEntityService.saveOrUpdate(userSysNotice);
                    if (isOk2) {
                        adminSysNotice.setState(true);
                    }
                    break;
                case "Admin":
                    break;
            }

        }

        boolean isUpdateNoticeOk = adminSysNoticeEntityService.saveOrUpdateBatch(adminSysNotices);
        if (!isUpdateNoticeOk) {
            log.error("=============推送系统通知更新状态失败===============");
        }

    }

    @Override
    @Scheduled(cron = "0 0/20 * * * ?")
    public void check20MPendingSubmission() {
        DateTime dateTime = DateUtil.offsetMinute(new Date(), -15);
        String strTime = DateFormatUtils.format(dateTime, "yyyy-MM-dd HH:mm:ss");

        QueryWrapper<Judge> judgeQueryWrapper = new QueryWrapper<>();
        judgeQueryWrapper.select("distinct submit_id");
        judgeQueryWrapper.eq("status", Constants.Judge.STATUS_PENDING.getStatus());
        judgeQueryWrapper.apply("UNIX_TIMESTAMP('" + strTime + "') > UNIX_TIMESTAMP(gmt_modified)");
        List<Judge> judgeList = judgeEntityService.list(judgeQueryWrapper);
        if (!CollectionUtils.isEmpty(judgeList)) {
            log.info("Half An Hour Check Pending Submission to Rejudge:" + Arrays.toString(judgeList.toArray()));
            for (Judge judge : judgeList) {
                rejudgeService.rejudge(judge.getSubmitId());
            }
        }
    }

    /**
     * 每天6点检查一次有没有处于正在申请中的团队题目申请公开的进度单子，发消息给超级管理和题目管理员
     */
    @Override
    @Scheduled(cron = "0 0 6 * * *")
//    @Scheduled(cron = "0/5 * * * * *")
    public void checkUnHandleGroupProblemApplyProgress() {
        QueryWrapper<Problem> problemQueryWrapper = new QueryWrapper<>();
        problemQueryWrapper.eq("apply_public_progress", 1).isNotNull("gid");
        int count = problemEntityService.count(problemQueryWrapper);
        if (count > 0) {
            String title = "团队题目审批通知(Group Problem Approval Notice)";
            String content = getDissolutionGroupContent(count);
            List<String> superAdminUidList = userInfoEntityService.getSuperAdminUidList();
            List<String> problemAdminUidList = userInfoEntityService.getProblemAdminUidList();
            if (!CollectionUtils.isEmpty(problemAdminUidList)) {
                superAdminUidList.addAll(problemAdminUidList);
            }
            adminNoticeManager.addSingleNoticeToBatchUser(null, superAdminUidList, title, content, "Sys");
        }
    }

    private String getDissolutionGroupContent(int count) {
        return "您好，尊敬的管理员，目前有**" + count +
                "**条团队题目正在申请公开的单子，请您尽快前往后台 [团队题目审批](/admin/group-problem/apply) 进行审批！"
                + "\n\n" +
                "Hello, dear administrator, there are currently **" + count
                + "** problem problems applying for public list. " +
                "Please go to the backstage [Group Problem Examine](/admin/group-problem/apply) for approval as soon as possible!";
    }

}

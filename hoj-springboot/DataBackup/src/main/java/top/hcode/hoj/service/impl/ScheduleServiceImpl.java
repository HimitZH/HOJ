package top.hcode.hoj.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import top.hcode.hoj.pojo.entity.File;
import top.hcode.hoj.pojo.entity.UserInfo;
import top.hcode.hoj.pojo.entity.UserRecord;
import top.hcode.hoj.service.ScheduleService;
import top.hcode.hoj.service.UserInfoService;
import top.hcode.hoj.service.UserRecordService;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.JsoupUtils;
import top.hcode.hoj.utils.RedisUtils;

import java.util.*;


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
@Async
@Slf4j
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private FileServiceImpl fileService;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private UserInfoService userInfoDao;
    @Autowired
    private UserRecordService userRecordDao;

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
        List<File> files = fileService.queryDeleteAvatarList();
        // 如果查不到，直接结束
        if (files.isEmpty()) {
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        List<Long> idLists = new LinkedList<>();
        for (File file : files) {
            stringBuilder.append(file.getFilePath()).append(" ");
            idLists.add(file.getId());
        }
        //
        //  WINDOWS系统下删除 DEL + 指令符
        //  /P            删除每一个文件之前提示确认。
        //  /F            强制删除只读文件。
        //  /S            删除所有子目录中的指定的文件。
        //  /Q            安静模式。删除全局通配符时，不要求确认
        //  /A            根据属性选择要删除的文件
        Process process = null;
        try {
            String osName = System.getProperty("os.name");
            String execution;
            // 判断系统，执行不同删除代码
            if ("Windows 10".compareTo(osName) == 0) {
                execution = "cmd.exe /c DEL /F /S /Q " + stringBuilder.toString();
            } else {
                execution = "rm -rf " + stringBuilder.toString();
            }
            process = Runtime.getRuntime().exec(execution);
            process.waitFor();
            int result = process.exitValue();                 //为0表示执行成功，非0表示shell执行出错
            if (result != 0) {
                log.error("头像删除异常----------------->Shell执行失败");
            } else {
                boolean isSuccess = fileService.removeByIds(idLists);
                if (!isSuccess) {
                    log.error("数据库file表删除头像数据失败----------------->sql语句执行失败");
                }
            }

        } catch (Exception e) {
            log.error("头像删除异常--------------------->{}", e.getMessage());
        } finally {
            if (process != null) {
                process.destroy();
            }
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
        Calendar calendar = Calendar.getInstance();
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
        contestsList.sort((o1, o2) -> (int) (((Date) o1.get("beginTime")).getTime() - ((Date) o2.get("beginTime")).getTime()));
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
//    @Scheduled(cron = "0 0 3 * * *")
    @Scheduled(cron = "0/5 * * * * *")
    @Override
    public void getCodeforcesRating() {
        String codeforcesUserInfoAPI = "https://codeforces.com/api/user.info?handles=%s";
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        // 查询cf_username不为空的数据
        userInfoQueryWrapper.isNotNull("cf_username");
        List<UserInfo> userInfoList = userInfoDao.list(userInfoQueryWrapper);
        for (UserInfo userInfo : userInfoList) {
            // 获取cf名字
            String cfUsername = userInfo.getCfUsername();
            // 获取uuid
            String uuid = userInfo.getUuid();
            // 格式化api
            String ratingAPI = String.format(codeforcesUserInfoAPI, cfUsername);
            try {
                // 连接api，获取json格式对象
                JSONObject resultObject = JsoupUtils.getJsonFromConnection(JsoupUtils.getConnectionFromUrl(ratingAPI, null, null));
                // 获取状态码
                String status = resultObject.getStr("status");
                // 如果查无此用户，则跳过
                if ("FAILED".equals(status)) {
                    continue;
                }
                // 用户信息存放在result列表中的第0个
                JSONObject cfUserInfo = resultObject.getJSONArray("result").getJSONObject(0);
                // 获取cf的分数
                int cfRating = cfUserInfo.getInt("rating");
                UpdateWrapper<UserRecord> userRecordUpdateWrapper = new UpdateWrapper<>();
                // 将对应的cf分数修改
                userRecordUpdateWrapper.eq("uid", uuid).set("rating", cfRating);
                boolean result = userRecordDao.update(userRecordUpdateWrapper);
                if (!result) {
                    log.error("插入UserRecord表失败------------------------------->");
                }

            } catch (Exception e) {
                log.error("爬虫爬取Codeforces Rating分数异常----------------------->{}", e.getMessage());
            }
        }
        log.info("获取Codeforces Rating成功！");
    }


}

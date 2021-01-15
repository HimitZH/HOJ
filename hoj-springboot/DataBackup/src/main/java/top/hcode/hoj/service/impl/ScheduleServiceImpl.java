package top.hcode.hoj.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import top.hcode.hoj.pojo.entity.File;
import top.hcode.hoj.service.ScheduleService;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.JsoupUtils;
import top.hcode.hoj.utils.RedisUtils;

import java.text.SimpleDateFormat;
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

    /**
     * @MethodName deleteAvatar
     * @Params * @param null
     * @Description 每天4点定时查询数据库字段并删除未引用的头像
     * @Return
     * @Since 2021/1/13
     */
    @Scheduled(cron = "0 0 4 * * *")
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
            stringBuilder.append(file.getFilePath() + " ");
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
            process.destroy();
        }
    }


    /**
     * 获取其他OJ的比赛列表，并保存在redis里
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
        String codeforcesContestAPI = "https://codeforces.com/api/contest.list";
        List<Map<String, Object>> contestsList = new ArrayList<>();
        try {
            JSONObject resultObject = JsoupUtils.getJsonFromConnection(JsoupUtils.getConnectionFromUrl(codeforcesContestAPI, null, null));
            JSONArray contestsArray = resultObject.getJSONArray("result");
            for (int i = 0; i < contestsArray.size(); i++) {
                JSONObject contest = contestsArray.getJSONObject(i);
                // 如果比赛已经结束了，则停止获取
                if ("FINISHED".equals(contest.getStr("phase", "FINISHED"))) {
                    break;
                }
                // 把比赛列表信息添加在List里
                contestsList.add(MapUtil.builder(new HashMap<String, Object>())
                        .put("oj", "Codeforces")
                        .put("title", contest.getStr("name"))
                        .put("beginTime", new Date(contest.getLong("startTimeSeconds") * 1000L))
                        .put("endTime", new Date((contest.getLong("startTimeSeconds") + contest.getLong("durationSeconds")) * 1000L)).map());
            }
        } catch (Exception e) {
            log.error("爬虫爬取Codeforces比赛异常----------------------->{}", e.getMessage());
        }
        // 把所有比赛列表排序，将来题库变多时用到
        contestsList.sort((o1, o2) -> (int) (((Date) o1.get("beginTime")).getTime() - ((Date) o2.get("beginTime")).getTime()));
//        System.out.println(contestsList);
        String redisKey = Constants.Schedule.RECENT_OTHER_CONTEST.getCode();
        // redis缓存比赛列表时间一天
        redisUtils.set(redisKey, contestsList, 60 * 60 * 24);
    }

}

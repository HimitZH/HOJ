package top.hcode.hoj.judge;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import org.springframework.stereotype.Component;
import top.hcode.hoj.common.exception.SystemError;
import top.hcode.hoj.judge.entity.JudgeDTO;
import top.hcode.hoj.judge.entity.JudgeGlobalDTO;
import top.hcode.hoj.judge.task.DefaultJudge;
import top.hcode.hoj.judge.task.InteractiveJudge;
import top.hcode.hoj.judge.task.SpecialJudge;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.util.Constants;
import top.hcode.hoj.util.JudgeUtils;
import top.hcode.hoj.util.ThreadPoolUtils;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;
import java.util.concurrent.*;

/**
 * @Author: Himit_ZH
 * @Date: 2021/4/16 12:15
 * @Description: 判题流程解耦重构3.0，该类负责输入数据进入程序进行测评
 */
@Component
public class JudgeRun {

    @Resource
    private DefaultJudge defaultJudge;

    @Resource
    private SpecialJudge specialJudge;

    @Resource
    private InteractiveJudge interactiveJudge;

    public List<JSONObject> judgeAllCase(Long submitId,
                                         Problem problem,
                                         String judgeLanguage,
                                         String testCasesDir,
                                         JSONObject testCasesInfo,
                                         String userFileId,
                                         String userFileSrc,
                                         Boolean getUserOutput)
            throws SystemError, ExecutionException, InterruptedException {

        if (testCasesInfo == null) {
            throw new SystemError("The evaluation data of the problem does not exist", null, null);
        }

        List<FutureTask<JSONObject>> futureTasks = new ArrayList<>();
        JSONArray testcaseList = (JSONArray) testCasesInfo.get("testCases");

        // 默认给题目限制时间+200ms用来测评
        Long testTime = (long) problem.getTimeLimit() + 200;

        Constants.JudgeMode judgeMode = Constants.JudgeMode.getJudgeMode(problem.getJudgeMode());

        if (judgeMode == null) {
            throw new RuntimeException("The judge mode of problem " + problem.getProblemId() + " error:" + problem.getJudgeMode());
        }

        // 用户输出的文件夹
        String runDir = Constants.JudgeDir.RUN_WORKPLACE_DIR.getContent() + File.separator + submitId;

        Constants.RunConfig runConfig = Constants.RunConfig.getRunnerByLanguage(judgeLanguage);
        Constants.RunConfig spjConfig = Constants.RunConfig.getRunnerByLanguage("SPJ-" + problem.getSpjLanguage());
        Constants.RunConfig interactiveConfig = Constants.RunConfig.getRunnerByLanguage("INTERACTIVE-" + problem.getSpjLanguage());

        JudgeGlobalDTO judgeGlobalDTO = JudgeGlobalDTO.builder()
                .problemId(problem.getId())
                .judgeMode(judgeMode)
                .userFileId(userFileId)
                .userFileSrc(userFileSrc)
                .runDir(runDir)
                .testTime(testTime)
                .maxMemory((long) problem.getMemoryLimit())
                .maxTime((long) problem.getTimeLimit())
                .maxStack(problem.getStackLimit())
                .testCaseInfo(testCasesInfo)
                .judgeExtraFiles(JudgeUtils.getProblemExtraFileMap(problem, "judge"))
                .runConfig(runConfig)
                .spjRunConfig(spjConfig)
                .interactiveRunConfig(interactiveConfig)
                .needUserOutputFile(getUserOutput)
                .removeEOLBlank(problem.getIsRemoveEndBlank())
                .build();

        for (int index = 0; index < testcaseList.size(); index++) {
            JSONObject testcase = (JSONObject) testcaseList.get(index);
            // 将每个需要测试的线程任务加入任务列表中
            final int testCaseId = index + 1;
            // 输入文件名
            final String inputFileName = testcase.getStr("inputName");
            // 输出文件名
            final String outputFileName = testcase.getStr("outputName");
            // 题目数据的输入文件的路径
            final String testCaseInputPath = testCasesDir + File.separator + inputFileName;
            // 题目数据的输出文件的路径
            final String testCaseOutputPath = testCasesDir + File.separator + outputFileName;
            // 数据库表的测试样例id
            final Long caseId = testcase.getLong("caseId", null);
            // 该测试点的满分
            final Integer score = testcase.getInt("score", 0);

            final Long maxOutputSize = Math.max(testcase.getLong("outputSize", 0L) * 2, 16 * 1024 * 1024L);

            JudgeDTO judgeDTO = JudgeDTO.builder()
                    .testCaseId(testCaseId)
                    .testCaseInputPath(testCaseInputPath)
                    .testCaseOutputPath(testCaseOutputPath)
                    .maxOutputSize(maxOutputSize)
                    .build();

            futureTasks.add(new FutureTask<>(() -> {
                JSONObject result;
                switch (judgeMode) {
                    case DEFAULT:
                        result = defaultJudge.judge(judgeDTO, judgeGlobalDTO);
                        break;
                    case SPJ:
                        result = specialJudge.judge(judgeDTO, judgeGlobalDTO);
                        break;
                    case INTERACTIVE:
                        result = interactiveJudge.judge(judgeDTO, judgeGlobalDTO);
                        break;
                    default:
                        throw new RuntimeException("The problem mode is error:" + judgeMode);
                }
                result.set("caseId", caseId);
                result.set("score", score);
                result.set("inputFileName", inputFileName);
                result.set("outputFileName", outputFileName);
                return result;
            }));
        }

        // 提交到线程池进行执行
        for (FutureTask<JSONObject> futureTask : futureTasks) {
            ThreadPoolUtils.getInstance().getThreadPool().submit(futureTask);
        }
        List<JSONObject> result = new LinkedList<>();
        while (futureTasks.size() > 0) {
            Iterator<FutureTask<JSONObject>> iterable = futureTasks.iterator();
            //遍历一遍
            while (iterable.hasNext()) {
                FutureTask<JSONObject> future = iterable.next();
                if (future.isDone() && !future.isCancelled()) {
                    // 获取线程返回结果
                    JSONObject tmp = future.get();
                    result.add(tmp);
                    // 任务完成移除任务
                    iterable.remove();
                } else {
                    Thread.sleep(10); // 避免CPU高速运转，这里休息10毫秒
                }
            }
        }
        return result;
    }

}
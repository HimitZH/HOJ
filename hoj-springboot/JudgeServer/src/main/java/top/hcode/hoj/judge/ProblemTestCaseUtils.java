package top.hcode.hoj.judge;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import top.hcode.hoj.common.exception.SystemError;
import top.hcode.hoj.dao.ProblemCaseEntityService;
import top.hcode.hoj.pojo.entity.problem.ProblemCase;
import top.hcode.hoj.util.Constants;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @Author: Himit_ZH
 * @Date: 2021/4/16 13:21
 * @Description: 判题流程解耦重构2.0，该类只负责题目测试数据的检查与初始化
 */
@Component
public class ProblemTestCaseUtils {

    @Autowired
    private ProblemCaseEntityService problemCaseEntityService;

    private final static Pattern EOL_PATTERN = Pattern.compile("[^\\S\\n]+(?=\\n)");

    // 本地无文件初始化测试数据，写成json文件
    public JSONObject initTestCase(List<HashMap<String, Object>> testCases,
                                   Long problemId,
                                   String version,
                                   String judgeMode,
                                   String judgeCaseMode) throws SystemError {

        if (testCases == null || testCases.size() == 0) {
            throw new SystemError("题号为：" + problemId + "的评测数据为空！", null, "The test cases does not exist.");
        }

        if (StringUtils.isEmpty(judgeCaseMode)) {
            judgeCaseMode = Constants.JudgeCaseMode.DEFAULT.getMode();
        }
        JSONObject result = new JSONObject();
        result.set("mode", judgeMode);
        result.set("version", version);
        result.set("judgeCaseMode", judgeCaseMode);
        result.set("testCasesSize", testCases.size());

        JSONArray testCaseList = new JSONArray(testCases.size());

        String testCasesDir = Constants.JudgeDir.TEST_CASE_DIR.getContent() + "/problem_" + problemId;

        // 无论有没有测试数据，一旦执行该函数，一律清空，重新生成该题目对应的测试数据文件

        FileUtil.del(testCasesDir);
        for (int index = 0; index < testCases.size(); index++) {
            JSONObject jsonObject = new JSONObject();
            String inputName = (index + 1) + ".in";
            jsonObject.set("caseId", testCases.get(index).get("caseId"));
            if (judgeCaseMode.equals(Constants.JudgeCaseMode.SUBTASK_AVERAGE.getMode())
                    || judgeCaseMode.equals(Constants.JudgeCaseMode.SUBTASK_LOWEST.getMode())) {
                jsonObject.set("groupNum", testCases.get(index).getOrDefault("groupNum", null));
            }
            jsonObject.set("score", testCases.get(index).getOrDefault("score", null));
            jsonObject.set("inputName", inputName);
            // 生成对应文件
            FileWriter infileWriter = new FileWriter(testCasesDir + "/" + inputName, CharsetUtil.UTF_8);
            // 将该测试数据的输入写入到文件
            infileWriter.write((String) testCases.get(index).get("input"));

            String outputName = (index + 1) + ".out";
            jsonObject.set("outputName", outputName);
            // 生成对应文件
            String outputData = (String) testCases.get(index).get("output");
            FileWriter outFile = new FileWriter(testCasesDir + "/" + outputName, CharsetUtil.UTF_8);
            outFile.write(outputData);

            // spj或interactive是根据特判程序输出判断结果，所以无需初始化测试数据
            if (Constants.JudgeMode.DEFAULT.getMode().equals(judgeMode)) {
                // 原数据MD5
                jsonObject.set("outputMd5", DigestUtils.md5DigestAsHex(outputData.getBytes(StandardCharsets.UTF_8)));
                // 原数据大小
                jsonObject.set("outputSize", outputData.getBytes(StandardCharsets.UTF_8).length);
                // 去掉全部空格的MD5，用来判断pe
                jsonObject.set("allStrippedOutputMd5", DigestUtils.md5DigestAsHex(outputData.replaceAll("\\s+", "").getBytes(StandardCharsets.UTF_8)));
                // 默认去掉文末空格的MD5
                jsonObject.set("EOFStrippedOutputMd5", DigestUtils.md5DigestAsHex(rtrim(outputData).getBytes(StandardCharsets.UTF_8)));
            }

            testCaseList.add(jsonObject);
        }

        result.set("testCases", testCaseList);

        FileWriter infoFile = new FileWriter(testCasesDir + File.separator + "info", CharsetUtil.UTF_8);
        // 写入记录文件
        infoFile.write(JSONUtil.toJsonStr(result));
        return result;
    }

    // 本地有文件，进行数据初始化 生成json文件
    public JSONObject initLocalTestCase(String judgeMode,
                                        String judgeCaseMode,
                                        String version,
                                        String testCasesDir,
                                        List<ProblemCase> problemCaseList) {


        if (StringUtils.isEmpty(judgeCaseMode)) {
            judgeCaseMode = Constants.JudgeCaseMode.DEFAULT.getMode();
        }

        JSONObject result = new JSONObject();
        result.set("mode", judgeMode);
        result.set("judgeCaseMode", judgeCaseMode);
        result.set("version", version);
        result.set("testCasesSize", problemCaseList.size());
        result.set("testCases", new JSONArray());

        for (ProblemCase problemCase : problemCaseList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("caseId", problemCase.getId());
            if (judgeCaseMode.equals(Constants.JudgeCaseMode.SUBTASK_AVERAGE.getMode())
                    || judgeCaseMode.equals(Constants.JudgeCaseMode.SUBTASK_LOWEST.getMode())) {
                jsonObject.set("groupNum", problemCase.getGroupNum());
            }
            jsonObject.set("score", problemCase.getScore());
            jsonObject.set("inputName", problemCase.getInput());
            jsonObject.set("outputName", problemCase.getOutput());

            // 读取输出文件
            String output = "";
            String outputFilePath = testCasesDir + File.separator + problemCase.getOutput();
            if (FileUtil.exist(outputFilePath)) {
                FileReader outputFile = new FileReader(outputFilePath, CharsetUtil.UTF_8);
                output = outputFile.readString().replaceAll("\r\n", "\n");
                FileWriter outFileWriter = new FileWriter(testCasesDir + File.separator + problemCase.getOutput(), CharsetUtil.UTF_8);
                outFileWriter.write(output);
            } else {
                FileWriter fileWriter = new FileWriter(outputFilePath);
                fileWriter.write("");
            }

            // spj或interactive是根据特判程序输出判断结果，所以无需初始化测试数据
            if (Constants.JudgeMode.DEFAULT.getMode().equals(judgeMode)) {
                // 原数据MD5
                jsonObject.set("outputMd5", DigestUtils.md5DigestAsHex(output.getBytes(StandardCharsets.UTF_8)));
                // 原数据大小
                jsonObject.set("outputSize", output.getBytes(StandardCharsets.UTF_8).length);
                // 去掉全部空格的MD5，用来判断pe
                jsonObject.set("allStrippedOutputMd5", DigestUtils.md5DigestAsHex(output.replaceAll("\\s+", "").getBytes(StandardCharsets.UTF_8)));
                // 默认去掉文末空格的MD5
                jsonObject.set("EOFStrippedOutputMd5", DigestUtils.md5DigestAsHex(rtrim(output).getBytes(StandardCharsets.UTF_8)));
            }

            ((JSONArray) result.get("testCases")).put(jsonObject);
        }

        FileWriter infoFile = new FileWriter(testCasesDir + File.separator + "info", CharsetUtil.UTF_8);
        // 写入记录文件
        infoFile.write(JSONUtil.toJsonStr(result));

        return result;
    }


    // 获取指定题目的info数据
    public JSONObject loadTestCaseInfo(Long problemId, String testCasesDir, String version, String judgeMode, String judgeCaseMode) throws SystemError {
        if (FileUtil.exist(testCasesDir + File.separator + "info")) {
            FileReader fileReader = new FileReader(testCasesDir + File.separator + "info", CharsetUtil.UTF_8);
            String infoStr = fileReader.readString();
            JSONObject testcaseInfo = JSONUtil.parseObj(infoStr);
            // 测试样例被改动需要重新生成
            if (!testcaseInfo.getStr("version", null).equals(version)) {
                return tryInitTestCaseInfo(testCasesDir, problemId, version, judgeMode, judgeCaseMode);
            }
            return testcaseInfo;
        } else {
            return tryInitTestCaseInfo(testCasesDir, problemId, version, judgeMode, judgeCaseMode);
        }
    }

    // 若没有测试数据，则尝试从数据库获取并且初始化到本地，如果数据库中该题目测试数据为空，rsync同步也出了问题，则直接判系统错误
    public JSONObject tryInitTestCaseInfo(String testCasesDir,
                                          Long problemId,
                                          String version,
                                          String judgeMode,
                                          String judgeCaseMode) throws SystemError {

        QueryWrapper<ProblemCase> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pid", problemId);
        List<ProblemCase> problemCases = problemCaseEntityService.list(queryWrapper);

        if (problemCases.size() == 0) { // 数据库也为空的话
            throw new SystemError("problemID:[" + problemId + "] test case has not found.", null, null);
        }

        // 可能是zip上传记录的是文件名，
        if (StringUtils.isEmpty(problemCases.get(0).getInput())
                || StringUtils.isEmpty(problemCases.get(0).getOutput())
                || (problemCases.get(0).getInput().endsWith(".in")
                && (problemCases.get(0).getOutput().endsWith(".out")
                || problemCases.get(0).getOutput().endsWith(".ans")))) {

            if (FileUtil.isEmpty(new File(testCasesDir))) { //如果本地对应文件夹也为空，说明文件丢失了
                throw new SystemError("problemID:[" + problemId + "] test case has not found.", null, null);
            } else {
                return initLocalTestCase(judgeMode, judgeCaseMode, version, testCasesDir, problemCases);
            }
        } else {

            List<HashMap<String, Object>> testCases = new LinkedList<>();
            for (ProblemCase problemCase : problemCases) {
                HashMap<String, Object> tmp = new HashMap<>();
                tmp.put("input", problemCase.getInput());
                tmp.put("output", problemCase.getOutput());
                tmp.put("caseId", problemCase.getId());
                tmp.put("score", problemCase.getScore());
                tmp.put("groupNum", problemCase.getGroupNum());
                testCases.add(tmp);
            }

            return initTestCase(testCases, problemId, version, judgeMode, judgeCaseMode);
        }
    }

    // 去除每行末尾的空白符
    public static String rtrim(String value) {
        if (value == null) return null;
        return EOL_PATTERN.matcher(StrUtil.trimEnd(value)).replaceAll("");
    }
}
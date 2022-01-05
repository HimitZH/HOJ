package top.hcode.hoj.judge;

import cn.hutool.core.util.ReUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import top.hcode.hoj.common.exception.SystemError;
import top.hcode.hoj.judge.entity.JudgeDTO;
import top.hcode.hoj.judge.entity.JudgeGlobalDTO;
import top.hcode.hoj.judge.entity.SandBoxRes;
import top.hcode.hoj.util.Constants;
import top.hcode.hoj.util.JudgeUtils;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2022/1/2 20:46
 * @Description:
 */
public abstract class AbstractJudge {

    protected static final int SPJ_PC = 99;

    protected static final int SPJ_AC = 100;

    protected static final int SPJ_PE = 101;

    protected static final int SPJ_WA = 102;

    protected static final int SPJ_ERROR = 103;

    public JSONObject judge(JudgeDTO judgeDTO, JudgeGlobalDTO judgeGlobalDTO) throws SystemError {

        JSONArray judgeResultList = judgeCase(judgeDTO, judgeGlobalDTO);

        switch (judgeGlobalDTO.getJudgeMode()) {
            case SPJ:
            case DEFAULT:
                return process(judgeDTO, judgeGlobalDTO, judgeResultList);
            case INTERACTIVE:
                return processMultiple(judgeDTO, judgeGlobalDTO, judgeResultList);
            default:
                throw new RuntimeException("The problem mode is error:" + judgeGlobalDTO.getJudgeMode());
        }

    }

    public abstract JSONArray judgeCase(JudgeDTO judgeDTO, JudgeGlobalDTO judgeGlobalDTO) throws SystemError;

    private JSONObject process(JudgeDTO judgeDTO, JudgeGlobalDTO judgeGlobalDTO, JSONArray judgeResultList) throws SystemError {
        JSONObject judgeResult = (JSONObject) judgeResultList.get(0);
        SandBoxRes sandBoxRes = SandBoxRes.builder()
                .stdout(((JSONObject) judgeResult.get("files")).getStr("stdout"))
                .stderr(((JSONObject) judgeResult.get("files")).getStr("stderr"))
                .time(judgeResult.getLong("time") / 1000000) //  ns->ms
                .memory(judgeResult.getLong("memory") / 1024) // b-->kb
                .exitCode(judgeResult.getInt("exitStatus"))
                .status(judgeResult.getInt("status"))
                .build();

        return checkResult(sandBoxRes, judgeDTO, judgeGlobalDTO);
    }

    private JSONObject processMultiple(JudgeDTO judgeDTO, JudgeGlobalDTO judgeGlobalDTO, JSONArray judgeResultList) throws SystemError {

        JSONObject userJudgeResult = (JSONObject) judgeResultList.get(0);
        SandBoxRes userSandBoxRes = SandBoxRes.builder()
                .stdout(((JSONObject) userJudgeResult.get("files")).getStr("stdout"))
                .stderr(((JSONObject) userJudgeResult.get("files")).getStr("stderr"))
                .time(userJudgeResult.getLong("time") / 1000000) //  ns->ms
                .memory(userJudgeResult.getLong("memory") / 1024) // b-->kb
                .exitCode(userJudgeResult.getInt("exitStatus"))
                .status(userJudgeResult.getInt("status"))
                .build();

        JSONObject interactiveJudgeResult = (JSONObject) judgeResultList.get(1);
        SandBoxRes interactiveSandBoxRes = SandBoxRes.builder()
                .stdout(((JSONObject) interactiveJudgeResult.get("files")).getStr("stdout"))
                .stderr(((JSONObject) interactiveJudgeResult.get("files")).getStr("stderr"))
                .time(interactiveJudgeResult.getLong("time") / 1000000) //  ns->ms
                .memory(interactiveJudgeResult.getLong("memory") / 1024) // b-->kb
                .exitCode(interactiveJudgeResult.getInt("exitStatus"))
                .status(interactiveJudgeResult.getInt("status"))
                .build();

        return checkMultipleResult(userSandBoxRes, interactiveSandBoxRes, judgeDTO, judgeGlobalDTO);
    }

    public abstract JSONObject checkResult(SandBoxRes sandBoxRes, JudgeDTO judgeDTO, JudgeGlobalDTO judgeGlobalDTO) throws SystemError;

    public abstract JSONObject checkMultipleResult(SandBoxRes userSandBoxRes, SandBoxRes interactiveSandBoxRes, JudgeDTO judgeDTO, JudgeGlobalDTO judgeGlobalDTO);

    protected List<String> parseRunCommand(String command,
                                           Constants.RunConfig runConfig,
                                           String testCaseInputName,
                                           String userOutputName,
                                           String testCaseOutputName) {

        command = MessageFormat.format(command, Constants.JudgeDir.TMPFS_DIR.getContent(),
                runConfig.getExeName(), Constants.JudgeDir.TMPFS_DIR.getContent() + File.separator + testCaseInputName,
                Constants.JudgeDir.TMPFS_DIR.getContent() + File.separator + userOutputName,
                Constants.JudgeDir.TMPFS_DIR.getContent() + File.separator + testCaseOutputName);

        return JudgeUtils.translateCommandline(command);
    }

    protected JSONObject parseTestLibErr(String err) {

        JSONObject res = new JSONObject(2);

        if (err.startsWith("ok ")) {
            res.set("code", SPJ_AC);
            res.set("errMsg", err.split("ok ")[1]);
        } else if (err.startsWith("wrong answer ")) {
            res.set("code", SPJ_WA);
            res.set("errMsg", err.split("wrong answer ")[1]);
        } else if (err.startsWith("wrong output format ")) {
            res.set("code", SPJ_WA);
            res.set("errMsg", "May be output presentation error. " + err.split("wrong output format")[1]);
        } else if (err.startsWith("partially correct ")) {
            res.set("errMsg", err.split("partially correct ")[1]);
            String numStr = ReUtil.get("partially correct \\(([\\s\\S]*?)\\) ", err, 1);
            double percentage = 0.0;
            if (!StringUtils.isEmpty(numStr)) {
                percentage = Integer.parseInt(numStr) * 1.0 / 100;
            }
            res.set("percentage", percentage);
            res.set("code", SPJ_PC);
        } else if (err.startsWith("points ")) {
            res.set("code", SPJ_PC);
            String numStr = err.split("points ")[1].split(" ")[0];
            double percentage = 0.0;
            if (!StringUtils.isEmpty(numStr)) {
                percentage = Double.parseDouble(numStr) / 100;
            }
            if (percentage == 1) {
                res.set("code", SPJ_AC);
            } else {
                res.set("percentage", percentage);
            }
            String tmp = err.split("points ")[1];
            res.set("errMsg", tmp.substring(0, Math.min(1024, tmp.length())));
        } else if (err.startsWith("FAIL ")) {
            res.set("code", SPJ_ERROR);
            res.set("errMsg", err.split("FAIL ")[1]);
        } else {
            res.set("code", SPJ_ERROR);
            res.set("errMsg", err);
        }
        return res;
    }
}
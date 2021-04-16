package top.hcode.hoj.judge;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import org.springframework.util.StringUtils;
import top.hcode.hoj.common.exception.CompileError;
import top.hcode.hoj.common.exception.SubmitError;
import top.hcode.hoj.common.exception.SystemError;
import top.hcode.hoj.util.Constants;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2021/4/16 12:14
 * @Description: 判题流程解耦重构2.0，该类只负责编译
 */
public class Compiler {
    public static String compile(Constants.CompileConfig compileConfig, String code, String language) throws SystemError, CompileError, SubmitError {

        if (compileConfig == null) {
            throw new CompileError("Unsupported language " + language, null, null);
        }

        // 调用安全沙箱进行编译
        JSONArray result = SandboxRun.compile(compileConfig.getMaxCpuTime(),
                compileConfig.getMaxRealTime(),
                compileConfig.getMaxMemory(),
                128 * 1024 * 1024L,
                compileConfig.getSrcName(),
                compileConfig.getExeName(),
                parseCompileCommand(compileConfig.getCommand(), compileConfig),
                compileConfig.getEnvs(),
                code,
                true,
                false,
                null
        );
        JSONObject compileResult = (JSONObject) result.get(0);
        if (compileResult.getInt("status").intValue() != Constants.Judge.STATUS_ACCEPTED.getStatus()) {
            throw new CompileError("Compile Error.", ((JSONObject) compileResult.get("files")).getStr("stderr"),
                    ((JSONObject) compileResult.get("files")).getStr("stderr"));
        }

        String fileId = ((JSONObject) compileResult.get("fileIds")).getStr(compileConfig.getExeName());
        if (StringUtils.isEmpty(fileId)) {
            throw new SubmitError("Executable file not found.",  ((JSONObject) compileResult.get("files")).getStr("stderr"),
                    ((JSONObject) compileResult.get("files")).getStr("stderr"));
        }
        return fileId;
    }

    public static Boolean compileSpj(String code, Long pid, String spjLanguage) throws SystemError, CompileError {

        Constants.CompileConfig spjCompiler = Constants.CompileConfig.getCompilerByLanguage("SPJ-" + spjLanguage);
        if (spjCompiler == null) {
            throw new CompileError("Unsupported language " + spjLanguage, null, null);
        }

        boolean copyOutExe = true;
        if (pid == null) { // 题目id为空，则不进行本地存储，可能为新建题目时测试特判程序是否正常的判断而已
            copyOutExe = false;
        }

        // 调用安全沙箱对特别判题程序进行编译
        JSONArray res = SandboxRun.compile(spjCompiler.getMaxCpuTime(),
                spjCompiler.getMaxRealTime(),
                spjCompiler.getMaxMemory(),
                128 * 1024 * 1024L,
                spjCompiler.getSrcName(),
                spjCompiler.getExeName(),
                parseCompileCommand(spjCompiler.getCommand(), spjCompiler),
                spjCompiler.getEnvs(),
                code,
                false,
                copyOutExe,
                Constants.JudgeDir.SPJ_WORKPLACE_DIR.getContent() + "/" + pid
        );
        JSONObject compileResult = (JSONObject) res.get(0);
        if (compileResult.getInt("status").intValue() != Constants.Judge.STATUS_ACCEPTED.getStatus()) {
            throw new SystemError("Special Judge Code Compile Error.", ((JSONObject) compileResult.get("files")).getStr("stderr"),
                    ((JSONObject) compileResult.get("files")).getStr("stderr"));
        }
        return true;
    }

    private static List<String> parseCompileCommand(String command, Constants.CompileConfig compileConfig) {

        command = MessageFormat.format(command, Constants.JudgeDir.TMPFS_DIR.getContent(),
                compileConfig.getSrcName(), compileConfig.getExeName());

        return Arrays.asList(command.split(" "));
    }
}
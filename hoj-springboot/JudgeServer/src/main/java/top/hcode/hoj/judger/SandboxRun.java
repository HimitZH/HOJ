package top.hcode.hoj.judger;

import cn.hutool.core.io.file.FileReader;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import top.hcode.hoj.util.Constants;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2021/1/23 13:44
 * @Description:
 */
@Slf4j
public final class SandboxRun {

    public static JSONObject run(Long maxCpuTime, Long maxRealTime, Long maxMemory, Long maxStack, Long maxOutputSize,
                                 Integer maxProcessNumber, String exePath, String inputPath, String outputPath,
                                 String errorPath, List<String> args, List<String> envs, String logPath,
                                 String seccompRuleName, Integer uid, Integer gid, Integer memoryLimitCheckOnly) {

        StringBuffer sb = new StringBuffer(Constants.Compiler.JUDGER_LIB_PATH.getContent());

        if (args != null) {
            for (String arg : args) {
                sb.append(" --args=" + arg);
            }
        }

        if (envs != null) {
            for (String env : envs) {
                sb.append(" --env=" + env);
            }
        }

        sb.append(" --max_cpu_time=" + maxCpuTime);
        sb.append(" --max_real_time=" + maxRealTime);
        sb.append(" --max_memory=" + maxMemory);
        sb.append(" --max_stack=" + maxStack);
        sb.append(" --max_output_size=" + maxOutputSize);
        sb.append(" --max_process_number=" + maxProcessNumber);
        sb.append(" --uid=" + uid);
        sb.append(" --gid=" + gid);
        sb.append(" --memory_limit_check_only=0");

        sb.append(" --exe_path=" + exePath);
        sb.append(" --input_path=" + inputPath);
        sb.append(" --output_path=" + outputPath);
        sb.append(" --error_path=" + errorPath);
        sb.append(" --log_path=" + logPath);

        if (seccompRuleName != null) {
            sb.append(" --seccomp_rule=" + seccompRuleName);
        }

        Process process = null;
        BufferedReader bufIn = null;
        BufferedReader bufError = null;
        try {
            String execution = sb.toString();
            process = Runtime.getRuntime().exec(execution);
            process.waitFor();
            // 获取命令执行结果, 有两个结果: 正常的输出 和 错误的输出（PS: 子进程的输出就是主进程的输入）
            bufIn = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            bufError = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"));

            // 读取输出
            String line = null;
            StringBuffer out = new StringBuffer();
            StringBuffer errorTmp = new StringBuffer();

            while ((line = bufIn.readLine()) != null) {
                out.append(line).append('\n');
            }

            while ((line = bufError.readLine()) != null) {
                errorTmp.append(line).append('\n');
            }

            String error = errorTmp.toString();

            if (!StringUtils.isEmpty(error)) { // 如果错误输出不为空的话
                log.error("调用沙盒运行程序时出错--------------------->{}", error);
            }
            JSONObject jsonObject = JSONUtil.parseObj(out.toString());
            return jsonObject;

        } catch (Exception e) {
            log.error("调用沙盒运行程序时出错--------------------->{}", e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return null;
    }


}
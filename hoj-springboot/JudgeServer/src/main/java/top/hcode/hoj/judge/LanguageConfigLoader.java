package top.hcode.hoj.judge;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;
import org.yaml.snakeyaml.Yaml;
import top.hcode.hoj.judge.entity.LanguageConfig;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author Himit_ZH
 * @Date 2022/11/22
 */
@Configuration
public class LanguageConfigLoader {

    private static List<String> defaultEnv = Arrays.asList(
            "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
            "LANG=en_US.UTF-8",
            "LC_ALL=en_US.UTF-8",
            "LANGUAGE=en_US:en",
            "HOME=/w");

    private static List<String> python3Env = Arrays.asList("LANG=en_US.UTF-8",
            "LANGUAGE=en_US:en", "LC_ALL=en_US.UTF-8", "PYTHONIOENCODING=utf-8");

    private static List<String> golangEnv = Arrays.asList("GODEBUG=madvdontneed=1",
            "GOCACHE=off", "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
            "LANG=en_US.UTF-8", "LANGUAGE=en_US:en", "LC_ALL=en_US.UTF-8");

    private static AtomicBoolean init = new AtomicBoolean(false);

    private static HashMap<String, LanguageConfig> languageConfigMap;

    @PostConstruct
    public void init() {
        if (init.compareAndSet(false, true)) {
            Iterable<Object> languageConfigIter = loadYml("language.yml");
            for (Object configObj : languageConfigIter) {
                JSONObject configJson = JSONUtil.parseObj(configObj);
            }
        }
    }

    public static void main(String[] args) {
        Iterable<Object> languageConfigIter = loadYml("language.yml");
        for (Object configObj : languageConfigIter) {
            JSONObject configJson = JSONUtil.parseObj(configObj);
            System.out.println(buildLanguageConfig(configJson));
        }
    }

    private static Iterable<Object> loadYml(String fileName) {
        try {
            Yaml yaml = new Yaml();
            File ymlFile = ResourceUtils.getFile("classpath:" + fileName);
            return yaml.loadAll(FileUtil.readUtf8String(ymlFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static LanguageConfig buildLanguageConfig(JSONObject configJson) {
        LanguageConfig languageConfig = new LanguageConfig();
        languageConfig.setLanguage(configJson.getStr("language"));
        languageConfig.setSrcName(configJson.getStr("srcName"));
        languageConfig.setExeName(configJson.getStr("exeName"));

        JSONObject compileJson = configJson.getJSONObject("compile");
        if (compileJson != null) {
            String command  = compileJson.getStr("command");
            command = command.replace("{srcName}", languageConfig.getSrcName())
                    .replace("{exeName}", languageConfig.getExeName());
            languageConfig.setCompileCommand(command);
            languageConfig.setMaxCpuTime(parseTimeStr(compileJson.getStr("maxCpuTime")));
            languageConfig.setMaxRealTime(parseTimeStr(compileJson.getStr("maxRealTime")));
            languageConfig.setMaxMemory(parseMemoryStr(compileJson.getStr("maxMemory")));
        }

        JSONObject runJson = configJson.getJSONObject("run");
        if (runJson != null) {
            String command = runJson.getStr("command");
            command = command.replace("{exeName}", languageConfig.getExeName());
            languageConfig.setRunCommand(command);
        }

        String env = configJson.getStr("env");
        env =  env.toLowerCase();
        switch (env){
            case "python3":
                languageConfig.setEnvs(python3Env);
                break;
            case "golang":
                languageConfig.setEnvs(golangEnv);
                break;
            default:
                languageConfig.setEnvs(defaultEnv);
        }
        return languageConfig;
    }


    private static Long parseTimeStr(String timeStr) {
        if (StrUtil.isBlank(timeStr)) {
            return 3000L;
        }
        timeStr = timeStr.toLowerCase();
        if (timeStr.endsWith("s")) {
            return Long.parseLong(timeStr.replace("s", "")) * 1000;
        } else if (timeStr.endsWith("ms")) {
            return Long.parseLong(timeStr.replace("s", ""));
        } else {
            return Long.parseLong(timeStr);
        }
    }

    private static Long parseMemoryStr(String memoryStr) {
        if (StrUtil.isBlank(memoryStr)) {
            return 256 * 1024 * 1024L;
        }
        memoryStr = memoryStr.toLowerCase();
        if (memoryStr.endsWith("mb")) {
            return Long.parseLong(memoryStr.replace("mb", "")) * 1024 * 1024;
        } else if (memoryStr.endsWith("kb")) {
            return Long.parseLong(memoryStr.replace("kb", "")) * 1024;
        } else if (memoryStr.endsWith("b")) {
            return Long.parseLong(memoryStr.replace("b", ""));
        } else {
            return Long.parseLong(memoryStr) * 1024 * 1024;
        }
    }

}

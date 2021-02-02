package top.hcode.hoj.judger;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import top.hcode.hoj.common.exception.SystemError;
import top.hcode.hoj.util.Constants;

import java.lang.ref.PhantomReference;
import java.util.HashMap;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2021/1/23 13:44
 * @Description:
 */

/**
 * args: string[]; // command line argument
 * env?: string[]; // environment
 * <p>
 * // specifies file input / pipe collector for program file descriptors
 * files?: (LocalFile | MemoryFile | PreparedFile | Pipe | null)[];
 * tty?: boolean; // enables tty on the input and output pipes (should have just one input & one output)
 * // Notice: must have TERM environment variables (e.g. TERM=xterm)
 * <p>
 * // limitations
 * cpuLimit?: number;     // ns
 * realCpuLimit?: number; // deprecated: use clock limit instead (still working)
 * clockLimit?: number;   // ns
 * memoryLimit?: number;  // byte
 * stackLimit?: number;   // byte (N/A on windows, macOS cannot set over 32M)
 * procLimit?: number;
 * <p>
 * // copy the correspond file to the container dst path
 * copyIn?: {[dst:string]:LocalFile | MemoryFile | PreparedFile};
 * <p>
 * // copy out specifies files need to be copied out from the container after execution
 * copyOut?: string[];
 * // similar to copyOut but stores file in executor service and returns fileId, later download through /file/:fileId
 * copyOutCached?: string[];
 * // specifies the directory to dump container /w content
 * copyOutDir: string
 * // specifies the max file size to copy out
 * copyOutMax: number; // byte
 */

@Slf4j
public class SandboxRun {

    private static final RestTemplate restTemplate;

    // 单例模式
    private static final SandboxRun instance = new SandboxRun();

    private static final String SANDBOX_BASE_URL = "http://localhost:5050";

    public static final HashMap<String, Integer> RESULT_MAP_STATUS = new HashMap<>();

    private static final int maxProcessNumber = 32;

    private static final int TIME_LIMIT_MS = 16000;

    private static final int MEMORY_LIMIT_MB = 1024;

    private static final int STACK_LIMIT_MB = 128;

    private SandboxRun() {

    }

    static {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10000);
        requestFactory.setReadTimeout(10000);
        restTemplate = new RestTemplate(requestFactory);
    }

    static {
        RESULT_MAP_STATUS.put("Time Limit Exceeded", Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED.getStatus());
        RESULT_MAP_STATUS.put("Memory Limit Exceeded", Constants.Judge.STATUS_MEMORY_LIMIT_EXCEEDED.getStatus());
        RESULT_MAP_STATUS.put("Output Limit Exceeded", Constants.Judge.STATUS_RUNTIME_ERROR.getStatus());
        RESULT_MAP_STATUS.put("Accepted", Constants.Judge.STATUS_ACCEPTED.getStatus());
        RESULT_MAP_STATUS.put("Nonzero Exit Status", Constants.Judge.STATUS_RUNTIME_ERROR.getStatus());
        RESULT_MAP_STATUS.put("Internal Error", Constants.Judge.STATUS_SYSTEM_ERROR.getStatus());
        RESULT_MAP_STATUS.put("File Error", Constants.Judge.STATUS_SYSTEM_ERROR.getStatus());
        RESULT_MAP_STATUS.put("Signalled", Constants.Judge.STATUS_RUNTIME_ERROR.getStatus());
    }

    public JSONArray run(String uri, JSONObject param) throws SystemError {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(JSONUtil.toJsonStr(param), headers);
        ResponseEntity<String> postForEntity;
        try {
            postForEntity = restTemplate.postForEntity(SANDBOX_BASE_URL + uri, request, String.class);
            return JSONUtil.parseArray(postForEntity.getBody());
        } catch (RestClientResponseException ex) {
            if (ex.getRawStatusCode() != 200) {
                throw new SystemError("Failed to call safe sandbox.", null, ex.getResponseBodyAsString());
            }
        }
        return null;
    }

    public static void delFile(String fileId) {

        try {
            restTemplate.delete(SANDBOX_BASE_URL + "/file/{0}", fileId);
        } catch (RestClientResponseException ex) {
            if (ex.getRawStatusCode() != 200) {
                log.error("安全沙箱判题的删除内存中的文件缓存操作异常----------------->{}", ex.getResponseBodyAsString());
            }
        }

    }

    /**
     * "files": [{
     * "content": ""
     * }, {
     * "name": "stdout",
     * "max": 1024 * 1024 * 32
     * }, {
     * "name": "stderr",
     * "max": 1024 * 1024 * 32
     * }]
     */
    private static final JSONArray COMPILE_FILES = new JSONArray();

    static {
        JSONObject content = new JSONObject();
        content.set("content", "");

        JSONObject stdout = new JSONObject();
        stdout.set("name", "stdout");
        stdout.set("max", 1024 * 1024 * 32);

        JSONObject stderr = new JSONObject();
        stderr.set("name", "stderr");
        stderr.set("max", 1024 * 1024 * 32);
        COMPILE_FILES.put(content);
        COMPILE_FILES.put(stdout);
        COMPILE_FILES.put(stderr);
    }

    public static JSONArray compile(Long maxCpuTime,Long maxRealTime,Long maxMemory, Long maxStack,String srcName, String exeName,
                                    List<String> args,List<String> envs, String code, Boolean needCopyOutCached,
                                    Boolean needCopyOutExe, String copyOutDir) throws SystemError {
        JSONObject cmd = new JSONObject();
        cmd.set("args", args);
        cmd.set("env", envs);
        cmd.set("files", COMPILE_FILES);
        // ms-->ns
        cmd.set("cpuLimit", maxCpuTime * 1000 * 1000L);
        cmd.set("realCpuLimit", maxRealTime * 1000 * 1000L);
        // byte
        cmd.set("memoryLimit", maxMemory);
        cmd.set("procLimit", maxProcessNumber);
        cmd.set("stackLimit", maxStack);

        JSONObject file = new JSONObject();
        file.set("content", code);
        JSONObject copyIn = new JSONObject();
        copyIn.set(srcName, file);

        cmd.set("copyIn", copyIn);
        cmd.set("copyOut", new JSONArray().put("stdout").put("stderr"));

        if (needCopyOutCached) {
            cmd.set("copyOutCached", new JSONArray().put(exeName));
        }

        if (needCopyOutExe) {
            cmd.set("copyOutDir", copyOutDir);
        }

        JSONObject param = new JSONObject();
        param.set("cmd", new JSONArray().put(cmd));

        JSONArray result = instance.run("/run", param);

        return result;
    }


    public static JSONArray testCase(List<String> args, List<String> envs, String testCasePath,
                                     String exeName, String fileId) throws SystemError {
        JSONObject cmd = new JSONObject();
        cmd.set("args", args);
        cmd.set("env", envs);

        JSONArray files = new JSONArray();
        JSONObject content = new JSONObject();
        content.set("src", testCasePath);

        JSONObject stdout = new JSONObject();
        stdout.set("name", "stdout");
        stdout.set("max", 1024 * 1024 * 32);

        JSONObject stderr = new JSONObject();
        stderr.set("name", "stderr");
        stderr.set("max", 1024 * 1024 * 32);
        files.put(content);
        files.put(stdout);
        files.put(stderr);

        cmd.set("files", files);

        // ms-->ns
        cmd.set("cpuLimit", TIME_LIMIT_MS * 1000 * 1000L);
        cmd.set("realCpuLimit", TIME_LIMIT_MS * 1000 * 1000L * 3);
        // byte
        cmd.set("memoryLimit", MEMORY_LIMIT_MB * 1024 * 1024L);
        cmd.set("procLimit", maxProcessNumber);
        cmd.set("stackLimit", STACK_LIMIT_MB * 1024 * 1024L);

        JSONObject exeFile = new JSONObject();
        exeFile.set("fileId", fileId);
        JSONObject copyIn = new JSONObject();
        copyIn.set(exeName, exeFile);

        cmd.set("copyIn", copyIn);
        cmd.set("copyOut", new JSONArray().put("stdout").put("stderr"));

        JSONObject param = new JSONObject();
        param.set("cmd", new JSONArray().put(cmd));

        // 调用判题安全沙箱
        JSONArray result = instance.run("/run", param);
        return result;
    }


    private static final JSONArray pipeMapping = new JSONArray();

    /*
        "pipeMapping": [{
            "in" : {"index": 0, "fd": 1 },
            "out" : {"index": 1, "fd" : 0 }
        }]
     */
    static {
        JSONObject in = new JSONObject();
        in.set("index", 0);
        in.set("fd", 1);

        JSONObject out = new JSONObject();
        in.set("index", 1);
        in.set("fd", 0);

        JSONObject pipe = new JSONObject();
        pipe.set("in", in);
        pipe.set("out", out);

        pipeMapping.put(pipe);
    }

    public static JSONArray spjTestCase(List<String> args, List<String> envs, String userExeName, String userFileId,
                                        String testCasePath, String spjExeSrc, String spjExeName
    ) throws SystemError {

        /**
         *  注意：用户源代码需要先编译，若是通过编译需要先将文件存入内存，再利用管道判题，同时特殊判题程序必须已编译且存在（否则判题失败，系统错误）！
         */

        // 管道输入，利用已经编译好存在内存的可执行用户代码，同时通过当前题目输入后运行得到输出结果，作为管道数据输入！
        JSONObject pipeInputCmd = new JSONObject();
        pipeInputCmd.set("args", args);
        pipeInputCmd.set("env", envs);

        JSONArray files = new JSONArray();
        JSONObject content = new JSONObject();
        content.set("src", testCasePath);

        JSONObject stderr = new JSONObject();
        stderr.set("name", "stderr");
        stderr.set("max", 1024 * 1024 * 32);
        files.put(content);
        files.put(null);
        files.put(stderr);

        pipeInputCmd.set("files", files);

        // ms-->ns
        pipeInputCmd.set("cpuLimit", TIME_LIMIT_MS * 1000 * 1000L);
        pipeInputCmd.set("realCpuLimit", TIME_LIMIT_MS * 1000 * 1000L * 3);
        // byte
        pipeInputCmd.set("memoryLimit", MEMORY_LIMIT_MB * 1024 * 1024L);
        pipeInputCmd.set("procLimit", maxProcessNumber);
        pipeInputCmd.set("stackLimit", STACK_LIMIT_MB * 1024 * 1024L);

        JSONObject exeFile = new JSONObject();
        exeFile.set("fileId", userFileId);
        JSONObject copyIn = new JSONObject();
        copyIn.set(userExeName, exeFile);

        pipeInputCmd.set("copyIn", copyIn);
        pipeInputCmd.set("copyOut", new JSONArray().put("stderr"));


        // 管道输出，用户程序输出数据经过特殊判题程序后，得到的最终输出结果。
        JSONObject pipeOutputCmd = new JSONObject();
        pipeInputCmd.set("args", args);
        pipeInputCmd.set("env", envs);

        JSONArray outFiles = new JSONArray();

        JSONObject outStdout = new JSONObject();
        outStdout.set("name", "stdout");
        outStdout.set("max", 1024 * 1024 * 32);

        JSONObject outStderr = new JSONObject();
        outStderr.set("name", "stderr");
        outStderr.set("max", 1024 * 1024 * 32);
        outFiles.put(null);
        outFiles.put(outStdout);
        outFiles.put(outStderr);

        pipeOutputCmd.set("files", files);

        // ms-->ns
        pipeOutputCmd.set("cpuLimit", TIME_LIMIT_MS * 1000 * 1000L);
        pipeOutputCmd.set("realCpuLimit", TIME_LIMIT_MS * 1000 * 1000L * 3);
        // byte
        pipeOutputCmd.set("memoryLimit", MEMORY_LIMIT_MB * 1024 * 1024L);
        pipeOutputCmd.set("procLimit", maxProcessNumber);
        pipeOutputCmd.set("stackLimit", STACK_LIMIT_MB * 1024 * 1024L);

        JSONObject spjExeFile = new JSONObject();
        exeFile.set("src", spjExeSrc);
        JSONObject spjCopyIn = new JSONObject();
        copyIn.set(spjExeName, spjExeFile);

        pipeOutputCmd.set("copyIn", spjCopyIn);
        pipeOutputCmd.set("copyOut", new JSONArray().put("stdout").put("stderr"));

        JSONArray cmdList = new JSONArray();
        cmdList.put(pipeInputCmd);
        cmdList.put(pipeOutputCmd);

        JSONObject param = new JSONObject();
        // 添加cmd指令
        param.set("cmd", cmdList);

        // 添加管道隐射管道映射
        param.set("pipeMapping", pipeMapping);

        // 调用判题安全沙箱
        JSONArray result = instance.run("/run", param);

        return result;
    }

}
 /*
     1. compile
        Json Request Body
        {
            "cmd": [{
                "args": ["/usr/bin/g++", "a.cc", "-o", "a"],
                "env": ["PATH=/usr/bin:/bin"],
                "files": [{
                    "content": ""
                }, {
                    "name": "stdout",
                    "max": 10240
                }, {
                    "name": "stderr",
                    "max": 10240
                }],
                "cpuLimit": 10000000000,
                "memoryLimit": 104857600,
                "procLimit": 50,
                "copyIn": {
                    "a.cc": {
                        "content": "#include <iostream>\nusing namespace std;\nint main() {\nint a, b;\ncin >> a >> b;\ncout << a + b << endl;\n}"
                    }
                },
                "copyOut": ["stdout", "stderr"],
                "copyOutCached": ["a.cc", "a"],
                "copyOutDir": "1"
            }]
        }

        Json Response Data

        [
            {
                "status": "Accepted",
                "exitStatus": 0,
                "time": 303225231,
                "memory": 32243712,
                "runTime": 524177700,
                "files": {
                    "stderr": "",
                    "stdout": ""
                },
                "fileIds": {
                    "a": "5LWIZAA45JHX4Y4Z",
                    "a.cc": "NOHPGGDTYQUFRSLJ"
                }
            }
        ]
    2.test case

            Json Request Body
              {
                "cmd": [{
                    "args": ["a"],
                    "env": ["PATH=/usr/bin:/bin","LANG=en_US.UTF-8","LC_ALL=en_US.UTF-8","LANGUAGE=en_US:en"],
                    "files": [{
                        "src": "/judge/test_case/problem_1010/1.in"
                    }, {
                        "name": "stdout",
                        "max": 10240
                    }, {
                        "name": "stderr",
                        "max": 10240
                    }],
                    "cpuLimit": 10000000000,
                    "realCpuLimit":30000000000,
                    "stackLimit":134217728,
                    "memoryLimit": 104811111,
                    "procLimit": 50,
                    "copyIn": {
                        "a":{"fileId":"WDQL5TNLRRVB2KAP"}
                    },
                    "copyOut": ["stdout", "stderr"]
                }]
            }

            Json Response Data
             [{
              "status": "Accepted",
              "exitStatus": 0,
              "time": 3171607,
              "memory": 475136,
              "runTime": 110396333,
              "files": {
                "stderr": "",
                "stdout": "23\n"
              }
            }]

    3. SPJ

        {
            "cmd": [{
                "args": ["a"],
                "env": ["PATH=/usr/bin:/bin"],
                "files": [{
                    "src": "/judge/test_case/problem_1010/0.in"
                }, null, {
                    "name": "stderr",
                    "max": 10240
                }],
                "cpuLimit": 1000000000,
                "memoryLimit": 1048576,
                "procLimit": 50,
                "copyIn": {
                    "a": { "fileId": "THEXDZCK57PJNKZ6"}
                },
                "copyOut": ["stderr"]
            },
            {
                "args": ["c","outfile"],
                "env": ["PATH=/usr/bin:/bin"],
                "files": [null, {
                    "name": "stdout",
                    "max": 10240
                }, {
                    "name": "stderr",
                    "max": 10240
                }],
                "cpuLimit": 1000000000,
                "memoryLimit": 1048576,
                "procLimit": 50,
                 "copyIn": {
                    "c": { "src": "/home/hzh/c"}
                },
                "copyOut": ["stdout", "stderr"]
            }],
            "pipeMapping": [{
                "in" : {"index": 0, "fd": 1 },
                "out" : {"index": 1, "fd" : 0 }
            }]
        }

  */

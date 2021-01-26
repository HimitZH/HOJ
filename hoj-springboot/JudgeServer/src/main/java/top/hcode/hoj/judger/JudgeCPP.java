package top.hcode.hoj.judger;

import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import top.hcode.hoj.pojo.entity.Judge;
import top.hcode.hoj.pojo.entity.Problem;
import top.hcode.hoj.util.Constants;

import java.util.HashMap;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2021/1/22 21:14
 * @Description:
 */
@Slf4j
public class JudgeCPP extends JudgeStrategy{

    private String code;

    private Problem problem;

    private Long submitId;

    public JudgeCPP(Problem problem, Judge judge) {
        super(problem,judge.getSubmitId());
        this.submitId = judge.getSubmitId();
        this.code = judge.getCode();
        this.problem = problem;
    }


    @Override
    public HashMap<String, Object> judge() {
        // 该提交对应的文件夹路径
        String fileDir = Constants.Compiler.WORKPLACE.getContent() + "/" + submitId;
        // 将代码写入文件
        String srcFilePath = fileDir + "/" + Constants.CompileConfig.CPP.getSrcName();
        FileWriter fileWriter = new FileWriter(srcFilePath, CharsetUtil.UTF_8);
        fileWriter.write(code);
        // 编译指令
        String command = Constants.CompileConfig.CPP.getCommand();
        // 输出exe文件路径
        String exePath = fileDir+"/"+Constants.CompileConfig.CPP.getExeName();

        String spjCompileResult = checkOrCompileSpj(problem.getSpjCode(), problem.getSpjLanguage());

        // 如果该题是需要特别判题的，但是该特别判题程序不存在或编译时产生异常，则此次判题直接判系统错误。
        if (!spjCompileResult.equals("success")){
            HashMap<String,Object> result = new HashMap<>();
            result.put("code", Constants.Judge.STATUS_SYSTEM_ERROR.getStatus());
            result.put("msg", spjCompileResult);
            return result;
        }

        // 编译的一些时间空间参数等
        Long maxCpuTime = Constants.CompileConfig.CPP.getMaxCpuTime();
        Long maxRealTime = Constants.CompileConfig.CPP.getMaxRealTime();
        Long maxMemory = Constants.CompileConfig.CPP.getMaxMemory();
        List<String> envs = Constants.RunConfig.CPP.getEnvs();

        // 运行的一些指令参数，环境配置等
        String runCommand = Constants.RunConfig.CPP.getCommand();
        List<String> runEnvs = Constants.RunConfig.CPP.getEnvs();
        String runSeccompRule = Constants.RunConfig.CPP.getSeccompRule();
        Integer memoryLimitCheckOnly = Constants.RunConfig.CPP.getMemoryLimitCheckOnly();

        // 特别判题的参数,如果并非特别判题则为null即可
        String spjRunSeccompRule = null;
        if (problem.getSpjLanguage().equals("C")) {
            spjRunSeccompRule = Constants.RunConfig.SPJ_C.getSeccompRule();
        }else if(problem.getSpjLanguage().equals("C++")){
            spjRunSeccompRule = Constants.RunConfig.SPJ_CPP.getSeccompRule();
        }

        // 调用安全沙盒进行编译操作
        HashMap<String, Object> result = compile(srcFilePath, fileDir, command, exePath, maxCpuTime,maxRealTime,maxMemory,envs);
        if (!(Boolean) result.get("result")){ // 编译失败
            // {msg:error,code:STATUS_COMPILE_ERROR}
            result.remove("result");
        }else{
            // 编译成功，则进行每个测试点进行测试
            List<JSONObject> testCaseResultList = judgeAllCase(exePath, fileDir, (long) problem.getTimeLimit(), problem.getTimeLimit() * 3L,
                    problem.getMemoryLimit()*1024*1024L, runCommand, runEnvs, runSeccompRule, spjRunSeccompRule, memoryLimitCheckOnly);
            // 获取判题结果
            return getJudgeInfo(testCaseResultList, problem.getType() == 0);
        }
        return result;
    }
}
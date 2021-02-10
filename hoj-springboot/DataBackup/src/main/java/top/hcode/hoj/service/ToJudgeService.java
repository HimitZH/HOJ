package top.hcode.hoj.service;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import top.hcode.hoj.common.exception.CloudHandler;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.config.RibbonConfig;
import top.hcode.hoj.pojo.entity.CompileSpj;
import top.hcode.hoj.pojo.entity.ToJudge;


//需要的判题微服务名
@FeignClient(value = "hoj-judge-server", fallback = CloudHandler.class, configuration = RibbonConfig.class)
@Component
public interface ToJudgeService {

    @PostMapping(value = "/judge")
    public CommonResult submitProblemJudge(@RequestBody ToJudge toJudge);

    @PostMapping(value = "/compile-spj")
    public CommonResult compileSpj(@RequestBody CompileSpj compileSpj);

    @PostMapping(value = "/remote-judge")
    public CommonResult remoteJudge(@RequestBody ToJudge toJudge);
}

package top.hcode.hoj.service;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import top.hcode.hoj.common.exception.CloudHandler;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.ToJudgeDto;
import top.hcode.hoj.pojo.entity.Judge;


@FeignClient(value = "hoj-judge-server",fallback = CloudHandler.class) //需要的判题微服务名
@Component
public interface ToJudgeService {

    @PostMapping(value = "/judge")
    public CommonResult submitProblemJudge(@RequestBody Judge judge);


}

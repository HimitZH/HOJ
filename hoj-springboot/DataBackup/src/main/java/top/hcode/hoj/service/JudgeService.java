package top.hcode.hoj.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import top.hcode.hoj.pojo.entity.Judge;
import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.vo.JudgeVo;


/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */

public interface JudgeService extends IService<Judge> {
    IPage<JudgeVo> getCommonJudgeList(Integer limit, Integer currentPage, Long pid, Integer status, String username,
                                      Long cid,String uid);

}

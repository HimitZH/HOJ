package top.hcode.hoj.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.common.exception.SystemError;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.entity.problem.Problem;

import java.util.HashMap;


/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
public interface JudgeService extends IService<Judge> {

    Judge Judge(Problem problem, Judge judge);

    Boolean compileSpj(String code, Long pid, String spjLanguage, HashMap<String,String> extraFiles) throws SystemError;

    Boolean compileInteractive(String code, Long pid, String interactiveLanguage, HashMap<String,String> extraFiles) throws SystemError;

    void updateOtherTable(Long submitId, Integer status, Long cid, String uid, Long pid, Integer score,Integer useTime);
}

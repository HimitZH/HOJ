package top.hcode.hoj.service;

import top.hcode.hoj.common.exception.SystemError;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.entity.judge.ToJudge;

import java.util.HashMap;

public interface JudgeService {

    public void judge(Judge judge);

    public void remoteJudge(ToJudge toJudge);

    public Boolean compileSpj(String code, Long pid, String spjLanguage, HashMap<String, String> extraFiles) throws SystemError;

    public Boolean compileInteractive(String code, Long pid, String interactiveLanguage, HashMap<String, String> extraFiles) throws SystemError;

}

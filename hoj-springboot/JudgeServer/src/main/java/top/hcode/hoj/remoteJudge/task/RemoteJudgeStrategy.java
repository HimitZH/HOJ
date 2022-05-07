package top.hcode.hoj.remoteJudge.task;

import lombok.Getter;
import lombok.Setter;
import top.hcode.hoj.remoteJudge.entity.RemoteJudgeDTO;
import top.hcode.hoj.remoteJudge.entity.RemoteJudgeRes;

import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;


/**
 * 远程评测抽象类
 */
public abstract class RemoteJudgeStrategy {

    @Setter
    @Getter
    private RemoteJudgeDTO remoteJudgeDTO;

    public abstract void submit() throws ScriptException, FileNotFoundException, NoSuchMethodException, UnsupportedEncodingException;

    public abstract RemoteJudgeRes result();

    public abstract void login() throws UnsupportedEncodingException;

    public abstract String getLanguage(String language);

}

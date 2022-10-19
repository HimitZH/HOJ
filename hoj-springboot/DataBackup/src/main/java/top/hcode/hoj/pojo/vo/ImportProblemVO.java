package top.hcode.hoj.pojo.vo;

import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: Himit_ZH
 * @Date: 2021/5/27 15:21
 * @Description:
 */
@ToString
@Accessors(chain = true)
public class ImportProblemVO implements Serializable {

    private HashMap<String,Object> problem;

    private List<String> languages;

    private List<HashMap<String,Object>> samples;

    private List<String> tags;

    private List<HashMap<String,String>> codeTemplates;

    private HashMap<String,String> userExtraFile;

    private HashMap<String,String> judgeExtraFile;

    private String judgeMode;

    public Map<String, Object> getProblem() {
        return problem;
    }

    public void setProblem(HashMap<String, Object> problem) {
        this.problem = problem;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public List<HashMap<String, Object>> getSamples() {
        return samples;
    }

    public void setSamples(List<HashMap<String, Object>> samples) {
        this.samples = samples;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<HashMap<String, String>> getCodeTemplates() {
        return codeTemplates;
    }

    public void setCodeTemplates(List<HashMap<String, String>> codeTemplates) {
        this.codeTemplates = codeTemplates;
    }

    public String getJudgeMode() {
        return judgeMode;
    }

    public void setJudgeMode(String judgeMode) {
        this.judgeMode = judgeMode;
    }

    public HashMap<String, String> getUserExtraFile() {
        return userExtraFile;
    }

    public void setUserExtraFile(HashMap<String, String> userExtraFile) {
        this.userExtraFile = userExtraFile;
    }

    public HashMap<String, String> getJudgeExtraFile() {
        return judgeExtraFile;
    }

    public void setJudgeExtraFile(HashMap<String, String> judgeExtraFile) {
        this.judgeExtraFile = judgeExtraFile;
    }
}
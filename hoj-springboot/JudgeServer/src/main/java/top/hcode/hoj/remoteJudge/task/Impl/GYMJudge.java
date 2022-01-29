package top.hcode.hoj.remoteJudge.task.Impl;


/**
 * @Author: Himit_ZH
 * @Date: 2021/11/6 11:17
 * @Description:
 */
public class GYMJudge extends CodeForcesJudge {
    @Override
    protected String getSubmitUrl(String contestNum) {
        return IMAGE_HOST + "/gym/" + contestNum + "/submit";
    }
}
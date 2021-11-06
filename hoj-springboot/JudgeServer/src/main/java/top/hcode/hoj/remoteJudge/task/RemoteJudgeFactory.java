package top.hcode.hoj.remoteJudge.task;

import top.hcode.hoj.remoteJudge.task.Impl.CodeForcesJudge;
import top.hcode.hoj.remoteJudge.task.Impl.GYMJudge;
import top.hcode.hoj.remoteJudge.task.Impl.HduJudge;
import top.hcode.hoj.remoteJudge.task.Impl.POJJudge;
import top.hcode.hoj.util.Constants;


public class RemoteJudgeFactory {

    public static RemoteJudgeStrategy selectJudge(String judgeName) {
        Constants.RemoteJudge remoteJudge = Constants.RemoteJudge.getTypeByName(judgeName);
        switch (remoteJudge) {
            case HDU_JUDGE:
                return new HduJudge();
            case CF_JUDGE:
                return new CodeForcesJudge();
            case POJ_JUDGE:
                return new POJJudge();
            case GYM_JUDGE:
                return new GYMJudge();
            default:
                return null;
        }
    }
}

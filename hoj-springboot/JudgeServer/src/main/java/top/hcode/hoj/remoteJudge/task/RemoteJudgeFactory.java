package top.hcode.hoj.remoteJudge.task;

import top.hcode.hoj.remoteJudge.task.Impl.*;
import top.hcode.hoj.util.Constants;

public class RemoteJudgeFactory {

    public static RemoteJudgeStrategy selectJudge(String judgeName) {
        Constants.RemoteJudge remoteJudge = Constants.RemoteJudge.getTypeByName(judgeName);
        switch (remoteJudge) {
            case HDU_JUDGE:
                return new HDUJudge();
            case CF_JUDGE:
                return new CodeForcesJudge();
            case POJ_JUDGE:
                return new POJJudge();
            case GYM_JUDGE:
                return new GYMJudge();
            case SPOJ_JUDGE:
                return new SPOJJudge();
            case ATCODER_JUDGE:
                return new AtCoderJudge();
            default:
                return null;
        }
    }
}

package top.hcode.hoj.remotejudger;

import top.hcode.hoj.remotejudger.Impl.HduJudge;
import top.hcode.hoj.util.Constants;


public class RemoteJudgeFactory {

    public static RemoteJudgeStrategy selectJudge(String judgeName) {
        Constants.RemoteJudge remoteJudge = Constants.RemoteJudge.getTypeByName(judgeName);
        switch (remoteJudge) {
            case HDU_JUDGE:
                return new HduJudge();
            default:
                return null;
        }
    }
}

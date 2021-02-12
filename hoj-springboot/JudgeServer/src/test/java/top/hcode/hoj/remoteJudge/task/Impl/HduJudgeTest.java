package top.hcode.hoj.remoteJudge.task.Impl;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HduJudgeTest {

    @Test
    void getLoginCookie() {
        HduJudge hduJudge = new HduJudge();

        try {
            Map<String, Object> submit = hduJudge.result(35329033L);
            System.out.println(submit);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
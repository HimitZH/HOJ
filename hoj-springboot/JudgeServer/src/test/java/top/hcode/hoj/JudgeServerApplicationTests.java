package top.hcode.hoj;

import org.jsoup.Connection;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.hcode.hoj.remoteJudge.task.Impl.HduJudge;
import top.hcode.hoj.util.JsoupUtils;

import java.io.IOException;
import java.util.Map;

@SpringBootTest
public class JudgeServerApplicationTests {
    @Test
    void test1() {
        HduJudge hduJudge = new HduJudge();
        String code = "#include<iostream>\n" +
                "using namespace std;\n" +
                "\n" +
                "int main()\n" +
                "{\n" +
                "\tint t;\n" +
                "\tcin >> t;\n" +
                "\twhile (t--) {\n" +
                "\t\tint a, b;\n" +
                "\t\tcin >> a >> b;\n" +
                "\t\tcout << a + b;\n" +
                "\t\tif (t == 0){\n" +
                "\t\t\tcout << endl;\n" +
                "\t\t} else {\n" +
                "\t\t\tcout << endl;\n" +
                "\t\t}\n" +
                "\t}\n" +
                "\treturn 1;\n" +
                "}";
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static final String HOST = "https://codeforces.com";

    @Test
    public void test2() throws IOException {
        Connection connection = JsoupUtils.getConnectionFromUrl(HOST, null, null);
        Connection.Response response = JsoupUtils.getResponse(connection, null);
        Map<String, String> cookies = response.cookies();
        System.out.println(cookies);
    }
}

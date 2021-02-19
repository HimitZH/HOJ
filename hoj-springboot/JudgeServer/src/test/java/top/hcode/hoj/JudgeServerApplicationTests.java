package top.hcode.hoj;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.hcode.hoj.remoteJudge.task.Impl.HduJudge;

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
}

package top.hcode.hoj;

import org.jsoup.Connection;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.hcode.hoj.remoteJudge.task.Impl.CodeForcesJudge;
import top.hcode.hoj.remoteJudge.task.Impl.HduJudge;
import top.hcode.hoj.remoteJudge.task.Impl.POJJudge;
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
    public void test2() throws Exception {
        CodeForcesJudge codeForcesJudge = new CodeForcesJudge();
        Map<String, Object> result = codeForcesJudge.submit("账号", "密码", "750A", "GNU G++11 5.1.0", "#include <bits/stdc++.h>\n" +
                "using namespace std;\n" +
                "const int maxn = 105;\n" +
                "struct node\n" +
                "{\n" +
                "    int l,r;\n" +
                "    node() {}\n" +
                "    node(int _l,int _r)\n" +
                "    {\n" +
                "        l = _l;\n" +
                "        r = _r;\n" +
                "    }\n" +
                "};\n" +
                "int main(void)\n" +
                "{\n" +
                "    int n;\n" +
                "    scanf(\"%d\",&n);\n" +
                "    int last = 1;\n" +
                "    vector<node>ans;\n" +
                "    for(int i=1;i<=n;i++)\n" +
                "    {\n" +
                "        int x;\n" +
                "        scanf(\"%d\",&x);\n" +
                "        if(x)\n" +
                "        {\n" +
                "            ans.push_back(node(last,i));\n" +
                "            last = i+1;\n" +
                "        }\n" +
                "    }\n" +
                "    if(ans.size())\n" +
                "    {\n" +
                "        puts(\"YES\");\n" +
                "        printf(\"%d\\n\",ans.size());\n" +
                "        for(unsigned i=0;i<ans.size()-1;i++)\n" +
                "            printf(\"%d %d\\n\",ans[i].l,ans[i].r);\n" +
                "        printf(\"%d %d\\n\",ans[ans.size()-1].l,n);\n" +
                "    }\n" +
                "    else\n" +
                "        puts(\"NO\");\n" +
                "    return 0;\n" +
                "}");
        System.out.println(result);
    }


    @Test
    void  test03() throws Exception {
        HduJudge hduJudge = new HduJudge();
        Map<String, Object> submit = hduJudge.submit("账号", "密码", "1000", "GCC", "#include<stdio.h> \\n\" +\n" +
                "                        \"int main()\\n\" +\n" +
                "                        \"{\\n\" +\n" +
                "                        \"\\tint a,b,sum;\\n\" +\n" +
                "                        \"\\twhile(scanf(\\\"%d%d\\\",&a,&b)!=EOF)\\n\" +\n" +
                "                        \"\\t{\\n\" +\n" +
                "                        \"\\t\\tsum=a+b;\\n\" +\n" +
                "                        \"\\t\\tprintf(\\\"%d\\\\n\\\",sum);\\n\" +\n" +
                "                        \"\\n\" +\n" +
                "                        \"\\t}\\n\" +\n" +
                "                        \"\\treturn 0;\\n\" +\n" +
                "                        \"}");
        System.out.println(submit);
    }

    @Test
    void  test04(){
        POJJudge pojJudge = new POJJudge();
        Map<String, Object> loginUtils = pojJudge.getLoginUtils("账号", "密码");

    }


}

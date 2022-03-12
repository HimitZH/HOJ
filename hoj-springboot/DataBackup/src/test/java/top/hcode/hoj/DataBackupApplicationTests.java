package top.hcode.hoj;

import cn.hutool.core.util.ReUtil;

import cn.hutool.crypto.SecureUtil;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import top.hcode.hoj.mapper.*;
import top.hcode.hoj.pojo.entity.problem.Language;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.dao.common.impl.AnnouncementEntityServiceImpl;
import top.hcode.hoj.dao.discussion.impl.DiscussionEntityServiceImpl;
import top.hcode.hoj.dao.problem.impl.LanguageEntityServiceImpl;
import top.hcode.hoj.dao.user.impl.UserInfoEntityServiceImpl;
import top.hcode.hoj.dao.user.impl.UserRoleEntityServiceImpl;
import top.hcode.hoj.utils.IpUtils;
import top.hcode.hoj.utils.JsoupUtils;
import top.hcode.hoj.utils.RedisUtils;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @Author: Himit_ZH
 * @Date: 2020/10/24 17:24
 * @Description:
 */
@SpringBootTest
public class DataBackupApplicationTests {

    @Autowired
    private JudgeMapper judgeMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private UserRoleEntityServiceImpl userRoleService;

    @Autowired
    private RoleAuthMapper roleAuthMapper;

    @Autowired
    private ContestMapper contestMapper;

    @Autowired
    private UserInfoEntityServiceImpl userInfoService;


    @Autowired
    private AnnouncementEntityServiceImpl announcementService;

    @Autowired
    private DiscussionEntityServiceImpl discussionService;

    @Test
    public void Test1() {
        String hoj123456 = SecureUtil.md5("hoj123456");
        System.out.println(hoj123456);
    }

    @Autowired
    private NacosDiscoveryProperties discoveryProperties;

    @Test
    public void Test2() {
        String clusterName = discoveryProperties.getClusterName();
        System.out.println(clusterName);
        // 获取该微服务的所有健康实例
        // 获取服务发现的相关API
        NamingService namingService = discoveryProperties.namingServiceInstance();
        try {
            // 获取该微服务的所有健康实例
            List<Instance> instances = namingService.selectInstances("hoj-judge-server", true);
            System.out.println(instances);
        } catch (NacosException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void Test3() {
        String serviceIp = IpUtils.getServiceIp();
        System.out.println(serviceIp);
    }


    @Autowired
    private RestTemplate restTemplate;

    @Test
    public void Test5() throws IOException {
        Enumeration<NetworkInterface> ifaces = null;
        try {
            ifaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {

        }
        String siteLocalAddress = null;
        while (ifaces.hasMoreElements()) {
            NetworkInterface iface = ifaces.nextElement();
            Enumeration<InetAddress> addresses = iface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                String hostAddress = addr.getHostAddress();
                if (addr instanceof Inet4Address) {
                    if (addr.isSiteLocalAddress()) {
                        siteLocalAddress = hostAddress;
                    } else {
                        break;
                    }
                }
            }
        }
        System.out.println(siteLocalAddress == null ? "" : siteLocalAddress);
    }

    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private UserInfoMapper userInfoMapper;

    @Test
    public void Test6() throws IOException {
        String JUDGE_NAME = "HDU";
        String HOST = "http://acm.hdu.edu.cn";
        String PROBLEM_URL = "/showproblem.php?pid=%s";
        Problem info = new Problem();
        String url = HOST + String.format(PROBLEM_URL, 1016);
        Connection connection = JsoupUtils.getConnectionFromUrl(url, null, null);
        Document document = JsoupUtils.getDocument(connection, null);
        String html = document.html();
        info.setDescription(ReUtil.get(">Problem Description</div> <div class=.*?>([\\s\\S]*?)</div>", html, 1).replaceAll("src=\"../../", "src=\"" + HOST + "/"));
        info.setInput(ReUtil.get(">Input</div> <div class=.*?>([\\s\\S]*?)</div>", html, 1));
        info.setOutput(ReUtil.get(">Output</div> <div class=.*?>([\\s\\S]*?)</div>", html, 1));
        info.setIsRemote(true);
        System.out.println(info.getDescription());
        System.out.println(info.getInput());
        System.out.println(info.getOutput());
    }

    @Test
    public void Test7() throws IOException {
        String JUDGE_NAME = "CF";
        String HOST = "https://codeforces.com";
        String PROBLEM_URL = "/problemset/problem/%s/%s";

        String problemId = "750A";
        String contestId = ReUtil.get("([0-9]+)[A-Z]{1}[0-9]{0,1}", problemId, 1);
        String problemNum = ReUtil.get("[0-9]+([A-Z]{1}[0-9]{0,1})", problemId, 1);


        String url = HOST + String.format(PROBLEM_URL, contestId, problemNum);
        Connection connection = JsoupUtils.getConnectionFromUrl(url, null, null);
        Document document = JsoupUtils.getDocument(connection, null);
        String html = document.html();
        Problem info = new Problem();
        info.setProblemId(JUDGE_NAME + "-" + problemId);

        info.setTitle(ReUtil.get("<div class=\"title\">\\s*" + problemNum + "\\. ([\\s\\S]*?)</div>", html, 1).trim());

        info.setTimeLimit(1000 * Integer.parseInt(ReUtil.get("</div>([\\d\\.]+) (seconds?|s)\\s*</div>", html, 1)));

        info.setMemoryLimit(Integer.parseInt(ReUtil.get("</div>(\\d+) (megabytes|MB)\\s*</div>", html, 1)));

        String tmpDesc = ReUtil.get("standard output\\s*</div></div><div>([\\s\\S]*?)</div><div class=\"input-specification",
                html, 1);
        if (StringUtils.isEmpty(tmpDesc)) {
            tmpDesc = "<div>" + ReUtil.get("(<div class=\"input-file\">[\\s\\S]*?)</div><div class=\"input-specification", html, 1);
        }

        info.setDescription(tmpDesc.replaceAll("src=\"../../", "src=\"" + HOST + "/"));

        info.setInput(ReUtil.get("<div class=\"section-title\">\\s*Input\\s*</div>([\\s\\S]*?)</div><div class=\"output-specification\">", html, 1));

        info.setOutput(ReUtil.get("<div class=\"section-title\">\\s*Output\\s*</div>([\\s\\S]*?)</div><div class=\"sample-tests\">", html, 1));


        List<String> inputExampleList = ReUtil.findAll(Pattern.compile("<div class=\"input\"><div class=\"title\">Input</div><pre>([\\s\\S]*?)</pre></div>"), html, 1);

        List<String> outputExampleList = ReUtil.findAll(Pattern.compile("<div class=\"output\"><div class=\"title\">Output</div><pre>([\\s\\S]*?)</pre></div>"), html, 1);


        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < inputExampleList.size() && i < outputExampleList.size(); i++) {
            sb.append("<input>");
            sb.append(inputExampleList.get(i).replace("<br>", "")).append("</input>");
            sb.append("<output>");
            sb.append(outputExampleList.get(i).replace("<br>", "")).append("</output>");
        }

        info.setExamples(sb.toString());

        System.out.println(info.getExamples());

        info.setHint(ReUtil.get("<div class=\"section-title\">\\s*Note\\s*</div>([\\s\\S]*?)</div></div>", html, 1));
        info.setIsRemote(true);
        info.setSource(String.format("<p>Problem：<a style='color:#1A5CC8' href='https://codeforces.com/problemset/problem/%s/%s'>%s</a></p><p>" +
                        "Contest：" + ReUtil.get("(<a[^<>]+/contest/\\d+\">.+?</a>)", html, 1).replace("/contest", HOST + "/contest")
                        .replace("color: black", "color: #009688;") + "</p>",
                contestId, problemNum, JUDGE_NAME + "-" + problemId));

        List<String> all = ReUtil.findAll(Pattern.compile("<span class=\"tag-box\" style=\"font-size:1\\.2rem;\" title=\"[\\s\\S]*?\">([\\s\\S]*?)</span>"), html, 1);
    }


    @Autowired
    private LanguageEntityServiceImpl languageService;

    @Test
    public void Test8() throws IOException {
        LinkedHashMap<String, String> languageList = new LinkedHashMap<>();
        languageList.put("GNU GCC C11 5.1.0", "text/x-csrc");
        languageList.put("Clang++17 Diagnostics", "text/x-c++src");
        languageList.put("GNU G++11 5.1.0", "text/x-c++src");
        languageList.put("GNU G++14 6.4.0", "text/x-c++src");
        languageList.put("GNU G++17 7.3.0", "text/x-c++src");
        languageList.put("Microsoft Visual C++ 2010", "text/x-c++src");
        languageList.put("Microsoft Visual C++ 2017", "text/x-c++src");
        languageList.put("C# Mono 6.8", "text/x-csharp");
        languageList.put("D DMD32 v2.091.0", "text/x-d");
        languageList.put("Go 1.15.6", "text/x-go");
        languageList.put("Haskell GHC 8.10.1", "text/x-haskell");
        languageList.put("Java 1.8.0_241", "text/x-java");
        languageList.put("Kotlin 1.4.0", "text/x-java");
        languageList.put("OCaml 4.02.1", "text/x-ocaml");
        languageList.put("Delphi 7", "text/x-pascal");
        languageList.put("Free Pascal 3.0.2", "text/x-pascal");
        languageList.put("PascalABC.NET 3.4.2", "text/x-pascal");
        languageList.put("Perl 5.20.1", "text/x-perl");
        languageList.put("PHP 7.2.13", "text/x-php");
        languageList.put("Python 2.7.18", "text/x-python");
        languageList.put("Python 3.9.1", "text/x-python");
        languageList.put("PyPy 2.7 (7.3.0)", "text/x-python");
        languageList.put("PyPy 3.7 (7.3.0)", "text/x-python");
        languageList.put("Ruby 3.0.0", "text/x-ruby");
        languageList.put("Rust 1.49.0", "text/x-rustsrc");
        languageList.put("Scala 2.12.8", "text/x-scala");
        languageList.put("JavaScript V8 4.8.0", "text/javascript");
        languageList.put("Node.js 12.6.3", "text/javascript");
        languageList.put("C# 8, .NET Core 3.1", "text/x-csharp");
        languageList.put("Java 11.0.6", "text/x-java");

        List<Language> languageList1 = new LinkedList<>();
        for (String key : languageList.keySet()) {
            String tmp = languageList.get(key);
            languageList1.add(new Language().setName(key).setDescription(key).setOj("CF").setIsSpj(false).setContentType(tmp));

        }
        boolean b = languageService.saveOrUpdateBatch(languageList1);
        System.out.println(b);
    }


}
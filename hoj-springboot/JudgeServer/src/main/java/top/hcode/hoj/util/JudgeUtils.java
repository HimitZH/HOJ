package top.hcode.hoj.util;

import cn.hutool.json.JSONUtil;
import org.springframework.util.StringUtils;
import top.hcode.hoj.pojo.entity.problem.Problem;

import java.util.*;

/**
 * @Author: Himit_ZH
 * @Date: 2021/11/24 19:16
 * @Description:
 */
public class JudgeUtils {

    @SuppressWarnings("All")
    public static HashMap<String, String> getProblemExtraFileMap(Problem problem, String type) {
        if ("user".equals(type)) {
            if (!StringUtils.isEmpty(problem.getUserExtraFile())) {
                return (HashMap<String, String>) JSONUtil.toBean(problem.getUserExtraFile(), Map.class);
            }
        } else if ("judge".equals(type)) {
            if (!StringUtils.isEmpty(problem.getJudgeExtraFile())) {
                return (HashMap<String, String>) JSONUtil.toBean(problem.getJudgeExtraFile(), Map.class);
            }
        }
        return null;
    }

    public static List<String> translateCommandline(String toProcess) {
        if (toProcess != null && !toProcess.isEmpty()) {
            int state = 0;
            StringTokenizer tok = new StringTokenizer(toProcess, "\"' ", true);
            List<String> result = new ArrayList<>();
            StringBuilder current = new StringBuilder();
            boolean lastTokenHasBeenQuoted = false;

            while (true) {
                while (tok.hasMoreTokens()) {
                    String nextTok = tok.nextToken();
                    switch (state) {
                        case 1:
                            if ("'".equals(nextTok)) {
                                lastTokenHasBeenQuoted = true;
                                state = 0;
                            } else {
                                current.append(nextTok);
                            }
                            continue;
                        case 2:
                            if ("\"".equals(nextTok)) {
                                lastTokenHasBeenQuoted = true;
                                state = 0;
                            } else {
                                current.append(nextTok);
                            }
                            continue;
                    }

                    if ("'".equals(nextTok)) {
                        state = 1;
                    } else if ("\"".equals(nextTok)) {
                        state = 2;
                    } else if (" ".equals(nextTok)) {
                        if (lastTokenHasBeenQuoted || current.length() > 0) {
                            result.add(current.toString());
                            current.setLength(0);
                        }
                    } else {
                        current.append(nextTok);
                    }

                    lastTokenHasBeenQuoted = false;
                }

                if (lastTokenHasBeenQuoted || current.length() > 0) {
                    result.add(current.toString());
                }

                if (state != 1 && state != 2) {
                    return result;
                }

                throw new RuntimeException("unbalanced quotes in " + toProcess);
            }
        } else {
            return new ArrayList<>();
        }
    }

}
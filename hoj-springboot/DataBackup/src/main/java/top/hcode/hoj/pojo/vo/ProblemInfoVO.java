package top.hcode.hoj.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.problem.Tag;
import java.util.HashMap;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2020/12/30 21:37
 * @Description:
 */
@Data
@AllArgsConstructor
public class ProblemInfoVO {
    private Problem problem;
    private List<Tag> tags;
    private List<String> languages;
    private ProblemCountVO problemCount;
    private HashMap<String, String> codeTemplate;
}
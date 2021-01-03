package top.hcode.hoj.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import top.hcode.hoj.pojo.entity.Problem;
import top.hcode.hoj.pojo.entity.ProblemCount;

import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2020/12/30 21:37
 * @Description:
 */
@Data
@AllArgsConstructor
public class ProblemInfoVo {
    private Problem problem;
    private List<String> tags;
    private List<String> languages;
    private ProblemCount problemCount;
}
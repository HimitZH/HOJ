package top.hcode.hoj.pojo.bo;

import lombok.Data;

import java.util.HashMap;
import java.util.List;

/**
 * @Author Himit_ZH
 * @Date 2022/10/15
 */
@Data
public class HydroConfigYamlBO {

    /**
     * # 题目类型，可以为 default(比对输出，可以含spj), objective(客观题), interactive(交互题)
     */
    private String type;

    /**
     * # 全局时空限制（此处的限制优先级低于测试点的限制）
     */
    private String time;

    private String memory;

    /**
     * # 输入输出文件名（例：使用 foo.in 和 foo.out），若使用标准 IO 删除此配置项即可
     */
    private String filename;

    /**
     * # 此部分设置当题目类型为 default 时生效
     * # 比较器类型，支持的值有 default（直接比对，忽略行末空格和文件末换行）, ccr, cena, hustoj, lemon, qduoj, syzoj, testlib(比较常用)
     */
    private String checker_type;

    /**
     * # 比较器文件（当比较器类型不为 default 时填写）
     * # 文件路径（位于压缩包中的路径）
     * # 将通过扩展名识别语言，与编译命令处一致。在默认配置下，C++ 扩展名应为 .cc 而非 .cpp
     */
    private String checker;

    /**
     * # 此部分设置当题目类型为interactive时生效
     * # 交互器路径（位于压缩包中的路径）
     */
    private String interactor;

    /**
     * # Extra files 额外文件
     */
    private List<String> user_extra_files;

    private List<String> judge_extra_files;

    private List<HashMap<String, String>> cases;

    /**
     *  # 单个测试点分数
     */
    private Integer score;

    private List<String> langs;

    private List<HashMap<String,Object>> subtasks;

}

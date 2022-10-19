package top.hcode.hoj.pojo.vo;

import lombok.Data;
import top.hcode.hoj.pojo.entity.problem.Tag;
import top.hcode.hoj.pojo.entity.problem.TagClassification;

import java.io.Serializable;
import java.util.List;

/**
 * @Author Himit_ZH
 * @Date 2022/8/3
 */
@Data
public class ProblemTagVO implements Serializable {
    /**
     * 标签分类
     */
    private TagClassification classification;

    /**
     * 标签列表
     */
    private List<Tag> tagList;

}

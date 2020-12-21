package top.hcode.hoj.pojo.dto;

import lombok.Data;
import top.hcode.hoj.pojo.entity.Language;
import top.hcode.hoj.pojo.entity.Problem;
import top.hcode.hoj.pojo.entity.ProblemCase;
import top.hcode.hoj.pojo.entity.Tag;

import java.util.List;


/**
 * @Author: Himit_ZH
 * @Date: 2020/12/14 22:30
 * @Description:
 */
@Data
public class ProblemDto {

    private Problem problem;

    private List<ProblemCase> samples;

    private List<Language> languages;

    private List<Tag> tags;

}
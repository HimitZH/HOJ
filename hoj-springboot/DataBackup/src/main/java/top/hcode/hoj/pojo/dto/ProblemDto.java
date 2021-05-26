package top.hcode.hoj.pojo.dto;

import lombok.Data;
import top.hcode.hoj.pojo.entity.*;

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

    private Boolean isUploadTestCase;

    private String uploadTestcaseDir;

    private Boolean isSpj;

    private List<Language> languages;

    private List<Tag> tags;

    private List<CodeTemplate> codeTemplates;

}
package top.hcode.hoj.pojo.dto;

import lombok.Data;

/**
 * @Author Himit_ZH
 * @Date 2022/10/22
 */
@Data
public class LastAcceptedCodeVO {

    private Long submitId;

    private String code;

    private String language;
}

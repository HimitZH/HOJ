package top.hcode.hoj.pojo.dto;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * @Author sgpublic
 * @Date 2022/4/2 19:44
 * @Description
 */
@Data
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WebConfigDTO {

    /**
     * 基础 URL
     */
    private String baseUrl;

    /**
     * 网站名称
     */
    private String name;

    /**
     * 网站简称
     */
    private String shortName;

    /**
     * 网站简介
     */
    private String description;

    /**
     * 是否允许注册
     */
    private Boolean register;

    /**
     * 备案名
     */
    private String recordName;

    /**
     * 备案地址
     */
    private String recordUrl;

    /**
     * 项目名
     */
    private String projectName;

    /**
     * 项目地址
     */
    private String projectUrl;
}

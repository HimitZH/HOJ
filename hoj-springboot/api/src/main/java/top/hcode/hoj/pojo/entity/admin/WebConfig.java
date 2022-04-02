package top.hcode.hoj.pojo.entity.admin;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Author sgpublic
 * @Date 2022/4/2 19:44
 * @Description
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="WebConfig", description="")
public class WebConfig {

    /**
     * 基础 URL
     */
    private String baseUrl = null;

    /**
     * 网站名称
     */
    private String name = null;

    /**
     * 网站简称
     */
    private String shortName = null;

    /**
     * 网站简介
     */
    private String description = null;

    /**
     * 是否允许注册
     */
    private Boolean register = null;

    /**
     * 备案名
     */
    private String recordName = null;

    /**
     * 备案地址
     */
    private String recordUrl = null;

    /**
     * 项目名
     */
    private String projectName = null;

    /**
     * 项目地址
     */
    private String projectUrl = null;
}

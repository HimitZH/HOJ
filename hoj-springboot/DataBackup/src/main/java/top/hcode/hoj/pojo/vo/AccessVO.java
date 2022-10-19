package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/10 17:51
 * @Description:
 */
@Data
public class AccessVO {

    @ApiModelProperty(value = "是否有进入比赛或训练的权限")
    private Boolean access;
}
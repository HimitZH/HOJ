package top.hcode.hoj.pojo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @Author: Himit_ZH
 * @Date: 2021/2/4 22:29
 * @Description:
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="ToJudge", description="后台服务与判题服务之间的数据交互格式")
public class ToJudge implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("判题数据或特判程序编译源码")
    private Judge judge;

    @ApiModelProperty("验证的token")
    private String token;

    @ApiModelProperty("远程判题不为空，hoj判题为null，例如HDU-1000")
    private String remoteJudge;

    @ApiModelProperty("远程判题所用账号")
    private String username;

    @ApiModelProperty("远程判题所用密码")
    private String password;

//    @ApiModelProperty("重新尝试的次数,三次重新调用判题机依旧失败，直接判为系统错误！")
//    private Integer tryAgainNum;

}
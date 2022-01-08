package top.hcode.hoj.pojo.entity.judge;

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

    @ApiModelProperty("判题数据")
    private Judge judge;

    @ApiModelProperty("验证的token")
    private String token;

    @ApiModelProperty("远程判题不为空，hoj判题为null，例如HDU-1000")
    private String remoteJudgeProblem;

    @ApiModelProperty("是否为远程判题重判，仅限于已有远程OJ的提交id的重判")
    private Boolean isHasSubmitIdRemoteReJudge;

    @ApiModelProperty("远程判题所用账号")
    private String username;

    @ApiModelProperty("远程判题所用密码")
    private String password;

    @ApiModelProperty("调用判题机的ip")
    private String judgeServerIp;

    @ApiModelProperty("调用判题机的port")
    private Integer judgeServerPort;

    /**
     * VJ判題辅助选择判题机序号使用
     */
    private Integer index;

    private Integer size;

}
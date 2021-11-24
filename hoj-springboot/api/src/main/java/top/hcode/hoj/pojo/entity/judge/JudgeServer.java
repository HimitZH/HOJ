package top.hcode.hoj.pojo.entity.judge;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @Author: Himit_ZH
 * @Date: 2021/4/15 11:08
 * @Description:
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "JudgeServer对象", description = "判题服务器配置")
public class JudgeServer {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "判题服务名字")
    private String name;

    @ApiModelProperty(value = "判题机ip")
    private String ip;

    @ApiModelProperty(value = "判题机端口号")
    private Integer port;

    @ApiModelProperty(value = "ip:port")
    private String url;

    @ApiModelProperty(value = "判题机所在服务器cpu核心数")
    private Integer cpuCore;

    @ApiModelProperty(value = "当前判题数")
    private Integer taskNumber;

    @ApiModelProperty(value = "判题并发最大数")
    private Integer maxTaskNumber;

    @ApiModelProperty(value = "0可用，1不可用")
    private Integer status;

    @ApiModelProperty(value = "是否为远程判题vj")
    private Boolean isRemote;

    @ApiModelProperty(value = "是否可提交CF")
    private Boolean cfSubmittable;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;

}
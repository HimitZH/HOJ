package top.hcode.hoj.pojo.entity.group;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="GroupMember对象", description="")
public class GroupMember implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "团队id")
    private Long gid;

    @ApiModelProperty(value = "用户id")
    private String uid;

    @ApiModelProperty(value = "默认为1表示申请中，2为拒绝，3为普通成员，4为管理员，5为超级管理员")
    private Integer auth;

    @ApiModelProperty(value = "申请理由")
    private String reason;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;
}

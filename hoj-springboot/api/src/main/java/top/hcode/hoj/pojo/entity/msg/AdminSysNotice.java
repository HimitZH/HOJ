package top.hcode.hoj.pojo.entity.msg;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @Author: Himit_ZH
 * @Date: 2021/10/1 20:11
 * @Description:
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="AdminSysNotice", description="")
public class AdminSysNotice {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "通知标题")
    private String title;

    @ApiModelProperty(value = "通知内容")
    private String content;

    @ApiModelProperty(value = "发给哪些用户类型,例如全部用户All，指定单个用户Single，管理员Admin")
    private String type;

    @ApiModelProperty(value = "是否已被拉取过，如果已经拉取过，就无需再次拉取")
    private Boolean state;

    @ApiModelProperty(value = "接受通知的用户的id，如果type为single，那么recipient 为该用户的id;否则recipient为null")
    private String recipientId;

    @ApiModelProperty(value = "发布通知的管理员id")
    private String adminId;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;

}
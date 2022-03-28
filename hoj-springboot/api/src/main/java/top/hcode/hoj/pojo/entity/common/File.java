package top.hcode.hoj.pojo.entity.common;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @Author: Himit_ZH
 * @Date: 2021/1/11 13:58
 * @Description:
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="File对象", description="")
@TableName("`file`")
public class File {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "用户id")
    private String uid;

    @ApiModelProperty(value = "团队id")
    private Long gid;

    @ApiModelProperty(value = "文件所属类型，例如avatar")
    @TableField("`type`")
    private String type;

    @ApiModelProperty(value = "文件名")
    private String name;

    @ApiModelProperty(value = "文件后缀格式")
    private String suffix;

    @ApiModelProperty(value = "文件所在文件夹的路径")
    private String folderPath;

    @ApiModelProperty(value = "文件绝对路径")
    private String filePath;

    @ApiModelProperty(value = "是否删除")
    @TableField("`delete`")
    private Boolean delete;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @ApiModelProperty(value = "修改时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;

}
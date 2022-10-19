package top.hcode.hoj.pojo.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @Author Himit_ZH
 * @Date 2022/5/13
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class DiscussionReportVO {

    private Long id;

    @ApiModelProperty(value = "讨论id")
    private Integer did;

    @ApiModelProperty(value = "讨论所属团队id")
    private Long gid;

    @ApiModelProperty(value = "讨论标题")
    private String discussionTitle;

    @ApiModelProperty(value = "讨论作者")
    private String discussionAuthor;

    @ApiModelProperty(value = "举报者的用户名")
    private String reporter;

    @ApiModelProperty(value = "举报内容")
    private String content;

    @ApiModelProperty(value = "是否已读")
    private Boolean status;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;

}

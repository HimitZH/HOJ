package top.hcode.hoj.pojo.entity.discussion;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @Author: Himit_ZH
 * @Date: 2021/5/4 22:11
 * @Description:
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="Discussion对象", description="")
public class Discussion {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "分类id")
    private Integer categoryId;

    @ApiModelProperty(value = "讨论标题")
    private String title;

    @ApiModelProperty(value = "讨论简介")
    private String description;

    @ApiModelProperty(value = "讨论内容")
    private String content;

    @ApiModelProperty(value = "题目关联 默认为null则不关联题目")
    private String pid;

    @ApiModelProperty(value = "发表者id")
    private String uid;

    @ApiModelProperty(value = "发表者用户名")
    private String author;

    @ApiModelProperty(value = "发表者头像地址")
    private String avatar;

    @ApiModelProperty(value = "发表者角色")
    private String role;

    @ApiModelProperty(value = "浏览数量")
    private Integer viewNum;

    @ApiModelProperty(value = "点赞数量")
    private Integer likeNum;

    @ApiModelProperty(value = "评论数量,包括其评论及其回复数")
    private Integer commentNum;

    @ApiModelProperty(value = "优先级，是否置顶")
    private Boolean topPriority;

    @ApiModelProperty(value = "是否封禁或删除 0正常，1封禁")
    private Integer status;

    @ApiModelProperty(value = "团队ID")
    private Long gid;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;
}
package top.hcode.hoj.pojo.entity.problem;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="Problem对象", description="")
public class Problem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "题目的自定义ID 例如（HOJ-1000）")
    private String problemId;

    @ApiModelProperty(value = "题目")
    private String title;

    @ApiModelProperty(value = "作者")
    private String author;

    @ApiModelProperty(value = "0为ACM,1为OI")
    private Integer type;

    @ApiModelProperty(value = "default,spj,interactive")
    private String judgeMode;

    @ApiModelProperty(value = "default,subtask_lowest,subtask_average")
    private String judgeCaseMode;

    @ApiModelProperty(value = "单位ms")
    private Integer timeLimit;

    @ApiModelProperty(value = "单位mb")
    private Integer memoryLimit;

    @ApiModelProperty(value = "单位mb")
    private Integer stackLimit;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "输入描述")
    private String input;

    @ApiModelProperty(value = "输出描述")
    private String output;

    @ApiModelProperty(value = "题面样例")
    private String examples;

    @ApiModelProperty(value = "是否为vj判题")
    private Boolean isRemote;

    @ApiModelProperty(value = "题目来源（vj判题时例如HDU-1000的链接）")
    private String source;

    @ApiModelProperty(value = "题目难度")
    private Integer difficulty;

    @ApiModelProperty(value = "备注,提醒")
    private String hint;

    @ApiModelProperty(value = "默认为1公开，2为私有，3为比赛中")
    private Integer auth;

    @ApiModelProperty(value = "当该题目为oi题目时的分数")
    private Integer ioScore;

    @ApiModelProperty(value = "该题目对应的相关提交代码，用户是否可用分享")
    private Boolean codeShare;

    @ApiModelProperty(value = "特判程序或交互程序的代码")
    @TableField(value="spj_code",updateStrategy = FieldStrategy.IGNORED)
    private String spjCode;

    @ApiModelProperty(value = "特判程序或交互程序的语言")
    @TableField(value="spj_language",updateStrategy = FieldStrategy.IGNORED)
    private String spjLanguage;

    @ApiModelProperty(value = "特判程序或交互程序的额外文件 json key:name value:content")
    @TableField(value="user_extra_file",updateStrategy = FieldStrategy.IGNORED)
    private String userExtraFile;

    @ApiModelProperty(value = "特判程序或交互程序的额外文件 json key:name value:content")
    @TableField(value="judge_extra_file",updateStrategy = FieldStrategy.IGNORED)
    private String judgeExtraFile;

    @ApiModelProperty(value = "是否默认去除用户代码的每行末尾空白符")
    private Boolean isRemoveEndBlank;

    @ApiModelProperty(value = "是否默认开启该题目的测试样例结果查看")
    private Boolean openCaseResult;

    @ApiModelProperty(value = "题目测试数据是否是上传的")
    private Boolean isUploadCase;

    @ApiModelProperty(value = "题目测试数据的版本号")
    private String caseVersion;

    @ApiModelProperty(value = "修改题目的管理员用户名")
    private String modifiedUser;

    @ApiModelProperty(value = "是否为团队内的题目")
    private Boolean isGroup;

    @ApiModelProperty(value = "团队ID")
    private Long gid;

    @ApiModelProperty(value = "申请公开的进度：null为未申请，1为申请中，2为申请通过，3为申请拒绝")
    private Integer applyPublicProgress;

    @ApiModelProperty(value = "是否是file io自定义输入输出文件模式")
    @TableField(value="is_file_io")
    private Boolean isFileIO;

    @ApiModelProperty(value = "题目指定的file io输入文件的名称")
    @TableField(value="io_read_file_name")
    private String ioReadFileName;

    @ApiModelProperty(value = "题目指定的file io输出文件的名称")
    @TableField(value="io_write_file_name")
    private String ioWriteFileName;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;


}

package top.hcode.hoj.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @Author Himit_ZH
 * @Date 2022/10/3
 */
@ApiModel(value = "比赛滚榜页面所需比赛信息")
@Data
public class ContestScrollBoardInfoVO {

    @ApiModelProperty(value = "比赛id")
    private Long id;

    @ApiModelProperty(value = "排行榜显示（username、nickname、realname）")
    private String rankShowName;

    @ApiModelProperty(value = "比赛题目数")
    private Integer problemCount;

    @ApiModelProperty(value = "比赛题目对应气球颜色")
    private HashMap<String,String> balloonColor;

    @ApiModelProperty(value = "比赛开始时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @ApiModelProperty(value = "比赛封榜时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss", timezone = "GMT+8")
    private Date sealRankTime;

    @ApiModelProperty(value = "打星的用户名列表")
    private List<String> starUserList;

}

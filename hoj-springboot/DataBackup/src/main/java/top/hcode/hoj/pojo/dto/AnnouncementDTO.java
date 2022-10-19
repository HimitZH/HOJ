package top.hcode.hoj.pojo.dto;

import lombok.Data;
import top.hcode.hoj.pojo.entity.common.Announcement;

import javax.validation.constraints.NotBlank;

/**
 * @Author: Himit_ZH
 * @Date: 2020/12/21 22:55
 * @Description:
 */
@Data
public class AnnouncementDTO {
    @NotBlank(message = "比赛id不能为空")
    private Long cid;

    private Announcement announcement;
}
package top.hcode.hoj.pojo.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import top.hcode.hoj.pojo.entity.discussion.Reply;

/**
 * @Author: Himit_ZH
 * @Date: 2021/6/24 17:00
 * @Description:
 */
@Data
@Accessors(chain = true)
public class ReplyDTO {

    private Reply reply;

    private Integer did;

    private Integer quoteId;

    private String quoteType;
}
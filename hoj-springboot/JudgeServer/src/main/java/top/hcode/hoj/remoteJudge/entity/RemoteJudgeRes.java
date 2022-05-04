package top.hcode.hoj.remoteJudge.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.hcode.hoj.pojo.entity.judge.JudgeCase;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2022/1/29 11:25
 * @Description:
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class RemoteJudgeRes implements Serializable {
    private static final long serialVersionUID = 999L;

    private Integer status;

    private Integer time;

    private Integer memory;

    private String errorInfo;

    private List<JudgeCase> judgeCaseList;
}
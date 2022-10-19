package top.hcode.hoj.pojo.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import top.hcode.hoj.pojo.entity.training.Training;
import top.hcode.hoj.pojo.entity.training.TrainingCategory;

/**
 * @Author: Himit_ZH
 * @Date: 2021/11/22 21:49
 * @Description: 后台管理训练的传输类
 */
@Data
@Accessors(chain = true)
public class TrainingDTO {

    private Training training;

    private TrainingCategory trainingCategory;
}
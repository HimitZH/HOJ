package top.hcode.hoj.pojo.dto;

import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Author sgpublic
 * @Date 2022/4/2 19:49
 * @Description
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
@ApiModel(value="DBAndRedisConfig", description="")
public class DBAndRedisConfigDto {

    /**
     * 数据库名称
     */
    private String dbName;

    /**
     * MySQL 主机
     */
    private String dbHost;

    /**
     * MySQL 端口
     */
    private Integer dbPort;

    /**
     * MySQL 用户名
     */
    private String dbUsername;

    /**
     * MySQL 密码
     */
    private String dbPassword ;

    /**
     * Redis 主机
     */
    private String redisHost;

    /**
     * Redis 端口
     */
    private Integer redisPort;

    /**
     * Redis 密码
     */
    private String redisPassword ;
}

package top.hcode.hoj.pojo.entity.admin;

import io.swagger.annotations.ApiModel;
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
@ApiModel(value="DBAndRedisConfig", description="")
public class DBAndRedisConfig {

    /**
     * 数据库名称
     */
    private String dbName = null;

    /**
     * MySQL 主机
     */
    private String dbHost = null;

    /**
     * MySQL 端口
     */
    private Integer dbPort = -1;

    /**
     * MySQL 用户名
     */
    private String dbUsername = null;

    /**
     * MySQL 密码
     */
    private String dbPassword = null;

    /**
     * Redis 主机
     */
    private String redisHost = null;

    /**
     * Redis 端口
     */
    private Integer redisPort = -1;

    /**
     * Redis 密码
     */
    private String redisPassword = null;
}

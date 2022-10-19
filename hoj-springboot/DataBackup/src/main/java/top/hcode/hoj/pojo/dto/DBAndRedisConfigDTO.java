package top.hcode.hoj.pojo.dto;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * @Author sgpublic
 * @Date 2022/4/2 19:49
 * @Description
 */
@Data
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DBAndRedisConfigDTO {

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

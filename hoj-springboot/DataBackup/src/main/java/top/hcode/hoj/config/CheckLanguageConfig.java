package top.hcode.hoj.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import top.hcode.hoj.pojo.entity.problem.Language;

import java.util.List;

/**
 * @Author Himit_ZH
 * @Date 2023/7/8
 */
@Configuration
@ConfigurationProperties(prefix = "check-language-config")
@Data
public class CheckLanguageConfig {

    private List<Language> list;

    @Override
    public String toString() {
        return "AntPoolConfigList{" +
                "list=" + list +
                '}';
    }
}

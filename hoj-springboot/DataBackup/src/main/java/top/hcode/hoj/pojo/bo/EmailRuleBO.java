package top.hcode.hoj.pojo.bo;

import lombok.Data;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * @Author: Himit_ZH
 * @Date: 2021/10/17 12:10
 * @Description: 邮箱规则类，读取email-rule.yml文件
 */

@Component
@PropertySource(value = "classpath:email-rule.yml",factory = CompositePropertySourceFactory.class)
@ConfigurationProperties(prefix = "hoj")
@Data
public class EmailRuleBO {

    private List<String> blackList;
}

class CompositePropertySourceFactory extends DefaultPropertySourceFactory {
    @Override
    public org.springframework.core.env.PropertySource<?> createPropertySource(@Nullable String name, EncodedResource resource) throws IOException {
        String sourceName = Optional.ofNullable(name).orElse(resource.getResource().getFilename());
        if (!resource.getResource().exists()) {
            // return an empty Properties
            return new PropertiesPropertySource(sourceName, new Properties());
        } else if (sourceName.endsWith(".yml") || sourceName.endsWith(".yaml")) {
            Properties propertiesFromYaml = loadYaml(resource);
            return new PropertiesPropertySource(sourceName, propertiesFromYaml);
        } else {
            return super.createPropertySource(name, resource);
        }
    }
    /**
     * load yaml file to properties
     *
     * @param resource
     * @return
     * @throws IOException
     */
    private Properties loadYaml(EncodedResource resource) throws IOException {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(resource.getResource());
        factory.afterPropertiesSet();
        return factory.getObject();
    }
}
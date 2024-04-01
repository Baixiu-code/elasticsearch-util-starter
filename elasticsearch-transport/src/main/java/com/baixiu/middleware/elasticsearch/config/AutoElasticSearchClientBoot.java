package com.baixiu.middleware.elasticsearch.config;

import com.baixiu.middleware.elasticsearch.transport.ElasticSearchTemplateClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * es starter 入口.
 * 1.关于properties的注入解析
 * 2.properties解析后的 elasticsearch client build
 * @author chenfanglin1
 * @date 创建时间 2024/4/1 10:52 AM
 */
@Configuration
@EnableConfigurationProperties(value=ElasticSearchTemplateProperties.class)
public class AutoElasticSearchClientBoot {
    
    @Bean
    public ElasticSearchTemplateClient buildESTemplateClient(){
        ElasticSearchTemplateClient elasticSearchTemplateClient=new ElasticSearchTemplateClient();
        elasticSearchTemplateClient.init();
        return elasticSearchTemplateClient;
    }
    
    
    
}

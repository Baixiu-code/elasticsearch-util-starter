package com.baixiu.middleware.elasticsearch.config;

import com.baixiu.middleware.elasticsearch.transport.ElasticSearchTemplateClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * es starter 入口.
 * 1.关于properties的注入解析
 * 2.properties解析后的 elasticsearch client build
 * @author chenfanglin1
 * @date 创建时间 2024/4/1 10:52 AM
 */
@Configuration
@ComponentScan(basePackages="com.baixiu.middleware.elasticsearch")
@EnableConfigurationProperties(value=ElasticSearchTemplateProperties.class)
public class AutoElasticSearchClientBoot {
    
    @Autowired
    private ElasticSearchTemplateProperties elasticSearchTemplateProperties;
    
    @Bean
    public ElasticSearchTemplateClient buildESTemplateClient(){
        ElasticSearchTemplateClient elasticSearchTemplateClient=new ElasticSearchTemplateClient(elasticSearchTemplateProperties);
        elasticSearchTemplateClient.init ();
        return elasticSearchTemplateClient;
    }
    
    
    
}

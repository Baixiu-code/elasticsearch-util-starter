package com.baixiu.middleware.elasticsearch.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * es template config 
 * @author chenfanglin1
 * @date 创建时间 2024/4/1 10:52 AM
 */
@ConfigurationProperties(prefix = "es.client.cluster.properties")
@Data
public class ElasticSearchTemplateProperties {

    /**
     * 集群节点
     */
    private String clusterNodes;

    /**
     * 集群name
     */
    private String clusterName;

    /**
     * 是否开启嗅探
     */
    private String clientTransportSniff;

    /**
     * 用户名
     */
    private String username;

    /**
     * pwd
     */
    private String password;
    
}

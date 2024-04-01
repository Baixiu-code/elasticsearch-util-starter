package com.baixiu.middleware.elasticsearch.transport;

import com.baixiu.middleware.elasticsearch.config.ElasticSearchTemplateProperties;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

/**
 * es template tcp transport client 
 * @author baixiu
 * @date 2024年03月30日
 */
public class ElasticSearchTemplateClient {

    private static final String AUTH_KEY = "request.headers.Authorization";
    
    private ElasticSearchTemplateProperties elasticSearchTemplateProperties;

    private Client client;

    public ElasticSearchTemplateClient(ElasticSearchTemplateProperties elasticSearchTemplateProperties) {
        this.elasticSearchTemplateProperties=elasticSearchTemplateProperties;
    }


    public void init() {
        Settings settings = Settings.builder()
                .put("cluster.name", elasticSearchTemplateProperties.getClusterName())
                .put("client.transport.sniff", elasticSearchTemplateProperties.getClientTransportSniff())
                .put(AUTH_KEY, basicAuthHeaderValue(elasticSearchTemplateProperties.getUsername(),elasticSearchTemplateProperties.getPassword()))
                .build();

        if (StringUtils.isBlank(elasticSearchTemplateProperties.getClusterNodes())) {
            throw new RuntimeException ("clusterNodes is empty.");
        }

        //创建集群client并添加集群节点地址
        PreBuiltTransportClient transportClient = new PreBuiltTransportClient(settings);

        try {
            String[] nodes = elasticSearchTemplateProperties.getClusterNodes().split(";");
            for (String node : nodes) {
                String[] host = node.split(":");
                transportClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host[0])
                        , Integer.parseInt(host[1])));
            }
        } catch (Exception e) {
            throw new RuntimeException ("ElasticsearchTemplate init error", e);
        }
        client = transportClient;
    }

    /**
     * 基础的base64生成
     * @param username 用户名
     * @param passwd   密码
     * @return 加密后的用户名和密码
     */
    private static String basicAuthHeaderValue(String username, String passwd) {
        CharBuffer chars = CharBuffer.allocate(username.length() + passwd.length() + 1);
        byte[] charBytes = null;
        try {
            chars.put(username).put(':').put(passwd.toCharArray());
            charBytes = toUtf8Bytes(chars.array());

            String basicToken = Base64.getEncoder().encodeToString(charBytes);
            return "Basic " + basicToken;
        } finally {
            Arrays.fill(chars.array(), (char) 0);
            if (charBytes != null) {
                Arrays.fill(charBytes, (byte) 0);
            }
        }
    }

    public static byte[] toUtf8Bytes(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(byteBuffer.array(), (byte) 0);
        return bytes;
    }

    @PreDestroy
    public void destroy(){
        client.close();
    }

    public Client getClient() {
        return client;
    }

    
}

封装es常见操作.
# v1.0
虽然是Elasticsearch目前正在推进Restful形式API,当目前仍旧不少应用仍在老版本的transport版本.涉及到代码迁移和测试运维成本.目前可能长期还是会有使用.
提供一个相对来说比较易于操作的transport API 显得仍有需要和必要.

5.4.3 transport client .https://www.elastic.co/cn/downloads/past-releases/elasticsearch-5-4-3 download.

```xml
 <dependency>
    <groupId>org.elasticsearch.client</groupId>
    <artifactId>transport</artifactId>
    <version>5.4.3</version>
</dependency>
<dependency>
    <groupId>org.elasticsearch</groupId>
    <artifactId>elasticsearch</artifactId>
    <version>5.4.3</version>
</dependency>
<dependency>
    <groupId>org.elasticsearch.plugin</groupId>
    <artifactId>transport-netty4-client</artifactId>
    <version>5.4.3</version>
</dependency>

```

5.6.x 版本 java doc 官方地址:https://www.elastic.co/guide/en/elasticsearch/reference/5.6/install-elasticsearch.html

需要注意的是目前transport6.x版本不兼容9300连接端口.并且5.5版本链接出现NoNodeAvailableException问题.故这里选用了这个版本进行transport client 链接.

>引用:按照目前elasticsearch的规划,会在Elasticsearch7.0版本deprecating该transport形式连接.Elasticsearch8.0版本完成完整的丢弃.转而使用HTTP Restful的形式api.
>参见:https://www.elastic.co/guide/en/elasticsearch/client/java-api/5.6/java-api.html
><img width="876" alt="image" src="https://github.com/Baixiu-code/elasticsearch-util-starter/assets/12585680/47894a8a-07d7-457b-b21b-a979e40283f3">

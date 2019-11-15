package com.example.elasticsearch.configuration;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.example.elasticsearch.configuration.ElasticsearchProperties.PREFIX;

/**
 * @author jackie wang
 * @Title: ElasticsearchAutoConfiguration
 * @ProjectName elasticsearch-crud
 * @Description: Elasticsearch客户端自动配置
 * @date 2019/9/27 16:52
 */
@Configuration
@EnableConfigurationProperties(ElasticsearchProperties.class) // 激活配置属性
@ConditionalOnProperty(prefix = PREFIX, value = "hostAndPortList[0]") // 条件加载配置属性，如果满足条件，实例化当前类
public class ElasticsearchAutoConfiguration {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ElasticsearchProperties properties;

    @Bean
    @ConditionalOnProperty(prefix = PREFIX, value = "auth", havingValue = "false") // 判断auth是否为true，如果是则获取属性值username，password
    public RestHighLevelClient restHignLevelClient() {
        HttpHost[] httpHosts = getHttpHost();
        return new RestHighLevelClient(RestClient.builder(httpHosts));
    }

    /**
     * auth模式
     * @return
     */
    @Bean
    @ConditionalOnProperty(prefix = PREFIX, value = "auth") // 判断auth是否为true，如果是则获取属性值username，password
    public RestHighLevelClient restHignLevelClientAuth() {
        HttpHost[] httpHosts = getHttpHost();

        // 安全凭据
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(properties.getUsername(), properties.getPassword()));

        // 设置认证
        RestClientBuilder builder = RestClient.builder(httpHosts);
        builder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
            @Override
            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
                // 禁用抢先认证的方式
                httpAsyncClientBuilder.disableAuthCaching();
                return httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
        });

        return new RestHighLevelClient(builder);
    }

    private HttpHost[] getHttpHost() {
        HttpHost[] httpHosts = new HttpHost[properties.getHostAndPortList().size()];
        // 获取es主机和端口列表，然后添加到httpHosts
        int count=0;
        for (String host: properties.getHostAndPortList()) {
            HttpHost httpHost = new HttpHost(host.split(":")[0],
                    Integer.parseInt(host.split(":")[1]),"http");
            httpHosts[count] = httpHost;
            count++;
        }
        return httpHosts;
    }

}

package com.example.elasticsearch.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

import static com.example.elasticsearch.configuration.ElasticsearchProperties.PREFIX;

/**
 * @author jackie wang
 * @Title: ElasticsearchProperties
 * @ProjectName elasticsearch-crud
 * @Description: 自定义Elasticsearch属性
 * @date 2019/9/27 16:48
 */
@ConfigurationProperties(PREFIX)
public class ElasticsearchProperties {
    public static final String PREFIX = "spring.elasticsearch";

    /** host和port配置（192.168.1.10:9200） */
    private List<String> hostAndPortList;
    /** elasticsearch是否需要认证，如果需要，则username和password不能为空 */
    private Boolean auth;
    private String username;
    private String password;

    public List<String> getHostAndPortList() {
        return hostAndPortList;
    }

    public void setHostAndPortList(List<String> hostAndPortList) {
        this.hostAndPortList = hostAndPortList;
    }

    public Boolean getAuth() {
        return auth;
    }

    public void setAuth(Boolean auth) {
        this.auth = auth;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

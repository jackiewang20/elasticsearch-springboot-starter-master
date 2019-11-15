package com.example.es.user.provider;

import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

/**
 * @author jackie wang
 * @Title: ElasticsearchCrudUserProviderApp
 * @ProjectName elasticsearch-crud
 * @Description:
 * @date 2019/9/25 16:42
 */
@SpringBootApplication(scanBasePackages = {"com.example.es.user.provider","com.example.elasticsearch"})
public class ElasticsearchCrudUserProviderApp implements CommandLineRunner {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) {
        SpringApplication.run(ElasticsearchCrudUserProviderApp.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
//        init();
    }

        public void init() throws IOException {

//        System.out.println("hello--------");

        /* 使用帮助文档参考官网：
         * https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.1/java-rest-high-supported-apis.html
         */
//         elasticsearch 初始化

        logger.info("初始化连接elasticsearch");
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("192.168.213.118", 9200, "http"),
                        new HttpHost("192.168.213.119", 9200, "http")
                ));
        logger.info("elasticsearch已经链接成功。");

        // 1.Index Request创建文档：index名称为posts，创建文档时候如果索引不存在，则先创建索引
//        IndexRequest request = new IndexRequest("posts");
//        request.id("2")
//        .source("field", "value")
//        .setIfSeqNo(10L) // 新创建的doc和当前_seq_no序列号版本不同，说明该doc已经创建；用来检测现有文档版本
//        .setIfPrimaryTerm(20) // 新创建的doc和当前_primary_term主键版本不同，说明该doc已经创建；用来检测现有文档版本
//        ;

        IndexRequest request = new IndexRequest("posts");
        request.id("3")
                .source("field", "value")
                .opType(DocWriteRequest.OpType.CREATE) // 判断id编号为3的文档是否创建，如果存在，报文档冲突异常
        ;

        String jsonString = "{" +
                "\"user\":\"kimchy\"," +
                "\"postDate\":\"2013-01-30\"," +
                "\"message\":\"trying out Elasticsearch\"" +
                "}";
        request.source(jsonString, XContentType.JSON);
        try {
            IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
            logger.info("[IndexResponse]:"+indexResponse.getResult());
        } catch (ElasticsearchException e) {
            if (e.status() == RestStatus.CONFLICT) {
                logger.info("document already exists.");
            } else {
                logger.error("Elasticsearch Exception:", e);
            }
        }

        client.close();
        logger.info("elasticsearch已关闭。");
    }
}

package com.example.es.user.provider.controller;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author jackie wang
 * @Title: PostsController
 * @ProjectName elasticsearch-crud
 * @Description: 发帖子：使用未封装的代码对ES进行CRUD操作。
 * @date 2019/9/27 18:36
 */
@RequestMapping("posts")
@RestController
public class PostsController {
    private final static String ES_INDEX = "posts";
    private final static String ES_DOC = "_doc";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RestHighLevelClient client;

    @RequestMapping(value = "/create", method = RequestMethod.GET)
    public String indexRequest() throws Exception {
        String msg = "[IndexResponse] Created successful.";
        String id = "4";
        IndexResponse indexResponse = null;
        IndexRequest request = new IndexRequest(ES_INDEX);
        request.id(id)
                .opType(DocWriteRequest.OpType.CREATE) // 判断id编号为3的文档是否创建，如果存在，报文档冲突异常
        ;

        String jsonString = "{" +
                "\"user\":\"kimchy\"," +
                "\"postDate\":\"2013-01-30\"," +
                "\"message\":\"trying out Elasticsearch\"" +
                "}";
        request.source(jsonString, XContentType.JSON);

        try {
            indexResponse = client.index(request, RequestOptions.DEFAULT);
            logger.info("[IndexResponse]:" + indexResponse.getResult());
        } catch (ElasticsearchException e) {
            if (e.status() == RestStatus.CONFLICT) {
                msg = String.format("%s%s%s", "[ElasticsearchException] Document ", id, " already exists.");
                logger.warn(msg);
            } else if(e.status() == RestStatus.NOT_FOUND) {
                msg = String.format("%s%s", "[ElasticsearchException]索引不存在。", e);
                logger.error(msg);
            } else {
                msg = String.format("%s%s", "[ElasticsearchException]", e);
                logger.error(msg);
            }
        } catch (Exception e) {
            msg = String.format("%s%s", "[Exception]", e);
            logger.error(msg);
        }

        return msg;
    }


    @RequestMapping(value = "/createAsync", method = RequestMethod.GET)
    public String indexRequestAsync() throws Exception {
        String msg = "[IndexResponseAsync] Created successful.";
        String id ="5";
        IndexRequest indexRequest = new IndexRequest(ES_INDEX);
        indexRequest.id(id)
                .opType(DocWriteRequest.OpType.CREATE);

        String jsonString = "{" +
                "\"user\":\"jackie\"," +
                "\"postDate\":\"2019-01-30\"," +
                "\"message\":\"Hello Elasticsearch, this is a async request.\"" +
                "}";
        indexRequest.source(jsonString, XContentType.JSON);

        try {
            ActionListener<IndexResponse> actionListener = new ActionListener<IndexResponse>() {
                @Override
                public void onResponse(IndexResponse indexResponse) {
                    String index = indexResponse.getIndex();
                    String id = indexResponse.getId();
                    Long version = indexResponse.getVersion();

                    logger.info("[IndexResponseAsync] Index:{}, Id:{}, Version:{}", index, id, version);

                    if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
                        logger.info("[IndexResponseAsync] Created successful.");
                    } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
                        logger.info("[IndexResponseAsync] Updated successful.");
                    } else {
                        logger.info("[IndexResponseAsync] {}", indexResponse.getResult());
                    }

                    ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
                    if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
                        logger.warn("[IndexResponseAsync] Partial sharding successful.");
                    }
                    if (shardInfo.getFailed() > 0) {
                        for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
                            String reason = failure.reason();
                            logger.warn("IndexResponseAsync] Shard error:{}", reason);
                        }
                    }

                }

                @Override
                public void onFailure(Exception e) {
                    logger.error("[IndexResponseAsync] Elasticsearch Exception:", e);
                }
            };
            client.indexAsync(indexRequest, RequestOptions.DEFAULT, actionListener);
        } catch (ElasticsearchException e) {
            if (e.status() == RestStatus.CONFLICT) {
                msg = String.format("%s%s%s", "[ElasticsearchException] Document ", id, " already exists.");
                logger.info(msg);
            } else if(e.status() == RestStatus.NOT_FOUND) {
                msg = String.format("%s%s", "[ElasticsearchException]Index is not exist.", e);
                logger.error(msg);
            } else {
                msg = String.format("%s%s", "[ElasticsearchException]", e);
                logger.error(msg);
            }
        } catch (Exception e) {
            msg = String.format("%s%s", "[Exception]", e);
            logger.error(msg);
        }
        return msg;
    }


    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public String getRequest() throws Exception {
        String msg = null;
        String id = "5";
        try {
            GetRequest getRequest = new GetRequest(ES_INDEX, id);
            GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
            if (getResponse.isExists()) {
                msg = getResponse.getSourceAsString();
//            Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
                return msg;
            }
        } catch (ElasticsearchException e) {
            if(e.status() == RestStatus.NOT_FOUND) {
                msg = String.format("%s%s", "[ElasticsearchException]索引或文档不存在。", e);
                logger.error(msg);
            }
        } catch (Exception e) {
            msg = String.format("%s%s", "[Exception]", e);
            logger.error(msg);
        }

        return msg;
    }

    @RequestMapping(value = "update", method = RequestMethod.GET)
    public String updateRequest(){
        String msg = null;
        String id = "4";

        try{
            Map<String, Object> map = new HashMap<>();
            Date currentDate = new Date();
            map.put("message", "Update Hello Elasticsearch." + currentDate); //+":"+new Random());

            /** 使用map格式数据更新es */
//            UpdateRequest updateRequest = new UpdateRequest(ES_INDEX, id);
//            updateRequest.doc(map);

            /** 使用json格式数据更新es */
            UpdateRequest updateRequest = new UpdateRequest(ES_INDEX, id);
            String jsonString = "{" +
                    "\"updated\":\"2019-01-01\"," +
                    "\"reason\":\"daily update00\"" +
                    "}";
            updateRequest.doc(jsonString, XContentType.JSON);

            UpdateResponse updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
            msg = updateResponse.getResult().toString();
//            logger.info(updateResponse.status().toString());
            logger.info(msg+":"+currentDate);
//            if(updateResponse.getResult() == DocWriteResponse.Result.UPDATED)
            if(updateResponse.getResult() == DocWriteResponse.Result.NOOP) {
                logger.warn("提交的内容相同，ES取消操作。");
            }
            return msg;
        } catch (ElasticsearchException e) {
            if(e.status() == RestStatus.NOT_FOUND) {
                msg = String.format("%s%s", "[ElasticsearchException]索引不存在。", e);
                logger.error(msg);
            }
        } catch (Exception e) {
            msg = String.format("%s%s", "[Exception]", e);
            logger.error(msg);
        }

        return msg;
    }

    @RequestMapping(value = "delete", method = RequestMethod.GET)
    public String deleteRequest() throws Exception {
        String msg = null;
        String id = "5";
        try {
            DeleteRequest deleteRequest = new DeleteRequest(ES_INDEX, id);
            DeleteResponse deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);
            if(deleteResponse.getResult() == DocWriteResponse.Result.DELETED) {
                msg = "删除成功。";
                logger.info(msg);
            } else if(deleteResponse.getResult() == DocWriteResponse.Result.NOT_FOUND) {
                msg = "文档不存在。";
                logger.info(msg);
            }
            return msg;
        } catch (ElasticsearchException e) {
            if(e.status() == RestStatus.NOT_FOUND) {
                msg = String.format("%s%s", "[ElasticsearchException]索引不存在。", e);
                logger.error(msg);
            }
        } catch (Exception e) {
            msg = String.format("%s%s", "[Exception]", e);
            logger.error(msg);
        }

        return msg;
    }

    @RequestMapping(value = "exist", method = RequestMethod.GET)
    public Boolean exist() {
        Boolean exist = false;
        String msg = null;
        String id ="5";
        try{
            GetRequest getRequest = new GetRequest(ES_INDEX, id);
            getRequest.fetchSourceContext(new FetchSourceContext(false));
            getRequest.storedFields("_none_");
            exist = client.exists(getRequest, RequestOptions.DEFAULT);
            return exist;
        } catch (ElasticsearchException e) {
            if(e.status() == RestStatus.NOT_FOUND) {
                msg = String.format("%s%s", "[ElasticsearchException]索引不存在。", e);
                logger.error(msg);
            }
        } catch (Exception e) {
            msg = String.format("%s%s", "[Exception]", e);
            logger.error(msg);
        }

        return exist;
    }

}

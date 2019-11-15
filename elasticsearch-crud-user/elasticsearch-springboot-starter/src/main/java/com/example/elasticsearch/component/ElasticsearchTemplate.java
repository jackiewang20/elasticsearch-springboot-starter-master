package com.example.elasticsearch.component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.ScrollableHitSource;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author jackie wang
 * @Title: ElasticsearchTemplate
 * @ProjectName elasticsearch-crud
 * @Description: Elasticsearch模板类
 * @date 2019/9/30 14:42
 */
@Component
public class ElasticsearchTemplate implements ElasticsearchOperations {
    /**
     * es类型默认值
     */
    private final static String ES_TYPE = "_doc";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RestHighLevelClient client;

    /**
     * 根据id创建文档
     *
     * @param indexName 索引；
     * @param type 文档；
     * @param mapping   可以是map/String(json)/XContentBuilder类型，根据参数类型动态适配；例如：
     *        String jsonString = "{" +
     *                 "\"user\":\"kimchy\"," +
     *                 "\"postDate\":\"2013-01-30\"," +
     *                 "\"message\":\"trying out Elasticsearch\"" +
     *                 "}";
     *          或
     *         Map<String, Object> jsonMap = new HashMap<>();
     *         jsonMap.put("user", "kimchy");
     *         jsonMap.put("postDate", new Date());
     *         jsonMap.put("message", "Create a document.");
     * @return
     */
    @Override
    public boolean create(String indexName, String type, String id, Object mapping) throws Exception {
        Assert.hasText(indexName, "The indexName can't be empty.");
        Assert.hasText(id, "The id can't be empty.");
        Assert.notNull(mapping, "The mapping can't be empty.");

        if (StringUtils.isEmpty(type)) {
            type = ES_TYPE;
        }

        boolean result = false;
        IndexResponse indexResponse = null;

        IndexRequest request = new IndexRequest(indexName, type);
        request
                .id(id)
                .opType(DocWriteRequest.OpType.CREATE); // 判断id编号为3的文档是否创建，如果存在，报文档冲突异常;
        // 设置操作类型type就必须设置id，否则报错

//        String jsonString = "{" +
//                "\"user\":\"kimchy\"," +
//                "\"postDate\":\"2013-01-30\"," +
//                "\"message\":\"trying out Elasticsearch\"" +
//                "}";
        if (mapping instanceof String) {
            request.source(String.valueOf(mapping), XContentType.JSON);
        } else if (mapping instanceof Map) {
            request.source((Map) mapping);
        } else if (mapping instanceof XContentBuilder) {
            request.source((XContentBuilder) mapping);
        } else {
            throw new RuntimeException("mapping参数类型无效。");
        }

        try {
            indexResponse = client.index(request, RequestOptions.DEFAULT);
            logger.info("[IndexResponse]:" + indexResponse.getResult());
            result = true;
        } catch (ElasticsearchException e) {
            if (e.status() == RestStatus.CONFLICT) {
                throw new RuntimeException("[Elasticsearch]The document already exists.", e);
            } else if (e.status() == RestStatus.NOT_FOUND) {
                throw new RuntimeException("[Elasticsearch]The index does not exist.", e);
            } else if (e.status() == RestStatus.BAD_REQUEST) {
                throw new RuntimeException("[Elasticsearch]The requested parameter is invalid.", e);
            } else {
                throw new RuntimeException("[Elasticsearch]The operation failure.", e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    /**
     * 根据id创建文档
     *
     * @param indexName 索引；
     * @param type 文档；
     * @param mapping   可以是map/String(json)/XContentBuilder类型，根据参数类型动态适配；
     * @return
     */
    @Override
    public boolean create(String indexName, String type, Object mapping) throws Exception {
        Assert.hasText(indexName, "The indexName can't be empty.");
        Assert.notNull(mapping, "The mapping can't be empty.");

        if (StringUtils.isEmpty(type)) {
            type = ES_TYPE;
        }

        boolean result = false;

        IndexResponse indexResponse = null;
        IndexRequest request = new IndexRequest(indexName, type);

//        String jsonString = "{" +
//                "\"user\":\"kimchy\"," +
//                "\"postDate\":\"2013-01-30\"," +
//                "\"message\":\"trying out Elasticsearch\"" +
//                "}";
        if (mapping instanceof String) {
            request.source(String.valueOf(mapping), XContentType.JSON);
        } else if (mapping instanceof Map) {
            request.source((Map) mapping);
        } else if (mapping instanceof XContentBuilder) {
            request.source((XContentBuilder) mapping);
        } else {
            throw new RuntimeException("mapping参数类型无效。");
        }

        try {
            indexResponse = client.index(request, RequestOptions.DEFAULT);
            logger.info("[IndexResponse]:" + indexResponse.getResult());
            result = true;
        } catch (ElasticsearchException e) {
            if (e.status() == RestStatus.CONFLICT) {
                throw new RuntimeException("[Elasticsearch]The document already exists.", e);
            } else if (e.status() == RestStatus.NOT_FOUND) {
                throw new RuntimeException("[Elasticsearch]The index does not exist.", e);
            } else if (e.status() == RestStatus.BAD_REQUEST) {
                throw new RuntimeException("[Elasticsearch]The requested parameter is invalid.", e);
            } else {
                throw new RuntimeException("[Elasticsearch]The operation failure.", e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    /**
     * 根据id创建文档
     *
     * @param indexName 索引；
     * @param type 文档；
     * @param mapping   可以是map/String(json)/XContentBuilder类型，根据参数类型动态适配；
     * @return
     */
    @Override
    public void createAsync(String indexName, String type, Object mapping) throws Exception {
        Assert.hasText(indexName, "The indexName can't be empty.");
        Assert.notNull(mapping, "The mapping can't be empty.");

        if (StringUtils.isEmpty(type)) {
            type = ES_TYPE;
        }

        IndexRequest indexRequest = new IndexRequest(indexName, type);

//        String jsonString = "{" +
//                "\"user\":\"jackie\"," +
//                "\"postDate\":\"2019-01-30\"," +
//                "\"message\":\"Hello Elasticsearch, this is a async request.\"" +
//                "}";
        if (mapping instanceof String) {
            indexRequest.source(String.valueOf(mapping), XContentType.JSON);
        } else if (mapping instanceof Map) {
            indexRequest.source((Map) mapping);
        } else if (mapping instanceof XContentBuilder) {
            indexRequest.source((XContentBuilder) mapping);
        } else {
            throw new RuntimeException("mapping参数类型无效。");
        }

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
                    logger.error("[IndexResponseAsync] Elasticsearch document creation exception:", e);
                }
            };
            client.indexAsync(indexRequest, RequestOptions.DEFAULT, actionListener);
        } catch (ElasticsearchException e) {
            if (e.status() == RestStatus.CONFLICT) {
                throw new RuntimeException("[Elasticsearch]The document already exists.", e);
            } else if (e.status() == RestStatus.NOT_FOUND) {
                throw new RuntimeException("[Elasticsearch]The index does not exist.", e);
            } else if (e.status() == RestStatus.BAD_REQUEST) {
                throw new RuntimeException("[Elasticsearch]The requested parameter is invalid.", e);
            } else {
                throw new RuntimeException("[Elasticsearch]The operation failure.", e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 根据id更新文档
     *
     * @param indexName 索引；
     * @param type 文档；
     * @param id id；
     * @param mapping   可以是map/String(json)/XContentBuilder类型，根据参数类型动态适配；
     * @return
     */
    @Override
    public boolean update(String indexName, String type, String id, Object mapping) throws Exception {
        Assert.hasText(indexName, "The indexName can't be empty.");
        Assert.hasText(id, "The id can't be empty.");
        Assert.notNull(mapping, "The mapping can't be empty.");

        if (StringUtils.isEmpty(type)) {
            type = ES_TYPE;
        }

        boolean result = false;

        // 查询文档是否存在
        boolean exist = exist(indexName, type, id);
        if (!exist) {
            String str = String.format("A document with id %s does not exist.", id);
            throw new Exception(str);
        }

        try {

            /** 使用map格式数据更新es */
//            UpdateRequest updateRequest = new UpdateRequest(indexName, id);
//            updateRequest.doc(map);

            /** 使用json格式数据更新es */
//            UpdateRequest updateRequest = new UpdateRequest(indexName, id);
//            String jsonString = "{" +
//                    "\"updated\":\"2019-01-01\"," +
//                    "\"reason\":\"daily update00\"" +
//                    "}";
//            updateRequest.doc(jsonString, XContentType.JSON);


            // 更新
            UpdateRequest updateRequest = new UpdateRequest(indexName, type, id);
            if (mapping instanceof String) {
                updateRequest.doc(String.valueOf(mapping), XContentType.JSON);
            } else if (mapping instanceof Map) {
                updateRequest.doc((Map) mapping);
            } else if (mapping instanceof XContentBuilder) {
                updateRequest.doc((XContentBuilder) mapping);
            }

            UpdateResponse updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);

            if (updateResponse.getResult() == DocWriteResponse.Result.UPDATED) {
                logger.info("{}{} 更新内容：{}", "[ElasticsearchUpdate]", updateResponse.getResult().toString(), mapping);
                result = true;
            } else if (updateResponse.getResult() == DocWriteResponse.Result.NOOP) {
                logger.warn("提交的内容相同，ES取消操作。");
            } else if (updateResponse.getResult() == DocWriteResponse.Result.NOT_FOUND) {
                logger.warn("文档不存在。");
            }
        } catch (ElasticsearchException e) {
            if (e.status() == RestStatus.NOT_FOUND) {
                throw new RuntimeException("[Elasticsearch]The index does not exist.", e);
            } else if (e.status() == RestStatus.BAD_REQUEST) {
                throw new RuntimeException("[Elasticsearch]The requested parameter is invalid.", e);
            } else {
                throw new RuntimeException("[Elasticsearch]The operation failure.", e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    /**
     * 根据id删除文档
     *
     * @param indexName 索引；
     * @param type 文档；
     * @param id id；
     * @return
     */
    @Override
    public boolean delete(String indexName, String type, String id) throws Exception {
        Assert.hasText(indexName, "The indexName can't be empty.");
        Assert.hasText(id, "The id can't be empty.");

        if (StringUtils.isEmpty(type)) {
            type = ES_TYPE;
        }

        String msg = null;
        boolean result = false;
        try {
            DeleteRequest deleteRequest = new DeleteRequest(indexName, type, id);
            DeleteResponse deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);
            if (deleteResponse.getResult() == DocWriteResponse.Result.DELETED) {
                msg = "删除成功。";
                logger.info(msg);
                result = true;
            } else if (deleteResponse.getResult() == DocWriteResponse.Result.NOT_FOUND) {
                msg = "文档不存在。";
                logger.info(msg);
            }
        } catch (ElasticsearchException e) {
            if (e.status() == RestStatus.NOT_FOUND) {
                throw new RuntimeException("[Elasticsearch]Index does not exist", e);
            } else {
                throw new RuntimeException("[Elasticsearch]The operation failure.", e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    /**
     * 根据查询更新索引中的文档：最简单的用法是更新索引中的每个文档，而无需更改源。
     * 注意：批量执行比较耗时。
     *
     * @param indexName 索引；
     * @param termQueryBuilder 例如：new TermQueryBuilder("user","kimchy" )
     * @param script           脚本：例如"if (ctx._source.user == 'kimchy') {ctx._source.likes++;}"
     * @return
     * @throws Exception
     */
    @Override
    public Long updateByQuery(String indexName, TermQueryBuilder termQueryBuilder, String script) throws Exception {
        Assert.hasText(indexName, "The indexName can't be empty.");
        Assert.hasText(script, "The script can't be empty.");
        Assert.notNull(termQueryBuilder, "The termQueryBuilder can't be empty.");

        long updatedDocs = 0;

        try {
            UpdateByQueryRequest request = new UpdateByQueryRequest(indexName);
            // 设置proceed版本冲突
            request.setConflicts("proceed");
            // 通过添加条件查询限制文档，仅复制将属性字段user设置为“kimchy”的文档
//            request.setQuery(new TermQueryBuilder("user","kimchy" ));
            request.setQuery(termQueryBuilder);
            // 只复制10个文档
            request.setSize(100);
            // 使用100个文档批次大小
            request.setBatchSize(1000);
            // 按查询更新还可以通过指定管道来使用提取功能
//            request.setPipeline("my_pipeline");

            /**支持script修改文档：setScript使用户为kimchy的所有文档上的likes字段递增。
             * 请确保执行脚本中的属性存在，否则报错,没有属性可以进行赋值操作添加属性；例如更新message属性：
             * "if (ctx._source.user == 'kimchy') {ctx._source.message='test';}",
             */
            request.setScript(
                    new Script(
                            ScriptType.INLINE, "painless",
//                            "if (ctx._source.user == 'kimchy') {ctx._source.likes=0;}",
//                            "if (ctx._source.user == 'kimchy') {ctx._source.likes++;}",
                            script,
                            Collections.emptyMap()));
            // 设置使用的切片数
            request.setSlices(2);
            // 使用scroll参数来控制它保持“搜索上下文”活动的时间，即设置滚动时间
            request.setScroll(TimeValue.timeValueMinutes(10));
            // 设置路由：如果提供路由，则路由将复制到滚动查询，从而将进程限制为与该路由值匹配的碎片。
//            request.setRouting("=cat");
            //  等待查询请求更新执行作为TimeValue的超时时间
            request.setTimeout(TimeValue.timeValueMinutes(2));
            // 通过调用查询更新后刷新索引
            request.setIndicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);

            // 响应信息
            BulkByScrollResponse bulkResponse = client.updateByQuery(request, RequestOptions.DEFAULT);
            // 获取总耗时
            TimeValue timeTaken = bulkResponse.getTook();
            // 检查请求是否超时
            boolean timedOut = bulkResponse.isTimedOut();
            // 获取处理的文档总数
            long totalDocs = bulkResponse.getTotal();
            // 已更新的文档数
            updatedDocs = bulkResponse.getUpdated();
            // 已删除的文档数
            long deletedDocs = bulkResponse.getDeleted();
            // 已执行的批次数
            long batches = bulkResponse.getBatches();
            // 跳过的文档数
            long noops = bulkResponse.getNoops();
            // 版本冲突数
            long versionConflicts = bulkResponse.getVersionConflicts();
            // 请求重试批量索引操作的次数
            long bulkRetries = bulkResponse.getBulkRetries();
            // 请求重试搜索操作的次数
            long searchRetries = bulkResponse.getSearchRetries();
            // 此请求限制的总时间不包括当前正在休眠的当前节流时间
            TimeValue throttledMillis = bulkResponse.getStatus().getThrottled();
            // 任何当前节流阀休眠的剩余延迟或如果不休眠则为0
            TimeValue throttledUntilMillis = bulkResponse.getStatus().getThrottledUntil();
            // 搜索阶段的失败
            List<ScrollableHitSource.SearchFailure> searchFailures = bulkResponse.getSearchFailures();
            // 批量索引操作期间的失败
            List<BulkItemResponse.Failure> bulkFailures = bulkResponse.getBulkFailures();
            for (BulkItemResponse.Failure failure : bulkFailures) {
                logger.error("[批量索引操作期间的失败文档]{} ", failure.getMessage(), failure);
            }

            logger.info("[updateByQuery]已更新的文档数:{}", updatedDocs);
        } catch (ElasticsearchException e) {
            if (e.status() == RestStatus.NOT_FOUND) {
                throw new RuntimeException("[Elasticsearch]Index does not exist", e);
            } else {
                throw new RuntimeException("[Elasticsearch]The operation failure.", e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return updatedDocs;
    }


    /**
     * 根据查询条件删除索引中的文档。
     *
     * @param indexName 索引；
     * @param termQueryBuilder 例如：new TermQueryBuilder("user","kimchy" )
     * @return
     * @throws Exception
     */
    @Override
    public Long deleteByQuery(String indexName, TermQueryBuilder termQueryBuilder) throws Exception {
        Assert.hasText(indexName, "The indexName can't be empty.");
        Assert.notNull(termQueryBuilder, "The termQueryBuilder can't be empty.");

        long deletedDocs = 0;

        try {
            DeleteByQueryRequest request = new DeleteByQueryRequest(indexName);
            // 设置proceed版本冲突
            request.setConflicts("proceed");
            // 通过添加条件查询限制文档，仅复制将属性字段user设置为“kimchy”的文档
//            request.setQuery(new TermQueryBuilder("user","kimchy" ));
            request.setQuery(termQueryBuilder);
            // 只复制10个文档
            request.setSize(100);
            // 使用100个文档批次大小
            request.setBatchSize(1000);
            // 按查询更新还可以通过指定管道来使用提取功能
//            request.setPipeline("my_pipeline");
            // 设置使用的切片数
            request.setSlices(2);
            // 使用scroll参数来控制它保持“搜索上下文”活动的时间，即设置滚动时间
            request.setScroll(TimeValue.timeValueMinutes(10));
            // 设置路由：如果提供路由，则路由将复制到滚动查询，从而将进程限制为与该路由值匹配的碎片。
//            request.setRouting("=cat");
            //  等待查询请求更新执行作为TimeValue的超时时间
            request.setTimeout(TimeValue.timeValueMinutes(2));
            // 通过查询调用delete后刷新索引
            request.setRefresh(true);
            // 设置索引选项
            request.setIndicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);

            // 响应信息
            BulkByScrollResponse bulkResponse = client.deleteByQuery(request, RequestOptions.DEFAULT);
            // 获取总耗时
            TimeValue timeTaken = bulkResponse.getTook();
            // 检查请求是否超时
            boolean timedOut = bulkResponse.isTimedOut();
            // 获取处理的文档总数
            long totalDocs = bulkResponse.getTotal();
            // 已删除的文档数
            deletedDocs = bulkResponse.getDeleted();
            // 已执行的批次数
            long batches = bulkResponse.getBatches();
            // 跳过的文档数
            long noops = bulkResponse.getNoops();
            // 版本冲突数
            long versionConflicts = bulkResponse.getVersionConflicts();
            // 请求重试批量索引操作的次数
            long bulkRetries = bulkResponse.getBulkRetries();
            // 请求重试搜索操作的次数
            long searchRetries = bulkResponse.getSearchRetries();
            // 此请求限制的总时间不包括当前正在休眠的当前节流时间
            TimeValue throttledMillis = bulkResponse.getStatus().getThrottled();
            // 任何当前节流阀休眠的剩余延迟或如果不休眠则为0
            TimeValue throttledUntilMillis = bulkResponse.getStatus().getThrottledUntil();
            // 搜索阶段的失败
            List<ScrollableHitSource.SearchFailure> searchFailures = bulkResponse.getSearchFailures();
            // 批量索引操作期间的失败
            List<BulkItemResponse.Failure> bulkFailures = bulkResponse.getBulkFailures();
            for (BulkItemResponse.Failure failure : bulkFailures) {
                logger.error("[批量索引操作期间的失败文档]{} ", failure.getMessage(), failure);
            }

            logger.info("[deleteByQuery]已删除的文档数:{}", deletedDocs);
        } catch (ElasticsearchException e) {
            if (e.status() == RestStatus.NOT_FOUND) {
                throw new RuntimeException("[Elasticsearch]Index does not exist", e);
            } else {
                throw new RuntimeException("[Elasticsearch]The operation failure.", e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return deletedDocs;
    }

    /**
     * 根据id查询文档
     *
     * @param indexName 索引；
     * @param type 文档；
     * @param id id；
     * @return
     */
    @Override
    public Map get(String indexName, String type, String id) throws Exception {
        Assert.hasText(indexName, "The indexName can't be empty.");
        Assert.hasText(id, "The id can't be empty.");

        if (StringUtils.isEmpty(type)) {
            type = ES_TYPE;
        }

        Map<String, Object> sourceAsMap = null;
        String msg = null;

        try {
            GetRequest getRequest = new GetRequest(indexName, type, id);
            GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
            if (getResponse.isExists()) {
//                msg = getResponse.getSourceAsString();
                sourceAsMap = getResponse.getSourceAsMap();
            }
            logger.info("[ElasticsearchGet]根据id:{}查询文档。", id);
            logger.info("[ElasticsearchGet]{}", sourceAsMap);
        } catch (ElasticsearchException e) {
            if (e.status() == RestStatus.NOT_FOUND) {
                throw new RuntimeException("[Elasticsearch]Index does not exist", e);
            } else {
                throw new RuntimeException("[Elasticsearch]The query fails.", e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return sourceAsMap;
    }

    /**
     * 根据id判断文档是否存在
     *
     * @param indexName 索引；
     * @param type 文档；
     * @param id id；
     * @return
     */
    @Override
    public boolean exist(String indexName, String type, String id) throws Exception {
        Assert.hasText(indexName, "The indexName can't be empty.");
        Assert.hasText(id, "The id can't be empty.");

        if (StringUtils.isEmpty(type)) {
            type = ES_TYPE;
        }

        Boolean exist = false;
        try {
            GetRequest getRequest = new GetRequest(indexName, type, id);
            getRequest.fetchSourceContext(new FetchSourceContext(false));
            getRequest.storedFields("_none_");
            exist = client.exists(getRequest, RequestOptions.DEFAULT);
            logger.info("[Elasticsearch]{}", exist);
            return exist;
        } catch (ElasticsearchException e) {
            if (e.status() == RestStatus.NOT_FOUND) {
                throw new RuntimeException("[Elasticsearch]Index does not exist", e);
            } else {
                throw new RuntimeException("[Elasticsearch]The query fails.", e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    /**
     * 分页查询文档。
     * 控制台查询：
     * GET posts/_search
     * {"query":{"match_all":{}},"from":0,"size":1,"_source":["user","postDate","message"]}
     *
     * @param indexName     索引；
     * @param pageSize      页显示大小；
     * @param pageNow       当前页，默认值0；
     * @param sortFieldName 排序字段；
     * @return
     * @throws Exception
     */
    @Override
    public List<Map<String, Object>> queryByPage(String indexName, Integer pageSize, Integer pageNow, String sortFieldName) throws Exception {
        return queryByPage(indexName, pageSize, pageNow, sortFieldName, QueryBuilders.matchAllQuery());
    }

    /**
     * 精准查询：搜索精准匹配的关键字，不进行分词，因此如果是一段含有空格的短语关键字不一定匹配到。
     * 控制台查询：
     * GET posts/_search
     * {"from":0,"size":3,"query":{"term":{"user":{"value":"kimchy"}}},"sort":[{"_id":{"order":"desc"}}]}
     *
     * @param indexName        索引；
     * @param pageSize         页显示大小；
     * @param pageNow          当前页；
     * @param sortFieldName    排序字段；
     * @param termQueryBuilder 查询条件；例如：TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("user", "kimchy");
     * @return
     * @throws Exception
     */
    @Override
    public List<Map<String, Object>> termQueryByPage(String indexName, Integer pageSize, Integer pageNow, String sortFieldName, TermQueryBuilder termQueryBuilder) throws Exception {
        return queryByPage(indexName, pageSize, pageNow, sortFieldName, termQueryBuilder);
    }

    /**
     * 精准查询：搜索精准匹配的关键字，不进行分词，因此如果是一段含有空格的短语关键字不一定匹配到。
     * 控制台查询:
     * GET posts/_search
     * {"from":0,"size":3,"timeout":"60s","query":{"terms":{"_id":["1","2"],"boost":1.0}},"sort":[{"_id":{"order":"desc"}}]}
     *
     * @param indexName         索引；
     * @param pageSize          页显示大小；
     * @param pageNow           当前页；
     * @param sortFieldName     排序字段；
     * @param termsQueryBuilder 查询条件；例如：TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery("_id", new String[]{"1","2"});
     * @return
     * @throws Exception
     */
    @Override
    public List<Map<String, Object>> termsQueryByPage(String indexName, Integer pageSize, Integer pageNow, String sortFieldName, TermsQueryBuilder termsQueryBuilder) throws Exception {
        return queryByPage(indexName, pageSize, pageNow, sortFieldName, termsQueryBuilder);
    }


    /**
     * 全文检索（match query）
     * 控制台查询：
     * GET posts/_search
     * {"from":0,"size":3,"query":{"match":{"user":{"query":"kimchy","operator":"OR"}}},"sort":[{"_id":{"order":"desc"}}]}
     *
     * 例如：查询关键中的4个分词，至少满足2个
     * GET /forum/article/_search
     * {"query":{"match":{"title":{"query":"java elasticsearch spark hadoop","minimum_should_match":"50%"}}}}
     *
     * @param indexName         索引；
     * @param pageSize          页显示大小；
     * @param pageNow           当前页；
     * @param sortFieldName     排序字段；
     * @param matchQueryBuilder 查询条件；例如：MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("user", "kimchy");
     * @return
     * @throws Exception
     */
    @Override
    public List<Map<String, Object>> matchQueryByPage(String indexName, Integer pageSize, Integer pageNow,
                                                      String sortFieldName, MatchQueryBuilder matchQueryBuilder) throws Exception {
        return queryByPage(indexName, pageSize, pageNow, sortFieldName, matchQueryBuilder);
    }


    /**
     * 全文检索（multi match query），根据关键字匹配多个Field进行全文检索。
     * 控制台查询：
     * GET posts/_search
     * {"query":{"multi_match":{"query":"jackie 6 create","fields":["user","message"],"minimum_should_match":"50%"}}}
     *
     * @param indexName              索引；
     * @param pageSize               页显示大小；
     * @param pageNow                当前页；
     * @param sortFieldName          排序字段；
     * @param multiMatchQueryBuilder 查询条件；例如：MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("jackie 6 create","user", "message");
     * @return
     * @throws Exception
     */
    @Override
    public List<Map<String, Object>> multiMatchQueryByPage(String indexName, Integer pageSize, Integer pageNow,
                                                           String sortFieldName, MultiMatchQueryBuilder multiMatchQueryBuilder) throws Exception {
        return queryByPage(indexName, pageSize, pageNow, sortFieldName, multiMatchQueryBuilder);
    }


    /**
     * 条件查询
     * 控制台查询：
     * GET website/article/_search
     * {"query":{"bool":{"must":[{"match":{"title":"elasticsearch hadoop"}},{"match":{"content":"goods"}},{"term":{"author_id":"112"}}],"should":[{"match":{"content":"flink"}}],"must_not":[{"match":{"author_id":111}}]}}}
     * 条件查询参数说明：
     * must:只有符合所有查询的文档才被查询出来,相当于AND；
     * should:至少符合其中一个,相当于OR；
     * must_not:排除的条件,相当于NOT；
     * 其中must中match为匹配Field查询，term为精确查找；should查找不是必须的，如果查询到增加score；must_not为排除查询。
     *
     * @param indexName        索引；
     * @param pageSize         页显示大小；
     * @param pageNow          当前页；
     * @param sortFieldName    排序字段；
     * @param boolQueryBuilder 查询条件；例如：
     *         // 全文检索，一次匹配多个Field进行全文检索 Field:content
     *         MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("elasticsearch hadoop goods","title", "content");
     *
     *         // 精确查找
     *         TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("author_id", 112);
     *         // 全文检索Field:content
     *         MatchQueryBuilder shouldMatchQueryBuilder = QueryBuilders.matchQuery("content", "flink");
     *         // 全文检索Field:author_id
     *         MatchQueryBuilder mustNotMatchQueryBuilder = QueryBuilders.matchQuery("author_id", 111);
     *
     *         BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
     *                 .must(multiMatchQueryBuilder)
     *                 .must(termQueryBuilder)
     *                 .should(shouldMatchQueryBuilder)
     *                 .mustNot(mustNotMatchQueryBuilder);
     * @return
     * @throws Exception
     */
    @Override
    public List<Map<String, Object>> conditionQueryByPage(String indexName, Integer pageSize, Integer pageNow,
                                                          String sortFieldName, BoolQueryBuilder boolQueryBuilder) throws Exception {
        return queryByPage(indexName, pageSize, pageNow, sortFieldName, boolQueryBuilder);
    }

    /**
     * 聚合查询
     * 控制台查询：
     * 聚合分类company属性，并统计该分类下的平均年龄
     * GET posts/_search
     * {"size":0,"aggs":{"by_company":{"terms":{"field":"company.keyword"},"aggs":{"average_age":{"avg":{"field":"age"}}}}}}
     *
     * @param indexName               索引；
     * @param termsAggregationBuilder 查询条件；例如：
     *                                TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms("by_company").field("company.keyword");
     *                                aggregationBuilder.subAggregation(AggregationBuilders.avg("average_age").field("age"));
     * @param aggregationAlias        聚合查询别名；
     * @return String类型的json数组。
     * @throws Exception
     */
    @Override
    public String aggregationQueryByPage(String indexName, TermsAggregationBuilder termsAggregationBuilder, String aggregationAlias) throws Exception {
//        List<Map<String, Object>> mapList = new ArrayList<>();
        Assert.notNull(termsAggregationBuilder, "termsAggregationBuilder can not null.");

        try {
            // 设置queryBuilder
            SearchRequest searchRequest = new SearchRequest(indexName);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.aggregation(termsAggregationBuilder);
            searchSourceBuilder.size(0);
            searchRequest.source(searchSourceBuilder);

            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

            /** 方法1 */
//            Aggregations aggregations = searchResponse.getAggregations();
//            Terms terms = aggregations.get("by_company");
//            List<? extends Terms.Bucket> buckets = terms.getBuckets();
//            for (Terms.Bucket bucket : buckets) {
//                logger.info("key:"+bucket.getKeyAsString()+", docCount:"+bucket.getDocCount());
//            }

            /** 方法2 */
            JSONObject jsonObject = JSON.parseObject(searchResponse.toString());
//            logger.error(jsonObject.getString("took"));
//             获取json串中buckets部分json内容
            JSONArray jsonObjectBuckets = jsonObject.getJSONObject("aggregations").getJSONObject("sterms#" + aggregationAlias).getJSONArray("buckets");

            logger.info(jsonObjectBuckets.toJSONString());

            /** 方法3 */
//            Aggregation aggregation = searchResponse.getAggregations().getAsMap().get(aggregationAlias);
//            String jsonResult = JSON.toJSONString(aggregation, SerializerFeature.IgnoreErrorGetter);
//            JSONObject jsonObject = JSON.parseObject(jsonResult);

            return jsonObjectBuckets.toJSONString();
        } catch (ElasticsearchException e) {
            if (e.status() == RestStatus.NOT_FOUND) {
                throw new RuntimeException("[Elasticsearch]Index does not exist", e);
            } else {
                throw new RuntimeException("[Elasticsearch]The query fails.", e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 聚合查询
     * 控制台查询：
     * 聚合分类company属性，并统计该分类下的平均年龄
     * GET posts/_search
     * {"size":0,"aggs":{"by_company":{"terms":{"field":"company.keyword"},"aggs":{"average_age":{"avg":{"field":"age"}}}}}}
     *
     * @param indexName               索引；
     * @param termsAggregationBuilder 查询条件；例如：
     *                                TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms("by_company").field("company.keyword");
     *                                aggregationBuilder.subAggregation(AggregationBuilders.avg("average_age").field("age"));
     * @return String类型的json数组。
     * @throws Exception
     */
    @Override
    public String aggregationQueryByPage(String indexName, TermsAggregationBuilder termsAggregationBuilder) throws Exception {
//        List<Map<String, Object>> mapList = new ArrayList<>();
        Assert.notNull(termsAggregationBuilder, "termsAggregationBuilder can not null.");

        try {
            // 设置queryBuilder
            SearchRequest searchRequest = new SearchRequest(indexName);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.aggregation(termsAggregationBuilder);
            searchSourceBuilder.size(0);
            searchRequest.source(searchSourceBuilder);

            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

            /** 方法1 */
//            Aggregations aggregations = searchResponse.getAggregations();
//            Terms terms = aggregations.get("by_company");
//            List<? extends Terms.Bucket> buckets = terms.getBuckets();
//            for (Terms.Bucket bucket : buckets) {
//                logger.info("key:"+bucket.getKeyAsString()+", docCount:"+bucket.getDocCount());
//            }

            /** 方法2 */
            JSONObject jsonObject = JSON.parseObject(searchResponse.toString());
//            logger.error(jsonObject.getString("took"));

            // 动态获取聚合别名
            String jsonAggregations = jsonObject.getJSONObject("aggregations").toJSONString();
            String[] arr = jsonAggregations.split(":");
            if (arr == null || arr[0].length() == 0) {
                throw new RuntimeException("Invalid aggregate query.");
            }
            String getAlias = arr[0].replace("\"", "").replace("{", "");
            // 获取json串中buckets部分json内容
            JSONArray jsonObjectBuckets = jsonObject.getJSONObject("aggregations").getJSONObject(getAlias).getJSONArray("buckets");

            logger.info(jsonObjectBuckets.toJSONString());

            /** 方法3 */
//            Aggregation aggregation = searchResponse.getAggregations().getAsMap().get(aggregationAlias);
//            String jsonResult = JSON.toJSONString(aggregation, SerializerFeature.IgnoreErrorGetter);
//            JSONObject jsonObject = JSON.parseObject(jsonResult);

            return jsonObjectBuckets.toJSONString();
        } catch (ElasticsearchException e) {
            if (e.status() == RestStatus.NOT_FOUND) {
                throw new RuntimeException("[Elasticsearch]Index does not exist", e);
            } else {
                throw new RuntimeException("[Elasticsearch]The query fails.", e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 搜索建议
     * 控制台查询：
     * 聚合分类company属性，并统计该分类下的平均年龄
     * GET posts/_search
     * {"suggest":{"suggest_user":{"text":"jack","term":{"field":"user"}}}}
     * @param indexName      索引；
     * @param suggestBuilder 搜索建议示例：
     *        SuggestionBuilder termSuggestionBuilder = SuggestBuilders.termSuggestion("user").text("kmichy");
     *         SuggestBuilder suggestBuilder = new SuggestBuilder();
     *         suggestBuilder.addSuggestion("suggest_user", termSuggestionBuilder);
     *
     * @return json格式String类型。
     * @throws Exception
     */
    @Override
    public String suggestQueryByPage(String indexName, SuggestBuilder suggestBuilder) throws Exception {
        Assert.notNull(suggestBuilder, "SuggestBuilder can not null.");

        try {
            // 设置queryBuilder
            SearchRequest searchRequest = new SearchRequest(indexName);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.suggest(suggestBuilder);
            searchRequest.source(searchSourceBuilder);

            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            String result = searchResponse.getSuggest().toString();
            logger.info(result);
            return result;
        } catch (ElasticsearchException e) {
            if (e.status() == RestStatus.NOT_FOUND) {
                throw new RuntimeException("[Elasticsearch]Index does not exist", e);
            } else {
                throw new RuntimeException("[Elasticsearch]The query fails.", e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * es文档分页查询
     *
     * @param indexName     索引；
     * @param pageSize      页显示大小；
     * @param pageNow       当前页；
     * @param sortFieldName 排序字段；
     * @param queryBuilder  全文检索；
     * @return
     * @throws Exception
     */
    @Override
    public List<Map<String, Object>> queryByPage(String indexName, Integer pageSize, Integer pageNow,
                                                 String sortFieldName, QueryBuilder queryBuilder) throws Exception {
        return queryByPage(indexName, pageSize, pageNow, sortFieldName, queryBuilder, null);
    }


    /**
     * es文档分页查询
     *
     * @param indexName        索引；
     * @param pageSize         页显示大小；
     * @param pageNow          当前页；
     * @param sortFieldName    排序字段；
     * @param queryBuilder     全文检索；
     * @param highlightBuilder 高亮显示搜索关键字；
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> queryByPage(String indexName, Integer pageSize, Integer pageNow,
                                                 String sortFieldName, QueryBuilder queryBuilder,
                                                 HighlightBuilder highlightBuilder) throws Exception {
        List<Map<String, Object>> mapList = new ArrayList<>();

        try {
            if (pageNow <= 0) {
                pageNow = 1;
            }

            if (pageSize <= 0) {
                pageSize = 10;
            }

            Integer from = pageSize * (pageNow - 1);
            Integer size = pageSize;

            // 设置queryBuilder
            SearchRequest searchRequest = new SearchRequest(indexName);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            /** 查询 */
            searchSourceBuilder.query(queryBuilder);


            /** 分页 */
            searchSourceBuilder.from(from);
            searchSourceBuilder.size(size);
            searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

            /** 排序：默认按分数(_score)降序排序 */
            if (StringUtils.hasText(sortFieldName)) {
                searchSourceBuilder.sort(new FieldSortBuilder(sortFieldName).order(SortOrder.DESC));
//                searchSourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC));
//                searchSourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.ASC));
            }

            /** 排除，包含某些字段*/
//            String[] includes;
//            searchSourceBuilder.fetchSource(includes, excludes);

            /** 高亮 */
            if (!StringUtils.isEmpty(highlightBuilder)) {
                searchSourceBuilder.highlighter(highlightBuilder);
            }

            searchRequest.source(searchSourceBuilder);

            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                sourceAsMap.put("_id", hit.getId());
                mapList.add(sourceAsMap);
            }
            logger.info(mapList.toString());
        } catch (ElasticsearchException e) {
            if (e.status() == RestStatus.NOT_FOUND) {
                throw new RuntimeException("[Elasticsearch]Index does not exist", e);
            } else {
                throw new RuntimeException("[Elasticsearch]The query fails.", e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return mapList;
    }

}

package com.example.es.user.provider.controller;

import com.example.elasticsearch.component.ElasticsearchTemplate;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jackie wang
 * @Title: PostsController
 * @ProjectName elasticsearch-crud
 * @Description: 发帖子：使用封装的代码对ES进行CRUD操作。
 * 使用自定义ES组件，进行测试
 * @date 2019/9/27 18:36
 */
@RequestMapping("posts2")
@RestController
public class Posts2Controller {
    private final static String ES_INDEX = "posts";
    private final static String ES_TYPE = "_doc";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @RequestMapping(value = "/create", method = RequestMethod.GET)
    public Boolean indexRequest() throws Exception {
        Boolean result = false;

        String jsonString = "{" +
                "\"user\":\"kimchy\"," +
                "\"postDate\":\"2013-01-30\"," +
                "\"message\":\"trying out Elasticsearch\"" +
                "}";
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("user", "kimchy");
        jsonMap.put("postDate", new Date());
        jsonMap.put("message", "Create a document.");

        result = elasticsearchTemplate.create(ES_INDEX, ES_TYPE, jsonMap);
//        result = elasticsearchTemplate.create(ES_INDEX, ES_TYPE, jsonString);
//        result = elasticsearchTemplate.create(ES_INDEX, ES_TYPE,id, jsonString);
        logger.info("[IndexResponse]:{}", result);
        return result;

    }

    @RequestMapping(value = "/create/{id}", method = RequestMethod.GET)
    public Boolean indexRequest2(@PathVariable String id) throws Exception {
        Boolean result = false;
//        String id = "4";

        String jsonString = "{" +
                "\"user\":\"kimchy\"," +
                "\"postDate\":\"2013-01-30\"," +
                "\"message\":\"trying out Elasticsearch\"" +
                "}";
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("user", "jackie " + id);
        jsonMap.put("postDate", new Date());
        jsonMap.put("message", "Create a document.");

        result = elasticsearchTemplate.create(ES_INDEX, ES_TYPE, id, jsonMap);
//        result = elasticsearchTemplate.create(ES_INDEX, ES_TYPE, id, jsonString);
        logger.info("[IndexResponse]:{}", result);
        return result;

    }

    @RequestMapping(value = "/createAsync", method = RequestMethod.GET)
    public String indexRequestAsync() throws Exception {
        String jsonString = "{" +
                "\"user\":\"jackie\"," +
                "\"postDate\":\"2019-09-30\"," +
                "\"message\":\"Hello Elasticsearch, this is a async request.\"" +
                "}";

        elasticsearchTemplate.createAsync(ES_INDEX, null, jsonString);

        return "已经异步提交创建文档请求。";
    }


    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public Map<String, Object> getRequest() throws Exception {
        String id = "4";

        return elasticsearchTemplate.get(ES_INDEX, ES_TYPE, id);
    }

    @RequestMapping(value = "update", method = RequestMethod.GET)
    public Boolean updateRequest() throws Exception {
        String msg = null;
        String id = "4";

        Map<String, Object> map = new HashMap<>();
        Date currentDate = new Date();
        map.put("message", "Update Hello Elasticsearch." + currentDate); //+":"+new Random());

        /** 使用json格式数据更新es */
        String jsonString = "{" +
                "\"updated\":\"" + System.currentTimeMillis() + "\"," +
                "\"reason\":\"daily update00\"" +
                "}";

        return elasticsearchTemplate.update(ES_INDEX, ES_TYPE, id, jsonString);
//        return elasticsearchTemplate.update(ES_INDEX, ES_TYPE, id, map);
    }

    @RequestMapping(value = "delete", method = RequestMethod.GET)
    public Boolean deleteRequest() throws Exception {
        String msg = null;
        String id = "4";

        return elasticsearchTemplate.delete(ES_INDEX, ES_TYPE, id);
    }

    @RequestMapping(value = "exist", method = RequestMethod.GET)
    public Boolean exist() throws Exception {
        return elasticsearchTemplate.exist(ES_INDEX, ES_TYPE, "4");
    }

    @RequestMapping(value = "updateByQuery", method = RequestMethod.GET)
    public Long updateByQuery() throws Exception {
        return elasticsearchTemplate.updateByQuery(ES_INDEX,
                new TermQueryBuilder("user", "kimchy"),
                "if (ctx._source.user == 'kimchy') {ctx._source.likes++;}");
    }

    @RequestMapping(value = "deleteByQuery", method = RequestMethod.GET)
    public Long deleteByQuery() throws Exception {
        return elasticsearchTemplate.deleteByQuery(ES_INDEX,
                new TermQueryBuilder("user", "kimchy"));
    }

    @RequestMapping(value = "queryByPage", method = RequestMethod.GET)
    public List<Map<String, Object>> queryByPage() throws Exception {
        return elasticsearchTemplate.queryByPage(ES_INDEX, 3, 2, "_id");
    }

    @RequestMapping(value = "termQueryByPage", method = RequestMethod.GET)
    public List<Map<String, Object>> termQueryByPage() throws Exception {
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("user", "kimchy");
        return elasticsearchTemplate.termQueryByPage(ES_INDEX, 3, 1, "_id", termQueryBuilder);
    }

    @RequestMapping(value = "termsQueryByPage", method = RequestMethod.GET)
    public List<Map<String, Object>> termsQueryByPage() throws Exception {
        // 根据id列表查询文档
        TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery("_id", new String[]{"1", "2"});
        return elasticsearchTemplate.termsQueryByPage(ES_INDEX, 3, 1, "_id", termsQueryBuilder);
    }


    @RequestMapping(value = "matchQueryByPage", method = RequestMethod.GET)
    public List<Map<String, Object>> matchQueryByPage() throws Exception {
        // 根据id列表查询文档
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("user", "kimchy");
//                .fuzziness(Fuzziness.AUTO)
//                .prefixLength(3)
//                .maxExpansions(10);
        return elasticsearchTemplate.matchQueryByPage(ES_INDEX, 3, 1, null, matchQueryBuilder);
//        return elasticsearchTemplate.matchQueryByPage(ES_INDEX, 3, 1, "_id", matchQueryBuilder);
    }


    /**
     * 全文检索：指定一些关键字中，必须至少匹配其中的多少个关键字，才能作为结果返回；
     * 例如：查询关键中的4个分词，至少满足2个
     * GET /forum/article/_search
     * {"query":{"match":{"title":{"query":"java elasticsearch spark hadoop","minimum_should_match":"50%"}}}}
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "minimumShouldMatchQuery", method = RequestMethod.GET)
    public List<Map<String, Object>> minimumShouldMatchQuery() throws Exception {
//        MatchQueryBuilder matchQueryBuilder = QueryBuilders.
//                matchQuery("title", "java elasticsearch spark hadoop").minimumShouldMatch("50%");
//
//        return elasticsearchTemplate.matchQueryByPage("forum", 3, 1, null, matchQueryBuilder);

        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("user", "jackie 6").minimumShouldMatch("50%");
        return elasticsearchTemplate.matchQueryByPage(ES_INDEX, 3, 1, null, matchQueryBuilder);
    }

    /**
     * 全文检索：指定一些关键字中，一次匹配多个Field进行全文检索，同时必须至少匹配其中的多少个关键字，才能作为结果返回；
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "multiMatchQueryByPage", method = RequestMethod.GET)
    public List<Map<String, Object>> multiMatchQueryByPage() throws Exception {
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("jackie out","user", "message");
        return elasticsearchTemplate.multiMatchQueryByPage(ES_INDEX, 3, 1, null, multiMatchQueryBuilder);
    }

    /**
     * 条件查询：根据多个条件查询
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "conditionQueryByPage", method = RequestMethod.GET)
    public List<Map<String, Object>> conditionQueryByPage() throws Exception {
        /* ------------方法1------------*/
/*
        // 全文检索Field:title
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("title", "elasticsearch hadoop");
        // 全文检索Field:content
        MatchQueryBuilder matchQueryBuilder2 = QueryBuilders.matchQuery("content", "goods");
        // 精确查找
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("author_id", 112);
        // 全文检索Field:content
        MatchQueryBuilder shouldMatchQueryBuilder = QueryBuilders.matchQuery("content", "flink");
        // 全文检索Field:author_id
        MatchQueryBuilder mustNotMatchQueryBuilder = QueryBuilders.matchQuery("author_id", 111);

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .must(matchQueryBuilder)
                .must(matchQueryBuilder2)
                .must(termQueryBuilder)
                .should(shouldMatchQueryBuilder)
                .mustNot(mustNotMatchQueryBuilder);
        return elasticsearchTemplate.conditionQueryByPage("website", 3, 1, null, boolQueryBuilder);
*/

        /* ------------方法2------------*/
        // 全文检索，一次匹配多个Field进行全文检索 Field:content
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("elasticsearch hadoop goods","title", "content");

        // 精确查找
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("author_id", 112);
        // 全文检索Field:content
        MatchQueryBuilder shouldMatchQueryBuilder = QueryBuilders.matchQuery("content", "flink");
        // 全文检索Field:author_id
        MatchQueryBuilder mustNotMatchQueryBuilder = QueryBuilders.matchQuery("author_id", 111);

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .must(multiMatchQueryBuilder)
                .must(termQueryBuilder)
                .should(shouldMatchQueryBuilder)
                .mustNot(mustNotMatchQueryBuilder);
        return elasticsearchTemplate.conditionQueryByPage("website", 3, 1, null, boolQueryBuilder);
    }


    /**
     * 过滤查询：过滤器查询是根据关键字匹配文档，不去计算和判断文档的匹配度得分，同时可以cache，
     * 所以过滤器性能比全文检索查询性能高。
     * 注意：过滤器使用的前提是bool查询，过滤器可以单独使用,过滤器每个Query都是单字段过滤。
     * GET website/_search
     * {"query":{"bool":{"must":[{"match":{"title":"elasticsearch hadoop"}},{"match":{"content":"goods"}}],"filter":{"range":{"author_id":{"gte":112,"lte":115}}}}}}
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "filterQueryByPage", method = RequestMethod.GET)
    public List<Map<String, Object>> filterQueryByPage() throws Exception {
        // 全文检索Field:title
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("title", "elasticsearch hadoop");
        // 全文检索Field:content
        MatchQueryBuilder matchQueryBuilderContent = QueryBuilders.matchQuery("content", "goods");
        // Filter检索Field:author_id
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("author_id").gte(112).lte(115);

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .must(matchQueryBuilder)
                .must(matchQueryBuilderContent)
                .filter(rangeQueryBuilder);
        return elasticsearchTemplate.conditionQueryByPage("website", 5, 1, null, boolQueryBuilder);
    }

    /**
     * 聚合查询
     */
    @RequestMapping(value = "aggregationQueryByPage", method = RequestMethod.GET)
    public String aggregationQueryByPage() throws Exception {
        String nicknameCompany = "by_company";
        TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms(nicknameCompany).field("company.keyword");
        aggregationBuilder.subAggregation(AggregationBuilders.avg("average_age").field("age"));

        // 重载使用聚合别名
//        String jsonResult = elasticsearchTemplate.aggregationQueryByPage(ES_INDEX, aggregationBuilder, nicknameCompany);
        // 不使用聚合别名（动态获取）
        String jsonResult = elasticsearchTemplate.aggregationQueryByPage(ES_INDEX, aggregationBuilder);
//        List<AggregationBean> list = JSON.parseArray(jsonResult, AggregationBean.class);
//        for (AggregationBean aggregationBean : list) {
//            logger.info(aggregationBean.getDoc_count());
//            logger.info(aggregationBean.getKey());
//            logger.info(aggregationBean.getAverageAge().getValue().toString());
//        }
        return jsonResult;
    }

    /**
     * 搜索建议
     */
    @RequestMapping(value = "suggestQueryByPage", method = RequestMethod.GET)
    public String suggestQueryByPage() throws Exception {
        String nickname = "suggest_user";
        SuggestionBuilder termSuggestionBuilder = SuggestBuilders
                .termSuggestion("user").text("jack");
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion(nickname, termSuggestionBuilder);
        return elasticsearchTemplate.suggestQueryByPage(ES_INDEX, suggestBuilder);
    }


}

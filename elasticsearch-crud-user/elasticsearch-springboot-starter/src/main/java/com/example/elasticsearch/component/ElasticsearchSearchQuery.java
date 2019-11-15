package com.example.elasticsearch.component;

import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.suggest.SuggestBuilder;

import java.util.List;
import java.util.Map;

/**
 * @author jackie wang
 * @Title: ElasticsearchSearchQuery
 * @ProjectName elasticsearch-crud
 * @Description: elasticsearch客户端工具类查询操作接口。
 * @date 2019/9/30 14:16
 */
public interface ElasticsearchSearchQuery {

    /**
     * 根据id查询文档
     *
     * @param indexName 索引；
     * @param type 文档；
     * @param id id；
     * @return
     */
    Map get(String indexName, String type, String id) throws Exception;

    /**
     * 根据id判断文档是否存在
     *
     * @param indexName 索引；
     * @param type 文档；
     * @param id id；
     * @return
     */
    boolean exist(String indexName, String type, String id) throws Exception;

    /**
     * es文档分页查询
     * @param indexName 索引；
     * @param pageSize 页显示大小；
     * @param pageNow 当前页；
     * @param sortFieldName 排序字段；
     * @param queryBuilder 全文检索；
     * @return
     * @throws Exception
     */
    List<Map<String, Object>> queryByPage(String indexName, Integer pageSize, Integer pageNow,
                                          String sortFieldName, QueryBuilder queryBuilder) throws Exception;

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
    List<Map<String, Object>> queryByPage(String indexName, Integer pageSize, Integer pageNow, String sortFieldName) throws Exception;

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
    List<Map<String, Object>> termQueryByPage(String indexName, Integer pageSize, Integer pageNow,
                                              String sortFieldName, TermQueryBuilder termQueryBuilder) throws Exception;

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
    List<Map<String, Object>> termsQueryByPage(String indexName, Integer pageSize, Integer pageNow,
                                               String sortFieldName, TermsQueryBuilder termsQueryBuilder) throws Exception;


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
    List<Map<String, Object>> matchQueryByPage(String indexName, Integer pageSize, Integer pageNow,
                                               String sortFieldName, MatchQueryBuilder matchQueryBuilder) throws Exception;

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
    List<Map<String, Object>> multiMatchQueryByPage(String indexName, Integer pageSize, Integer pageNow,
                                                    String sortFieldName, MultiMatchQueryBuilder multiMatchQueryBuilder) throws Exception;

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
    List<Map<String, Object>> conditionQueryByPage(String indexName, Integer pageSize, Integer pageNow,
                                                   String sortFieldName, BoolQueryBuilder boolQueryBuilder) throws Exception;

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
    public String aggregationQueryByPage(String indexName, TermsAggregationBuilder termsAggregationBuilder, String aggregationAlias) throws Exception;

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
    public String aggregationQueryByPage(String indexName, TermsAggregationBuilder termsAggregationBuilder) throws Exception;


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
    public String suggestQueryByPage(String indexName, SuggestBuilder suggestBuilder) throws Exception;

}

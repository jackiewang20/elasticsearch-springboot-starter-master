package com.example.elasticsearch.component;

import org.elasticsearch.index.query.TermQueryBuilder;

/**
 * @author jackie wang
 * @Title: ElasticsearchExecute
 * @ProjectName elasticsearch-crud
 * @Description: elasticsearch客户端工具类更新操作接口。
 * @date 2019/9/30 14:16
 */
public interface ElasticsearchExecute {

    /**
     * 为给定的indexName创建一个索引
     * @return
     * @throws Exception
     */
//    boolean createIndex() throws Exception;


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
    boolean create(String indexName, String type, String id, Object mapping) throws Exception;


    /**
     * 根据id创建文档
     *
     * @param indexName 索引；
     * @param type 文档；
     * @param mapping   可以是map/String(json)/XContentBuilder类型，根据参数类型动态适配；
     * @return
     */
    boolean create(String indexName, String type, Object mapping) throws Exception;


    /**
     * 根据id异步创建文档
     * @param indexName
     * @param type
     * @param mapping 可以是map/String(json)/XContentBuilder类型，根据参数类型动态适配；
     * @return
     */
    void createAsync(String indexName, String type, Object mapping) throws Exception;

    /**
     * 根据id更新文档
     * @param indexName
     * @param type
     * @param id
     * @param mapping 可以是map/String(json)/XContentBuilder类型，根据参数类型动态适配；
     * @return
     */
    boolean update(String indexName, String type, String id, Object mapping) throws Exception;

    /**
     * 根据id删除文档
     *
     * @param indexName 索引；
     * @param type 文档；
     * @param id id；
     * @return
     */
    boolean delete(String indexName, String type, String id) throws Exception;

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
    Long updateByQuery(String indexName, TermQueryBuilder termQueryBuilder, String script) throws Exception;

    /**
     * 根据查询条件删除索引中的文档。
     *
     * @param indexName 索引；
     * @param termQueryBuilder 例如：new TermQueryBuilder("user","kimchy" )
     * @return
     * @throws Exception
     */
    Long deleteByQuery(String indexName, TermQueryBuilder termQueryBuilder) throws Exception;

}

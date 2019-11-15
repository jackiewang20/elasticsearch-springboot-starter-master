# 注意事项

1.说明
（1）PostsController 使用了未封装的ES类进行CRUD操作。
（2）Posts2Controller 使用了封装的ES类进行CRUD操作。
（3）ES初始化数据

##  组合多个搜索条件
PUT /website/article/1
{
  "title":"my elasticsearch article",
  "content":"es is very bad",
  "author_id":110
}

PUT website/article/2
{
  "title":"my elasticsearch article",
  "content":"hadoop is very bad",
  "author_id":111
}

PUT website/article/7
{
  "title":"my hadoop article",
  "content":"hadoop and flink is very goods",
  "author_id":115
}

## 查询website数据
GET /website/article/_search

----------------------------
GET posts/_search

GET posts/_search
{
  "query": {
    "match": {
      "user": {
        "query": "jackie 6",
        "minimum_should_match": "50%"
      }
    }
  }
}

## 修改
POST posts/_doc/5/_update
{
  "doc": {
    "user":"jackie"
  }
}

## 增加字段：company,age
POST /posts/_doc/_bulk
{ "update": { "_id": "1"} }
{ "doc" : {"company" : "baidu"} }
{ "update": { "_id": "2"} }
{ "doc" : {"company" : "dianxin"} }
{ "update": { "_id": "3"} }
{ "doc" : {"company" : "dianxin"} }
{ "update": { "_id": "4"} }
{ "doc" : {"company" : "dianxin"} }
{ "update": { "_id": "5"} }
{ "doc" : {"company" : "yidong"} }

```
POST /posts/_doc/_bulk
{ "update": { "_id": "1"} }
{ "doc" : {"age" : 22} }
{ "update": { "_id": "2"} }
{ "doc" : {"age" : 20} }
{ "update": { "_id": "3"} }
{ "doc" : {"age" : 28} }
{ "update": { "_id": "4"} }
{ "doc" : {"age" : 33} }
{ "update": { "_id": "5"} }
{ "doc" : {"age" : 50} }
```

# 2.Maven依赖

        <dependency>
            <groupId>org.elasticsearch.client</groupId>
            <artifactId>elasticsearch-rest-high-level-client</artifactId>
            <version>7.1.1</version>
        </dependency>
es间接依赖包，spring boot中会关联间接依赖包
		<dependency>
			<groupId>org.apache.logging.log4j</groupId>
			<artifactId>log4j-api</artifactId>
			<version>2.7</version>
		</dependency>
		<dependency>
			<groupId>org.apache.logging.log4j</groupId>
			<artifactId>log4j-core</artifactId>
			<version>2.7</version>
		</dependency>

如果使用log4j2，那么在主maven pom.xml排除logback接口，添加log4j2：

```xml
    <!-- 依赖传递到子模块 -->
    <dependencies>
        <!-- starter包排除默认的logback，使用log4j2 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter</artifactId>
            <exclusions>
                <exclusion>
                    <artifactId>spring-boot-starter-logging</artifactId>
                    <groupId>org.springframework.boot</groupId>
                </exclusion>
            </exclusions>
        </dependency>

        <!-- 日志:log4j2 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-log4j2</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

    </dependencies>
```



package com.example.es.user.provider.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author jackie wang
 * @Title: AggregationBean
 * @ProjectName elasticsearch-crud
 * @Description: TODO
 * @date 2019/10/15 19:21
 */
public class AggregationBean {
    private String doc_count;
    private String key;
    @JSONField(name = "avg#average_age")
    private AverageAge averageAge;

    public AggregationBean() {
    }

    public String getDoc_count() {
        return doc_count;
    }

    public void setDoc_count(String doc_count) {
        this.doc_count = doc_count;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public AverageAge getAverageAge() {
        return averageAge;
    }

    public void setAverageAge(AverageAge averageAge) {
        this.averageAge = averageAge;
    }
}

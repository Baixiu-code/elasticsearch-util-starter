package com.baixiu.middleware.elasticsearch.page;

import java.io.Serializable;

/**
 * 游标分页请求,用于遍历全量数据
 */
public class ScrollPageRequest implements Serializable {

    private int pageSize = 200;
    private long scrollSeconds = 300;
    private String scrollId;

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getScrollSeconds() {
        return scrollSeconds;
    }

    public void setScrollSeconds(long scrollSeconds) {
        this.scrollSeconds = scrollSeconds;
    }

    public String getScrollId() {
        return scrollId;
    }

    public void setScrollId(String scrollId) {
        this.scrollId = scrollId;
    }
}
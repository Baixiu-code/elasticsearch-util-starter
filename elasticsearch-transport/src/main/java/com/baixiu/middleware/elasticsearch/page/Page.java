package com.baixiu.middleware.elasticsearch.page;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 分页对象
 */
public class Page<T> implements Serializable {
    private long totalElements = 0;
    private int size = 10;
    private int page = 1;
    private List<T> content = new ArrayList<T>();

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public long getTotalPage() {
        return totalElements / size + (totalElements % size == 0 ? 0 : 1);
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}

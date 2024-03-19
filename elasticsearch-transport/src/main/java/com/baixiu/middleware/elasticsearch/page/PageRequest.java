package com.baixiu.middleware.elasticsearch.page;

import java.io.Serializable;

/**
 * 分页请求
 */
public class PageRequest implements Serializable {

    private int page = 1;
    
    private int pageSize = 10;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getOffset() {
        return (long) (page - 1) * pageSize;
    }

    public static PageRequest getOrInitPage(PageRequest pageRequest){
        if(pageRequest!=null){
            if(pageRequest.getPage()==0){
                pageRequest.setPage(1);
            }
            if(pageRequest.getPageSize()==0){
                pageRequest.setPageSize(20);
            }
            return pageRequest;
        }
        pageRequest=new PageRequest();
        pageRequest.setPage(1);
        pageRequest.setPageSize(20);
        return pageRequest;
    }

    @Override
    public String toString() {
        return "PageRequest{" +
                "page=" + page +
                ", pageSize=" + pageSize +
                '}';
    }

    public PageRequest() {}

    public PageRequest(int page, int pageSize) {
        this.page = page;
        this.pageSize = pageSize;
    }
}

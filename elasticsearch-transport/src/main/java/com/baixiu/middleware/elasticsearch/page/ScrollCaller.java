package com.baixiu.middleware.elasticsearch.page;

/**
 * 以游标的方式扫描数据<br/>
 * 适用于10000以上的数据获取<br/>
 * 此接口为游标回调接口
 *
 * @author baixiu
 * @date  2024年03月30日
 */
public interface ScrollCaller {
    
    /**
     * 游标读取一条数据时会调用该方法
     * @param id 起始的游标id
     */
    void call(String id) throws Exception;
}

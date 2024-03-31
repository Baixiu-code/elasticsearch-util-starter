package com.jd.rd.product.domain.biz.service.search.service;

/**
 * 以游标的方式扫描数据<br/>
 * 适用于10000以上的数据获取<br/>
 * 此接口为游标回调接口
 *
 * @author wuyanghong3
 * date  2020/11/12
 */
public interface ScrollCaller {
    /**
     * 游标读取一条数据时会调用该方法
     * @param id
     */
    void call(String id) throws Exception;
}

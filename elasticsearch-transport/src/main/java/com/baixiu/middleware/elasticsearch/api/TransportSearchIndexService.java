package com.baixiu.middleware.elasticsearch.api;


import com.baixiu.middleware.elasticsearch.order.OrderBy;
import com.baixiu.middleware.elasticsearch.page.*;
import java.util.List;

/**
 * 搜索及索引服务基础功能接口定义
 * @author baixiu
 * @date  2024年03月30日
 */
public interface TransportSearchIndexService<S, T> {
    

    /**
     * 建索引
     * @param indexBean 索引具体的bean 泛型处理
     */
    void addBean(Long tenantId,T indexBean);
    

    /**
     * 删除索引
     * @param id docID(可自定义)
     */
    void delete(Long tenantId,String id);

    /**
     * 删除索引
     * @param id  docID(可自定义)
     * @param parentId docID(可自定义)
     */
    void delete(Long tenantId,String id, String parentId);

    /**
     * 根据路由字段删除索引
     * @param id
     * @param route
     */
    void deleteByRoute(Long tenantId,String id, String route);


    /**
     * 普通查询 根据 searchBean 返回
     * @param tenantId tenantId
     * @param searchBean searchBean
     * @param pageRequest pageRequest
     * @param order order 
     * @return pageResult
     */
    Page<T> search(Long tenantId, S searchBean, PageRequest pageRequest, List<OrderBy> order);


    /**
     * 查询某些字段,去重
     * @param tenantId tenantId
     * @param searchBean searchBean
     * @param pageRequest pageRequest
     * @return
     */
    Page<T> searchFiled(Long tenantId, S searchBean, PageRequest pageRequest);

    /**
     * 以游标的方式扫描全量数据<br/>
     * 不支持排序<br/>
     *
     * @param searchBean 查询条件
     * @param caller     游标回调
     */
    void scrollFull(Long tenantId,S searchBean, ScrollCaller caller);

 

    /**
     * 使用游标方式搜索， 适用于10000以上的数据获取，不支持排序
     * @param searchBean searchBean
     * @param scrollPageRequest scrollPageRequest
     * @param sceneRequest sceneRequest
     * @return
     */
    public ScrollPage<T> scrollSearch(Long tenantId, S searchBean, ScrollPageRequest scrollPageRequest) ;


    void deleteByRoute(Long tenantId, String id, String parentId, String route);

    T getById(Long tenantId, String id);

  

    /**
     * 根据主键ID局部更新数据
     *
     * @param tenantId Long
     * @param id String
     * @param route String
     * @param indexBean T
     */
    void updateById(Long tenantId, String id, T indexBean);

    T getById(Long tenantId, String id, String route);

    void updateByRouteId(Long tenantId, String id, String route, T indexBean);
}

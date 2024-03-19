package com.baixiu.middleware.elasticsearch.api;


/**
 * 搜索及索引服务基础功能定义
 *
 * @author wuyanghong3
 * date  2020/11/12
 */
public interface TransportSearchIndexService<S, T> {

    /**
     * 建索引
     * @param indexBean
     */
    void addBean(Long tenantId,T indexBean);
    

    /**
     * 删除索引
     *
     * @param id
     */
    void delete(Long tenantId,String id);

    /**
     * 删除索引
     * @param id
     * @param parentId
     */
    void delete(Long tenantId,String id, String parentId);

    /**
     * 根据路由字段删除索引
     * @param id
     * @param route
     */
    void deleteByRoute(Long tenantId,String id, String route);

    /**
     * 根据路由字段删除索引
     * @param id
     * @param route
     */
    void deleteByRoute(Long tenantId,String id, String parentId, String route);

    Page<T> search(Long tenantId, S searchBean, PageRequest pageRequest, List<OrderBy> order
            , TemplateRouteRequest sceneRequest);

    /**
     * 查询商品ID
     *
     * @param searchBean
     * @param pageRequest
     * @param order
     * @param isGoToJes true 则使用ES 6.0.false 则使用ES 5.0
     * @return
     */
    Page<T> search(Long tenantId, S searchBean, PageRequest pageRequest, List<OrderBy> order,Boolean isGoToJes);


    /**
     * 查询某些字段,去重
     * @param tenantId
     * @param searchBean
     * @param pageRequest
     * @param isGoToJes true则使用ES6.x; false则使用ES5.x
     * @return
     */
    Page<T> searchFiled(Long tenantId, S searchBean, PageRequest pageRequest, boolean isGoToJes);

    Page<T> searchFiled(Long tenantId, S searchBean, PageRequest pageRequest, TemplateRouteRequest sceneRequest);
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
     * @param tenantId tenantId
     * @param searchBean searchBean
     * @param scrollPageRequest scrollPageRequest
     * @param isGoToJes isGoToJes
     * @return
     */
    @Deprecated
    public ScrollPage<T> scrollSearch(Long tenantId, S searchBean, ScrollPageRequest scrollPageRequest,Boolean isGoToJes) ;

    /**
     * 使用游标方式搜索， 适用于10000以上的数据获取，不支持排序
     * @param searchBean searchBean
     * @param scrollPageRequest scrollPageRequest
     * @param sceneRequest sceneRequest
     * @return
     */
    public ScrollPage<T> scrollSearch(Long tenantId, S searchBean, ScrollPageRequest scrollPageRequest,TemplateRouteRequest sceneRequest) ;


    T getById(Long tenantId,String id);

    T getById(Long tenantId,String id, String route);

    /**
     * 根据主键ID局部更新数据
     *
     * @param tenantId Long
     * @param id String
     * @param route String
     * @param indexBean T
     */
    void updateByRouteId(Long tenantId, String id, String route, T indexBean);
}

package com.baixiu.middleware.elasticsearch.api;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baixiu.middleware.elasticsearch.order.OrderBy;
import com.baixiu.middleware.elasticsearch.page.*;
import com.baixiu.middleware.elasticsearch.transport.ElasticSearchTemplateClient;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.collapse.CollapseBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Component;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ES索引服务基类
 * @author migration
 * @date  2020/11/12
 */
@Component
@Slf4j
public abstract class AbstractTransportSearchIndexService<S, T> implements TransportSearchIndexService<S, T> {

    public static String defaultRouting = "0";
    public static final int DEFAULT_PAGE_MAX_DEEP = 10000;

    private String index;
    private String type;

    private ElasticSearchTemplateClient elasticSearchTemplate;

   

    @Override
    public Page<T> search(Long tenantId, S searchBean, PageRequest pageRequest, List<OrderBy> order) {

        Page<T> returnPage = new Page<T>();
        returnPage.setPage(pageRequest.getPage());
        returnPage.setSize(pageRequest.getPageSize());

        if (searchBean == null) {
            return returnPage;
        }

        int from = pageRequest.getPage() < 1 ? 0 : (pageRequest.getPage() - 1) * pageRequest.getPageSize();
        // 控制分页深度
        if (from + pageRequest.getPageSize() > DEFAULT_PAGE_MAX_DEEP) {
            log.info("最多查询[" + DEFAULT_PAGE_MAX_DEEP + "]条数据，请增加查询条件过滤结果集");
            throw new RuntimeException ("最多查询[" + DEFAULT_PAGE_MAX_DEEP + "]条数据，请增加查询条件过滤结果集");
        }


        SearchRequestBuilder srb = getSearchRequestBuilderBySwitch().setFrom(from)
                .setSize(pageRequest.getPageSize())
                .setQuery(getQueryBuilder(searchBean));
        String[] routing = getRouting4Search(searchBean);
        if (routing != null && routing.length > 0) {
            srb.setRouting(routing);
        }
        srb.addSort(SortBuilders.scoreSort().order(SortOrder.DESC));
        // 构建排序
        for (OrderBy searchOrder : order) {
            if (null == searchOrder) {
                continue;
            }
            srb.addSort(searchOrder.getField(), getSortOrder(searchOrder));
        }
        log.info("EsAbstractIndexService.SearchRequestBuilder:{}", srb);

        SearchHits searchHits = null;
        try {
            searchHits = srb.execute().actionGet().getHits();
        } catch (Exception e) {
            if(e instanceof SearchPhaseExecutionException){
                log.info("searchMultiError.SearchPhaseExecutionException.noBizError.{}.{}",tenantId,e.getMessage());
                return null;
            }
            if(e instanceof RuntimeException){
                log.info("searchMultiError.BadArgumentException.noBizError.{}.{}",tenantId,e.getMessage());
                return null;
            }
            log.error("searchMultiError.{}.{}.{}.{}", tenantId, JSONObject.toJSONString(searchBean)
                    , JSONObject.toJSONString(pageRequest),JSONObject.toJSONString(order), e);
            return null;
        }

        long totalCount = searchHits.getTotalHits();
        log.info("searchTotalCount:{}", totalCount);
        if (totalCount == 0) {
            return returnPage;
        }

        returnPage.setTotalElements(totalCount);
        for (SearchHit searchHitFields : searchHits.getHits()) {
            returnPage.getContent().add(JSONObject.parseObject(searchHitFields.getSourceAsString(), getClassT()));
        }

        return returnPage;
    }

    private SearchRequestBuilder getSearchRequestBuilderBySwitch() {
        Client client = elasticSearchTemplate.getClient();
        String indexReal = index;
        String typeReal =  type;
        return client.prepareSearch(indexReal).setTypes(typeReal);
    }

    @Override
    public Page<T> searchFiled(Long tenantId, S searchBean, PageRequest pageRequest) {

        Page<T> returnPage = new Page<>();
        returnPage.setPage(pageRequest.getPage());
        returnPage.setSize(pageRequest.getPageSize());

        if (searchBean == null) {
            return returnPage;
        }

        int from = pageRequest.getPage() < 1 ? 0 : (pageRequest.getPage() - 1) * pageRequest.getPageSize();

        // 控制分页深度
        if (from + pageRequest.getPageSize() > DEFAULT_PAGE_MAX_DEEP) {
            throw new RuntimeException ("最多查询[" + DEFAULT_PAGE_MAX_DEEP + "]条数据，请增加查询条件过滤结果集");
        }

        SearchRequestBuilder srb = getSearchRequestBuilderBySwitch()
                .setFrom(from)
                .setSize(pageRequest.getPageSize())
                .setCollapse(new CollapseBuilder("skuIdskuName"))
                .setQuery(getQueryBuilder(searchBean));

        log.info("EsAbstractIndexService searchRequestBuilder: {}", srb.toString());
        srb.addSort(SortBuilders.scoreSort().order(SortOrder.DESC));
        SearchHits searchHits = srb.execute().actionGet().getHits();
        Long totalCount = searchHits.getTotalHits();
        if (totalCount == 0){
            return returnPage;
        }

        returnPage.setTotalElements(totalCount.intValue());
        for (SearchHit searchHitFields : searchHits.getHits()) {
            returnPage.getContent().add(JSONObject.parseObject(searchHitFields.getSourceAsString(), getClassT()));
        }

        return returnPage;
    }

   

    private List<Map<String, String>> getFlatBucket(int layer, Terms.Bucket bucket, String... groupColumnsNames) {
        ArrayListMultimap<BucketNode, BucketNode> bucketRowMultimap = ArrayListMultimap.create();
        Stack<BucketNode> nodeStack = new Stack<>();
        BucketNode bucketNode = new BucketNode(layer, groupColumnsNames[layer], bucket);
        nodeStack.add(bucketNode);
        bucketRowMultimap.put(bucketNode, bucketNode);
        while (!nodeStack.isEmpty()) {
            bucketNode = nodeStack.pop();
            List<BucketNode> childerNodes = getChildrenBucketNodes(bucketNode, groupColumnsNames);
            if (childerNodes != null && !childerNodes.isEmpty()) {
                List<BucketNode> parentRoute = bucketRowMultimap.removeAll(bucketNode);
                for (BucketNode child : childerNodes) {
                    nodeStack.push(child);
                    bucketRowMultimap.putAll(child, parentRoute);
                    bucketRowMultimap.put(child, child);
                }
            }
        }
        return convertToRows(bucketRowMultimap.asMap().values());
    }

    /**
     * 获得下一层Bucket的节点列表
     *
     * @param parentNode
     * @param groupColumnsNames
     * @return
     */
    private List<BucketNode> getChildrenBucketNodes(BucketNode parentNode, String... groupColumnsNames) {
        int currentLayer = parentNode.layer + 1;
        if (currentLayer < groupColumnsNames.length) {
            String currentAggName = groupColumnsNames[currentLayer];
            Terms currentAgg = parentNode.bucket.getAggregations().get(currentAggName);
            if (Objects.nonNull(currentAgg)) {
                return currentAgg.getBuckets().stream().map(bucket -> new BucketNode(currentLayer, currentAggName, bucket)).collect(Collectors.toList());
            }
        }
        return null;

    }

    private List<Map<String, String>> convertToRows(Collection<Collection<BucketNode>> bucketRoutes) {
        return bucketRoutes.stream().map(bucketRoute -> convertToRow(bucketRoute)).collect(Collectors.toList());
    }

    private Map<String, String> convertToRow(Collection<BucketNode> bucketRoute) {
        Map<String, String> row = new HashMap<>();
        bucketRoute.stream().forEach(bucketNode -> row.put(bucketNode.aggName, bucketNode.bucket.getKeyAsString()));
        return row;
    }

    class BucketNode {

        int layer;
        String aggName;
        Terms.Bucket bucket;

        public BucketNode(int layer, String aggName, Terms.Bucket bucket) {
            BucketNode.this.layer = layer;
            BucketNode.this.aggName = aggName;
            BucketNode.this.bucket = bucket;
        }

        @Override
        public String toString() {
            return "BucketNode{" + "layer=" + layer + ", aggName=" + aggName + ", bucket_key=" + bucket.getKeyAsString() + '}';
        }

    }

    @Override
    public ScrollPage<T> scrollSearch(Long tenantId, S searchBean, ScrollPageRequest scrollPageRequest) {
        ScrollPage<T> returnPage = new ScrollPage<T>();
        returnPage.setSize(scrollPageRequest.getPageSize());
        SearchResponse scrollResp;

        // 没有ScrollId  说明是首次请求
        if (StringUtils.isBlank(scrollPageRequest.getScrollId())) {
            SearchRequestBuilder srb = getSearchRequestBuilderBySwitch()
                    .addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)
                    .setScroll(new TimeValue(scrollPageRequest.getScrollSeconds() * 1000))
                    .setQuery(getQueryBuilder(searchBean))
                    .setSize(scrollPageRequest.getPageSize());
            String[] routing = getRouting4Search(searchBean);
            if (routing != null && routing.length > 0) {
                srb.setRouting(routing);
            }
            scrollResp = srb.execute().actionGet();
        }
        // 使用ScrollId获取剩余的数据
        else {
            scrollResp = (elasticSearchTemplate.getClient())
                    .prepareSearchScroll(scrollPageRequest.getScrollId())
                    .setScroll(new TimeValue(scrollPageRequest.getScrollSeconds() * 1000))
                    .execute()
                    .actionGet();
        }

        SearchHits searchHits = scrollResp.getHits();
        long totalCount = searchHits.getTotalHits();
        if (totalCount == 0) {
            return returnPage;
        }
        returnPage.setTotalElements((int) totalCount);
        for (SearchHit searchHitFields : searchHits.getHits()) {
            returnPage.getContent().add(JSONObject.parseObject(searchHitFields.getSourceAsString(), getClassT()));
        }
        returnPage.setScrollId(scrollResp.getScrollId());
        return returnPage;
    }


    @Override
    public void scrollFull(Long tenantId, S searchBean, ScrollCaller caller) {

        if (caller == null) return;

        Integer scrollTime = 60000;
        Integer scrollSize = 100;
        SearchRequestBuilder srb = elasticSearchTemplate.getClient()
                .prepareSearch(index)
                .addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)
                .setTypes(type)
                .setScroll(new TimeValue(scrollTime))
                .setSize(scrollSize);

        if (searchBean != null) srb.setQuery(getQueryBuilder(searchBean));

        String[] routing = getRouting4Search(searchBean);
        if (routing != null && routing.length > 0) {
            srb.setRouting(routing);
        }

        SearchResponse scrollResp = srb.execute().actionGet();

        Long scrollMaxPage = 1000_000_000L;
        for (int i = 0; i < scrollMaxPage; i++) {
            for (SearchHit hit : scrollResp.getHits().getHits()) {
                try {
                    caller.call(hit.getId());
                } catch (Exception e) {
                    log.error("es.scroll.call error id[{}]", hit.getId(), e);
                }
            }

            scrollResp = elasticSearchTemplate.getClient()
                    .prepareSearchScroll(scrollResp.getScrollId())
                    .setScroll(new TimeValue(scrollTime))
                    .execute()
                    .actionGet();

            if (scrollResp.getHits().getHits().length == 0) {
                return;
            }
        }
    }

    /**
     * 创建索引
     *
     * @param id     主键
     * @param source 需要存储的对象
     */
    public void index(String id, Object source) {
        index(id, source, null);
    }

    /**
     * 创建索引
     *
     * @param id     主键
     * @param source 需要存储的对象
     * @param parent 父对象主键
     */
    public void index(String id, Object source, String parent) {
        indexByRoute(id, source, parent, null);
    }

    /**
     * 创建索引
     *
     * @param id     主键
     * @param source 需要存储的对象
     */
    public void indexByRoute(String id, Object source, String route) {
        indexByRoute(id, source, null, route);
    }

    /**
     * 创建索引
     *
     * @param id     主键
     * @param source 需要存储的对象
     * @param parent 父对象主键
     */
    public void indexByRoute(String id, Object source, String parent, String route) {

        elasticSearchTemplate.getClient()
                .prepareIndex(index, type, id)
                .setRouting(route)
                .setSource(JSONObject.toJSONString(source), XContentType.JSON)
                .execute()
                .actionGet();

        
    }
    
    @Override
    public void delete(Long tenantId, String id) {
        deleteByRoute(tenantId, id, null);
    }

    @Override
    public void delete(Long tenantId, String id, String parentId) {
        deleteByRoute(tenantId, id, parentId, null);
    }

    @Override
    public void deleteByRoute(Long tenantId, String id, String route) {

        elasticSearchTemplate.getClient()
                .prepareDelete(index, type, id)
                .setRouting(route)
                .get();
    }

    @Override
    public void deleteByRoute(Long tenantId, String id, String parentId, String route) {

        elasticSearchTemplate.getClient()
                .prepareDelete(index, type, id)
                .setParent(parentId)
                .setRouting(route)
                .get();
    }

    @Override
    public T getById(Long tenantId, String id) {
        return getById(tenantId, id, null);
    }

    @Override
    public T getById(Long tenantId, String id, String route) {

        GetResponse response = elasticSearchTemplate.getClient()
                .prepareGet(index, type, id)
                .setRouting(route)
                .setFetchSource(true)
                .execute()
                .actionGet();

        return JSONObject.parseObject(JSONObject.toJSONString(response.getSource()), getClassT());
    }

    @Override
    public void updateByRouteId(Long tenantId, String id, String route, T indexBean) {
        try {
            UpdateRequest updateRequest = new UpdateRequest()
                    .index(index)
                    .type(type)
                    .routing(route)
                    .id(String.valueOf(id))
                    .retryOnConflict(3)
                    .doc(JSONObject.toJSONString(indexBean))
                    .docAsUpsert(true);
            UpdateResponse updateResponse = elasticSearchTemplate.getClient().update(updateRequest).get();
            log.info("updateById, res updateResponse:" + JSON.toJSONString(updateResponse));
            if (!(updateResponse != null && updateResponse.getShardInfo() != null && updateResponse.getShardInfo().getFailed() == 0)) {
                log.error("updateById, ex indexBean:" + JSON.toJSONString(indexBean) + "  updateResponse:" + JSON.toJSONString(updateResponse));
                throw new RuntimeException ("updateById 更新异常");
            }
        } catch (Exception e) {
            log.error("updateById, ex indexBean:" + JSON.toJSONString(indexBean), e);
            throw new RuntimeException (e);
        }
    }

    /**
     * 创建ES查询对象
     */
    public abstract QueryBuilder getQueryBuilder(S searchBean);

    protected abstract String[] getRouting4Search(S searchBean);


    public void setIndex(String index) {
        this.index = index;
    }

    public void setType(String type) {
        this.type = type;
    }



    public void setElasticSearchTemplate(ElasticSearchTemplateClient elasticsearchTemplate) {
        this.elasticSearchTemplate = elasticsearchTemplate;
    }

    /**
     * 集合类型转换
     */
    public String[] convert2String(List<String> list) {
        List<String> ls = Lists.newArrayList();
        for (String id : list) {
            ls.add(String.valueOf(id));
        }
        return ls.toArray(new String[ls.size()]);
    }

    public String[] convert2StringArray(List<Long> list) {
        List<String> ls = Lists.newArrayList();
        for (Long id : list) {
            ls.add(String.valueOf(id));
        }
        return ls.toArray(new String[ls.size()]);
    }

    /**
     * BigDecimal集合类型转换成Double集合类型
     */
    public List<Double> convert2Double(List<BigDecimal> list) {
        List<Double> ls = Lists.newArrayList();
        for (BigDecimal item : list) {
            ls.add(item.doubleValue());
        }
        return ls;
    }


    /**
     * 通过当前查询的index bean 查询当前index 传入的泛型类
     * @return 传入的泛型类
     */
    private Class<T> getClassT() {

        Class tmp = getClass();
        Class clz = tmp;
        while (tmp != AbstractTransportSearchIndexService.class) {
            clz = tmp;
            tmp = clz.getSuperclass();
        }

        return (Class<T>) ((ParameterizedType) clz.getGenericSuperclass()).getActualTypeArguments()[1];
    }

    public Map<String, JoinLogic.LogicType> convertLogicList2Map(List<JoinLogic> logicList) {

        Map<String, JoinLogic.LogicType> map = Maps.newHashMap();
        if (logicList == null) {
            return map;
        }

        for (JoinLogic joinLogic : logicList) {
            map.put(joinLogic.getField(), JoinLogic.LogicType.valueOf(joinLogic.getType()));
        }
        return map;

    }

    /**
     * 转换为ES需要的排序类型
     */
    private SortOrder getSortOrder(OrderBy orderBy) {
        return OrderBy.OrderByType.desc.name().equals(orderBy.getType()) ? SortOrder.DESC : SortOrder.ASC;
    }

  


}

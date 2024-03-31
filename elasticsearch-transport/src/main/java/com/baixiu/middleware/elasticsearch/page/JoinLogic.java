package com.baixiu.middleware.elasticsearch.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询条件之间的关联逻辑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinLogic {

    /**
     * 指定关联字段
     */
    private String field;

    /**
     * 关联逻辑，值从枚举里取
     */
    private String type;

    /**
     * childAnd,childOr 查询条件内的关联关系, 默认childAnd
     */
    public enum LogicType{
        childAnd,childOr
    }
}

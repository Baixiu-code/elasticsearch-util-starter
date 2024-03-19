package com.baixiu.middleware.elasticsearch.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * 排序辅助类
 * @author chenfanglin1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderBy implements Serializable {
    
    /**
     * 排序字段.值从枚举里取
     */
    private String field;

    /**
     * 排序类型，值从枚举里取,值为asc或desc
     */
    private String type;

    public enum OrderByType{
        asc,desc
    }
    
    
}

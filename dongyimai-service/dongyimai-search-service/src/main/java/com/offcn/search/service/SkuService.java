package com.offcn.search.service;

import java.util.Map;

public interface SkuService {
    /***
     * 导入SKU数据
     */
    void importSku();

    /**
     * 根据品牌、分类、规格等关键字搜索
     */
    Map search(Map<String, String> searchMap);
}

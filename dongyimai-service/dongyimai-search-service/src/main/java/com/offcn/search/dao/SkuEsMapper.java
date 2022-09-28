package com.offcn.search.dao;

import com.offcn.search.pojo.SkuInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * dao层不单单只关联数据库，而是关联数据流向，如ES、redis...
 * 该接口主要用于索引数据操作，主要使用它来实现将数据从数据库导入到ES索引库中
 *
 * ElasticsearchRepository 封装了ES基础API
 */
@Repository
public interface SkuEsMapper extends ElasticsearchRepository<SkuInfo,Long> {
}

package com.offcn.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.offcn.entity.Result;
import com.offcn.search.dao.SkuEsMapper;
import com.offcn.search.pojo.SkuInfo;
import com.offcn.search.service.SkuService;
import com.offcn.sellergoods.feign.ItemFeign;
import com.offcn.sellergoods.pojo.Item;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    private SkuEsMapper skuEsMapper;

    @Autowired
    private ItemFeign itemFeign;

    @Override
    public void importSku() {

        //远程调用商品微服务，从sql中获取sku商品数据
        Result<List<Item>> result = itemFeign.findByStatus("1");

        //把数据转换为搜索实体类数据(按照指定的格式转换,将SkuInfo实体的字段对应填入skuInfoList)
        List<SkuInfo> skuInfoList = JSON.parseArray(JSON.toJSONString(result.getData()), SkuInfo.class);

        //遍历sku集合
        for (SkuInfo skuInfo : skuInfoList) {
            //获取规格
            Map<String, Object> specMap = JSON.parseObject(skuInfo.getSpec());
            //关联设置到specMap
            skuInfo.setSpecMap(specMap);
        }

        //保存sku集合数据到es
        skuEsMapper.saveAll(skuInfoList);
    }


    @Autowired
    private ElasticsearchRestTemplate esRestTemplateRest;

    public Map search(Map<String, String> searchMap) {
        //1.获取搜索关键字
        String keywords = searchMap.get("keywords");
        if (StringUtils.isEmpty(keywords)) {
            keywords = "华为";// 赋值给keywords一个默认的值
        }
        // 2.创建查询对象的构建对象
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();

        //3.设置查询条件
        // 使用：QueryBuilders.matchQuery("title", keywords) 搜索华为 ---> 华 为 二字可以拆分查询，
        // 使用：QueryBuilders.matchPhraseQuery("title", keywords) 搜索华为 --->华为二字不拆分查询
        //设置分组条件 按照商品分类进行分组

        /* AggregationBuilders聚合条件构造器
         terms("skuCategorygroup")：给列取别名,通过skuCategorygroup就可以知道是根据category来分组
         field("category")：字段名称,即根据"category"字段进行分组
         注意,若添加分组代码后报错,一般是版本不匹配引起的代码不同,尝试改成field("category.keyword") 或 field("category.keywords")
         size 指定查询结果的数量 默认是10个*/
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuCategorygroup").field("category").size(50));
        //设置分组条件  商品品牌
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuBrandgroup").field("brand").size(50));
        //设置分组条件  商品规格
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuSpecgroup").field("spec.keyword").size(100));

        // 使用：QueryBuilders.matchQuery("title", keywords) ，搜索华为 ---> 华 为 二字可以拆分查询，
        // 使用：QueryBuilders.matchPhraseQuery("title", keywords) 华为二字不拆分查询
        nativeSearchQueryBuilder.withQuery(QueryBuilders.matchQuery("title", keywords));


        // ========================过滤查询 开始 =====================================
        // 创建多条件组合查询对象
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 设置品牌查询条件
        if (!StringUtils.isEmpty(searchMap.get("brand"))) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("brand", searchMap.get("brand")));
        }
        // 设置分类查询条件
        if (!StringUtils.isEmpty(searchMap.get("category"))) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category", searchMap.get("category")));
        }
        /*规格过滤查询
        按照一定要求来发送数据，例如规格名字以特殊前缀提交到后台：`spec_网络制式：电信4G、spec_显示屏尺寸：4.0-4.9英寸`
        后台接到数据后，可以根据前缀spec_来区分是否是规格*/
        if (searchMap != null) {
            for (String key : searchMap.keySet()) { // { brand:"",category:"",spec_网络:"电信4G"}
                if (key.startsWith("spec_")) {
                    // 截取规格的名称
                    boolQueryBuilder.filter(QueryBuilders.termQuery("specMap." + key.substring(5) + ".keyword", searchMap.get(key)));
                }
            }
        }
        // 价格过滤查询
        String price = searchMap.get("price");
        if (!StringUtils.isEmpty(price)) {
            String[] split = price.split("-");
            if (!split[1].equalsIgnoreCase("*")) {
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").from(split[0], true).to(split[1], true));
            } else {
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(split[0]));
            }
        }
        // 关联过滤查询对象到查询器
        nativeSearchQueryBuilder.withFilter(boolQueryBuilder);
        // ========================过滤查询 结束 =====================================

        //构建分页查询
        Integer pageNum = 1;
        if (!StringUtils.isEmpty(searchMap.get("pageNum"))) {
            try {
                pageNum = Integer.valueOf(searchMap.get("pageNum"));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                pageNum = 1;
            }
        }
        Integer pageSize = 5;
        nativeSearchQueryBuilder.withPageable(PageRequest.of(pageNum - 1, pageSize));
//=================================================================================

        //构建排序查询
        String sortRule = searchMap.get("sortRule");
        String sortField = searchMap.get("sortField");
        if (!StringUtils.isEmpty(sortRule) && !StringUtils.isEmpty(sortField)) {
            nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(sortField).order(sortRule.equals("DESC") ? SortOrder.DESC : SortOrder.ASC));
        }
//=================================================================================

        //设置高亮条件
        nativeSearchQueryBuilder.withHighlightFields(new HighlightBuilder.Field("title"));
        nativeSearchQueryBuilder.withHighlightBuilder(new HighlightBuilder().preTags("<em style=\"color:red\">").postTags("</em>"));
        //设置主关键字查询,修改为多字段的搜索条件
        nativeSearchQueryBuilder.withQuery(QueryBuilders.multiMatchQuery(keywords, "title", "brand", "category"));

        // 4.构建查询对象
        NativeSearchQuery query = nativeSearchQueryBuilder.build();
        // 5. 执行搜索，获取封装响应数据结果的SearchHits集合
        SearchHits<SkuInfo> searchHits = esRestTemplateRest.search(query, SkuInfo.class);
        //对响应数据进行分页封装
        //获取分组结果
        Terms terms = searchHits.getAggregations().get("skuCategorygroup"); // 获取分组结果 商品分类
        List<String> categoryList = getStringsCategoryList(terms); // 获取分类名称集合

        Terms termsBrand = searchHits.getAggregations().get("skuBrandgroup"); // 获取分组结果 商品品牌
        List<String> brandList = getStringsBrandList(termsBrand); // 获取品牌名称集合

        Terms termsSpec = searchHits.getAggregations().get("skuSpecgroup"); // 获取分组结果 商品规格
        Map<String, Set<String>> specMap = getStringSetMap(termsSpec);

        // 对搜索searchHits集合进行分页封装
        SearchPage<SkuInfo> skuPage = SearchHitSupport.searchPageFor(searchHits, query.getPageable());


        List<SkuInfo> skuList = new ArrayList<>();
        for (SearchHit<SkuInfo> searchHit : skuPage.getContent()) {// 获取搜索到的数据
            SkuInfo content = (SkuInfo) searchHit.getContent();
            SkuInfo skuInfo = new SkuInfo();
            BeanUtils.copyProperties(content, skuInfo);
            // 处理高亮
            Map<String, List<String>> highlightFields = searchHit.getHighlightFields();
            for (Map.Entry<String, List<String>> stringHighlightFieldEntry : highlightFields.entrySet()) {
                String key = stringHighlightFieldEntry.getKey();
                if (StringUtils.equals(key, "title")) {
                    List<String> fragments = stringHighlightFieldEntry.getValue();
                    StringBuilder sb = new StringBuilder();
                    for (String fragment : fragments) {
                        sb.append(fragment.toString());
                    }
                    skuInfo.setTitle(sb.toString());
                }
            }
            skuList.add(skuInfo);
        }

        // 6.返回结果
        Map resultMap = new HashMap<>();
        resultMap.put("categoryList", categoryList);
        resultMap.put("brandList", brandList);
        resultMap.put("specMap", specMap);
        // resultMap.put("rows", skuPage.getContent());
        resultMap.put("rows", skuList);//获取所需SkuInfo集合数据内容
        resultMap.put("total", skuPage.getTotalElements()); // 总记录数
        resultMap.put("totalPages", skuPage.getTotalPages()); // 总页数

        //分页数据保存
        //设置当前页码
        resultMap.put("pageNum", pageNum);
        resultMap.put("pageSize", 30);
        return resultMap;
    }


    /**
     * 获取分类列表数据
     *
     * @param terms
     * @return
     */
    private List<String> getStringsCategoryList(Terms terms) {
        List<String> categoryList = new ArrayList<>();
        if (terms != null) {
            for (Terms.Bucket bucket : terms.getBuckets()) {
                String keyAsString = bucket.getKeyAsString();//分组的值
                categoryList.add(keyAsString);
            }
        }
        return categoryList;
    }

    /**
     * 获取品牌列表
     *
     * @param terms
     * @return
     */
    private List<String> getStringsBrandList(Terms terms) {
        List<String> brandList = new ArrayList<>();
        if (terms != null) {
            for (Terms.Bucket bucket : terms.getBuckets()) {
                brandList.add(bucket.getKeyAsString());
            }
        }
        return brandList;
    }


    /**
     * 获取规格列表数据
     * Set集合对规格选项去重
     */
    private Map<String, Set<String>> getStringSetMap(Terms termsSpec) {
        Map<String, Set<String>> specMap = new HashMap<String, Set<String>>();
        Set<String> specList = new HashSet<>();
        if (termsSpec != null) {
            for (Terms.Bucket bucket : termsSpec.getBuckets()) {
                specList.add(bucket.getKeyAsString());
            }
        }

         /*
        specList
        [
            {"机身内存":"16G","网络":"双卡"},
            {"机身内存":"128G","网络":"电信4G"},
            {"机身内存":"16G","网络":"联通2G"},
            {"机身内存":"16G","网络":"电信4G"},
            ...
        ]

        需要的格式{"机身内存":["16G","128G"],"网络":["双卡","电信4G","联通2G"]}
        */
        for (String specjson : specList) {
            //将json格式数据转为map集合 {"机身内存":"16G","网络":"双卡"},
            Map<String, String> map = JSON.parseObject(specjson, Map.class);
            for (Map.Entry<String, String> entry : map.entrySet()) {//
                String key = entry.getKey(); //规格名称
                String value = entry.getValue(); //规格选项
                //获取当前规格名称对应的规格数据
                Set<String> specValues = specMap.get(key);
                if (specValues == null) {
                    specValues = new HashSet<String>();
                }
                //将规格选项（16G）加到集合中
                specValues.add(value); //["16G","128G"],["双卡","电信4G","联通2G"]
                //再将数据存到specMap中并返回
                specMap.put(key, specValues); //{"机身内存":["16G","128G"],"网络":["双卡","电信4G","联通2G"]}
            }
        }
        return specMap;
    }
}


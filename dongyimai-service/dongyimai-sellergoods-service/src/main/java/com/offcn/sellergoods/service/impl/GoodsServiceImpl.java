package com.offcn.sellergoods.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.offcn.entity.PageResult;
import com.offcn.sellergoods.dao.*;
import com.offcn.sellergoods.group.GoodsEntity;
import com.offcn.sellergoods.pojo.*;
import com.offcn.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/****
 * @Author:ujiuye
 * @Description:Goods业务层接口实现类
 * @Date 2021/2/1 14:19
 *****/
@Service
@Transactional
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {


    /**
     * Goods条件+分页查询
     *
     * @param goods 查询条件
     * @param page  页码
     * @param size  页大小
     * @return 分页结果
     */
    @Override
    public PageResult<Goods> findPage(Goods goods, int page, int size) {
        Page<Goods> mypage = new Page<>(page, size);
        QueryWrapper<Goods> queryWrapper = this.createQueryWrapper(goods);
        IPage<Goods> iPage = this.page(mypage, queryWrapper);
        return new PageResult<Goods>(iPage.getTotal(), iPage.getRecords());
    }

    /**
     * Goods分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResult<Goods> findPage(int page, int size) {
        Page<Goods> mypage = new Page<>(page, size);
        IPage<Goods> iPage = this.page(mypage, new QueryWrapper<Goods>());

        return new PageResult<Goods>(iPage.getTotal(), iPage.getRecords());
    }

    /**
     * Goods条件查询
     *
     * @param goods
     * @return
     */
    @Override
    public List<Goods> findList(Goods goods) {
        //构建查询条件
        QueryWrapper<Goods> queryWrapper = this.createQueryWrapper(goods);
        //根据构建的条件查询数据
        return this.list(queryWrapper);
    }


    /**
     * Goods构建查询对象
     *
     * @param goods
     * @return
     */
    public QueryWrapper<Goods> createQueryWrapper(Goods goods) {
        QueryWrapper<Goods> queryWrapper = new QueryWrapper<>();
        if (goods != null) {
            // 主键
            if (!StringUtils.isEmpty(goods.getId())) {
                queryWrapper.eq("id", goods.getId());
            }
            // 商家ID
            if (!StringUtils.isEmpty(goods.getSellerId())) {
                queryWrapper.eq("seller_id", goods.getSellerId());
            }
            // SPU名
            if (!StringUtils.isEmpty(goods.getGoodsName())) {
                queryWrapper.eq("goods_name", goods.getGoodsName());
            }
            // 默认SKU
            if (!StringUtils.isEmpty(goods.getDefaultItemId())) {
                queryWrapper.eq("default_item_id", goods.getDefaultItemId());
            }
            // 状态
            if (!StringUtils.isEmpty(goods.getAuditStatus())) {
                queryWrapper.eq("audit_status", goods.getAuditStatus());
            }
            // 是否上架
            if (!StringUtils.isEmpty(goods.getIsMarketable())) {
                queryWrapper.eq("is_marketable", goods.getIsMarketable());
            }
            // 品牌
            if (!StringUtils.isEmpty(goods.getBrandId())) {
                queryWrapper.eq("brand_id", goods.getBrandId());
            }
            // 副标题
            if (!StringUtils.isEmpty(goods.getCaption())) {
                queryWrapper.eq("caption", goods.getCaption());
            }
            // 一级类目
            if (!StringUtils.isEmpty(goods.getCategory1Id())) {
                queryWrapper.eq("category1_id", goods.getCategory1Id());
            }
            // 二级类目
            if (!StringUtils.isEmpty(goods.getCategory2Id())) {
                queryWrapper.eq("category2_id", goods.getCategory2Id());
            }
            // 三级类目
            if (!StringUtils.isEmpty(goods.getCategory3Id())) {
                queryWrapper.eq("category3_id", goods.getCategory3Id());
            }
            // 小图
            if (!StringUtils.isEmpty(goods.getSmallPic())) {
                queryWrapper.eq("small_pic", goods.getSmallPic());
            }
            // 商城价
            if (!StringUtils.isEmpty(goods.getPrice())) {
                queryWrapper.eq("price", goods.getPrice());
            }
            // 分类模板ID
            if (!StringUtils.isEmpty(goods.getTypeTemplateId())) {
                queryWrapper.eq("type_template_id", goods.getTypeTemplateId());
            }
            // 是否启用规格
            if (!StringUtils.isEmpty(goods.getIsEnableSpec())) {
                queryWrapper.eq("is_enable_spec", goods.getIsEnableSpec());
            }
            // 是否删除
            if (!StringUtils.isEmpty(goods.getIsDelete())) {
                queryWrapper.eq("is_delete", goods.getIsDelete());
            }
        }
        return queryWrapper;
    }

    /**
     * 删除
     *
     * @param id
     */
    //@Override
    //public void delete(Long id) {
    //    this.removeById(id);
    //}
    @Override
    public void delete(Long id) {
        // 逻辑删除
        Goods goods = goodsMapper.selectById(id);
        //检查是否下架的商品
        if (goods.getIsMarketable() != null &&
                !goods.getIsMarketable().equals("0")) {
            throw new RuntimeException("必须先下架再删除！");
        }
        goods.setIsDelete("1");
        //未审核
        goods.setAuditStatus("0");
        goodsMapper.updateById(goods);
    }

    /**
     * 修改Goods
     *
     * @param goods
     */
    //@Override
    //public void update(Goods goods) {
    //    this.updateById(goods);
    //}

    /**
     * 增加Goods
     *
     * @param goods
     */
    @Override
    public void add(Goods goods) {
        this.save(goods);
    }

    /**
     * 根据ID查询Goods
     *
     * @param id
     * @return
     */
    @Override
    public GoodsEntity findById(Long id) {
        //1.根据ID查询SPU信息
        Goods goods = goodsMapper.selectById(id);
        //2.根据ID查询商品扩展信息
        GoodsDesc goodsDesc = goodsDescMapper.selectById(id);
        //3.根据ID查询SKU列表
        QueryWrapper<Item> queryWrapper = new QueryWrapper();
        queryWrapper.eq("goods_id", id);
        List<Item> itemList = itemMapper.selectList(queryWrapper);
        //4.设置复合实体对象
        GoodsEntity goodsEntity = new GoodsEntity();
        goodsEntity.setGoods(goods);
        goodsEntity.setGoodsDesc(goodsDesc);
        goodsEntity.setItemList(itemList);
        return goodsEntity;
    }

    /**
     * 查询Goods全部数据
     *
     * @return
     */
    @Override
    public List<Goods> findAll() {
        return this.list(new QueryWrapper<Goods>());
    }

    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private GoodsDescMapper goodsDescMapper;
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private ItemCatMapper itemCatMapper;
    @Autowired
    private BrandMapper brandMapper;

    /**
     * 增加Goods
     *
     * @param goodsEntity
     */
    @Override
    public void add(GoodsEntity goodsEntity) {
        goodsEntity.getGoods().setAuditStatus("0"); //审核状态 未审核
        //1.保存SPU 商品信息对象
        goodsMapper.insert(goodsEntity.getGoods());
        //2.获取商品信息对象主键ID ,向商品扩展信息对象中设置主键
        goodsEntity.getGoodsDesc().setGoodsId(goodsEntity.getGoods().getId());
        //3.保存商品扩展信息
        goodsDescMapper.insert(goodsEntity.getGoodsDesc());
        //4.保存SKU 商品详情信息
        this.saveItemList(goodsEntity);
    }

    @Override
    public void update(GoodsEntity goodsEntity) {
        //将审核状态重新设置为 未审核
        goodsEntity.getGoods().setAuditStatus("0");
        //1.修改SPU的信息
        goodsMapper.updateById(goodsEntity.getGoods());
        //2.修改商品扩展信息
        goodsDescMapper.updateById(goodsEntity.getGoodsDesc());
        //3.先根据商品ID删除SKU信息
        QueryWrapper<Item> queryWrapper = new QueryWrapper();
        queryWrapper.eq("goods_id", goodsEntity.getGoods().getId());
        itemMapper.delete(queryWrapper);
        //4.重新添加SKU信息
        this.saveItemList(goodsEntity);
    }

    /***
     * 商品审核
     * @param goodsId
     */
    @Override
    public void audit(Long goodsId) {
        //查询商品
        Goods goods = goodsMapper.selectById(goodsId);
        //判断商品是否已经删除
        if (goods.getIsDelete() != null && goods.getIsDelete().equals("1")) {
            throw new RuntimeException("该商品已经删除！");
        }
        //实现上架和审核
        goods.setAuditStatus("1"); //审核通过
        goods.setIsMarketable("1"); //上架
        goodsMapper.updateById(goods);
    }

    /**
     * 商品下架
     *
     * @param goodsId
     */
    @Override
    public void pull(Long goodsId) {
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods.getIsDelete() != null && goods.getIsDelete().equals("1")) {
            throw new RuntimeException("此商品已删除！");
        }
        goods.setIsMarketable("0");//下架状态
        goodsMapper.updateById(goods);
    }

    /***
     * 商品上架
     * @param goodsId
     */
    @Override
    public void put(Long goodsId) {
        Goods goods = goodsMapper.selectById(goodsId);
        //检查是否删除的商品
        if (goods.getIsDelete() != null && goods.getIsDelete().equals("1")) {
            throw new RuntimeException("此商品已删除！");
        }
        if (!goods.getAuditStatus().equals("1")) {
            throw new RuntimeException("未通过审核的商品不能！");
        }
        //上架状态
        goods.setIsMarketable("1");
        goodsMapper.updateById(goods);
    }

    /**
     * 批量上架
     *
     * @param ids:需要上架的商品ID集合
     * @return
     */
    @Override
    public int putMany(Long[] ids) {
        Goods goods = new Goods();
        goods.setIsMarketable("1");//上架
        //批量修改
        QueryWrapper<Goods> queryWrapper = new QueryWrapper();
        queryWrapper.in("id", Arrays.asList(ids));
        //下架
        queryWrapper.eq("is_marketable", "0");
        //审核通过的
        queryWrapper.eq("audit_status", "1");
        //⾮删除的
        // queryWrapper.eq("is_delete","0");
        queryWrapper.isNull("is_delete");
        return goodsMapper.update(goods, queryWrapper);
    }

    private void setItemValue(GoodsEntity goodsEntity, Item item) {
        item.setCategoryId(goodsEntity.getGoods().getCategory3Id()); //商品分类 三级
        item.setCreateTime(new Date()); //创建时间
        item.setUpdateTime(new Date()); //更新时间
        item.setGoodsId(goodsEntity.getGoods().getId());
        //SPU ID
        item.setSellerId(goodsEntity.getGoods().getSellerId()); //商家ID
        //查询分类对象
        ItemCat itemCat = itemCatMapper.selectById(goodsEntity.getGoods().getCategory3Id());
        item.setCategory(itemCat.getName()); //分类名称
        //查询品牌对象
        Brand tbBrand = brandMapper.selectById(goodsEntity.getGoods().getBrandId());
        item.setBrand(tbBrand.getName()); //品牌名称
        List<Map> imageList =
                JSON.parseArray(goodsEntity.getGoodsDesc().getItemImages(), Map.class);
        if (imageList.size() > 0) {
            item.setImage((String) imageList.get(0).get("url")); //商品图⽚
        }
    }

    //保存SKU信息
    private void saveItemList(GoodsEntity goodsEntity) {
        if ("1".equals(goodsEntity.getGoods().getIsEnableSpec())) {
            //5.保存SKU 商品详情信息
            if (!CollectionUtils.isEmpty(goodsEntity.getItemList())) {
                for (Item item : goodsEntity.getItemList()) {
                    String title = goodsEntity.getGoods().getGoodsName();
                    //设置SKU的名称 商品名+规格选项
                    Map<String, String> specMap =
                            JSON.parseObject(item.getSpec(), Map.class); //取得SKU的规格选项，并做JSON类型转换
                    for (String key : specMap.keySet()) {
                        title += specMap.get(key) + " ";
                    }
                    item.setTitle(title);
                    //SKU名称
                    this.setItemValue(goodsEntity, item);
                    //保存SKU信息
                    itemMapper.insert(item);
                }
            }
        } else {
            //不启⽤规格 SKU信息为默认值
            Item item = new Item();
            item.setTitle(goodsEntity.getGoods().getGoodsName()); //商品名称
            item.setPrice(goodsEntity.getGoods().getPrice()); //默认使⽤SPU的价格
            item.setNum(9999);
            item.setStatus("1"); //是否启⽤
            item.setIsDefault("1"); //是否默认
            item.setSpec("{}"); //没有选择规格，则放置空JSON结构
            this.setItemValue(goodsEntity, item);
            itemMapper.insert(item);
        }
    }
}

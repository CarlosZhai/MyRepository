package com.offcn.sellergoods.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.offcn.order.pojo.OrderItem;
import com.offcn.sellergoods.pojo.Item;
import org.apache.ibatis.annotations.Update;

public interface ItemMapper extends BaseMapper<Item> {
    /**
     * 减少库存
     */
    @Update("update tb_item set num=num-#{num} where id=#{itemId} and num>=#{num}")
    int decrCount(OrderItem orderItem);
}

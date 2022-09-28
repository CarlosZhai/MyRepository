package com.offcn.order.service;

import com.offcn.order.pojo.Cart;

import java.util.List;

public interface CartService {
    /**
     * 添加商品到购物车
     */
    List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num);

    /**
     * 根据用户名，从redis中查询购物车数据
     */
    List<Cart> findCartListFromRedis(String username);

    /**
     * 将购物车保存到redis
     */
    void saveCartListToRedis(String username, List<Cart> cartList);

}

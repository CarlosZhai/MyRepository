package com.offcn.order.service.impl;

import com.offcn.entity.Result;
import com.offcn.order.pojo.Cart;
import com.offcn.order.pojo.OrderItem;
import com.offcn.order.service.CartService;
import com.offcn.sellergoods.feign.ItemFeign;
import com.offcn.sellergoods.pojo.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private ItemFeign itemFeign;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加商品到购物车
     */
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.根据商品id查询商品SKU信息
        Result<Item> itemResult = itemFeign.findById(itemId);
        Item item = itemResult.getData();
        if (null == item) {
            throw new RuntimeException("商品不存在");
        }
        if (!item.getStatus().equals("1")) {
            throw new RuntimeException("商品状态无效");
        }

        //2.从商品信息中获取商家ID
        String sellerId = item.getSellerId();

        //3.根据商家ID判断购物车列表中是否存在该商家的购物车
        Cart cart = searchCartBySellerId(cartList, sellerId);

        //4.购物车列表中不存在该商家的购物车
        if (null == cart) {
            //新建该商家的购物车对象
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());

            OrderItem orderItem = createOrderItem(item, num);
            List<OrderItem> orderItemList = new ArrayList<>();
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);

            //将购物车对象添加到购物车列表
            cartList.add(cart);
        } else {
            //5.购物车列表中已经该商家的购物车
            //先判断购物车明细列表中是否存在该商品
            OrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
            if (null == orderItem) {
                //如果没有该商品，则新增购物车明细
                orderItem = createOrderItem(item, num);
                cart.getOrderItemList().add(orderItem);
            } else {
                //如果已经有该商品，就在原购物车明细基础上添加数量、更改金额
                orderItem.setNum(orderItem.getNum() + num);
                BigDecimal decimal = orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getNum()));
                orderItem.setTotalFee(decimal);

                if (orderItem.getNum() <= 0) {
                    cart.getOrderItemList().remove(orderItem);
                }
                if (cart.getOrderItemList().size() == 0) {
                    cartList.remove(cart);
                }
            }
        }
        return cartList;
    }

    /**
     * 从redis中查询购物车
     */
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if (null == cartList) {
            cartList = new ArrayList<>();
        }
        return cartList;
    }

    /**
     * 将购物车保存到redis
     */
    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        redisTemplate.boundHashOps("cartList").put(username, cartList);
    }

    /**
     * 根据商品ID查询购物车明细
     */
    private OrderItem searchOrderItemByItemId(List<OrderItem> orderItemList, Long itemId) {
        for (OrderItem orderItem : orderItemList) {
            if (orderItem.getItemId().equals(itemId)) {
                return orderItem;
            }
        }
        return null;
    }

    /**
     * 创建订单明细
     */
    private OrderItem createOrderItem(Item item, Integer num) {
        if (num <= 0) {
            throw new RuntimeException("数量非法");
        }
        OrderItem orderItem = new OrderItem();
        orderItem.setItemId(item.getId());
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setTitle(item.getTitle());
        orderItem.setPrice(new BigDecimal(item.getPrice()));
        orderItem.setNum(num);
        orderItem.setTotalFee(orderItem.getPrice().multiply(BigDecimal.valueOf(num)));
        orderItem.setPicPath(item.getImage());
        orderItem.setSellerId(item.getSellerId());

        return orderItem;
    }

    /**
     * 根据商家ID查询购物车对象
     */
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if (cart.getSellerId().equals(sellerId)) {
                return cart;
            }
        }
        return null;
    }
}

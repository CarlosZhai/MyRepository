package com.offcn.order.controller;

import com.offcn.entity.Result;
import com.offcn.entity.StatusCode;
import com.offcn.order.pojo.Cart;
import com.offcn.order.service.CartService;
import com.offcn.utils.TokenDecode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
@CrossOrigin
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private TokenDecode tokenDecode;

    /**
     * 获取购物车列表
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
        Map<String, String> userInfo = tokenDecode.getUserInfo();
        System.out.println("userInfo:" + userInfo);
        String username = userInfo.get("username");
        if (username == null || username.equals("")) {
            username = "test";
        }
        return cartService.findCartListFromRedis(username);
    }


    /**
     * 添加商品到购物车
     */
    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId, Integer num) {
        //String username = "test";
        Map<String, String> userInfo = tokenDecode.getUserInfo();
        System.out.println("userInfo:" + userInfo);
        String username = userInfo.get("username");
        if (username == null || username.equals("")) {
            username = "test";
        }
        try {
            //先获取用户的购物车列表
            //List<Cart> cartList = findCartList(username);
            List<Cart> cartList = findCartList();

            if (cartList == null) {
                cartList = new ArrayList<>();
            }
            //添加商品到购物车
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);

            //将购物车保存到redis
            cartService.saveCartListToRedis(username, cartList);
            return new Result(true, StatusCode.OK, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, StatusCode.ERROR, "添加失败");
        }
    }
}

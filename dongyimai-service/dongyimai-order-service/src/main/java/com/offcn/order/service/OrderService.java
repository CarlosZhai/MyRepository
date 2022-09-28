package com.offcn.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.offcn.entity.PageResult;
import com.offcn.order.pojo.Order;
import com.offcn.order.pojo.PayLog;

import java.util.List;

public interface OrderService extends IService<Order> {

    /***
     * Order多条件分页查询
     * @param order
     * @param page
     * @param size
     * @return
     */
    PageResult<Order> findPage(Order order, int page, int size);

    /***
     * Order分页查询
     * @param page
     * @param size
     * @return
     */
    PageResult<Order> findPage(int page, int size);

    /***
     * Order多条件搜索方法
     * @param order
     * @return
     */
    List<Order> findList(Order order);

    /***
     * 删除Order
     * @param id
     */
    void delete(Long id);

    /***
     * 修改Order数据
     * @param order
     */
    void update(Order order);

    /***
     * 新增Order
     * @param order
     */
    void add(Order order);

    /**
     * 根据ID查询Order
     *
     * @param id
     * @return
     */
    Order findById(Long id);

    /***
     * 查询所有Order
     * @return
     */
    List<Order> findAll();

    /**
     * 根据用户查询payLog
     *
     * @param userId
     * @return
     */
    PayLog searchPayLogFromRedis(String userId);

    /***
     * 根据订单ID修改订单状态
     * @param transaction_id 支付宝交易流水号
     * @param out_trade_no
     */
    void updateOrderStatus(String out_trade_no, String transaction_id);
}

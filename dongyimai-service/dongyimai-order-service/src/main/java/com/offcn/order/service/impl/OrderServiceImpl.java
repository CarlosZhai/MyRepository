package com.offcn.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.offcn.entity.PageResult;
import com.offcn.order.dao.OrderItemMapper;
import com.offcn.order.dao.OrderMapper;
import com.offcn.order.dao.PayLogMapper;
import com.offcn.order.pojo.Cart;
import com.offcn.order.pojo.Order;
import com.offcn.order.pojo.OrderItem;
import com.offcn.order.pojo.PayLog;
import com.offcn.order.service.OrderService;
import com.offcn.sellergoods.feign.ItemFeign;
import com.offcn.user.feign.UserFeign;
import com.offcn.utils.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {


    /**
     * Order条件+分页查询
     *
     * @param order 查询条件
     * @param page  页码
     * @param size  页大小
     * @return 分页结果
     */
    @Override
    public PageResult<Order> findPage(Order order, int page, int size) {
        Page<Order> mypage = new Page<>(page, size);
        QueryWrapper<Order> queryWrapper = this.createQueryWrapper(order);
        IPage<Order> iPage = this.page(mypage, queryWrapper);
        return new PageResult<Order>(iPage.getTotal(), iPage.getRecords());
    }

    /**
     * Order分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResult<Order> findPage(int page, int size) {
        Page<Order> mypage = new Page<>(page, size);
        IPage<Order> iPage = this.page(mypage, new QueryWrapper<Order>());

        return new PageResult<Order>(iPage.getTotal(), iPage.getRecords());
    }

    /**
     * Order条件查询
     *
     * @param order
     * @return
     */
    @Override
    public List<Order> findList(Order order) {
        //构建查询条件
        QueryWrapper<Order> queryWrapper = this.createQueryWrapper(order);
        //根据构建的条件查询数据
        return this.list(queryWrapper);
    }


    /**
     * Order构建查询对象
     *
     * @param order
     * @return
     */
    public QueryWrapper<Order> createQueryWrapper(Order order) {
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        if (order != null) {
            // 订单id
            if (!StringUtils.isEmpty(order.getOrderId())) {
                queryWrapper.eq("order_id", order.getOrderId());
            }
            // 实付金额。精确到2位小数;单位:元。如:200.07，表示:200元7分
            if (!StringUtils.isEmpty(order.getPayment())) {
                queryWrapper.eq("payment", order.getPayment());
            }
            // 支付类型，1、在线支付，2、货到付款
            if (!StringUtils.isEmpty(order.getPaymentType())) {
                queryWrapper.eq("payment_type", order.getPaymentType());
            }
            // 邮费。精确到2位小数;单位:元。如:200.07，表示:200元7分
            if (!StringUtils.isEmpty(order.getPostFee())) {
                queryWrapper.eq("post_fee", order.getPostFee());
            }
            // 状态：1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭,7、待评价
            if (!StringUtils.isEmpty(order.getStatus())) {
                queryWrapper.eq("status", order.getStatus());
            }
            // 订单创建时间
            if (!StringUtils.isEmpty(order.getCreateTime())) {
                queryWrapper.eq("create_time", order.getCreateTime());
            }
            // 订单更新时间
            if (!StringUtils.isEmpty(order.getUpdateTime())) {
                queryWrapper.eq("update_time", order.getUpdateTime());
            }
            // 付款时间
            if (!StringUtils.isEmpty(order.getPaymentTime())) {
                queryWrapper.eq("payment_time", order.getPaymentTime());
            }
            // 发货时间
            if (!StringUtils.isEmpty(order.getConsignTime())) {
                queryWrapper.eq("consign_time", order.getConsignTime());
            }
            // 交易完成时间
            if (!StringUtils.isEmpty(order.getEndTime())) {
                queryWrapper.eq("end_time", order.getEndTime());
            }
            // 交易关闭时间
            if (!StringUtils.isEmpty(order.getCloseTime())) {
                queryWrapper.eq("close_time", order.getCloseTime());
            }
            // 物流名称
            if (!StringUtils.isEmpty(order.getShippingName())) {
                queryWrapper.eq("shipping_name", order.getShippingName());
            }
            // 物流单号
            if (!StringUtils.isEmpty(order.getShippingCode())) {
                queryWrapper.eq("shipping_code", order.getShippingCode());
            }
            // 用户id
            if (!StringUtils.isEmpty(order.getUserId())) {
                queryWrapper.eq("user_id", order.getUserId());
            }
            // 买家留言
            if (!StringUtils.isEmpty(order.getBuyerMessage())) {
                queryWrapper.eq("buyer_message", order.getBuyerMessage());
            }
            // 买家昵称
            if (!StringUtils.isEmpty(order.getBuyerNick())) {
                queryWrapper.eq("buyer_nick", order.getBuyerNick());
            }
            // 买家是否已经评价
            if (!StringUtils.isEmpty(order.getBuyerRate())) {
                queryWrapper.eq("buyer_rate", order.getBuyerRate());
            }
            // 收货人地区名称(省，市，县)街道
            if (!StringUtils.isEmpty(order.getReceiverAreaName())) {
                queryWrapper.eq("receiver_area_name", order.getReceiverAreaName());
            }
            // 收货人手机
            if (!StringUtils.isEmpty(order.getReceiverMobile())) {
                queryWrapper.eq("receiver_mobile", order.getReceiverMobile());
            }
            // 收货人邮编
            if (!StringUtils.isEmpty(order.getReceiverZipCode())) {
                queryWrapper.eq("receiver_zip_code", order.getReceiverZipCode());
            }
            // 收货人
            if (!StringUtils.isEmpty(order.getReceiver())) {
                queryWrapper.eq("receiver", order.getReceiver());
            }
            // 过期时间，定期清理
            if (!StringUtils.isEmpty(order.getExpire())) {
                queryWrapper.eq("expire", order.getExpire());
            }
            // 发票类型(普通发票，电子发票，增值税发票)
            if (!StringUtils.isEmpty(order.getInvoiceType())) {
                queryWrapper.eq("invoice_type", order.getInvoiceType());
            }
            // 订单来源：1:app端，2：pc端，3：M端，4：微信端，5：手机qq端
            if (!StringUtils.isEmpty(order.getSourceType())) {
                queryWrapper.eq("source_type", order.getSourceType());
            }
            // 商家ID
            if (!StringUtils.isEmpty(order.getSellerId())) {
                queryWrapper.eq("seller_id", order.getSellerId());
            }
        }
        return queryWrapper;
    }

    /**
     * 删除
     *
     * @param id
     */
    @Override
    public void delete(Long id) {
        this.removeById(id);
    }

    /**
     * 修改Order
     *
     * @param order
     */
    @Override
    public void update(Order order) {
        this.updateById(order);
    }


    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ItemFeign itemFeign;

    @Autowired
    private UserFeign userFeign;

    @Autowired
    private PayLogMapper payLogMapper;

    /**
     * 添加订单
     *
     * @param order
     */
    @Override
    public void add(Order order) {
        // 获取购物车数据
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
        List<String> orderIdList = new ArrayList<>();//订单ID列表

        BigDecimal decimal = null;
        for (Cart cart : cartList) {
            long orderId = idWorker.nextId();

            Order tborder = new Order();//新建订单对象(购物车点击结算后再次确认订单信息)
            tborder.setOrderId(orderId);// 订单ID
            tborder.setUserId(order.getUserId());// 用户名
            tborder.setPaymentType(order.getPaymentType());// 支付类型
            tborder.setStatus("1");// 状态：未付款
            tborder.setCreateTime(new Date());// 订单创建日期
            tborder.setUpdateTime(new Date());// 订单更新日期
            tborder.setReceiverAreaName(order.getReceiverAreaName());// 收货地址
            tborder.setReceiverMobile(order.getReceiverMobile());// 手机号
            tborder.setReceiver(order.getReceiver());// 收货人
            tborder.setSourceType(order.getSourceType());// 订单来源
            tborder.setSellerId(cart.getSellerId());// 商家ID

            // 循环购物车明细
            for (OrderItem orderItem : cart.getOrderItemList()) {
                orderItem.setId(idWorker.nextId());
                orderItem.setOrderId(orderId);// 订单ID
                orderItem.setSellerId(cart.getSellerId());
                orderItem.setTotalFee(orderItem.getTotalFee());
                decimal = orderItem.getTotalFee();
                //减少库存  调用sellergoods微服务的itemFeign
                itemFeign.decrCount(order.getUserId());
                //保存订单明细到数据库中
                orderItemMapper.insert(orderItem);
            }
            orderIdList.add(orderId + "");
            tborder.setPayment(decimal);//实付金额
            orderMapper.insert(tborder);
        }

        //选择在线支付的方式
        if ("1".equals(order.getPaymentType())) {
            PayLog payLog = new PayLog();
            String outTradeNo = idWorker.nextId() + "";
            payLog.setOutTradeNo(outTradeNo);//支付订单号
            payLog.setCreateTime(new Date());//创建时间
            //需要String类型的订单号列表
            String ids = orderIdList.toString()
                    .replace("[", "")
                    .replace("]", "")
                    .replace(" ", "");
            payLog.setOrderList(ids);
            payLog.setPayType(order.getPaymentType());//支付类型

            //把支付金额单位由元转换成分
            BigDecimal decimal1 = decimal.multiply(BigDecimal.valueOf(100));
            //payLog表中的支付金额需要Long类型
            payLog.setTotalFee(decimal1.toBigInteger().longValue());

            payLog.setTradeState("0");//支付状态  0 未支付 1已经支付
            payLog.setUserId(order.getUserId());//用户名
            payLogMapper.insert(payLog);//插入到支付日志表
            redisTemplate.boundHashOps("payLog").put(order.getUserId(), payLog);//放入缓存
        }

        //增加积分，调用用户微服务的userFeign
        int points = (int) (Math.random() * 100 + 1);
        userFeign.addUserPoints(points);

        //从redis中删除购物车
        redisTemplate.boundHashOps("cartList").delete(order.getUserId());
    }

    /**
     * 根据ID查询Order
     *
     * @param id
     * @return
     */
    @Override
    public Order findById(Long id) {
        return this.getById(id);
    }

    /**
     * 查询Order全部数据
     *
     * @return
     */
    @Override
    public List<Order> findAll() {
        return this.list(new QueryWrapper<Order>());
    }


    /**
     * 根据用户查询payLog
     */
    @Override
    public PayLog searchPayLogFromRedis(String userId) {
        PayLog payLog = (PayLog) redisTemplate.boundHashOps("payLog").get(userId);
        return payLog;
    }

    @Override
    public void updateOrderStatus(String out_trade_no, String transaction_id) {
        //1.修改支付日志状态
        PayLog payLog = payLogMapper.selectById(out_trade_no);
        payLog.setPayTime(new Date());
        payLog.setTradeState("1");//已支付
        payLog.setTransactionId(transaction_id);//交易号
        payLogMapper.updateById(payLog);
        //2.修改订单状态
        String orderList = payLog.getOrderList();//获取订单号列表
        String[] orderIds = orderList.split(",");//获取订单号数组

        for (String orderId : orderIds) {
            Order order = orderMapper.selectById(Long.parseLong(orderId));
            if (order != null) {
                order.setStatus("2");//已付款
                orderMapper.updateById(order);
            }
        }
        //清除redis缓存数据
        redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
    }
}


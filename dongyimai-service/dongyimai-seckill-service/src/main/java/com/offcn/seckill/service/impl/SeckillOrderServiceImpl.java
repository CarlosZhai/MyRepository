package com.offcn.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.offcn.entity.PageResult;
import com.offcn.entity.StatusCode;
import com.offcn.seckill.dao.SeckillOrderMapper;
import com.offcn.seckill.entity.SeckillStatus;
import com.offcn.seckill.pojo.SeckillOrder;
import com.offcn.seckill.service.SeckillOrderService;
import com.offcn.seckill.task.MultiThreadingCreateOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Service
public class SeckillOrderServiceImpl extends ServiceImpl<SeckillOrderMapper, SeckillOrder> implements SeckillOrderService {


    /**
     * SeckillOrder条件+分页查询
     *
     * @param seckillOrder 查询条件
     * @param page         页码
     * @param size         页大小
     * @return 分页结果
     */
    @Override
    public PageResult<SeckillOrder> findPage(SeckillOrder seckillOrder, int page, int size) {
        Page<SeckillOrder> mypage = new Page<>(page, size);
        QueryWrapper<SeckillOrder> queryWrapper = this.createQueryWrapper(seckillOrder);
        IPage<SeckillOrder> iPage = this.page(mypage, queryWrapper);
        return new PageResult<SeckillOrder>(iPage.getTotal(), iPage.getRecords());
    }

    /**
     * SeckillOrder分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResult<SeckillOrder> findPage(int page, int size) {
        Page<SeckillOrder> mypage = new Page<>(page, size);
        IPage<SeckillOrder> iPage = this.page(mypage, new QueryWrapper<SeckillOrder>());

        return new PageResult<SeckillOrder>(iPage.getTotal(), iPage.getRecords());
    }

    /**
     * SeckillOrder条件查询
     *
     * @param seckillOrder
     * @return
     */
    @Override
    public List<SeckillOrder> findList(SeckillOrder seckillOrder) {
        //构建查询条件
        QueryWrapper<SeckillOrder> queryWrapper = this.createQueryWrapper(seckillOrder);
        //根据构建的条件查询数据
        return this.list(queryWrapper);
    }


    /**
     * SeckillOrder构建查询对象
     *
     * @param seckillOrder
     * @return
     */
    public QueryWrapper<SeckillOrder> createQueryWrapper(SeckillOrder seckillOrder) {
        QueryWrapper<SeckillOrder> queryWrapper = new QueryWrapper<>();
        if (seckillOrder != null) {
            // 主键
            if (!StringUtils.isEmpty(seckillOrder.getId())) {
                queryWrapper.eq("id", seckillOrder.getId());
            }
            // 秒杀商品ID
            if (!StringUtils.isEmpty(seckillOrder.getSeckillId())) {
                queryWrapper.eq("seckill_id", seckillOrder.getSeckillId());
            }
            // 支付金额
            if (!StringUtils.isEmpty(seckillOrder.getMoney())) {
                queryWrapper.eq("money", seckillOrder.getMoney());
            }
            // 用户
            if (!StringUtils.isEmpty(seckillOrder.getUserId())) {
                queryWrapper.eq("user_id", seckillOrder.getUserId());
            }
            // 商家
            if (!StringUtils.isEmpty(seckillOrder.getSellerId())) {
                queryWrapper.eq("seller_id", seckillOrder.getSellerId());
            }
            // 创建时间
            if (!StringUtils.isEmpty(seckillOrder.getCreateTime())) {
                queryWrapper.eq("create_time", seckillOrder.getCreateTime());
            }
            // 支付时间
            if (!StringUtils.isEmpty(seckillOrder.getPayTime())) {
                queryWrapper.eq("pay_time", seckillOrder.getPayTime());
            }
            // 状态
            if (!StringUtils.isEmpty(seckillOrder.getStatus())) {
                queryWrapper.eq("status", seckillOrder.getStatus());
            }
            // 收货人地址
            if (!StringUtils.isEmpty(seckillOrder.getReceiverAddress())) {
                queryWrapper.eq("receiver_address", seckillOrder.getReceiverAddress());
            }
            // 收货人电话
            if (!StringUtils.isEmpty(seckillOrder.getReceiverMobile())) {
                queryWrapper.eq("receiver_mobile", seckillOrder.getReceiverMobile());
            }
            // 收货人
            if (!StringUtils.isEmpty(seckillOrder.getReceiver())) {
                queryWrapper.eq("receiver", seckillOrder.getReceiver());
            }
            // 交易流水
            if (!StringUtils.isEmpty(seckillOrder.getTransactionId())) {
                queryWrapper.eq("transaction_id", seckillOrder.getTransactionId());
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
     * 修改SeckillOrder
     *
     * @param seckillOrder
     */
    @Override
    public void update(SeckillOrder seckillOrder) {
        this.updateById(seckillOrder);
    }

    /**
     * 增加SeckillOrder
     *
     * @param seckillOrder
     */
    @Override
    public void add(SeckillOrder seckillOrder) {
        this.save(seckillOrder);
    }

    /**
     * 根据ID查询SeckillOrder
     *
     * @param id
     * @return
     */
    @Override
    public SeckillOrder findById(Long id) {
        return this.getById(id);
    }

    /**
     * 查询SeckillOrder全部数据
     *
     * @return
     */
    @Override
    public List<SeckillOrder> findAll() {
        return this.list(new QueryWrapper<SeckillOrder>());
    }

    @Autowired
    private MultiThreadingCreateOrder multiThreadingCreateOrder;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加秒杀订单
     *
     * @param id:商品ID
     * @param time:商品秒杀开始时间
     * @param username:用户登录名
     */
    @Override
    public Boolean add(Long id, String time, String username) {

        //递增，判断是否排队，如果值>1，则表明已经排队,不允许重复排队
        Long userQueueCount = redisTemplate.boundHashOps("UserQueueCount").increment(username, 1);
        if (userQueueCount > 1) {
            throw new RuntimeException(String.valueOf(StatusCode.REPERROR));
        }

        //排队信息封装
        SeckillStatus seckillStatus = new SeckillStatus(username, new Date(), 1, id, time);

        //将秒杀抢单信息存入到Redis中,这里采用List方式存储,List本身是一个队列
        redisTemplate.boundListOps("SeckillOrderQueue").leftPush(seckillStatus);

        //将抢单状态存入到Redis中
        // 虽然上一条代码已经将seckillStatus存放到了redis中, 但因队列"SeckillOrderQueue"存放的数据需要保留之前的顺序(记录了用户抢单顺序), 且读取时pop弹栈会删除掉数据, 所以针对订单状态的增删改查不应该在队列完成, 我们新创建一个数据库结构
        redisTemplate.boundHashOps("UserQueueStatus").put(username, seckillStatus);

        //多线程操作
        multiThreadingCreateOrder.createOrder();
        return true;
    }

    /***
     * 抢单状态查询
     * @param username
     */
    @Override
    public SeckillStatus queryStatus(String username) {
        return (SeckillStatus) redisTemplate.boundHashOps("UserQueueStatus").get(username);
    }
}

package com.offcn.seckill.timer;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.offcn.seckill.dao.SeckillGoodsMapper;
import com.offcn.seckill.pojo.SeckillGoods;
import com.offcn.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 定时将符合参与秒杀的商品查询出来再存入到Redis缓存
 */
@Component
public class SeckillGoodsPushTask {

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /****
     * 每30秒执行一次
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void loadGoodsPushRedis() {
        //获取时间段集合
        List<Date> dateMenus = DateUtil.getDateMenus();
        //以当前时间为参照,寻找5个活动时段。如:当前时间为15点, dateMenus值为{14, 16, 18, 20, 22}
        for (Date startTime : dateMenus) { // 寻找符合每个活动时段的商品, 存入redis以展示在页面

            //提取开始时间，转换为指定格式字符串
            //extName=2022081216  2022-08-12 16:00 当前时刻所属时间段的开始时间
            String extName = DateUtil.date2Str(startTime);
            //创建查询条件对象
            QueryWrapper<SeckillGoods> queryWrapper = new QueryWrapper<>();

            //设置查询条件
            //(1)商品必须审核通过  status=1
            queryWrapper.eq("status", "1");
            //(2)库存大于0
            queryWrapper.gt("stock_count", 0);
            //(3)数据库中商品秒杀开始时间 >= 本轮活动时段的开始时间
            queryWrapper.ge("start_time", DateUtil.date2StrFull(startTime));
            //(4)数据库中商品秒杀结束时间 < 本轮活动时段的开始时间+2小时, 即活动的结束时间
            queryWrapper.lt("end_time", DateUtil.date2StrFull(DateUtil.addDateHour(startTime, 2)));
            //(5)判断redis中是否已缓存过此商品:
            //若没缓存则添加缓存,并设置2小时后自动删除,因为整个秒杀时段就2小时
            //若缓存过就不应再覆盖缓存,否则会盖掉库存等信息,导致秒杀扣减的库存又被初始化了回来
            //key=SeckillGoods_2022081216，value=每个商品详情。
            Set keys = redisTemplate.boundHashOps("SeckillGoods_" + extName).keys();
            //判断keys不为空，说明redis缓存过，就设置排除条件
            if (keys != null && keys.size() > 0) {
                queryWrapper.notIn("id", keys);
            }
            //查询符合条件的数据库
            List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectList(queryWrapper);

            //从当前时间段开始，一共5个时间段，每个时间段有多个符合条件的商品
            //System.out.println("符合条件的数据: " + seckillGoodsList + "extName=" + extName);

            //遍历查询到数据集合,存储数据到redis
            for (SeckillGoods seckillGoods : seckillGoodsList) {
                redisTemplate.boundHashOps("SeckillGoods_" + extName).put(seckillGoods.getId(), seckillGoods);
                //设置超时时间2小时, 2小时后自动删除
                redisTemplate.expireAt("SeckillGoods_" + extName, DateUtil.addDateHour(startTime, 2));

                //商品数据队列存储,防止高并发超卖
                Long[] ids = pushIds(seckillGoods.getStockCount(), seckillGoods.getId());
                redisTemplate.boundListOps("SeckillGoodsCountList_" + seckillGoods.getId()).leftPushAll(ids);
                //自增计数器 - 防止库存余数混乱
                redisTemplate.boundHashOps("SeckillGoodsCount").increment(seckillGoods.getId(), seckillGoods.getStockCount());
            }
        }
    }

    /**
     * 根据库存量创建一个数组, 数组元素的值全部相同,都是当前商品的id.
     * 将商品ID存入到数组中
     *
     * @param len:长度
     * @param id:值
     * @return
     */
    public Long[] pushIds(int len, Long id) {
        Long[] ids = new Long[len];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = id;
        }
        return ids;
    }
}
                                                                                                                                                                                                                
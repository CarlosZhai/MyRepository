package com.offcn.canal.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.offcn.content.feign.ContentFeign;
import com.offcn.content.pojo.Content;
import com.offcn.entity.Result;
import com.offcn.item.feign.PageFeign;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.ListenPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

@CanalEventListener
public class CanalDataEventListener {

    ///***
    // * 增加数据监听
    // * @param eventType
    // * @param rowData
    // */
    //@InsertListenPoint
    //public void onEventInsert(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
    //    rowData.getAfterColumnsList().forEach((c) -> System.out.println("By--Annotation: " + c.getName() + " ::   " + c.getValue()));
    //   /*相当于
    //    for (CanalEntry.Column c : rowData.getAfterColumnsList()) {
    //        System.out.println("By--Annotation: " + c.getName() + " ::   " + c.getValue());
    //    }*/
    //}
    //
    ///***
    // * 修改数据监听
    // * @param rowData
    // */
    //@UpdateListenPoint
    //public void onEventUpdate(CanalEntry.RowData rowData) {
    //    System.out.println("UpdateListenPoint");
    //    rowData.getAfterColumnsList().forEach((c) -> System.out.println("By--Annotation: " + c.getName() + " ::   " + c.getValue()));
    //}
    //
    ///***
    // * 删除数据监听
    // * @param eventType
    // */
    //@DeleteListenPoint
    //public void onEventDelete(CanalEntry.EventType eventType) {
    //    System.out.println("DeleteListenPoint");
    //}
    //
    ///***
    // * 自定义数据修改监听
    // * @param eventType
    // * @param rowData
    // */
    //@ListenPoint(destination = "example", schema = "dongyimaidb", table = {"tb_content_category", "tb_content"}, eventType = CanalEntry.EventType.UPDATE)
    //public void onEventCustomUpdate(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
    //    System.out.println("ListenPoint");
    //    rowData.getAfterColumnsList().forEach((c) -> System.out.println("By--Annotation: " + c.getName() + " ::   " + c.getValue()));
    //}

    @Autowired
    private ContentFeign contentFeign;
    //字符串
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //自定义数据库的 操作来监听
    //destination = "example"
    @ListenPoint(destination = "example",
            schema = "dongyimaidb",
            table = {"tb_content", "tb_content_category"},
            eventType = {
                    CanalEntry.EventType.UPDATE,
                    CanalEntry.EventType.DELETE,
                    CanalEntry.EventType.INSERT})
    public void onEventCustomUpdate(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        //1.获取列名为category_id的值
        String categoryId = getColumnValue(eventType, rowData);
        //2.调用feign 获取该分类下的所有的广告集合
        Result<List<Content>> categoryresut = contentFeign.findByCategory(Long.valueOf(categoryId));
        List<Content> data = categoryresut.getData();
        //3.使用redisTemplate存储到redis中
        stringRedisTemplate.boundValueOps("content_" + categoryId).set(JSON.toJSONString(data));
    }

    private String getColumnValue(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        String categoryId = "";
        //判断 如果操作类型是删除  则获取beforlist
        if (eventType == CanalEntry.EventType.DELETE) {
            for (CanalEntry.Column column : rowData.getBeforeColumnsList()) {
                //如果category_id发生变化，就记录下来
                if (column.getName().equalsIgnoreCase("category_id")) {
                    categoryId = column.getValue();
                    return categoryId;
                }
            }
        } else {
            //判断 如果操作类型是添加 或者是更新 获取afterlist
            for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
                if (column.getName().equalsIgnoreCase("category_id")) {
                    categoryId = column.getValue();
                    return categoryId;
                }
            }
        }
        return categoryId;
    }

    @Autowired
    private PageFeign pageFeign;

    @ListenPoint(destination = "example",
            schema = "dongyimaidb",
            table = {"tb_goods"},
            eventType = {CanalEntry.EventType.UPDATE, CanalEntry.EventType.INSERT, CanalEntry.EventType.DELETE})
    public void onEventCustomSpu(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {

        //判断操作类型
        if (eventType == CanalEntry.EventType.DELETE) {
            String goodsId = "";
            List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
            for (CanalEntry.Column column : beforeColumnsList) {
                if (column.getName().equals("id")) {
                    goodsId = column.getValue();//goodsId
                    break;
                }
            }
            //todo 删除静态页

        } else {
            //新增 或者 更新
            List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
            String goodsId = "";
            for (CanalEntry.Column column : afterColumnsList) {
                if (column.getName().equals("id")) {
                    goodsId = column.getValue();
                    break;
                }
            }
            //更新 生成静态页
            pageFeign.createHtml(Long.valueOf(goodsId));
        }
    }
}

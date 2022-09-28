package com.offcn.sellergoods.controller;

import com.offcn.entity.PageResult;
import com.offcn.entity.Result;
import com.offcn.entity.StatusCode;
import com.offcn.sellergoods.group.GoodsEntity;
import com.offcn.sellergoods.pojo.Goods;
import com.offcn.sellergoods.service.GoodsService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "GoodsController")
@RestController
@RequestMapping("/goods")
@CrossOrigin
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    /***
     * Goods分页条件搜索实现
     * @param goods
     * @param page
     * @param size
     * @return
     */
    @ApiOperation(value = "Goods条件分页查询", notes = "分页条件查询Goods方法详情", tags = {"GoodsController"})
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "page", value = "当前页", required = true),
            @ApiImplicitParam(paramType = "path", name = "size", value = "每页显示条数", required = true)
    })
    @PostMapping(value = "/search/{page}/{size}")
    public Result<PageResult<Goods>> findPage(@RequestBody(required = false) @ApiParam(name = "Goods对象", value = "传入JSON数据", required = false) Goods goods, @PathVariable int page, @PathVariable int size) {
        //调用GoodsService实现分页条件查询Goods
        PageResult<Goods> pageResult = goodsService.findPage(goods, page, size);
        return new Result(true, StatusCode.OK, "查询成功", pageResult);
    }

    /***
     * Goods分页搜索实现
     * @param page:当前页
     * @param size:每页显示多少条
     * @return
     */
    @ApiOperation(value = "Goods分页查询", notes = "分页查询Goods方法详情", tags = {"GoodsController"})
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "page", value = "当前页", required = true),
            @ApiImplicitParam(paramType = "path", name = "size", value = "每页显示条数", required = true)
    })
    @GetMapping(value = "/search/{page}/{size}")
    public Result<PageResult<Goods>> findPage(@PathVariable int page, @PathVariable int size) {
        //调用GoodsService实现分页查询Goods
        PageResult<Goods> pageResult = goodsService.findPage(page, size);
        return new Result<PageResult<Goods>>(true, StatusCode.OK, "查询成功", pageResult);
    }

    /***
     * 多条件搜索品牌数据
     * @param goods
     * @return
     */
    @ApiOperation(value = "Goods条件查询", notes = "条件查询Goods方法详情", tags = {"GoodsController"})
    @PostMapping(value = "/search")
    public Result<List<Goods>> findList(@RequestBody(required = false) @ApiParam(name = "Goods对象", value = "传入JSON数据", required = false) Goods goods) {
        //调用GoodsService实现条件查询Goods
        List<Goods> list = goodsService.findList(goods);
        return new Result<List<Goods>>(true, StatusCode.OK, "查询成功", list);
    }

    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @ApiOperation(value = "Goods根据ID删除", notes = "根据ID删除Goods方法详情", tags = {"GoodsController"})
    @ApiImplicitParam(paramType = "path", name = "id", value = "主键ID", required = true, dataType = "Long")
    @DeleteMapping(value = "/{id}")
    public Result delete(@PathVariable Long id) {
        //调用GoodsService实现根据主键删除
        goodsService.delete(id);
        return new Result(true, StatusCode.OK, "删除成功");
    }

    /***
     * 修改Goods数据
     * @param goodsEntity
     * @param id
     * @return
     */
    @ApiOperation(value = "Goods根据ID修改", notes = "根据ID修改Goods⽅法详情", tags = {"GoodsController"})
    @ApiImplicitParam(paramType = "path", name = "id", value = "主键ID", required = true, dataType = "Long")
    @PutMapping(value = "/{id}")
    public Result update(@RequestBody @ApiParam(name = "Goods复合实体对象", value = "传⼊JSON数据", required = false) GoodsEntity goodsEntity, @PathVariable Long id) {
        //设置主键值
        goodsEntity.getGoods().setId(id);
        //调⽤GoodsService实现修改Goods
        goodsService.update(goodsEntity);
        return new Result(true, StatusCode.OK, "修改成功");
    }

    /***
     * 新增Goods数据
     * @param goodsEntity
     * @return
     */
    @ApiOperation(value = "Goods添加", notes = "添加Goods⽅法详情", tags = {"GoodsController"})
    @PostMapping
    public Result add(@RequestBody @ApiParam(name = "Goods复合实体对象", value = "传⼊JSON数据", required = true) GoodsEntity goodsEntity) {
        //调⽤GoodsService实现添加Goods
        goodsService.add(goodsEntity);
        return new Result(true, StatusCode.OK, "添加成功");
    }

    /***
     * 根据ID查询Goods数据
     * @param id
     * @return
     */
    @ApiOperation(value = "Goods根据ID查询", notes = "根据ID查询Goods⽅法详情", tags = {"GoodsController"})
    @ApiImplicitParam(paramType = "path", name = "id", value = "主键ID", required = true, dataType = "Long")
    @GetMapping("/{id}")
    public Result<GoodsEntity> findById(@PathVariable Long id) {
        //调⽤GoodsService实现根据主键查询Goods
        GoodsEntity goodsEntity = goodsService.findById(id);
        return new Result<GoodsEntity>(true, StatusCode.OK, "查询成功", goodsEntity);
    }

    /***
     * 查询Goods全部数据
     * @return
     */
    @ApiOperation(value = "查询所有Goods", notes = "查询所Goods有方法详情", tags = {"GoodsController"})
    @GetMapping
    public Result<List<Goods>> findAll() {
        //调用GoodsService实现查询所有Goods
        List<Goods> list = goodsService.findAll();
        return new Result<List<Goods>>(true, StatusCode.OK, "查询成功", list);
    }

    /**
     * 审核
     *
     * @param id
     * @return
     */
    @PutMapping("/audit/{id}")
    public Result audit(@PathVariable Long id) {
        goodsService.audit(id);
        return new Result(true, StatusCode.OK, "审核成功");
    }

    /**
     * 下架
     *
     * @param id
     * @return
     */
    @PutMapping("/pull/{id}")
    public Result pull(@PathVariable Long id) {
        goodsService.pull(id);
        return new Result(true, StatusCode.OK, "下架成功");
    }

    /**
     * 商品上架
     *
     * @param id
     * @return
     */
    @PutMapping("/put/{id}")
    public Result put(@PathVariable Long id) {
        goodsService.put(id);
        return new Result(true, StatusCode.OK, "上架成功");
    }

    /**
     * 批量上架
     *
     * @param ids
     * @return
     */
    @PutMapping("/put/many")
    public Result putMany(@RequestBody Long[] ids) {
        int count = goodsService.putMany(ids);
        return new Result(true, StatusCode.OK, "上架" + count + "个商品");
    }
}

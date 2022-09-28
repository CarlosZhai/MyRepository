package com.offcn.sellergoods.controller;

import com.offcn.entity.PageResult;
import com.offcn.entity.Result;
import com.offcn.entity.StatusCode;
import com.offcn.sellergoods.pojo.Brand;
import com.offcn.sellergoods.service.BrandService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
@CrossOrigin//跨域，服务器保护行为
public class BrandController {
    @Autowired
    private BrandService brandService;

    @GetMapping //不写value值默认调⽤根路径
    public Result<List<Brand>> findAll() {
        List<Brand> brandList = brandService.findAll();
        return new Result<List<Brand>>(true, StatusCode.OK, "查询成功", brandList);
    }

    @GetMapping("/{id}")
    public Result<Brand> findById(@PathVariable Long id) {
        //调⽤BrandService实现根据主键查询Brand
        Brand brand = brandService.findById(id);
        return new Result<Brand>(true, StatusCode.OK, "查询成功", brand);
    }

    @PostMapping
    public Result add(@RequestBody Brand brand) {
        //调⽤BrandService实现添加Brand
        brandService.add(brand);
        return new Result(true, StatusCode.OK, "添加成功");
    }

    @PutMapping(value = "/{id}")
    public Result update(@RequestBody Brand brand, @PathVariable Long id) {
        //设置主键值
        brand.setId(id);
        //调⽤BrandService实现修改Brand
        brandService.update(brand);
        return new Result(true, StatusCode.OK, "修改成功");
    }

    @DeleteMapping(value = "/{id}")
    public Result delete(@PathVariable Long id) {
        //调⽤BrandService实现根据主键删除
        brandService.delete(id);
        return new Result(true, StatusCode.OK, "删除成功");
    }

    @PostMapping(value = "/search")
    public Result<List<Brand>> findList(@RequestBody(required = false) Brand brand) {
        List<Brand> list = brandService.findList(brand);
        return new Result<List<Brand>>(true, StatusCode.OK, "查询成功", list);
    }

    @GetMapping(value = "/search/{page}/{size}")
    public Result<PageResult<Brand>> findPage(@PathVariable int page, @PathVariable int size) {
        //调⽤BrandService实现分⻚查询Brand
        PageResult<Brand> pageResult = brandService.findPage(page, size);
        return new Result<PageResult<Brand>>(true, StatusCode.OK, "查询成功", pageResult);
    }

    @PostMapping(value = "/search/{page}/{size}")
    public Result<PageResult> findPage(@RequestBody(required = false) Brand brand, @PathVariable int page, @PathVariable int size) {
        //执⾏搜索
        PageResult<Brand> pageResult = brandService.findPage(brand, page, size);
        return new Result(true, StatusCode.OK, "查询成功", pageResult);
    }

    /**
     *
     * @return http封装的响应,泛型不能是Brand，因为前端需要的key为"text",而Brand里面的属性是"name"
     */
    @ApiOperation(value = "查询品牌下拉列表",notes = "查询品牌下拉列表",tags = {"BrandController"})
    @GetMapping("/selectOptions")
    public ResponseEntity<List<Map>> selectOptions() {
        return ResponseEntity.ok(brandService.selectOptions());
    }
}

package com.offcn.search.controller;

import com.offcn.entity.Result;
import com.offcn.entity.StatusCode;
import com.offcn.search.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/search")
@CrossOrigin
public class SkuController {

    @Autowired
    private SkuService skuService;

    /**
     * 导入数据到es
     */
    @GetMapping("/import")
    public Result search() {
        skuService.importSku();
        return new Result(true, StatusCode.OK, "导入数据到索引库成功！");
    }

    /**
     * 根据品牌、分类、规格等关键字搜索
     */
    /* @PostMapping //传入的是复杂类型数据结构，所以需要用Post方式*/
    @GetMapping
    public Map search(@RequestParam(required = false) Map searchMap) {
        return skuService.search(searchMap);
    }
}

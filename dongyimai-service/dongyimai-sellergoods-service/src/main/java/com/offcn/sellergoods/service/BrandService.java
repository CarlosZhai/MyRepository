package com.offcn.sellergoods.service;

import com.offcn.entity.PageResult;
import com.offcn.sellergoods.pojo.Brand;

import java.util.List;
import java.util.Map;

public interface BrandService {
    /***
     * 查询所有品牌
     * @return
     */
    List<Brand> findAll();

    Brand findById(Long id);

    void add(Brand brand);

    void update(Brand brand);

    void delete(Long id);

    List<Brand> findList(Brand brand);

    PageResult<Brand> findPage(int page, int size);

    PageResult<Brand> findPage(Brand brand, int page, int size);

    List<Map> selectOptions();
}

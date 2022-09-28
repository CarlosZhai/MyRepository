package com.offcn.sellergoods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.offcn.entity.PageResult;
import com.offcn.sellergoods.dao.BrandMapper;
import com.offcn.sellergoods.pojo.Brand;
import com.offcn.sellergoods.service.BrandService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class BrandServiceImpl extends ServiceImpl<BrandMapper, Brand> implements BrandService {

    @Override
    public List<Brand> findAll() {
        return this.list();
    }

    @Override
    public Brand findById(Long id) {
        return this.getById(id);
    }

    @Override
    public void add(Brand brand) {
        this.save(brand);
    }

    @Override
    public void update(Brand brand) {
        this.updateById(brand);
    }

    @Override
    public void delete(Long id) {
        this.removeById(id);
    }

    /**
     * Brand条件查询
     *
     * @param brand
     * @return
     */
    @Override
    public List<Brand> findList(Brand brand) {
//构建查询条件
        QueryWrapper<Brand> queryWrapper = this.createQueryWrapper(brand);
//根据构建的条件查询数据
        return this.list(queryWrapper);
    }

    /**
     * Brand构建查询对象
     *
     * @param brand
     * @return
     */
    private QueryWrapper<Brand> createQueryWrapper(Brand brand) {
        QueryWrapper<Brand> queryWrapper = new QueryWrapper<>();
        if (brand != null) {
            if (brand.getId() != null) {
                queryWrapper.eq("id", brand.getId());
            }
            // 品牌名称
            if (!StringUtils.isEmpty(brand.getName())) {
                queryWrapper.like("name", brand.getName());
            }
            // 品牌⾸字⺟
            if (!StringUtils.isEmpty(brand.getFirstChar())) {
                queryWrapper.eq("first_char", brand.getFirstChar());
            }
            // 品牌图像
            if (!StringUtils.isEmpty(brand.getImage())) {
                queryWrapper.eq("image", brand.getImage());
            }
        }
        return queryWrapper;
    }

    @Override
    public PageResult<Brand> findPage(int page, int size) {
        Page<Brand> mypage = new Page<>(page, size);
        IPage<Brand> iPage = this.page(mypage, new QueryWrapper<Brand>());
        return new PageResult<Brand>(iPage.getTotal(), iPage.getRecords());
    }

    @Override
    public PageResult<Brand> findPage(Brand brand, int page, int size) {
        Page<Brand> mypage = new Page<>(page, size);
        QueryWrapper<Brand> queryWrapper = this.createQueryWrapper(brand);
        IPage<Brand> iPage = this.page(mypage, queryWrapper);
        return new PageResult<Brand>(iPage.getTotal(), iPage.getRecords());
    }

    @Autowired
    private BrandMapper brandMapper;
    @Override
    public List<Map> selectOptions() {
        return brandMapper.selectOptions();
    }
}

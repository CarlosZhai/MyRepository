package com.offcn.sellergoods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.offcn.entity.PageResult;
import com.offcn.sellergoods.dao.SpecificationMapper;
import com.offcn.sellergoods.dao.SpecificationOptionMapper;
import com.offcn.sellergoods.group.SpecEntity;
import com.offcn.sellergoods.pojo.Specification;
import com.offcn.sellergoods.pojo.SpecificationOption;
import com.offcn.sellergoods.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/****
 * @Author:ujiuye
 * @Description:Specification业务层接口实现类
 * @Date 2021/2/1 14:19
 *****/
@Service
public class SpecificationServiceImpl extends ServiceImpl<SpecificationMapper, Specification> implements SpecificationService {

    @Autowired
    private SpecificationOptionMapper specificationOptionMapper;
    @Autowired
    private SpecificationMapper specificationMapper;

    /**
     * Specification条件+分页查询
     *
     * @param specification 查询条件
     * @param page          页码
     * @param size          页大小
     * @return 分页结果
     */
    @Override
    public PageResult<Specification> findPage(Specification specification, int page, int size) {
        Page<Specification> mypage = new Page<>(page, size);
        QueryWrapper<Specification> queryWrapper = this.createQueryWrapper(specification);
        IPage<Specification> iPage = this.page(mypage, queryWrapper);
        return new PageResult<Specification>(iPage.getTotal(), iPage.getRecords());
    }

    /**
     * Specification分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResult<Specification> findPage(int page, int size) {
        Page<Specification> mypage = new Page<>(page, size);
        IPage<Specification> iPage = this.page(mypage, new QueryWrapper<Specification>());

        return new PageResult<Specification>(iPage.getTotal(), iPage.getRecords());
    }

    /**
     * Specification条件查询
     *
     * @param specification
     * @return
     */
    @Override
    public List<Specification> findList(Specification specification) {
        //构建查询条件
        QueryWrapper<Specification> queryWrapper = this.createQueryWrapper(specification);
        //根据构建的条件查询数据
        return this.list(queryWrapper);
    }


    /**
     * Specification构建查询对象
     *
     * @param specification
     * @return
     */
    public QueryWrapper<Specification> createQueryWrapper(Specification specification) {
        QueryWrapper<Specification> queryWrapper = new QueryWrapper<>();
        if (specification != null) {
            // 主键
            if (!StringUtils.isEmpty(specification.getId())) {
                queryWrapper.eq("id", specification.getId());
            }
            // 名称
            if (!StringUtils.isEmpty(specification.getSpecName())) {
                queryWrapper.eq("spec_name", specification.getSpecName());
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

        QueryWrapper<SpecificationOption> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("spec_id", id);
        specificationOptionMapper.delete(queryWrapper);
    }

    /**
     * 修改Specification
     *
     * @param specification
     */
    @Override
    public void update(Specification specification) {
        this.updateById(specification);
    }

    /**
     * 增加Specification
     *
     * @param specification
     */
    @Override
    public void add(Specification specification) {
        this.save(specification);
    }

    /**
     * 根据ID查询Specification
     *
     * @param id
     * @return
     */

    @Override
    public SpecEntity findById(Long id) {
        //查到规格
        Specification specification = specificationMapper.selectById(id);
        QueryWrapper<SpecificationOption> queryWrapper = new QueryWrapper<>();
        //根据规格id查到所有对应的规格选项
        queryWrapper.eq("spec_id", id);
        List<SpecificationOption> specificationOptionList = specificationOptionMapper.selectList(queryWrapper);
        //将规格、规格选项封装进specEntity
        SpecEntity specEntity = new SpecEntity();
        specEntity.setSpecification(specification);
        specEntity.setSpecificationOptionList(specificationOptionList);
        return specEntity;
    }

    /**
     * 查询Specification全部数据
     *
     * @return
     */
    @Override
    public List<Specification> findAll() {
        return this.list(new QueryWrapper<Specification>());
    }

    @Override
    public void add(SpecEntity specEntity) {
        //保存规格
        specificationMapper.insert(specEntity.getSpecification());
        //保存规格选项（先获取规格选项）
        List<SpecificationOption> specificationOptionList = specEntity.getSpecificationOptionList();
        if (null != specificationOptionList && specificationOptionList.size() > 0) {
            for (SpecificationOption specificationOption : specificationOptionList) {
                //向规格选项中设置规格ID
                specificationOption.setSpecId(specEntity.getSpecification().getId());
                //保存规格选项
                specificationOptionMapper.insert(specificationOption);
            }
        }
    }

    @Override
    public void update(SpecEntity specEntity) {
        //1.修改规格对象
        specificationMapper.updateById(specEntity.getSpecification());
        //2.根据规格id先删除规格选项集合
        QueryWrapper<SpecificationOption> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("spec_id", specEntity.getSpecification().getId());
        specificationOptionMapper.delete(queryWrapper);
        //3.重新插入规格选项
        List<SpecificationOption> specificationOptionList = specEntity.getSpecificationOptionList();
        if (!CollectionUtils.isEmpty(specificationOptionList)) {
            for (SpecificationOption specificationOption : specificationOptionList) {
                //先设置规格选项对应的规格id（如果不设置，新增规格选项时就没有规格id），再插入
                specificationOption.setSpecId(specEntity.getSpecification().getId());
                specificationOptionMapper.insert(specificationOption);
            }
        }
    }

    @Override
    public List<Map> selectOptions() {
        return specificationMapper.selectOptions();
    }
}

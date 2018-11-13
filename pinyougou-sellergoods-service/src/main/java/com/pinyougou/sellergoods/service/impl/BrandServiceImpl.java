package com.pinyougou.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.pojo.TbBrandExample.Criteria;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;

@Service
public class BrandServiceImpl implements BrandService {

	@Autowired
	private TbBrandMapper brandMapper;

	@Override
	public List<TbBrand> findBrandAll() throws Exception {
		return brandMapper.selectByExample(null);
	}

	/**
	 * 分页查询
	 */
	@Override
	public PageResult<TbBrand> findBrandPage(Integer pageIndex, Integer pageSize) throws Exception {

		// 使用MyBatis进行分页查询
		PageHelper.startPage(pageIndex, pageSize);

		List<TbBrand> brandList = brandMapper.selectByExample(null);

		PageInfo<TbBrand> pageInfo = new PageInfo<>(brandList);

		return new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
	}
	
	
	/**
	 * 分页查询+条件
	 */
	@Override
	public PageResult<TbBrand> findBrandPage(Integer pageIndex, Integer pageSize, TbBrand brand) throws Exception {
		// 使用MyBatis进行分页查询
		PageHelper.startPage(pageIndex, pageSize);

		// mybatis提供的条件查询类
		TbBrandExample example = new TbBrandExample();
		Criteria criteria = example.createCriteria();

		// 添加条件
		if (brand.getName() != null && !"".equals(brand.getName().trim())) {
			criteria.andNameLike("%" + brand.getName() + "%");
		}

		if (brand.getFirstChar() != null && !"".equals(brand.getFirstChar().trim())) {
			criteria.andFirstCharLike("%" + brand.getFirstChar() + "%");
		}
		
		
		List<TbBrand> brandList = brandMapper.selectByExample(example);

		PageInfo<TbBrand> pageInfo = new PageInfo<>(brandList);

		return new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
	}

	@Override
	public boolean saveBrand(TbBrand brand) throws Exception {
		return brandMapper.insert(brand) > 0;
	}

	@Override
	public Long findBrandByNameCount(String name) throws Exception {
		return brandMapper.findBrandByNameCount(name);
	}

	@Override
	public TbBrand findBrandOne(Long id) throws Exception {
		return brandMapper.selectByPrimaryKey(id);
	}

	@Override
	public boolean updateBrand(TbBrand brand) throws Exception {
		return brandMapper.updateByPrimaryKey(brand) > 0;
	}

	@Override
	public boolean deleteBatchBrand(Long[] ids) throws Exception {
		return brandMapper.deleteByBatchPrimaryKey(ids) > 0;
	}

	@Override
	public List<Map<String, Object>> selectOptionList() {
		return brandMapper.selectOptionList();
	}
}

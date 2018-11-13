package com.pinyougou.sellergoods.service;

import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbBrand;

import entity.PageResult;

/**
 * 品牌接口
 * 
 * @author cy
 *
 */
public interface BrandService {
	/**
	 * 查询所有的品牌
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<TbBrand> findBrandAll() throws Exception;

	/**
	 * 分页查询
	 * 
	 * @param pageIndex 当前页码
	 * @param pageSize  每页显示的条数
	 * @return
	 * @throws Exception
	 */
	public PageResult<TbBrand> findBrandPage(Integer pageIndex, Integer pageSize) throws Exception;
	
	/**
	 * 条件查询
	 * @param pageIndex
	 * @param pageSize
	 * @param brand
	 * @return
	 * @throws Exception
	 */
	public PageResult<TbBrand> findBrandPage(Integer pageIndex, Integer pageSize,TbBrand brand ) throws Exception;

	/**
	 * 添加
	 * 
	 * @param brand
	 * @return
	 * @throws Exception
	 */
	public boolean saveBrand(TbBrand brand) throws Exception;

	/**
	 * 根据名称查询
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public Long findBrandByNameCount(String name) throws Exception;

	/**
	 * 根据ID查询
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public TbBrand findBrandOne(Long id) throws Exception;

	/**
	 * 更新
	 * @param brand
	 * @return
	 * @throws Exception
	 */
	public boolean updateBrand(TbBrand brand) throws Exception;
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 * @throws Exception
	 */
	public boolean deleteBatchBrand(Long[] ids) throws Exception;
	
	/**
	 * 返回下列列表的品牌数据
	 * @return
	 */
	public List<Map<String, Object>> selectOptionList();

}

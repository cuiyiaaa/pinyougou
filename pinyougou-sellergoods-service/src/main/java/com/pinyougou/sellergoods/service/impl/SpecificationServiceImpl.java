package com.pinyougou.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSpecificationMapper;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationExample;
import com.pinyougou.pojo.TbSpecificationExample.Criteria;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import com.pinyougou.pojogroup.Specification;
import com.pinyougou.sellergoods.service.SpecificationService;

import entity.PageResult;

/**
 * 服务实现层s
 * 
 * @author Administrator
 *
 */
@Service
public class SpecificationServiceImpl implements SpecificationService {

	// 规格
	@Autowired
	private TbSpecificationMapper specificationMapper;

	// 规格选项
	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbSpecification> findAll() {
		return specificationMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult<TbSpecification> findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbSpecification> page = (Page<TbSpecification>) specificationMapper.selectByExample(null);
		return new PageResult<TbSpecification>(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Specification specification) {
		// 获取规格实体
		TbSpecification tbSpecification = specification.getSpecification();

		// 添加规格信息，添加完成后会返回主键
		specificationMapper.insert(tbSpecification);

		// 将规格实体的主键设置到规格选项
		List<TbSpecificationOption> list = specification.getSpecificationOptionsList();
		if (list != null && list.size() > 0) {
			for (TbSpecificationOption sp : list) {
				sp.setSpecId(tbSpecification.getId());
				specificationOptionMapper.insert(sp);
			}
		}
	}

	/**
	 * 修改
	 */
	@Override
	public void update(Specification specification) {
			
		TbSpecification tbSpecification = specification.getSpecification();
		
		// 更新规格信息
		specificationMapper.updateByPrimaryKey(tbSpecification);

		//更新规格详细信息，需要将所有的先删除掉，在添加，因为在页面中你不知道你添加还是删除
		
		//构建删除条件
		TbSpecificationOptionExample example = new TbSpecificationOptionExample();
		com.pinyougou.pojo.TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
		criteria.andSpecIdEqualTo(tbSpecification.getId());
		//删除
		specificationOptionMapper.deleteByExample(example);
		
		List<TbSpecificationOption> specificationOptionsList = specification.getSpecificationOptionsList();
		//添加
		if (specificationOptionsList != null && specificationOptionsList.size() > 0) {
			for (TbSpecificationOption sp : specificationOptionsList) {
				sp.setSpecId(tbSpecification.getId());
				specificationOptionMapper.insert(sp);
			}
		}
	}

	/**
	 * 根据ID获取实体
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public Specification findOne(Long id) {
		Specification specification = new Specification();
		// 查询规格信息
		TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);
		specification.setSpecification(tbSpecification);

		// 查询规格详细信息
		TbSpecificationOptionExample example = new TbSpecificationOptionExample();
		com.pinyougou.pojo.TbSpecificationOptionExample.Criteria criteria = example.createCriteria();

		// 设置查询条件
		criteria.andSpecIdEqualTo(id);
		
		// 查询
		List<TbSpecificationOption> specificationOption = specificationOptionMapper.selectByExample(example);
		specification.setSpecificationOptionsList(specificationOption);

		return specification;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for (Long id : ids) {
			specificationMapper.deleteByPrimaryKey(id);
		}
	}

	@Override
	public PageResult<TbSpecification> findPage(TbSpecification specification, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbSpecificationExample example = new TbSpecificationExample();
		Criteria criteria = example.createCriteria();

		if (specification != null) {
			if (specification.getSpecName() != null && specification.getSpecName().length() > 0) {
				criteria.andSpecNameLike("%" + specification.getSpecName() + "%");
			}
		}

		Page<TbSpecification> page = (Page<TbSpecification>) specificationMapper.selectByExample(example);
		return new PageResult<TbSpecification>(page.getTotal(), page.getResult());
	}

	@Override
	public List<Map<String, Object>> selectOptionList() {
		return specificationMapper.selectOptionList();
	}

}

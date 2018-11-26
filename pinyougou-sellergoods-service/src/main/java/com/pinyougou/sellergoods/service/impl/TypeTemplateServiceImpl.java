package com.pinyougou.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.mapper.TbTypeTemplateMapper;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import com.pinyougou.pojo.TbTypeTemplate;
import com.pinyougou.pojo.TbTypeTemplateExample;
import com.pinyougou.pojo.TbTypeTemplateExample.Criteria;
import com.pinyougou.sellergoods.service.TypeTemplateService;

import entity.PageResult;

/**
 * 服务实现层
 * 
 * @author Administrator
 *
 */
@Service
@SuppressWarnings("all")
@Transactional
public class TypeTemplateServiceImpl implements TypeTemplateService {

	@Autowired
	private TbTypeTemplateMapper typeTemplateMapper;
	@Autowired
	private TbSpecificationOptionMapper specMapper;
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbTypeTemplate> findAll() {
		return typeTemplateMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult<TbTypeTemplate> findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbTypeTemplate> page = (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(null);
		return new PageResult<TbTypeTemplate>(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbTypeTemplate typeTemplate) {
		typeTemplateMapper.insert(typeTemplate);
	}

	/**
	 * 修改
	 */
	@Override
	public void update(TbTypeTemplate typeTemplate) {
		typeTemplateMapper.updateByPrimaryKey(typeTemplate);
	}

	/**
	 * 根据ID获取实体
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public TbTypeTemplate findOne(Long id) {
		return typeTemplateMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for (Long id : ids) {
			typeTemplateMapper.deleteByPrimaryKey(id);
		}
	}

	@Override
	public PageResult<TbTypeTemplate> findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbTypeTemplateExample example = new TbTypeTemplateExample();
		Criteria criteria = example.createCriteria();

		if (typeTemplate != null) {
			if (typeTemplate.getName() != null && typeTemplate.getName().length() > 0) {
				criteria.andNameLike("%" + typeTemplate.getName() + "%");
			}
			if (typeTemplate.getSpecIds() != null && typeTemplate.getSpecIds().length() > 0) {
				criteria.andSpecIdsLike("%" + typeTemplate.getSpecIds() + "%");
			}
			if (typeTemplate.getBrandIds() != null && typeTemplate.getBrandIds().length() > 0) {
				criteria.andBrandIdsLike("%" + typeTemplate.getBrandIds() + "%");
			}
			if (typeTemplate.getCustomAttributeItems() != null && typeTemplate.getCustomAttributeItems().length() > 0) {
				criteria.andCustomAttributeItemsLike("%" + typeTemplate.getCustomAttributeItems() + "%");
			}

		}

		Page<TbTypeTemplate> page = (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(example);
		
		cacheBrandAndSpec();
		
		return new PageResult<TbTypeTemplate>(page.getTotal(), page.getResult());
	}

	/**
	 * 缓存模板中的品牌和规格
	 */
	private void cacheBrandAndSpec() {
		//查询所有的模板
		List<TbTypeTemplate> templateList = findAll();
		
		for (TbTypeTemplate template : templateList) {
			//缓存品牌，以模板id为key 对应的品牌为value
			List<Map> brandList = JSON.parseArray(template.getBrandIds(), Map.class);
			redisTemplate.boundHashOps("brandList").put(template.getId(), brandList);
			
			//缓存规格，以模板id为key 对应的规格详细信息为value
			List<Map> specList = findSpecList(template.getId());
			redisTemplate.boundHashOps("specList").put(template.getId(), specList);	
		}
		System.out.println("缓存品牌和规格");
	}

	@Override
	public List<Map<String, Object>> selectOptionList() {
		return typeTemplateMapper.selectOptionList();
	}

	@Override
	public List<Map> findSpecList(Long id) {
		// 查询模板
		TbTypeTemplate typeTemplate = typeTemplateMapper.selectByPrimaryKey(id);

		List<Map> list = JSON.parseArray(typeTemplate.getSpecIds(), Map.class);

		for (Map map : list) {

			// 查询规格选项列表
			TbSpecificationOptionExample example = new TbSpecificationOptionExample();
			com.pinyougou.pojo.TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
			criteria.andSpecIdEqualTo(new Long((Integer) map.get("id")));
			List<TbSpecificationOption> options = specMapper.selectByExample(example);

			map.put("options", options); //{"id":"27","text":"网络","options":[{id:1,optionName:'2g'},{id:1,optionName:'3g'}...]}
		}
		
		return list;
	}
}


















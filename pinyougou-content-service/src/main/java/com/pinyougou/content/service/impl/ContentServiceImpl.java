package com.pinyougou.content.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.content.service.ContentService;
import com.pinyougou.mapper.TbContentMapper;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.pojo.TbContentExample;
import com.pinyougou.pojo.TbContentExample.Criteria;

import entity.PageResult;

/**
 * 服务实现层
 * 
 * @author Administrator
 *
 */
@Service
@SuppressWarnings("all")
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult<TbContent> findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbContent> page = (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult<TbContent>(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
		contentMapper.insert(content);
		updateCache(content.getCategoryId());
	}

	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content) {
		// 更新前
		TbContent tbContent = contentMapper.selectByPrimaryKey(content.getId());
		Long categoryId = tbContent.getCategoryId();
		updateCache(categoryId);

		contentMapper.updateByPrimaryKey(content);

		// 更新后
		if (categoryId.longValue() != content.getCategoryId().longValue()) {
			updateCache(content.getCategoryId());
		}
	}

	/**
	 * 根据ID获取实体
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id) {
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for (Long id : ids) {

			TbContent tbContent = contentMapper.selectByPrimaryKey(id);
			Long categoryId = tbContent.getCategoryId();
			updateCache(categoryId);

			contentMapper.deleteByPrimaryKey(id);
		}
	}

	@Override
	public PageResult<TbContent> findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbContentExample example = new TbContentExample();
		Criteria criteria = example.createCriteria();

		if (content != null) {
			if (content.getTitle() != null && content.getTitle().length() > 0) {
				criteria.andTitleLike("%" + content.getTitle() + "%");
			}
			if (content.getUrl() != null && content.getUrl().length() > 0) {
				criteria.andUrlLike("%" + content.getUrl() + "%");
			}
			if (content.getPic() != null && content.getPic().length() > 0) {
				criteria.andPicLike("%" + content.getPic() + "%");
			}
			if (content.getStatus() != null && content.getStatus().length() > 0) {
				criteria.andStatusLike("%" + content.getStatus() + "%");
			}
		}

		Page<TbContent> page = (Page<TbContent>) contentMapper.selectByExample(example);
		return new PageResult<TbContent>(page.getTotal(), page.getResult());
	}

	@Override
	public List<TbContent> findByCategoryId(Long id) {

		/**
		 * content:{ "1":首页广告 "2":楼层广告 "3":今日推荐 }
		 */
		// 从缓存中获取数据
		List<TbContent> list = (List<TbContent>) redisTemplate.boundHashOps("content").get(id);

		// 如果缓存中没有数据，则从数据库获取，获取之后在放入缓存中
		if (list == null) {
			TbContentExample example = new TbContentExample();
			Criteria criteria = example.createCriteria();
			criteria.andCategoryIdEqualTo(id);
			// 查询启用的
			criteria.andStatusEqualTo("1");
			// 排序
			example.setOrderByClause("sort_order");
			list = contentMapper.selectByExample(example);
			
			// 放入缓存中
			redisTemplate.boundHashOps("content").put(id, list);
		}

		return list;
	}

	private void updateCache(Long key) {
		redisTemplate.boundHashOps("content").delete(key);
	}
}

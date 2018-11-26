package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFacetQuery;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightEntry.Highlight;
import org.springframework.data.solr.core.query.result.HighlightPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Service(timeout = 5000)
@SuppressWarnings("all")
public class ItemSearchServiceImpl implements ItemSearchService {

	private static final String List = null;

	@Autowired
	private SolrTemplate solrTemplate;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Override
	public Map<String, Object> search(Map<String, Object> searchMap) {
		Map<String, Object> result = new HashMap<>();

		if ("".equals(searchMap.get("keywords"))) {
			return result;
		}

		// 去除关键字间的空格
		result.put("keywords", String.valueOf(searchMap.get("keywords")).replace(" ", ""));

		// 查询商品信息
		result.putAll(searchList(searchMap));

		// 查询品牌列表
		result.put("categoryList", searchCategoryList(searchMap));

		// 查询品牌和规格
		List<String> categoryList = (List<String>) result.get("categoryList");

		// 如果用户选择了品牌，则根据品牌选择的品牌查询，如果没有选择，则显示第一个品牌
		if (searchMap.get("category") != null && !"".equals(searchMap.get("category"))) {
			result.putAll(searchBrandAndSpec(String.valueOf(searchMap.get("category"))));
		} else {
			// 如果有多个商品分类，则只显示第一个商品分类的信息
			if (categoryList.size() > 0) {
				Map<String, Object> brandAndSpecMap = searchBrandAndSpec(categoryList.get(0));
				result.putAll(brandAndSpecMap);
			}
		}
		return result;
	}

	/**
	 * 根据搜索条件查询对应商品信息，并将关键字高亮显示
	 * 
	 * @param searchMap 搜索的条件
	 * @return 返回搜索的查询结果
	 */
	private Map<String, Object> searchList(Map<String, Object> searchMap) {

		// 存储查询结果
		Map<String, Object> result = new HashMap<>();

		// 高亮设置
		HighlightQuery query = new SimpleHighlightQuery();
		HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");// 高亮域
		// 设置高亮文字的处于那个html标签下
		highlightOptions.setSimplePrefix("<font style='color:#CA1623'>");
		highlightOptions.setSimplePostfix("</font>");
		query.setHighlightOptions(highlightOptions);

		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		// 设置查询条件
		query.addCriteria(criteria);

		// 对查询进行过滤
		filter(searchMap, query);

		// 设置分页条件
		Integer pageIndex = Integer.valueOf(String.valueOf(searchMap.get("page")));
		if (pageIndex == null) {
			pageIndex = 1;
		}

		Integer pageSize = Integer.valueOf(String.valueOf(searchMap.get("size")));
		if (pageSize == null) {
			pageSize = 20;
		}

		// 设置起始位置
		query.setOffset((pageIndex - 1) * pageSize);
		// 每页多少条
		query.setRows(pageSize);

		// 设置排序
		if (searchMap.get("sort") != null && !"".equals(searchMap.get("sort"))) {
			String sortField = String.valueOf(searchMap.get("sortField")); // 排序字段

			// 降序
			if ("DESC".equals(searchMap.get("sort"))) {
				// 参数1：枚举，升序还是降序 参数2：排序字段
				Sort sort = new Sort(Direction.DESC, "item_"+sortField);
				query.addSort(sort);
			}
			//升序
			if ("ASC".equals(searchMap.get("sort"))) {
				Sort sort = new Sort(Direction.ASC, "item_"+sortField);
				query.addSort(sort);
			}
		}

		

		// 高亮结果集，存储了分页的信息】
		HighlightPage<TbItem> highlightPage = solrTemplate.queryForHighlightPage(query, TbItem.class);

		// 每条记录的高亮入口，存储的是每一条记录
		List<HighlightEntry<TbItem>> entryList = highlightPage.getHighlighted();

		// 每个entry都代表一条查询出来的记录
		for (HighlightEntry<TbItem> entry : entryList) {

			// 获取高亮的列表(item_title)，与高亮域的个数有关
			List<Highlight> highlights = entry.getHighlights();

			/**
			 * highlights.get(0).getSnipplets(): 获取的是每个高亮列表中的值
			 * <font style='color:red'>三星</font> Note II (N7100)
			 */
			if (highlights.size() > 0 && highlights.get(0).getSnipplets().size() > 0) {
				/**
				 * item 与 highlightPage.getContent()里的每一条记录的引用是相同的，
				 * 相当于修改了item就相当于修改了getContent()里的数据
				 */
				TbItem item = entry.getEntity();
				item.setTitle(highlights.get(0).getSnipplets().get(0));
			}
		}

		// 分页后的结果集
		result.put("rows", highlightPage.getContent());
		// 总记录数
		result.put("total", highlightPage.getTotalElements());
		// 总页数
		result.put("totalPage", highlightPage.getTotalPages());
		return result;
	}

	/**
	 * 对查询进行过滤
	 * 
	 * @param searchMap
	 * @param query
	 */
	private void filter(Map<String, Object> searchMap, HighlightQuery query) {
		// 设置过滤条件

		// 过滤商品分类
		if (!"".equals(searchMap.get("category"))) {

			FilterQuery filterQuery = new SimpleFacetQuery();
			Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery);
		}

		// 过滤商品品牌
		if (!"".equals(searchMap.get("brand"))) {
			FilterQuery filterQuery = new SimpleFacetQuery();
			Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery);
		}

		// 过滤规格信息
		if (searchMap.get("spec") != null) {
			Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");

			for (Entry<String, String> entry : specMap.entrySet()) {
				FilterQuery filterQuery = new SimpleFacetQuery();
				Criteria filterCriteria = new Criteria("item_spec_" + entry.getKey()).is(entry.getValue());
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
		}

		// 过滤价格
		if (!"".equals(searchMap.get("price"))) {
			String price = String.valueOf(searchMap.get("price"));
			String[] priceRange = price.split("-");

			// 如果最低价格不是0， 如果最低价格是0，则不进行过滤 0-500 只 <= 500即可
			if (!"0".equals(priceRange[0])) {
				FilterQuery filterQuery = new SimpleFacetQuery();
				// greaterThanEqual:大于等于
				Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(priceRange[0]);
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);
			}

			// 如果最高价格不是 * 3000-* 只 >=3000即可
			if (!"*".equals(priceRange[1])) {
				FilterQuery filterQuery = new SimpleFilterQuery();
				// lessThanEqual:小于等于
				Criteria filterCriteria = new Criteria("item_price").lessThanEqual(priceRange[1]);
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
		}
	}

	/**
	 * 查询品牌分类列表
	 * 
	 * @param searchMap 查询条件
	 * @return 返回分组查询结果
	 */
	private List<String> searchCategoryList(Map<String, Object> searchMap) {
		List<String> list = new ArrayList<>();

		Query query = new SimpleQuery("*:*");

		// 设置查询条件 where
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);

		// 设置分组字段 group by
		GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
		query.setGroupOptions(groupOptions);

		// 获取分组页
		GroupPage<TbItem> groupPage = solrTemplate.queryForGroupPage(query, TbItem.class);

		// 获取分页结果对象 这里的参数必须是在groupOptions中设置过的分组字段
		GroupResult<TbItem> groupResult = groupPage.getGroupResult("item_category");

		// 获取分组入口页
		Page<GroupEntry<TbItem>> groupEntry = groupResult.getGroupEntries();

		// 获取分组集合
		List<GroupEntry<TbItem>> entryList = groupEntry.getContent();

		for (GroupEntry<TbItem> entry : entryList) {
			// SimpleGroupEntry [groupValue=手机, result=Page 1 ]
			list.add(entry.getGroupValue());
		}
		return list;
	}

	/**
	 * 从缓存中获取商品分类对应的品牌和规格选项
	 * 
	 * @param categoryName
	 * @return
	 */
	private Map<String, Object> searchBrandAndSpec(String categoryName) {
		Map<String, Object> map = new HashMap<>();

		// 获取商品分类对应的模板Id
		Object catName = redisTemplate.boundHashOps("itemCat").get(categoryName);

		if (catName != null) {
			// 根据模板ID获取对应的品牌列表
			List bradnList = (List) redisTemplate.boundHashOps("brandList").get(catName);
			map.put("brandList", bradnList);

			// 根据模板ID获取对应的规格信息
			List sepcList = (List) redisTemplate.boundHashOps("specList").get(catName);
			map.put("specList", sepcList);
		}

		return map;
	}
	
	@Override
	public void importList(List<TbItem> list) {
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
	}

	
	@Override
	public void deleteByGoodsIds(List goodsIds) {
		Query query=new SimpleQuery("*:*");		
		Criteria criteria=new Criteria("item_goodsId").in(goodsIds);
		query.addCriteria(criteria);		
		solrTemplate.delete(query);
		solrTemplate.commit();
	}
}


















package com.pinyougou.search.service;

import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbItem;

@SuppressWarnings("all")
public interface ItemSearchService {
	/**
	 * 根据关键字查询
	 * @param searchMap
	 * @return
	 */
	Map<String, Object> search(Map<String, Object> searchMap);
	
	/**
	 * 导入列表
	 */
	public void importList(List<TbItem> list);
	
	public void deleteByGoodsIds(List goodsIds);
}

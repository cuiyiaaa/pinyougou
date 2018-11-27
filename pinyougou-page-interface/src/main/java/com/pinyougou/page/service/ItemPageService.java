package com.pinyougou.page.service;

public interface ItemPageService {
	
	/**
	 * 生成商品详情页
	 * @param goodsId 商品Id
	 * @return
	 */
	public boolean genItemHtml(Long goodsId); 
}

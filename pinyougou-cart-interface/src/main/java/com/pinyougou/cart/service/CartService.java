package com.pinyougou.cart.service;

import java.util.List;

import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;

public interface CartService {
	
	/**
	 *  添加购物车
	 * @param list list就是整个购物车，list的泛型cart 就是 购物车对象(每个商家的购物车)
	 * @param itemId 商品Id
	 * @param num	商品数量
	 * @return
	 */
	public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num,String source);
	
	/**
	 * 从redis 中查询购物车
	 * @param username
	 * @return
	 */
	public List<Cart> findCartListFromRedis(String username);
	
	public List<TbOrderItem> findDelCartListFromRedis(String username);
	
	/**
	 * 将购物车列表存入Redis
	 */
	public void saveCartListToRedis(String username,List<Cart> cartList);
	
	/**
	 * 合并购物车
	 * @param cookieCartList
	 * @param redisCartList
	 * @return
	 */
	public List<Cart> mergeCartList(List<Cart> cookieCartList,List<Cart> redisCartList);
	
	/**
	 * 从Redis中删除
	 * @param username
	 * @param itemId
	 */
	public void delCartListToRedis(String username, Long itemId);
}














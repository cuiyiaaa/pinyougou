package com.pinyougou.cart.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;

@Service(timeout = 6000)
@SuppressWarnings("all")
public class CartServiceImpl implements CartService {

	@Autowired
	private TbItemMapper itemMapper;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Override
	public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num, String source) {
		// 1.根据skuId查询对应sku的详细信息
		TbItem tbItem = itemMapper.selectByPrimaryKey(itemId);

		if (tbItem == null || !"1".equals(tbItem.getStatus())) {
			throw new RuntimeException("商品不存在");
		}

		// 2.通过sku详细信息获取商家Id
		String sellerId = tbItem.getSellerId();
		// 3.根据商家id来查询购物车列表中的商家购物车
		Cart cart = searchCartBySellerId(cartList, sellerId);

		// 4.商家购物不存在
		if (cart == null) {
			// 4.1创建商家购物车
			cart = new Cart();

			cart.setSellerId(sellerId);
			cart.setSellerName(tbItem.getSeller());

			List<TbOrderItem> orderItemList = new ArrayList<>();
			TbOrderItem orderItem = createOrderItem(num, tbItem);
			orderItemList.add(orderItem);

			cart.setOrderItemList(orderItemList);

			// 将商家购物车添加购物车列表
			cartList.add(cart);
		} else {
			// 5.商家购物车存在

			TbOrderItem tbOrderItem = searchOrderItemByItemId(itemId, cart.getOrderItemList());
			if (tbOrderItem == null) {
				// 商品在商家购物车中不存在
				tbOrderItem = createOrderItem(num, tbItem);
				cart.getOrderItemList().add(tbOrderItem);
			} else {
				// 商品在商家购物车中存在,更新数量和金额

				// 判断添加购物车的来源，如果是从详情页中添加，则累加，如果是从购物车的文本框则覆盖
				if ("0".equals(source)) {
					// 从详情页添加
					tbOrderItem.setNum(tbOrderItem.getNum() + num);
				} else {
					// 从购物车文本框添加
					tbOrderItem.setNum(num);
				}
				tbOrderItem.setTotalFee(new BigDecimal(tbOrderItem.getPrice().doubleValue() * tbOrderItem.getNum()));
			}

		}
		return cartList;
	}

	/**
	 * 根据商品Id查询商品对象
	 * 
	 * @param itemId
	 * @param orderItemList
	 */
	private TbOrderItem searchOrderItemByItemId(Long itemId, List<TbOrderItem> orderItemList) {
		for (TbOrderItem orderItem : orderItemList) {
			if (orderItem.getItemId().longValue() == itemId.longValue()) {
				return orderItem;
			}
		}
		return null;
	}

	/**
	 * 封装TbOrderItem
	 * 
	 * @param num
	 * @param tbItem
	 * @return
	 */
	private TbOrderItem createOrderItem(Integer num, TbItem tbItem) {
		TbOrderItem orderItem = new TbOrderItem();
		orderItem.setGoodsId(tbItem.getGoodsId());
		orderItem.setItemId(tbItem.getId());
		orderItem.setNum(num);
		orderItem.setSellerId(tbItem.getSellerId());
		orderItem.setTitle(tbItem.getTitle());
		orderItem.setPicPath(tbItem.getImage());
		orderItem.setPrice(tbItem.getPrice());
		orderItem.setTotalFee(new BigDecimal(tbItem.getPrice().doubleValue() * num));
		return orderItem;
	}

	/**
	 * 根据商家 ID 查询购物车对象
	 * 
	 * @param cartList
	 * @param sellerId
	 */
	private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
		for (Cart cart : cartList) {
			if (cart.getSellerId().equals(sellerId)) {
				return cart;
			}
		}
		return null;
	}

	@Override
	public List<Cart> findCartListFromRedis(String username) {
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
		if (cartList == null) {
			cartList = new ArrayList<>();
		}
		return cartList;
	}

	@Override
	public void saveCartListToRedis(String username, List<Cart> cartList) {

		redisTemplate.boundHashOps("cartList").put(username, cartList);
	}

	/**
	 * 删除购物车商品
	 * @param username
	 * @param itemId
	 */
	@Override
	public void delCartListToRedis(String username, Long itemId) {
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
		for (Cart cart : cartList) {
			for (TbOrderItem orderItem : cart.getOrderItemList()) {
				if (orderItem.getItemId().longValue()==itemId) {
					//将商品从集合中删除
					cart.getOrderItemList().remove(orderItem);
					break;
				}
			}
			
			if (cart.getOrderItemList().size()==0) {
				cartList.remove(cart);
				break;
			}
		}
		
		//将最新的购物车写入到Redis
		redisTemplate.boundHashOps("cartList").put(username, cartList);
	}
	
	/**
	 * 根据商品Id查询商品
	 * @param cartList
	 * @param itemId
	 * @return
	 */
	private TbOrderItem searchCartToByItemId(List<Cart> cartList, Long itemId) {
		for (Cart cart : cartList) {
			for (TbOrderItem orderItem : cart.getOrderItemList()) {
				return orderItem;
			}
		}
		return null;
	}

	@Override
	public List<Cart> mergeCartList(List<Cart> cookieCartList, List<Cart> redisCartList) {
		for (Cart cookieCart : cookieCartList) {
			for (TbOrderItem orderItem : cookieCart.getOrderItemList()) {
				redisCartList = addGoodsToCartList(redisCartList, orderItem.getItemId(), orderItem.getNum(), "0");
			}
		}
		return redisCartList;
	}

	@Override
	public List<TbOrderItem> findDelCartListFromRedis(String username) {
		// List<TbOrderItem> redisTemplate.boundHashOps("delCartLlist").get(username);
		return null;
	}
}

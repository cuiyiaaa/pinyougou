package com.pinyougou.cart.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;

import entity.Result;
import util.CookieUtil;

@RestController
@RequestMapping("/cart")
public class CartController {

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private HttpServletResponse response;

	@Reference
	private CartService cartService;

	@RequestMapping("/findCartList")
	public List<Cart> findCartList() {
		// 获取当前登录用户名
		String username = getUserName();
		System.out.println(username);

		// 从cookie中读取购物车
		String cookieValue = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
		if (cookieValue == null || "".equals(cookieValue)) {
			cookieValue = "[]";
		}
		List<Cart> cookieCartList = JSON.parseArray(cookieValue, Cart.class);

		// 如果未登录
		if ("anonymousUser".equals(username)) {
			System.out.println("从cookie中读取");
			return cookieCartList;
		} else {
			// 已经登录
			System.out.println("从redis中读取");
			// 从redis中读取购物车
			List<Cart> redisCartList = cartService.findCartListFromRedis(username);

			if (cookieCartList.size() > 0) {
				// 合并购物车
				List<Cart> mergeCartList = cartService.mergeCartList(cookieCartList, redisCartList);

				// 将合并的购物车写入到Redis
				cartService.saveCartListToRedis(username, mergeCartList);

				// 写入后将cookie的购物车删除
				CookieUtil.deleteCookie(request, response, "cartList");
				// 返回合并后的购物车
				return mergeCartList;
			}

			return redisCartList;

		}
	}

	@RequestMapping("/findDelCartList")
	public List<TbOrderItem> findDelCartList() {
		List<TbOrderItem> orderItemList = getDeleteCartCookie();

		return orderItemList;
	}

	/**
	 * 向购物车添加
	 * 
	 * @param itemId
	 * @param num
	 * @return
	 */
	@RequestMapping("/addCart")
	@CrossOrigin(origins = "http://localhost:9105", allowCredentials = "true")
	public Result addGoodsToCartList(Long itemId, Integer num, String source) {

		// 设置头信息 ,只有某一个域才可以方法
		// response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
		// *所有的域都可以访问
		// response.setHeader("Access-Control-Allow-Origin", "*");
		// 如果需要操作cookie则需要使用下面代码，允许使用cookie。
		// 注意：如果需要使用cooike上面设置域时就不能使用*，必须指定一个域
		// response.setHeader("Access-Control-Allow-Credentials", "true");

		// 获取当前登录用户名
		String name = getUserName();
		if ("anonymousUser".equals(name)) {
			System.out.println("cookie中存");
			// 未登录，向cookie中存
			return addCartToCookie(itemId, num, source);
		} else {
			// 已经登录，向redis中存
			System.out.println("Redis存");
			return addCartToRedis(itemId, num, source, name);
		}
	}

	/**
	 * 向Redis中添加
	 * 
	 * @param itemId
	 * @param num
	 * @param source
	 * @param name
	 * @return
	 */
	private Result addCartToRedis(Long itemId, Integer num, String source, String name) {
		List<Cart> cartList = findCartList();
		cartList = cartService.addGoodsToCartList(cartList, itemId, num, source);
		cartService.saveCartListToRedis(name, cartList);
		return new Result(true, "添加成功");
	}

	/**
	 * 删除商品
	 * 
	 * @param itemId
	 * @return
	 */
	@RequestMapping("/deleteItems")
	public Result deleteItems(Long itemId) {
		// 获取当前登录用户名
		String username = getUserName();
		List<Cart> cartList = findCartList();

		// 从cookie中删除
		for (Cart cart : cartList) {
			for (TbOrderItem orderItem : cart.getOrderItemList()) {
				// 查找被删除的商品
				if (orderItem.getItemId().longValue() == itemId) {
					// 将删除的商品存入删除购物车
					addDelGoodsToCartList(orderItem);

					if ("anonymousUser".equals(username)) {
						// 将删除的商品从购物车移除
						cart.getOrderItemList().remove(orderItem);
						if (cart.getOrderItemList().size() == 0) {
							cartList.remove(cart);
						}
						CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600 * 24,
								"UTF-8");
					} else {
						// 从redis中删除
						cartService.delCartListToRedis(username, itemId);
					}
					return new Result(true, "删除成功");
				}
			}
		}

		return new Result(false, "删除失败");

	}

	/**
	 * 重新购买
	 * 
	 * @return
	 */
	@RequestMapping("/resetAddCart")
	public Result resetAddCart(Long itemId, Integer num, String source) {
		Result result = addCartToCookie(itemId, num, source);

		List<TbOrderItem> orderItemList = getDeleteCartCookie();
		if (orderItemList.size() > 0) {
			for (TbOrderItem orderItem : orderItemList) {
				if (orderItem.getItemId().longValue() == itemId) {
					orderItemList.remove(orderItem);
					break;
				}
			}

			CookieUtil.setCookie(request, response, "delCartList", JSON.toJSONString(orderItemList), 3600 * 24,
					"UTF-8");
		}
		return result;
	}

	@RequestMapping("/findLoginName")
	public String findLoginName() {
		return getUserName();
	}

	/**
	 * 从cookie中获取删除的购物车
	 * 
	 * @return
	 */
	private List<TbOrderItem> getDeleteCartCookie() {
		String cookieValue = CookieUtil.getCookieValue(request, "delCartList", "UTF-8");
		if (cookieValue == null || "".equals(cookieValue)) {
			cookieValue = "[]";
		}
		List<TbOrderItem> orderItemList = JSON.parseArray(cookieValue, TbOrderItem.class);
		return orderItemList;
	}

	/**
	 * 将删除的商品放入删除购物车中
	 * 
	 * @param orderItem
	 */
	private void addDelGoodsToCartList(TbOrderItem orderItem) {
		// 将删除的商品存储到删除cookie中
		List<TbOrderItem> orderItemList = getDeleteCartCookie();

		boolean falg = false;
		for (TbOrderItem item : orderItemList) {
			// 删除购物车中已经存在该商品,则更新商品数量
			if (item.getItemId().longValue() == orderItem.getItemId()) {
				item.setNum(item.getNum() + orderItem.getNum());
				falg = true;
				break;
			}
		}

		// 商品不存在，则直接添加
		if (!falg) {
			orderItemList.add(orderItem);
		}

		CookieUtil.setCookie(request, response, "delCartList", JSON.toJSONString(orderItemList), 3600 * 24, "UTF-8");
	}

	/**
	 * 添加
	 * 
	 * @param itemId
	 * @param num
	 * @param source
	 * @return
	 */
	private Result addCartToCookie(Long itemId, Integer num, String source) {
		try {
			if (num < 0) {
				return new Result(false, "商品数量不合法");
			}
			// 获取cookie中购物车的信息
			List<Cart> cartList = findCartList();
			cartList = cartService.addGoodsToCartList(cartList, itemId, num, source);

			// 将购物车重新写回cookie
			CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600 * 24, "UTF-8");
			return new Result(true, "添加成功");
		} catch (Exception e) {
			// TODO: handle exception
			return new Result(false, "添加失败");
		}
	}

	/**
	 * 获取当前登录用户
	 * 
	 * @return
	 */
	private String getUserName() {
		return SecurityContextHolder.getContext().getAuthentication().getName();
	}

}

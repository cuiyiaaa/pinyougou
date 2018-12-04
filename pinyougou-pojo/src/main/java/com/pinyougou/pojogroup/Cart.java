package com.pinyougou.pojogroup;

import java.io.Serializable;
import java.util.List;

import com.pinyougou.pojo.TbOrderItem;

/**
 * 购物车类
 * @author cy
 *
 */
public class Cart implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String sellerId;//商家Id
	private String sellerName; //商家名称
	
	private List<TbOrderItem> orderItemList; //购物车明细

	public String getSellerId() {
		return sellerId;
	}

	public void setSellerId(String sellerId) {
		this.sellerId = sellerId;
	}

	public String getSellerName() {
		return sellerName;
	}

	public void setSellerName(String sellerName) {
		this.sellerName = sellerName;
	}

	public List<TbOrderItem> getOrderItemList() {
		return orderItemList;
	}

	public void setOrderItemList(List<TbOrderItem> orderItemList) {
		this.orderItemList = orderItemList;
	}

	@Override
	public String toString() {
		return "Cart [sellerId=" + sellerId + ", sellerName=" + sellerName + ", orderItemList=" + orderItemList + "]";
	}
	
	
}

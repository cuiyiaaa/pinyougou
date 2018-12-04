package com.pinyougou.order.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderExample.Criteria;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;

import entity.PageResult;
import util.IdWorker;

/**
 * 服务实现层
 * 
 * @author Administrator
 *
 */
@Service
@SuppressWarnings("all")
@Transactional
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	private IdWorker idWorker;

	@Autowired
	private TbOrderItemMapper orderItemMapper;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult<TbOrder> findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbOrder> page = (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult<TbOrder>(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {
		// 从redis中提取购物车
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
		if (cartList == null || cartList.size() == 0) {
			return;
		}

		// 遍历购物车添加订单
		for (Cart cart : cartList) {
			TbOrder tbOrder = new TbOrder();

			long orderId = idWorker.nextId();
			// 订单ID
			tbOrder.setOrderId(orderId);
			// 支付方式
			tbOrder.setPaymentType(order.getPaymentType());
			// 状态
			tbOrder.setStatus("1");
			// 订单创建时间
			tbOrder.setCreateTime(new Date());
			// 更新时间
			tbOrder.setUpdateTime(new Date());
			// 当前用户
			tbOrder.setUserId(order.getUserId());
			// 收货地址
			tbOrder.setReceiverAreaName(order.getReceiverAreaName());
			// 收货人
			tbOrder.setReceiver(order.getReceiver());
			// 收货人电话
			tbOrder.setReceiverMobile(order.getReceiverMobile());
			// 商家id
			tbOrder.setSellerId(order.getSellerId());
			// 订单来源
			tbOrder.setSourceType(order.getSourceType());

			BigDecimal totalMoney = new BigDecimal("0");
			for (TbOrderItem orderItem : cart.getOrderItemList()) {
				orderItem.setId(idWorker.nextId());
				orderItem.setOrderId(orderId);

				// 计算总金额
				totalMoney = totalMoney.add(orderItem.getTotalFee());

				orderItemMapper.insert(orderItem);
			}
			System.out.println(totalMoney);
			
			//总金额
			tbOrder.setPayment(totalMoney);
			
			orderMapper.insert(tbOrder);
		}

		// 清除购物车
		redisTemplate.boundHashOps("cartList").delete(order.getUserId());
	}

	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order) {
		orderMapper.updateByPrimaryKey(order);
	}

	/**
	 * 根据ID获取实体
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long id) {
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for (Long id : ids) {
			orderMapper.deleteByPrimaryKey(id);
		}
	}

	@Override
	public PageResult<TbOrder> findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbOrderExample example = new TbOrderExample();
		Criteria criteria = example.createCriteria();

		if (order != null) {
			if (order.getPaymentType() != null && order.getPaymentType().length() > 0) {
				criteria.andPaymentTypeLike("%" + order.getPaymentType() + "%");
			}
			if (order.getPostFee() != null && order.getPostFee().length() > 0) {
				criteria.andPostFeeLike("%" + order.getPostFee() + "%");
			}
			if (order.getStatus() != null && order.getStatus().length() > 0) {
				criteria.andStatusLike("%" + order.getStatus() + "%");
			}
			if (order.getShippingName() != null && order.getShippingName().length() > 0) {
				criteria.andShippingNameLike("%" + order.getShippingName() + "%");
			}
			if (order.getShippingCode() != null && order.getShippingCode().length() > 0) {
				criteria.andShippingCodeLike("%" + order.getShippingCode() + "%");
			}
			if (order.getUserId() != null && order.getUserId().length() > 0) {
				criteria.andUserIdLike("%" + order.getUserId() + "%");
			}
			if (order.getBuyerMessage() != null && order.getBuyerMessage().length() > 0) {
				criteria.andBuyerMessageLike("%" + order.getBuyerMessage() + "%");
			}
			if (order.getBuyerNick() != null && order.getBuyerNick().length() > 0) {
				criteria.andBuyerNickLike("%" + order.getBuyerNick() + "%");
			}
			if (order.getBuyerRate() != null && order.getBuyerRate().length() > 0) {
				criteria.andBuyerRateLike("%" + order.getBuyerRate() + "%");
			}
			if (order.getReceiverAreaName() != null && order.getReceiverAreaName().length() > 0) {
				criteria.andReceiverAreaNameLike("%" + order.getReceiverAreaName() + "%");
			}
			if (order.getReceiverMobile() != null && order.getReceiverMobile().length() > 0) {
				criteria.andReceiverMobileLike("%" + order.getReceiverMobile() + "%");
			}
			if (order.getReceiverZipCode() != null && order.getReceiverZipCode().length() > 0) {
				criteria.andReceiverZipCodeLike("%" + order.getReceiverZipCode() + "%");
			}
			if (order.getReceiver() != null && order.getReceiver().length() > 0) {
				criteria.andReceiverLike("%" + order.getReceiver() + "%");
			}
			if (order.getInvoiceType() != null && order.getInvoiceType().length() > 0) {
				criteria.andInvoiceTypeLike("%" + order.getInvoiceType() + "%");
			}
			if (order.getSourceType() != null && order.getSourceType().length() > 0) {
				criteria.andSourceTypeLike("%" + order.getSourceType() + "%");
			}
			if (order.getSellerId() != null && order.getSellerId().length() > 0) {
				criteria.andSellerIdLike("%" + order.getSellerId() + "%");
			}

		}

		Page<TbOrder> page = (Page<TbOrder>) orderMapper.selectByExample(example);
		return new PageResult<TbOrder>(page.getTotal(), page.getResult());
	}

}

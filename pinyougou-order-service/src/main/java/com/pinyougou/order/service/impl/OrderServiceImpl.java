package com.pinyougou.order.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderExample.Criteria;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
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

	@Autowired
	private TbPayLogMapper payLogMapper;

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

		// 整个订单的总金额
		BigDecimal orderMoney = new BigDecimal("0");
		// 订单编号
		List<String> orderIdList = new ArrayList<>();

		// 遍历购物车添加订单
		for (Cart cart : cartList) {
			TbOrder tbOrder = new TbOrder();

			long orderId = idWorker.nextId();
			orderIdList.add(orderId + ",");
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

			// 每个商家购物车的总金额
			tbOrder.setPayment(totalMoney);

			orderMoney = orderMoney.add(tbOrder.getPayment());
			System.out.println("总金额" + orderMoney);

			orderMapper.insert(tbOrder);
		}

		// 如果是微信则记录支付日志
		if ("1".equals(order.getPaymentType())) {
			TbPayLog payLog = new TbPayLog();

			// 支付订单号
			payLog.setOutTradeNo(idWorker.nextId() + "");
			// 创建时间
			payLog.setCreateTime(new Date());
			// 支付金额，分为单位
			payLog.setTotalFee((long) (orderMoney.doubleValue() * 100));
			System.out.println(orderMoney.longValue() * 100);
			// 用户ID
			payLog.setUserId(order.getUserId());
			// 订单编号
			payLog.setOrderList(orderIdList.toString().replace("[", "").replace("]", ""));
			// 支付方式
			payLog.setPayType("1");

			payLogMapper.insert(payLog);

			// 将支付日志存入Redis中
			redisTemplate.boundHashOps("payLogList").put(order.getUserId(), payLog);
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

	@Override
	public TbPayLog searchPayLogFromRedis(String userId) {
		return (TbPayLog) redisTemplate.boundHashOps("payLogList").get(userId);
	}

	@Override
	public void updateOrderStatus(String out_trade_no, String transaction_id) {
		TbPayLog tbPayLog = payLogMapper.selectByPrimaryKey(out_trade_no);
		// 交易流水号
		tbPayLog.setTransactionId(transaction_id);
		// 交易状态
		tbPayLog.setTradeState("1");
		// 支付完成时间
		tbPayLog.setPayTime(new Date());
		payLogMapper.updateByPrimaryKey(tbPayLog);

		// 更新订单状态
		String orderList = tbPayLog.getOrderList();
		String[] orderIds = orderList.split(",");
		for (String id : orderIds) {
			TbOrder tbOrder = orderMapper.selectByPrimaryKey(Long.valueOf(id));
			if (tbOrder != null) {
				tbOrder.setStatus("2");
				orderMapper.updateByPrimaryKey(tbOrder);
			}
		}

		// 更新完成后，删除Redis中的支付日志
		redisTemplate.boundHashOps("payLogList").delete(tbPayLog.getUserId());
	}

}

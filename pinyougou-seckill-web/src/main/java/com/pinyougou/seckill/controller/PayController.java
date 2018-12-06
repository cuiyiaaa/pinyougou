package com.pinyougou.seckill.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeiXinPayService;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.Result;

@RestController
@RequestMapping("/pay")
public class PayController {

	@Reference
	private WeiXinPayService weiXinPayService;

	@Reference
	private SeckillOrderService seckillOrderService;

	/**
	 * 生成支付的二维码
	 * 
	 * @return
	 */
	@RequestMapping("/createNative")
	public Map<String, String> createNative() {
		// 获取当前登录用户
		String username = SecurityContextHolder.getContext().getAuthentication().getName();

		// 提取秒杀订单
		TbSeckillOrder tbSeckillOrder = seckillOrderService.searchOrderToRedis(username);
		if (tbSeckillOrder != null) {
			long fen = (long) (tbSeckillOrder.getMoney().doubleValue() * 100);
			return weiXinPayService.createNavtive(tbSeckillOrder.getId() + "", fen + "");
		}
		return new HashMap<>();
	}

	/**
	 * 查询订单状态，确认用户是否支付成功
	 * 
	 * @param out_trade_no
	 * @return
	 */
	@RequestMapping("/queryPayStatus")
	public Result queryPayStatus(String out_trade_no) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		Result result = null;
		int time = 0;
		while (true) {
			System.out.println("检查支付状态" + time);
			Map<String, String> map = weiXinPayService.queryPayStatus(out_trade_no);

			if (map == null) {
				result = new Result(false, "支付异常");
				break;
			}

			// 支付成功
			if ("SUCCESS".equals(map.get("trade_state"))) {
				result = new Result(true, "支付成功");
				System.out.println("支付成功");
				// 支付成功，保存订单
				try {
					seckillOrderService.saveOrderFromRedisToDb(username, Long.valueOf(out_trade_no),
							map.get("transaction_id"));
				} catch (RuntimeException e) {
					e.printStackTrace();
				}
				break;
			}

			// 如果一直进行查询不停歇对后端压力很大，应该每3-5秒查询一次
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			time++;
			// 每5秒查询一次，一分钟查询12次，5分钟查询60。不是很准确
			if (time >= 60) {
				result = new Result(false, "支付超时");

				// 取消支付订单
				Map<String, String> closeMap = weiXinPayService.closePay(out_trade_no);
				// 取消订单时，但是用户已经支付了
				if (closeMap != null && "FAIL".equals(closeMap.get("result_code"))) {
					if ("ORDERPAID".equals(closeMap.get("err_code"))) {
						// 支付成功，保存订单
						result = new Result(true, "支付成功");
						// 支付成功，保存订单
						try {
							seckillOrderService.saveOrderFromRedisToDb(username, Long.valueOf(out_trade_no),
									map.get("transaction_id"));
						} catch (RuntimeException e) {
							e.printStackTrace();
						}
					}
				}

				if (!result.isSuccess()) {
					// 支付超时，则取消订单
					seckillOrderService.deleteOrderFromRedis(username, Long.valueOf(out_trade_no));
				}

				break;
			}
		}

		return result;
	}
}

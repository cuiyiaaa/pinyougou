package com.pinyougou.cart.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeiXinPayService;
import com.pinyougou.pojo.TbPayLog;

import entity.Result;

@RestController
@RequestMapping("/pay")
public class PayController {

	@Reference
	private WeiXinPayService weiXinPayService;

	@Reference
	private OrderService orderService;

	/**
	 * 生成支付的二维码
	 * 
	 * @return
	 */
	@RequestMapping("/createNative")
	public Map<String, String> createNative() {
		// 获取当前登录用户
		String username = SecurityContextHolder.getContext().getAuthentication().getName();

		// 读取支付日志
		TbPayLog tbPayLog = orderService.searchPayLogFromRedis(username);
		if (tbPayLog != null) {
			return weiXinPayService.createNavtive(tbPayLog.getOutTradeNo(), tbPayLog.getTotalFee() + "");
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
				// 支付成功后修改支付日志和订单状态
				orderService.updateOrderStatus(out_trade_no, map.get("transaction_id"));
				break;
			}

			// 如果一直进行查询不停歇对后端压力很大，应该每3-5秒查询一次
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			time++;
			//每5秒查询一次，一分钟查询12次，5分钟查询60。不是很准确
			if (time >= 60) {
				result = new Result(false, "支付超时");
				break;
			}
		}

		return result;
	}

}

package com.pinyougou.pay.service;

import java.util.Map;

/**
 * 微信支付
 * @author cy
 *
 */
public interface WeiXinPayService {
	
	/**
	 * 生成微信支付二维码
	 * @param out_trade_no 订单号
	 * @param total_fee 总金额，以分为单位
	 * @return
	 */
	public Map<String, String> createNavtive(String out_trade_no,String total_fee);
	
	/**
	 * 查询订单
	 * @param out_trade_no
	 * @return
	 */
	public Map<String, String> queryPayStatus(String out_trade_no);
}

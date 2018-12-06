package com.pinyougou.pay.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeiXinPayService;

import util.HttpClient;

@Service
public class WeiXinPayServiceImpl implements WeiXinPayService {

	@Value("${appid}")
	private String appid; // 公众号Id

	@Value("${partner}")
	private String mch_id;// 商户号

	@Value("${partnerkey}")
	private String partnerkey;

	/**
	 * out_trade_no：商户系统内部订单号 total_fee：总金额
	 */
	@Override
	public Map<String, String> createNavtive(String out_trade_no, String total_fee) {
		// 1.参数封装
		Map<String, String> param = new HashMap<>();
		// 公众号Id
		param.put("appid", appid);
		// 商户号
		param.put("mch_id", mch_id);
		// 随机字符串
		param.put("nonce_str", WXPayUtil.generateNonceStr());
		// 商品描述
		param.put("body", "品优购");
		// 商户订单号
		param.put("out_trade_no", out_trade_no);
		// 标价金额
		param.put("total_fee", total_fee);
		// 终端ip
		param.put("spbill_create_ip", "127.0.0.1");
		// 通知地址
		param.put("notify_url", "http://www.itcast.cn");
		// 交易类型
		param.put("trade_type", "NATIVE");

		try {
			System.out.println(partnerkey);
			// 将map转换为xml
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
			System.out.println(xmlParam);

			// 2.发送请求
			HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
			httpClient.setHttps(true);
			httpClient.setXmlParam(xmlParam);
			httpClient.post();// 发送post请求

			// 3.获取结果
			String xmlResult = httpClient.getContent();
			Map<String, String> mapResult = WXPayUtil.xmlToMap(xmlResult);
			System.out.println("微信返回结果：" + mapResult);

			// 将获取结果的一部分信息传递到前端，不要把所有信息度传递到前端
			Map<String, String> map = new HashMap<>();
			// 生成支付二维码的链接
			map.put("code_url", mapResult.get("code_url"));
			map.put("out_trade_no", out_trade_no);
			map.put("total_fee", total_fee);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			return new HashMap<>();
		}
	}

	@Override
	public Map<String, String> queryPayStatus(String out_trade_no) {
		// 1.封装参数
		Map<String, String> param = new HashMap<>();
		// 公众号Id
		param.put("appid", appid);
		// 商户号
		param.put("mch_id", mch_id);
		// 商户订单号
		param.put("out_trade_no", out_trade_no);
		// 随机字符串
		param.put("nonce_str", WXPayUtil.generateNonceStr());

		try {
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);

			// 发送请求
			HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
			httpClient.setHttps(true);
			httpClient.setXmlParam(xmlParam);
			httpClient.post();

			// 接收返回结果
			String content = httpClient.getContent();
			// 转换为Map
			Map<String, String> resultMap = WXPayUtil.xmlToMap(content);
			return resultMap;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Map<String, String> closePay(String out_trade_no) {
		// 1.封装参数
		Map<String, String> param = new HashMap<>();
		// 公众号Id
		param.put("appid", appid);
		// 商户号
		param.put("mch_id", mch_id);
		// 商户订单号
		param.put("out_trade_no", out_trade_no);
		// 随机字符串
		param.put("nonce_str", WXPayUtil.generateNonceStr());

		try {
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);

			HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/closeorder");
			httpClient.setHttps(true);
			httpClient.setXmlParam(xmlParam);
			httpClient.post();
			// 接收返回结果
			String content = httpClient.getContent();
			// 转换为Map
			Map<String, String> resultMap = WXPayUtil.xmlToMap(content);
			return resultMap;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}

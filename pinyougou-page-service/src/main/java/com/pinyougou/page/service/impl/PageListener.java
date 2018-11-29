package com.pinyougou.page.service.impl;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pinyougou.page.service.ItemPageService;

/**
 * 监听类(用于生成网页)
 * @author cy
 *
 */
@Component
public class PageListener implements MessageListener {

	@Autowired
	private ItemPageService itemPageService;

	@Override
	public void onMessage(Message message) {
		try {
			TextMessage textMessage = (TextMessage) message;
			Long goodsId = Long.parseLong(textMessage.getText());
			boolean flag = itemPageService.genItemHtml(goodsId);
			System.out.println("页面生成完成：" + flag);
		} catch (NumberFormatException | JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

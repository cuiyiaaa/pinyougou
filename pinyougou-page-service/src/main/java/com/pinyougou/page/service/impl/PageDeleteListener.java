package com.pinyougou.page.service.impl;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pinyougou.page.service.ItemPageService;

@Component
public class PageDeleteListener implements MessageListener {
	
	@Autowired
	private ItemPageService itemPageService;

	@Override
	public void onMessage(Message message) {
		try {
			ObjectMessage objectMessage = (ObjectMessage) message;
			Long[] ids = (Long[]) objectMessage.getObject();
			itemPageService.deleteItemHtml(ids);
			System.out.println("删除成功");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

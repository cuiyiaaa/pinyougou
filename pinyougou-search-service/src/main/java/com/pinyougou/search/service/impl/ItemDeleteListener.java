package com.pinyougou.search.service.impl;

import java.util.Arrays;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pinyougou.search.service.ItemSearchService;

@Component
public class ItemDeleteListener implements MessageListener {

	@Autowired
	private ItemSearchService itemSearchService;
	
	@Override
	public void onMessage(Message message) {
		ObjectMessage obj=(ObjectMessage)message;
		try {
			Long[] goodsId = (Long[])obj.getObject();
			System.out.println(Arrays.asList(goodsId));
			itemSearchService.deleteByGoodsIds(Arrays.asList(goodsId));
			System.out.println("删除索引库");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

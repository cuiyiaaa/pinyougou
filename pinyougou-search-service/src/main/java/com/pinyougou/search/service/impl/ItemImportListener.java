package com.pinyougou.search.service.impl;

import java.util.List;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Component
public class ItemImportListener implements MessageListener {
	
	@Autowired
	private ItemSearchService itemSearchService;

	@Override
	public void onMessage(Message message) {
		try {
			//获取消息
			TextMessage textMessage=(TextMessage) message;
			List<TbItem> itemList = JSON.parseArray(textMessage.getText(), TbItem.class);
			//导入到Solr索引库
			itemSearchService.importList(itemList);
			System.out.println("成功导入到Solr索引库");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}










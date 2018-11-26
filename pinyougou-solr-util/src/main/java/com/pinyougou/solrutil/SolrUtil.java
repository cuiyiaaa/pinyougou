package com.pinyougou.solrutil;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbItemExample.Criteria;

@Component
@SuppressWarnings("all")
public class SolrUtil {
	@Autowired
	private TbItemMapper mapper;

	@Autowired
	private SolrTemplate solrTemplate;

	public void importItemData() {
		TbItemExample example = new TbItemExample();
		Criteria criteria = example.createCriteria();
		// 只查询上架的商品
		criteria.andStatusEqualTo("1");

		List<TbItem> list = mapper.selectByExample(example);

		for (TbItem tbItem : list) {
			//设置动态数据
			Map<String,String> parseObject = JSON.parseObject(tbItem.getSpec(),Map.class);
			
			tbItem.setSpecMap(parseObject);
		}

		solrTemplate.saveBeans(list);
		solrTemplate.commit();
	}

	public static void main(String[] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/application*.xml");
		SolrUtil bean = context.getBean(SolrUtil.class);
		bean.importItemData();
	}
}










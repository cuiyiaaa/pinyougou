package com.pinyougou.mannager.controller;

import java.util.List;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goodsgroup;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import entity.Result;

/**
 * controller
 * 
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
@SuppressWarnings("all")
public class GoodsController {

	@Reference
	private GoodsService goodsService;

	@Autowired
	private JmsTemplate JmsTemplate;
	
	//导入索引库
	@Autowired
	private Destination queueSolrImportDestination;
	
	//删除索引库
	@Autowired
	private Destination queueSolrDeleteDestination;
	
	//生产静态页面
	@Autowired
	private Destination topicPageDestination;
	
	//删除静态页面
	@Autowired
	private Destination topicDeletePageDestination;

	/**
	 * 返回全部列表
	 * 
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll() {
		return goodsService.findAll();
	}

	/**
	 * 返回全部列表
	 * 
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult<TbGoods> findPage(int page, int rows) {
		return goodsService.findPage(page, rows);
	}

	/**
	 * 增加
	 * 
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody TbGoods goods) {
		try {
			// goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}

	/**
	 * 修改
	 * 
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goodsgroup goods) {
		System.out.println(goods);
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}

	/**
	 * 获取实体
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goodsgroup findOne(Long id) {
		return goodsService.findOne(id);
	}

	/**
	 * 批量删除
	 * 
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long[] ids) {
		try {
			goodsService.delete(ids);
			
			//向搜索服务发送消息，从索引库中删除
			JmsTemplate.send(queueSolrDeleteDestination,new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});
			
			//删除每个服务器上的商品详情页
			JmsTemplate.send(topicDeletePageDestination,new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});

			return new Result(true, "删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}

	/**
	 * 查询+分页
	 * 
	 * @param brand
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult<TbGoods> search(@RequestBody TbGoods goods, int page, int rows) {

		return goodsService.findPage(goods, page, rows);
	}

	/**
	 * 更新商品状态
	 * 
	 * @param ids
	 * @param status
	 * @return
	 */
	@RequestMapping("/updateStatus")
	public Result updateStatus(Long[] ids, String status) {
		try {
			goodsService.updateStatus(ids, status);

			// 如果商品通过审核，则将通过审核后的商品导入到Solr
			if ("1".equals(status)) { // 商品通过审核

				// 根据审核通过的商品id 查询该商品对应的sku
				List<TbItem> itemList = goodsService.findItemListByGoodsId(ids, status);
				if (itemList != null && itemList.size() > 0) {
					
					//设置动态域的数据
					for (TbItem tbItem : itemList) {
						Map<String, String> parseObject = JSON.parseObject(tbItem.getSpec(), Map.class);
						tbItem.setSpecMap(parseObject);
					}

					// 发送消息
					sendImprotTextMessage(itemList);

					// 生成商品详情页
					for (Long goodsId : ids) {
						sendAutoPageMessage(goodsId);
					}
					
				}
			}

			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}

	/**
	 * 使用AvtiveMQ发送消息给搜索服务，通知页面生成服务进行生成静态页面
	 * @param goodsId
	 */
	private void sendAutoPageMessage(final Long goodsId) {
		JmsTemplate.send(topicPageDestination,new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				return session.createTextMessage(String.valueOf(goodsId));
			}
		});
	}


	/**
	 * 使用AvtiveMQ发送消息给搜索服务，通知搜索服务进行导入通过审核的数据
	 * @param itemList
	 */
	private void sendImprotTextMessage(List<TbItem> itemList) {
		// 将集合转换为JSON字符串，传递消息时传递字符串。因为传递的对象的话，List接口没有实现可序列接口
		final String itemListJson = JSON.toJSONString(itemList);
		JmsTemplate.send(queueSolrImportDestination, new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				return session.createTextMessage(itemListJson);
			}
		});
	}
}






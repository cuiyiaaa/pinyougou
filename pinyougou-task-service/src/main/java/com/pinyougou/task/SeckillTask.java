package com.pinyougou.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import com.pinyougou.pojo.TbSeckillGoodsExample.Criteria;

/**
 * 
 * @author cy
 *
 */
@Component
@SuppressWarnings("all")
public class SeckillTask {

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;

	/**
	 * 每分钟执行一次，更新缓存中的秒杀商品
	 */
	@Scheduled(cron = "0 * * * * ?") // 每分钟执行一次
	public void refreshSeckillGoods() {
		System.out.println("执行商品增量更新任务：" + new Date());
		// 从缓存中获取秒杀商品Id集合
		Set<Long> keys = redisTemplate.boundHashOps("seckillGoods").keys();
		System.out.println(keys);

		TbSeckillGoodsExample example = new TbSeckillGoodsExample();
		Criteria criteria = example.createCriteria();
		// 设置查询条件
		criteria.andStatusEqualTo("1");// 通过审核的商品
		criteria.andStartTimeLessThanOrEqualTo(new Date());// 开始时间小于当前时间
		criteria.andEndTimeGreaterThanOrEqualTo(new Date());// 结束时间大于当前时间
		criteria.andStockCountGreaterThan(0);// 库存大于0

		if (keys != null && keys.size() > 0) {
			criteria.andIdNotIn(new ArrayList<>(keys));// 排除缓存中已经存在的商品Id
		}

		List<TbSeckillGoods> list = seckillGoodsMapper.selectByExample(example);
		for (TbSeckillGoods tbSeckillGoods : list) {
			redisTemplate.boundHashOps("seckillGoods").put(tbSeckillGoods.getId(), tbSeckillGoods);
			System.out.println("增量更新秒杀商品Id：" + tbSeckillGoods.getId());
		}
		System.out.println("..end");
	}

	/**
	 * 每秒执行一次，清除过期秒杀商品
	 */
	@Scheduled(cron = "* * * * * ?")
	@Scheduled()
	public void removeSeckillGoods() {
		System.out.println("清除秒杀商品");
		// 查询除缓存中的数据，扫描每条记录，判断时间，如果当前时间超过了结束时间则移除该条记录
		List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("seckillGoods").values();
		for (TbSeckillGoods tbSeckillGoods : seckillGoodsList) {
			// 结束时间小于当前时间则清除
			if (tbSeckillGoods.getEndTime().getTime() < System.currentTimeMillis()) {
				// 将商品同步到数据库中
				seckillGoodsMapper.updateByPrimaryKey(tbSeckillGoods);
				// 清除缓存
				redisTemplate.boundHashOps("seckillGoods").delete(tbSeckillGoods.getId());
				System.out.println("清除秒杀商品" + tbSeckillGoods.getTitle());
			}
		}
	}
}

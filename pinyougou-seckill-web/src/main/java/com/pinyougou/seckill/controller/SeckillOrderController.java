package com.pinyougou.seckill.controller;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.PageResult;
import entity.Result;

/**
 * controller
 * 
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/seckillOrder")
public class SeckillOrderController {

	@Reference
	private SeckillOrderService seckillOrderService;

	/**
	 * 返回全部列表
	 * 
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbSeckillOrder> findAll() {
		return seckillOrderService.findAll();
	}

	/**
	 * 返回全部列表
	 * 
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult<TbSeckillOrder> findPage(int page, int rows) {
		return seckillOrderService.findPage(page, rows);
	}

	/**
	 * 增加
	 * 
	 * @param seckillOrder
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody TbSeckillOrder seckillOrder) {
		try {
			seckillOrderService.add(seckillOrder);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}

	/**
	 * 修改
	 * 
	 * @param seckillOrder
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody TbSeckillOrder seckillOrder) {
		try {
			seckillOrderService.update(seckillOrder);
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
	public TbSeckillOrder findOne(Long id) {
		return seckillOrderService.findOne(id);
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
			seckillOrderService.delete(ids);
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
	public PageResult<TbSeckillOrder> search(@RequestBody TbSeckillOrder seckillOrder, int page, int rows) {
		return seckillOrderService.findPage(seckillOrder, page, rows);
	}

	/**
	 * 秒杀下单
	 * 
	 * @return
	 */
	@RequestMapping("/submitOrder")
	public Result submitOrder(Long seckillId) {
		Result result = null;
		// 获取当前登录用户
		String usename = SecurityContextHolder.getContext().getAuthentication().getName();
		// 为该控制器设置匿名用户角色，是因为如果对该控制器进行拦截，前端点击抢购，向后端发送请求。如果没有登录被拦截，登录成功后回被重定向到该控制器，而不是重定向到前端页面
		if ("anonymousUser".equals(usename)) {
			return new Result(false, "未登录");
		}
		
		System.out.println(seckillId);
		try {
			seckillOrderService.submitOrder(seckillId, usename);
			result = new Result(true, "秒杀成功");

		} catch (RuntimeException e) {
			e.printStackTrace();
			result = new Result(false, e.getMessage());
		} catch (Exception e) {
			result = new Result(false, "秒杀失败");
		}
		return result;
	}
}

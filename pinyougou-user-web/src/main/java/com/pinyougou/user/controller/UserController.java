package com.pinyougou.user.controller;

import java.util.Date;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.user.service.UserService;

import entity.PageResult;
import entity.Result;
import util.PhoneFormatCheckUtils;

/**
 * controller
 * 
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/user")
public class UserController {

	@Reference
	private UserService userService;

	/**
	 * 返回全部列表
	 * 
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbUser> findAll() {
		return userService.findAll();
	}

	/**
	 * 返回全部列表
	 * 
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult<TbUser> findPage(int page, int rows) {
		return userService.findPage(page, rows);
	}

	/**
	 * 增加
	 * 
	 * @param user
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody TbUser user, String smsCode) {

		boolean flag = userService.checkSmsCode(user.getPhone(), smsCode);
		if (!flag) {
			// 验证失败
			return new Result(false, "验证码错误");
		}

		try {
			user.setCreated(new Date());// 用户注册时间
			user.setUpdated(new Date());// 修改时间
			user.setSourceType("1"); // 注册来源

			// 对密码进行MD5加密
			String md5Hex = DigestUtils.md5Hex(user.getPassword());
			user.setPassword(md5Hex);

			userService.add(user);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}

	/**
	 * 修改
	 * 
	 * @param user
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody TbUser user) {
		try {
			userService.update(user);
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
	public TbUser findOne(Long id) {
		return userService.findOne(id);
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
			userService.delete(ids);
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
	public PageResult<TbUser> search(@RequestBody TbUser user, int page, int rows) {
		return userService.findPage(user, page, rows);
	}

	/**
	 * 发送验证码
	 * 
	 * @param phone
	 * @return
	 */
	@RequestMapping("/sendSmsCode")
	public Result sendSmsCode(String phone) {
		// 校验手机号
		if (!PhoneFormatCheckUtils.isPhoneLegal(phone)) {
			return new Result(false, "手机号格式不正确");
		}

		try {
			userService.createSmsCode(phone);
			return new Result(true, "发送成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "发送失败");
		}
	}
	

}













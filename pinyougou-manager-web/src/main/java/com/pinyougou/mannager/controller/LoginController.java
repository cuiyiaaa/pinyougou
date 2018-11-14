package com.pinyougou.mannager.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class LoginController {

	/**
	 * 获取登录的用户名
	 * 
	 * @return
	 */
	@RequestMapping("/getName")
	public Map<String, Object> getName() {
		Map<String, Object> map = new HashMap<>();

		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		map.put("loginName", name);
		
		return map;
	}
}

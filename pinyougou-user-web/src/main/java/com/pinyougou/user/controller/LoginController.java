package com.pinyougou.user.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class LoginController {

	@RequestMapping("/name")
	public Map<String, String> showName() {
		Map<String, String> map=new HashMap<>();
		
		//获取登录名
		String loginName = SecurityContextHolder.getContext().getAuthentication().getName();
		map.put("loginName", loginName);
		
		return map;
	}
}

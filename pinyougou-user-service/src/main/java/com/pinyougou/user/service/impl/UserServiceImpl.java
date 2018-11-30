package com.pinyougou.user.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbUserMapper;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.pojo.TbUserExample;
import com.pinyougou.pojo.TbUserExample.Criteria;
import com.pinyougou.user.service.UserService;

import entity.PageResult;

/**
 * 服务实现层
 * 
 * @author Administrator
 *
 */
@Service(timeout = 8000)
@SuppressWarnings("all")
public class UserServiceImpl implements UserService {

	@Autowired
	private TbUserMapper userMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private Destination smsDestination;

	@Value("${template_code}")
	private String template_code;

	@Value("${sign_name}")
	private String sign_name;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbUser> findAll() {
		return userMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult<TbUser> findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbUser> page = (Page<TbUser>) userMapper.selectByExample(null);
		return new PageResult<TbUser>(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbUser user) {
		userMapper.insert(user);
	}

	/**
	 * 修改
	 */
	@Override
	public void update(TbUser user) {
		userMapper.updateByPrimaryKey(user);
	}

	/**
	 * 根据ID获取实体
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public TbUser findOne(Long id) {
		return userMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for (Long id : ids) {
			userMapper.deleteByPrimaryKey(id);
		}
	}

	@Override
	public PageResult<TbUser> findPage(TbUser user, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbUserExample example = new TbUserExample();
		Criteria criteria = example.createCriteria();

		if (user != null) {
			if (user.getUsername() != null && user.getUsername().length() > 0) {
				criteria.andUsernameLike("%" + user.getUsername() + "%");
			}
			if (user.getPassword() != null && user.getPassword().length() > 0) {
				criteria.andPasswordLike("%" + user.getPassword() + "%");
			}
			if (user.getPhone() != null && user.getPhone().length() > 0) {
				criteria.andPhoneLike("%" + user.getPhone() + "%");
			}
			if (user.getEmail() != null && user.getEmail().length() > 0) {
				criteria.andEmailLike("%" + user.getEmail() + "%");
			}
			if (user.getSourceType() != null && user.getSourceType().length() > 0) {
				criteria.andSourceTypeLike("%" + user.getSourceType() + "%");
			}
			if (user.getNickName() != null && user.getNickName().length() > 0) {
				criteria.andNickNameLike("%" + user.getNickName() + "%");
			}
			if (user.getName() != null && user.getName().length() > 0) {
				criteria.andNameLike("%" + user.getName() + "%");
			}
			if (user.getStatus() != null && user.getStatus().length() > 0) {
				criteria.andStatusLike("%" + user.getStatus() + "%");
			}
			if (user.getHeadPic() != null && user.getHeadPic().length() > 0) {
				criteria.andHeadPicLike("%" + user.getHeadPic() + "%");
			}
			if (user.getQq() != null && user.getQq().length() > 0) {
				criteria.andQqLike("%" + user.getQq() + "%");
			}
			if (user.getIsMobileCheck() != null && user.getIsMobileCheck().length() > 0) {
				criteria.andIsMobileCheckLike("%" + user.getIsMobileCheck() + "%");
			}
			if (user.getIsEmailCheck() != null && user.getIsEmailCheck().length() > 0) {
				criteria.andIsEmailCheckLike("%" + user.getIsEmailCheck() + "%");
			}
			if (user.getSex() != null && user.getSex().length() > 0) {
				criteria.andSexLike("%" + user.getSex() + "%");
			}

		}

		Page<TbUser> page = (Page<TbUser>) userMapper.selectByExample(example);
		return new PageResult<TbUser>(page.getTotal(), page.getResult());
	}

	@Override
	public void createSmsCode(final String phone) {
		// 1.生产一个6为随机数
		String random = (long) (Math.random() * 1000000) + "";
		System.out.println("验证码" + random);
		// 2.将随机数存入Reids中
		redisTemplate.boundHashOps("smscode").put(phone, random);
		// 3.设置过期时间

		// 3. 将短信内容发送给avtiveMQ
		jmsTemplate.send(smsDestination, new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				MapMessage message = session.createMapMessage();
				message.setString("mobile", phone);// 手机号
				message.setString("template_code", template_code);// 验证码
				message.setString("sign_name", sign_name);// 签名
				Map map = new HashMap();
				map.put("code", random);
				message.setString("param", JSON.toJSONString(map));
				return message;
			}
		});
	}

	@Override
	public boolean checkSmsCode(String phone, String smsCode) {
		String sysCode = String.valueOf(redisTemplate.boundHashOps("smscode").get(phone));

		// 校验是否存在内容
		if (!hasLength(sysCode) || !hasLength(smsCode) || !hasLength(phone)) {
			return false;
		}

		// 不一致
		if (!sysCode.equals(smsCode)) {
			return false;
		}

		// 如果一致则删除验证码
		redisTemplate.boundHashOps("smscode").delete(phone);
		return true;
	}

	private boolean hasLength(String value) {
		return value != null && !"".equals(value.trim());
	}
}

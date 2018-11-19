package com.pinyougou.sellergoods.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.mapper.TbSellerMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbGoodsExample;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.pojogroup.Goodsgroup;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;

/**
 * 服务实现层
 * 
 * @author Administrator
 *
 */
@Service
@SuppressWarnings("all")
@Transactional
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;

	@Autowired
	private TbGoodsDescMapper goodsDescMapper;

	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Autowired
	private TbBrandMapper brandMapper;

	@Autowired
	private TbSellerMapper sellerMapper;

	@Autowired
	private TbItemMapper itemMapper;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult<TbGoods> findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult<TbGoods>(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goodsgroup goods) {
		// 插入商品基本信息
		// 设置商品状态为未审核
		goods.getGoods().setAuditStatus("0");
		goods.getGoods().setIsMarketable("1");
		goodsMapper.insert(goods.getGoods());
		// 添加商品扩展信息
		// 将商品基本信息表的id给商品信息扩展表的id，因为 将两张表合并起来才是一条完整的商品数据
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
		goodsDescMapper.insert(goods.getGoodsDesc());

		saveItemList(goods);
	}

	private void saveItemList(Goodsgroup goods) {
		TbGoods tbGoods = goods.getGoods();
		TbGoodsDesc tbGoodsDesc = goods.getGoodsDesc();

		// 启用规格
		if ("1".equals(tbGoods.getIsEnableSpec())) {
			for (TbItem item : goods.getItemList()) {
				// 标题:spu名称+ 规格选项值
				String title = goods.getGoods().getGoodsName();
				// 获取规格选项值
				String spec = item.getSpec();
				Map<String, Object> map = JSON.parseObject(spec);
				for (Entry<String, Object> entry : map.entrySet()) {
					title += " " + entry.getValue();
				}
				item.setTitle(title);

				setItemValue(tbGoods, tbGoodsDesc, item);

				System.out.println(item);
				itemMapper.insert(item);
			}
		} else {
			// 没有启用规格
			TbItem item = new TbItem();
			// 标题名称
			item.setTitle(tbGoods.getGoodsName());
			// 价格
			item.setPrice(tbGoods.getPrice());
			// 设置为默认
			item.setIsDefault("1");
			// 设置为启用状态
			item.setStatus("1");

			setItemValue(tbGoods, tbGoodsDesc, item);
			System.out.println(item);
			itemMapper.insert(item);
		}
	}

	/**
	 * 为TbItem赋值
	 * 
	 * @param tbGoods
	 * @param tbGoodsDesc
	 * @param item
	 */
	private void setItemValue(TbGoods tbGoods, TbGoodsDesc tbGoodsDesc, TbItem item) {
		// 创建日期。更新日期
		item.setUpdateTime(new Date());
		item.setCreateTime(new Date());

		// 商品Id
		item.setGoodsId(tbGoods.getId());

		// 商家id
		item.setSellerId(tbGoods.getSellerId());

		// 三级分类名称和Id
		TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id());
		item.setCategory(itemCat.getName());
		item.setCategoryid(itemCat.getId());

		// 品牌
		TbBrand brand = brandMapper.selectByPrimaryKey(tbGoods.getBrandId());
		item.setBrand(brand.getName());

		// 商家名称
		TbSeller seller = sellerMapper.selectByPrimaryKey(tbGoods.getSellerId());
		item.setSeller(seller.getNickName());
		// 图片名称
		// 获取该商品上传的图片
		String images = tbGoodsDesc.getItemImages();
		List<Map> imgList = JSON.parseArray(images, Map.class);
		if (imgList.size() > 0) {
			item.setImage(String.valueOf(imgList.get(0).get("url")));
		}
	}

	/**
	 * 修改
	 */
	@Override
	public void update(Goodsgroup goods) {
		// 更新商品信息
		goodsMapper.updateByPrimaryKey(goods.getGoods());
		// 更新商品扩展信息
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());

		// 更新商品sku：先将该商品的SKU全部删除，在进行添加
		TbItemExample example = new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(example);

		// 删除之后进行添加
		saveItemList(goods);
	}

	/**
	 * 根据ID获取实体
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public Goodsgroup findOne(Long id) {
		Goodsgroup goods = new Goodsgroup();

		// 根据商品id查询商品
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		// 根据商品Id查询商品扩展信息
		TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);

		// 根据商品id查询该商品的sku
		TbItemExample example = new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);
		List<TbItem> itemList = itemMapper.selectByExample(example);

		goods.setGoods(tbGoods);
		goods.setGoodsDesc(tbGoodsDesc);
		goods.setItemList(itemList);

		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for (Long id : ids) {
			goodsMapper.deleteByPrimaryKey(id);
		}
	}

	@Override
	public PageResult<TbGoods> findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbGoodsExample example = new TbGoodsExample();
		Criteria criteria = example.createCriteria();

		if (goods != null) {
			if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
				criteria.andSellerIdEqualTo(goods.getSellerId());
			}
			if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
				criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
			}
			if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
				criteria.andAuditStatusLike("%" + goods.getAuditStatus() + "%");
			}
			if (goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0) {
				criteria.andIsMarketableLike("%" + goods.getIsMarketable() + "%");
			}
			if (goods.getCaption() != null && goods.getCaption().length() > 0) {
				criteria.andCaptionLike("%" + goods.getCaption() + "%");
			}
			if (goods.getSmallPic() != null && goods.getSmallPic().length() > 0) {
				criteria.andSmallPicLike("%" + goods.getSmallPic() + "%");
			}
			if (goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0) {
				criteria.andIsEnableSpecLike("%" + goods.getIsEnableSpec() + "%");
			}
			if (goods.getIsDelete() != null && goods.getIsDelete().length() > 0) {
				criteria.andIsDeleteLike("%" + goods.getIsDelete() + "%");
			}

		}

		Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
		return new PageResult<TbGoods>(page.getTotal(), page.getResult());
	}

	@Override
	public void updateStatus(Long[] goodsId, String status) {
		if (goodsId.length > 0) {
			for (Long id : goodsId) {
				TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
				tbGoods.setAuditStatus(status);

				goodsMapper.updateByPrimaryKey(tbGoods);
			}
		}
	}

	@Override
	public void updateMarketable(Long[] goodsId, String marketable) {
		if (goodsId.length > 0) {
			for (Long id : goodsId) {
				TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
				tbGoods.setIsMarketable(marketable);

				goodsMapper.updateByPrimaryKey(tbGoods);
			}
		}
	}

}

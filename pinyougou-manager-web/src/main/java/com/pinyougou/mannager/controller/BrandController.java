package com.pinyougou.mannager.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;
import entity.Result;

@RestController // 相当于ResponseBody+Controller 不需要再方法上使用@ResponseBody注解了
@RequestMapping("/brand")
public class BrandController {

	@Reference
	private BrandService brandService;

	/**
	 * 查询所有
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/findAll")
	public List<TbBrand> findAll() throws Exception {
		return brandService.findBrandAll();
	}

	/**
	 * 分页查询
	 * 
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/findPage")
	public PageResult<TbBrand> findPage(@RequestParam(name = "page", defaultValue = "1") Integer pageIndex,
			@RequestParam(name = "size", defaultValue = "10") Integer pageSize) throws Exception {
		return brandService.findBrandPage(pageIndex, pageSize);
	}

	/**
	 * 根据条件查询
	 * 
	 * @param pageIndex
	 * @param pageSize
	 * @param brand
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/findSearch")
	public PageResult<TbBrand> findPageSearch(@RequestParam(name = "page", defaultValue = "1") Integer pageIndex,
			@RequestParam(name = "size", defaultValue = "10") Integer pageSize, @RequestBody TbBrand brand)
			throws Exception {
		return brandService.findBrandPage(pageIndex, pageSize, brand);
	}

	/**
	 * 保存
	 * 
	 * @param brand
	 * @return
	 */
	@RequestMapping("/save")
	public Result save(@RequestBody TbBrand brand) {
		try {
			if (brandService.saveBrand(brand)) {
				return new Result(true, "添加成功");
			} else {
				return new Result(false, "添加失败");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "添加失败");
		}
	}

	/**
	 * 判断对应品牌是否存在
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/findByNameCount")
	public Result findByNameCount(String name) throws Exception {
		// 解决中文乱码
		byte[] bytes = name.getBytes("ISO-8859-1");
		name = new String(bytes, "UTF-8");

		System.out.println(name);
		if (brandService.findBrandByNameCount(name) > 0) {
			return new Result(false, "该品牌已存在");
		} else {
			return new Result(true, "该品牌可以添加");
		}
	}

	/**
	 * 根据id查询
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/findOne")
	public TbBrand findOne(Long id) throws Exception {
		return brandService.findBrandOne(id);
	}

	/**
	 * 更新
	 * 
	 * @param brand
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody TbBrand brand) {
		try {
			if (brandService.updateBrand(brand)) {
				return new Result(true, "修改成功");
			} else {
				return new Result(false, "修改失败");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}

	/**
	 * 删除
	 * 
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long[] ids) {
		try {
			if (brandService.deleteBatchBrand(ids)) {
				return new Result(true, "删除成功");
			} else {
				return new Result(false, "删除失败");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}

	@RequestMapping("/selectOptionList")
	public List<Map<String, Object>> selectOptionList() {
		return brandService.selectOptionList();
	}
}

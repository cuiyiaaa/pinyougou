package com.pinyougou.pojo;

import java.io.Serializable;

public class TbTypeTemplate implements Serializable {
 
	private static final long serialVersionUID = 1L;
	
	//主键
	private Long id;

	//模板名称
    private String name;

    //关联规格
    private String specIds;

    //关联品牌
    private String brandIds;

    //扩展属性
    private String customAttributeItems;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getSpecIds() {
        return specIds;
    }

    public void setSpecIds(String specIds) {
        this.specIds = specIds == null ? null : specIds.trim();
    }

    public String getBrandIds() {
        return brandIds;
    }

    public void setBrandIds(String brandIds) {
        this.brandIds = brandIds == null ? null : brandIds.trim();
    }

    public String getCustomAttributeItems() {
        return customAttributeItems;
    }

    public void setCustomAttributeItems(String customAttributeItems) {
        this.customAttributeItems = customAttributeItems == null ? null : customAttributeItems.trim();
    }
}
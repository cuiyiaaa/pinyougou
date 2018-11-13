package com.pinyougou.pojogroup;

import java.io.Serializable;
import java.util.List;

import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationOption;

public class Specification implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private TbSpecification specification;
	private List<TbSpecificationOption> specificationOptionsList;

	public TbSpecification getSpecification() {
		return specification;
	}

	public void setSpecification(TbSpecification specification) {
		this.specification = specification;
	}

	public List<TbSpecificationOption> getSpecificationOptionsList() {
		return specificationOptionsList;
	}

	public void setSpecificationOptionsList(List<TbSpecificationOption> specificationOptionsList) {
		this.specificationOptionsList = specificationOptionsList;
	}
}

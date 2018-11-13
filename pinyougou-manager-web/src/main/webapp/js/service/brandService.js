//定义服务
app.service("brandService",function($http){
	
	//查询所有
	this.findAll=function(){
		return $http.get('../brand/findAll.do');
	};
	
	//分页查询
	this.findPage=function(page,size){
		return $http.get('../brand/findPage.do?page='+page+'&size='+size+'');
	}
	
	//条件查询+分页
	this.search=function(page,size,searchEntity){
		return $http.post('../brand/findSearch.do?page='+page+'&size='+size+'',searchEntity);
	}
	
	//判断品牌是否存在
	this.findByName=function(name){
		return $http.get('../brand/findByNameCount.do?name='+name)
	}
	
	//添加
	this.save=function(entity){
		return $http.post('../brand/save.do',entity);
	}
	
	//更新
	this.update=function(entity){
		return $http.post('../brand/update.do',entity);
	}
	
	//根据Id查询
	this.findOne=function(id){
		return $http.get('../brand/findOne.do?id='+id);
	}
	
	//删除
	this.del=function(selectIds){
		return $http.get('../brand/delete.do?ids='+selectIds);
	}
	
	//获取下列列表数据
	this.selectOptionList=function(){
		return $http.get('../brand/selectOptionList.do');
	};
});
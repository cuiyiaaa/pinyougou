//控制层 
app.controller('typeTemplateController', function($scope, $controller, typeTemplateService,brandService,specificationService) {

	$controller('baseController', {
		$scope : $scope
	});// 继承

	// 读取列表数据绑定到表单中
	$scope.findAll = function() {
		typeTemplateService.findAll().success(function(response) {
			$scope.list = response;
		});
	}

	// 分页
	$scope.findPage = function(page, rows) {
		typeTemplateService.findPage(page, rows).success(function(response) {
			$scope.list = response.rows;
			$scope.paginationConf.totalItems = response.total;// 更新总记录数
		});
	}

	// 查询实体
	$scope.findOne = function(id) {
		typeTemplateService.findOne(id).success(function(response) {
			$scope.entity= response;
			
			console.log(typeof(response.brandIds));
			console.log(JSON.parse(response.brandIds));
			
			
			//将字符串转换为JSON对象，因为从后端获取到的是一个字符串
			response.brandIds=JSON.parse(response.brandIds);
			response.specIds=JSON.parse(response.specIds);
			response.customAttributeItems=JSON.parse(response.customAttributeItems);
		});
	}

	// 保存
	$scope.save = function() {
		var serviceObject;// 服务层对象
		if ($scope.entity.id != null) {// 如果有ID
			serviceObject = typeTemplateService.update($scope.entity); // 修改
		} else {
			serviceObject = typeTemplateService.add($scope.entity);// 增加
		}
		console.log($scope.entity);
		serviceObject.success(function(response) {
			if (response.success) {
				// 重新查询
				$scope.reloadList();// 重新加载
			} else {
				alert(response.message);
			}
		});
	}

	// 批量删除
	$scope.dele = function() {
		if ($scope.selectIds<=0) {
			alert("请选中要删除的数据");
			return;
		}
		
		// 获取选中的复选框
		typeTemplateService.dele($scope.selectIds).success(function(response) {
			if (response.success) {
				$scope.reloadList();// 刷新列表
				$scope.selectIds = [];
			}
		});
	}

	$scope.searchEntity = {};// 定义搜索对象

	// 搜索
	$scope.search = function(page, rows) {
		typeTemplateService.search(page, rows, $scope.searchEntity).success(function(response) {
			$scope.list = response.rows;
			$scope.paginationConf.totalItems = response.total;// 更新总记录数
		});
	}

	// 查询下列列表
	$scope.brandList = {data:[]};
	$scope.specificationList = {data:[]};
	
	$scope.findBrandList=function(){
		brandService.selectOptionList().success(result=>{
			$scope.brandList={data:result};
		});
	};
	
	$scope.findSpecificationList=function(){
		specificationService.selectOptionList().success(result=>{
			$scope.specificationList={data:result};
		});
	};
	
	
	// 添加行
	$scope.addTabRow=function(){
		$scope.entity.customAttributeItems.push({});
	}
	
	// 删除行
	$scope.delTabRow=function(index){
		$scope.entity.customAttributeItems.splice(index,1);
	}
});

















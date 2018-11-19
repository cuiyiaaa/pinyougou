//控制层 
app.controller('goodsController', function($scope, $controller, goodsService, itemCatService,brandService) {

	$controller('baseController', {
		$scope : $scope
	});// 继承

	// 读取列表数据绑定到表单中
	$scope.findAll = function() {
		goodsService.findAll().success(function(response) {
			$scope.list = response;
		});
	}

	// 分页
	$scope.findPage = function(page, rows) {
		goodsService.findPage(page, rows).success(function(response) {
			$scope.list = response.rows;
			$scope.paginationConf.totalItems = response.total;// 更新总记录数
		});
	}

	// 查询实体
	$scope.findOne = function(id) {
		goodsService.findOne(id).success(function(response) {
			editor.html(response.goodsDesc.introduction);
			$scope.entity = response;
			$scope.entity.goodsDesc.introduction=editor.text();
			$scope.entity.goodsDesc.itemImages=JSON.parse(response.goodsDesc.itemImages);
			$scope.entity.goodsDesc.customAttributeItems=JSON.parse(response.goodsDesc.customAttributeItems);
			
			$scope.entity.goodsDesc.specificationItems=JSON.parse(response.goodsDesc.specificationItems);
			
			
			for (var i = 0; i < $scope.entity.itemList.length; i++) {
				$scope.entity.itemList[i].spec=JSON.parse($scope.entity.itemList[i].spec);
			}
		});
	}

	// 保存
	$scope.save = function() {
		var serviceObject;// 服务层对象
		if ($scope.entity.id != null) {// 如果有ID
			serviceObject = goodsService.update($scope.entity); // 修改
		} else {
			serviceObject = goodsService.add($scope.entity);// 增加
		}
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
		// 获取选中的复选框
		goodsService.dele($scope.selectIds).success(function(response) {
			if (response.success) {
				$scope.reloadList();// 刷新列表
				$scope.selectIds = [];
			}
		});
	}

	$scope.searchEntity = {};// 定义搜索对象

	// 定义审核状态
	$scope.status = [ '未审核', '已审核', '已驳回', '已关闭' ];

	// 搜索
	$scope.search = function(page, rows) {
		goodsService.search(page, rows, $scope.searchEntity).success(function(response) {
			$scope.list = response.rows;
			$scope.paginationConf.totalItems = response.total;// 更新总记录数
		});
	}

	// 查询所有的商品分类
	$scope.itemCatList=[];
	$scope.findItemCatAll = function() {
		itemCatService.findAll().success(result=>{
			for (var i = 0; i < result.length; i++) {
				$scope.itemCatList[result[i].id]=result[i].name;
			}
		});
	}
	
	// 查询所有的商品品牌
	$scope.brandList=[];
	$scope.findBrandList=function(){
		brandService.findAll().success(result=>{
			for (var i = 0; i < result.length; i++) {
				$scope.brandList[result[i].id]=result[i].name;
			}
		});
	}
	
	// 审核状态
	$scope.updateStatus=function(status){
		goodsService.updateStatus($scope.selectIds,status).success(result=>{
			if (result.success) {
				$scope.reloadList();// 刷新列表
				$scope.selectIds = [];
			}else {
				alert(result.message);
			}
		});
	}
	
	
	
});

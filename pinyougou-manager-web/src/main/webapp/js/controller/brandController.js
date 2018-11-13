//定义控制器
app.controller("brandController",function($scope,$controller,brandService){
	// 继承控制器，其实是一种伪继承，相当于将两个控制器的$scope进行了共享
	$controller('baseController',{$scope: $scope});
	
	// 查询品牌列表
	$scope.findBrandAll=function(){
		brandService.findAll().success(result=>{
			$scope.brandList=result;
		});
	};
		
	// 分页查询
	$scope.findPage=function(page,size){
		brandService.findPage(page, size).success(result=>{
			// 分页查询数据
			$scope.brandList=result.rows;
			// 更新总记录数
			$scope.paginationConf.totalItems=result.total;
		});		
	};
	
	// 初始化分页条件
	$scope.searchEntity={};
	// 分页条件查询
	$scope.search=function(page,size){
		brandService.search(page,size,$scope.searchEntity).success(result=>{
			// 分页查询数据
			$scope.brandList=result.rows;
			// 更新总记录数
			$scope.paginationConf.totalItems=result.total;
		});		
	}
	
	// 判断品牌是否存在
	$scope.findByName=function(){
		if ($scope.entity.id == null) {
			brandService.findByName($scope.entity.name).success(result=>{
				if (result.success) {
					angular.element(".btn-success").removeAttr("disabled");
				}else {
					alert(result.message);
					angular.element(".btn-success").prop("disabled","disabled");
				}
			});
		}
	}
		
	// 添加
	$scope.save=function(){
		var object=null;
		// 如果存在id，则为修改，如果没有id则为添加
		if ($scope.entity.id!=null) {
			object=brandService.update($scope.entity);
		}else {
			object=brandService.save($scope.entity);
		}
		
		object.success(result=>{
			if (result.success) {
				$scope.reloadList(); // 添加成功刷新成功
			}else {
				alert(result.message);
			}
		});
	};
	
	// 显示修改数据
	$scope.findOne=function(id){
		brandService.findOne(id).success(result=>{
			$scope.entity=result;
		});
	};
	
	// 删除
	$scope.del=function(){
		if ($scope.selectIds.length<=0) {
			alert("请选中删除的元素");
			return;
		}
		if (confirm("是否删除！")) {
			brandService.del($scope.selectIds).success(result=>{
				if (result.success) {
					$scope.reloadList(); // 删除成功刷新成功
				}else {
					alert(result.message);
				}
			});;
		}
	};
	
});







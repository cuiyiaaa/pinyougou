//控制层 
app.controller('sellerController', function($scope, $controller, sellerService) {

	$controller('baseController', {
		$scope : $scope
	});// 继承

	// 定义表单验证的正则
	$scope.mobileRegx = "^1(3[0-9]|4[57]|5[0-35-9]|7[01678]|8[0-9])\\d{8}$";
	$scope.emailRegx = "^[a-z]([a-z0-9]*[-_]?[a-z0-9]+)*@([a-z0-9]*[-_]?[a-z0-9]+)+[\.][a-z]{2,3}([\.][a-z]{2})?$";
	$scope.pwdRegx = "[a-zA-Z0-9]*";

	// 读取列表数据绑定到表单中
	$scope.findAll = function() {
		sellerService.findAll().success(function(response) {
			$scope.list = response;
		});
	}

	// 分页
	$scope.findPage = function(page, rows) {
		sellerService.findPage(page, rows).success(function(response) {
			$scope.list = response.rows;
			$scope.paginationConf.totalItems = response.total;// 更新总记录数
		});
	}

	// 查询实体
	$scope.findOne = function(id) {
		sellerService.findOne(id).success(function(response) {
			
		});
	}

	// 保存
	$scope.save = function() {
		sellerService.add($scope.entity).success(function(response) {
			if (response.success) {
				$scope.title="入驻申请成功";
				$scope.content="申请入驻成功，请耐心等待，三日之内我们会将审核结果发送至您的邮箱，请注意查收";
				$scope.flag=true; //用于判断是添加成功还是添加失败
				//弹出模态框
				$('#myModal').modal(); 
			} else {
				$scope.title="入驻申请失败";
				$scope.content=response.message;
				$scope.flag=false; //用于判断是添加成功还是添加失败
				//弹出模态框
				$('#myModal').modal(); 
			}
		});
	}

	// 批量删除
	$scope.dele = function() {
		// 获取选中的复选框
		sellerService.dele($scope.selectIds).success(function(response) {
			if (response.success) {
				$scope.reloadList();// 刷新列表
				$scope.selectIds = [];
			}
		});
	}

	$scope.searchEntity = {};// 定义搜索对象

	// 搜索
	$scope.search = function(page, rows) {
		sellerService.search(page, rows, $scope.searchEntity).success(function(response) {
			$scope.list = response.rows;
			$scope.paginationConf.totalItems = response.total;// 更新总记录数
		});
	}

	//关闭模块窗口
	$scope.close=function(){
		if ($scope.flag) {
			location.href="shoplogin.html";
		}else {
			$('#myModal').modal('hide'); 
		}
	};
	
	

});

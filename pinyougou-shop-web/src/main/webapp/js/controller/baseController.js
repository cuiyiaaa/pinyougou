/**
 * 定义各个控制器中公共的部分
 */
app.controller("baseController", function($scope) {
	// 刷新列表，因为很多地方都需要调用，所以将其提取出来
	$scope.reloadList = function() {
		$scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
	};

	// 引入分页组件配置
	$scope.paginationConf = {
		currentPage : 1, // 当前页
		totalItems : 10, // 总记录数
		itemsPerPage : 10, // 每页显示记录数
		perPageOptions : [ 10, 20, 30, 40, 50 ], // 下列列表
		onChange : function() {
			// 下列列表变化事件
			$scope.reloadList();
		}
	};

	// 存储删除的ID信息
	$scope.selectIds = [];

	// 设置选中的id值
	$scope.updateSelection = function($event, id) {
		// $event.target获取到checkbox
		if ($event.target.checked) {
			// 点击选中,则添加
			$scope.selectIds.push(id);

			var flag = true;
			$('.childBox').each(function() {
				if (!this.checked) {
					flag = false;
					return false;
				}
			});
			if (flag) {
				$("#selall").prop('checked', true);
			}
		} else {
			// 取消选中
			var index = $scope.selectIds.indexOf(id); // 查找元素在数组中的索引
			$scope.selectIds.splice(index, 1); // 参数1：起点，参数2：参数几个

			$("#selall").prop('checked', false);
		}
	};

	// 获取json中指定Key的值
	$scope.jsonTostring = function(jsonString, key) {
		// 将字符串转换为json
		var json = JSON.parse(jsonString);
		// 存储值
		var value = "";
		for (var i = 0; i < json.length; i++) {
			value += json[i][key];
			if (i < json.length - 1) {
				// 最后一个不添加逗号
				value += ",";
			}
		}
		return value;
	}

	/**
	 * 在集合中根据key的值查询对应 list:要查询的集合 key:查找的key keyValue:key的值
	 */
	$scope.searchObjKey = function(list, key, keyValue) {
		if (list != null) {
			for (var i = 0; i < list.length; i++) {
				if (list[i][key] === keyValue) {
					return list[i];
				}
			}
		}
		return null;
	};

})

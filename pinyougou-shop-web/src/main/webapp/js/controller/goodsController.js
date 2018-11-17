//控制层 
app.controller('goodsController', function($scope, $controller, goodsService,uploadService,itemCatService,typeTemplateService) {

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
			$scope.entity = response;
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

	// 保存
	$scope.add = function() {
		// 获取富文本编辑器的值，设置给商品介绍
		$scope.entity.goodsDesc.introduction=editor.html();
		
		// 获取商品一级分类的id
		if ($scope.entity.goods.category1Id===0) {
			alert("请选择商品分类!");
			return;
		}
		
		serviceObject = goodsService.add($scope.entity).success(function(response) {
			if (response.success) {
				// 重新查询
				alert("新增成功！");
				$scope.entity={goods:{},goodsDesc:{itemImages:[]}};
				// 清空富文本编辑器的值
				editor.html('');
				// 将选项卡切换至第一个
				window.location.reload();
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

	// 搜索
	$scope.search = function(page, rows) {
		goodsService.search(page, rows, $scope.searchEntity).success(function(response) {
			$scope.list = response.rows;
			$scope.paginationConf.totalItems = response.total;// 更新总记录数
		});
	}
	
	
	
	$scope.uploadFile=function(){
		// 判断用户是否选择了图片
		var file= angular.element("#file").prop('value');
		if (file==='') {
			alert("请选择上传的图片");
			return;
		}
		
		uploadService.uploadFile().success(result=>{
			if(result.success){
				// 将返回的url设置给图片，让图片显示
				$scope.img.entity.url=result.message;
			}else {
				alert("上传失败!!");
			}
		});
	}
	
	$scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]}};
	// 将当前上传的图片实体存入到图片列表
	$scope.add_img_entity=function(){
		
		$scope.entity.goodsDesc.itemImages.push($scope.img.entity);
		
	}
	
	// 删除图片
	$scope.del_img_entity=function(index){
		$scope.entity.goodsDesc.itemImages.splice(index,1);
	}
	
	// 清除上传图片后控件旁的图片名称
	$scope.clearfileName=function(){
		angular.element("#file").prop('value','');
	}
	
	// 加载商品分类一级下列列表
	$scope.itemCat1List=function(){
		// 设置下列列表默认显示的项
		$scope.itemCat1List=[{'id':0,'name':'--请选择商品分类--'}];
		$scope.entity.goods.category1Id=0;
		
		itemCatService.findByParentId(0).success(result=>{
			// 设置一级下列列表值
			$scope.itemCat1List=$scope.itemCat1List.concat(result);
		});
	};
	
	// 参数1：监控的变量 参数2：当监控的变量发送变化时执行的方法，newValue：发生变化后的值 oldValue：发生变化前的值
	$scope.$watch('entity.goods.category1Id',function(newValue,oldValue){
		// 当一级下拉类别清空三级列表
		$scope.itemCat3List=[];
		// 清空模板
		$scope.entity.goods.tyepTemplateId=undefined;
		// 清空扩展属性
		$scope.entity.goodsDesc.customAttributeItems=[];
		// 清空规格信息
		$scope.entity.itemList=[];
		$scope.entity.goodsDesc.specificationItems=[];
		$scope.entity.goods.IsEnableSpec=0;
		
		// 如果一级列表选择了 ‘请选择’ 则将二三级列表清空
		if (newValue===0) {
			$scope.itemCat2List=[];
			return;
		}
	
		itemCatService.findByParentId(newValue).success(result=>{
			// 设置二级下列列表值
			$scope.itemCat2List=result;
		});
	});
	
	// 三级列表
	$scope.$watch('entity.goods.category2Id',function(newValue,oldValue){
		
		 if (newValue===undefined) {
			 return;
		 }
		 // 当二级列表发生变化时清空模板
		$scope.entity.goods.tyepTemplateId=undefined;
		itemCatService.findByParentId(newValue).success(result=>{
			// 设置三级下列列表值
			$scope.itemCat3List=result;
		});
	});
	
	// 读取模板id
	$scope.$watch('entity.goods.category3Id',function(newValue,oldValue){
		if (newValue===undefined) {
			return;
		}
		itemCatService.findOne(newValue).success(result=>{
			// 设置模板Id
			$scope.entity.goods.tyepTemplateId=result.typeId;
		});
	});
	
	// 根据模板id查询该模板中包含的品牌
	$scope.$watch('entity.goods.tyepTemplateId',function(newValue,oldValue){
		if (newValue===undefined) {
			return;
		}
		 
		// 查询品牌
		typeTemplateService.findOne(newValue).success(result=>{
			// 设置模板Id
			$scope.brandList=JSON.parse(result.brandIds);
			// 设置默认值显示请选择品牌
			$scope.brandList.unshift({"id":0,"text":"---请选择品牌---"});
			$scope.entity.goods.brandId=0;
			
			// 获取扩展属性
			$scope.entity.goodsDesc.customAttributeItems=JSON.parse(result.customAttributeItems);
			
		});
		
		// 查询规格信息
		typeTemplateService.findSpecList(newValue).success(result=>{
			$scope.specList=result;
		});
	});
		
	
	// specificationItems
	$scope.updateSpecAttribute=function($event,attrKey,attrValue){
		var list=$scope.entity.goodsDesc.specificationItems;
		var obj=$scope.searchObjKey(list,'attributeName',attrKey);
		
		// 集合中没有该信息
		if(obj==null){
			list.push({'attributeName':attrKey,'attributeValue':[attrValue]});
		}else {
			// 选中
			if ($event.target.checked) {
				obj.attributeValue.push(attrValue);
				
			}else {
				// 取消选中
				// 只有当该数组中的元素对于1个时,才进行移除，否则直接将该条数据移除
				obj.attributeValue.splice(obj.attributeValue.indexOf(attrValue),1);
				if (obj.attributeValue.length == 0) {
					list.splice(list.indexOf(obj),1);
				}
			}
		}
		$scope.entity.goodsDesc.specificationItems=list;
	};
	
	
	
	$scope.createItemList=function(){
		$scope.entity.itemList=[{spec:{},price:0,num:9999,status:'0',isDefault:'0'}];
		
		// specList=[
		// {attributeName:'网络',attributeValues:['移动4G','移动3G']},
		// {attributeName:'内存',attributeValues:['16G','32G']}
		// ];
		var specList=$scope.entity.goodsDesc.specificationItems;
		
		for (var i = 0; i < specList.length; i++) {
			$scope.entity.itemList=addColumn($scope.entity.itemList,specList[i].attributeName,specList[i].attributeValue);
		}
	}
	
	/**
	 * 第一次循环后的返回值 ，下次循环就是在该返回值的基础上进行
	 * [{spec:{'网络':'移动4G'},price:0,num:9999,status:'0',isDefault:'0'},
	 * {spec:{'网络':'移动3G'},price:0,num:9999,status:'0',isDefault:'0'} ];
	 * 
	 * 第二次循环的返回值
	 * [{spec:{'网络':'移动4G','内存':'16G'},price:0,num:9999,status:'0',isDefault:'0'},
	 * {spec:{'网络':'移动4G','内存':'32G'},price:0,num:9999,status:'0',isDefault:'0'},
	 * {spec:{'网络':'移动3G','内存':'16G'},price:0,num:9999,status:'0',isDefault:'0'},
	 * {spec:{'网络':'移动3G','内存':'32G'},price:0,num:9999,status:'0',isDefault:'0'} ];
	 */
	addColumn=function(list,columnName,columnValues){
		var newList=[];
		
		for (var i = 0; i < list.length; i++) {
			var oldRow=list[i];
			for (var j = 0;j < columnValues.length;j++) {
				var newRow=JSON.parse(JSON.stringify(oldRow)); // 深克隆
				newRow["spec"][columnName]=columnValues[j];
				newList.push(newRow);
			}
		}
		return newList;
	}
});
//控制层 
app.controller('goodsController', function($scope, $controller, $location,goodsService,uploadService,itemCatService,typeTemplateService) {

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
		// 获取地址栏中的id参数
		var id=$location.search()['id'];	
		// 如果Id有值则进行查询
		if (id!=null) {
			goodsService.findOne(id).success(function(response) {
				$scope.entity = response;
				console.log($scope.entity);
				// 显示富文本编辑器的值
				editor.html(response.goodsDesc.introduction);
				// 将itemImages转换为json
				$scope.entity.goodsDesc.itemImages=JSON.parse(response.goodsDesc.itemImages);
				
				$scope.entity.goodsDesc.customAttributeItems=JSON.parse(response.goodsDesc.customAttributeItems);
				
				$scope.entity.goodsDesc.specificationItems=JSON.parse(response.goodsDesc.specificationItems);
				
				
				for (var i = 0; i < $scope.entity.itemList.length; i++) {
					$scope.entity.itemList[i].spec=JSON.parse($scope.entity.itemList[i].spec);
				}
				
			});
		}
	}

	// 保存
	$scope.save = function() {
		// 获取商品一级分类的id
		if ($scope.entity.goods.category1Id===0) {
			alert("请选择商品分类!");
			return;
		}
		
		// 获取富文本编辑器的值，设置给商品介绍
		$scope.entity.goodsDesc.introduction=editor.html();
		
		var serviceObject;// 服务层对象
		if ($scope.entity.goods.id != null) {// 如果有ID
			serviceObject = goodsService.update($scope.entity); // 修改
		} else {
			serviceObject = goodsService.add($scope.entity);// 增加
		}
		serviceObject.success(function(response) {
			if (response.success) {
				// 重新查询
				// 重新查询
				alert("保存成功！");
				$scope.entity={goods:{},goodsDesc:{itemImages:[]}};
				// 清空富文本编辑器的值
				editor.html('');
				// 跳转到商品列表页面
				window.location.href="goods.html";
			} else {
				alert(response.message);
			}
		});
	}
	
	// 批量删除
	$scope.dele = function() {
		
		if ($scope.selectIds.length==0) {
			alert("请选中删除的商品");
			return;
		}
		
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
	$scope.status=['未审核','已审核','已驳回','已关闭'];
	$scope.marketable=['已下架','已上架'];
	
	// 搜索
	$scope.search = function(page, rows) {
		goodsService.search(page, rows, $scope.searchEntity).success(function(response) {
			$scope.list = response.rows;
			$scope.paginationConf.totalItems = response.total;// 更新总记录数
		});
	}
	
	$scope.updateMarketable=function(marketable){
		goodsService.updateMarketable($scope.selectIds,marketable).success(result=>{
			if(result.success){
				// 将返回的url设置给图片，让图片显示
				alert("修改成功");
				$scope.reloadList();// 刷新列表
			}else {
				alert("修改失败!!");
			}
		});
	}
	
	
	$scope.itemCatList=[];
	
	// 查询商品的分类Id
	$scope.findItemCatAll=function(){
		itemCatService.findAll().success(result=>{
			for (var i = 0; i < result.length; i++) {
				$scope.itemCatList[result[i].id]=result[i].name;
			}
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
		
		if ($scope.entity.goods.id==null) {
			// 当一级下拉类别清空三级列表
			$scope.itemCat3List=[];
			// 清空模板
			$scope.entity.goods.tyepTemplateId=undefined;
			// 清空扩展属性
			$scope.entity.goodsDesc.customAttributeItems=[];
			// 清空规格信息
			$scope.entity.itemList=[];
			$scope.entity.goodsDesc.specificationItems=[];
			// $scope.entity.goods.IsEnableSpec="0";
		}
		
		// 如果一级列表选择了 ‘请选择’ 则将二三级列表清空
		if (newValue===0 || newValue== undefined) {
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
			$scope.brandList=[{"id":0,"text":"---请选择品牌---"}];
			$scope.entity.goods.brandId=0;
			return;
		}
		
		
		
		// 查询品牌
		typeTemplateService.findOne(newValue).success(result=>{
			// 设置模板Id
			$scope.brandList=JSON.parse(result.brandIds);
			// 设置默认值显示请选择品牌
			if ($scope.entity.goods.id==null) {
				$scope.brandList.unshift({"id":0,"text":"---请选择品牌---"});
				$scope.entity.goods.brandId=0;
				// 获取扩展属性
				$scope.entity.goodsDesc.customAttributeItems=JSON.parse(result.customAttributeItems);
			}
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
		
		if (list==null) {
			list=[];
		}
		
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
				// {spec:{},price:0,num:9999,status:'0',isDefault:'0'}
				var newRow=JSON.parse(JSON.stringify(oldRow)); // 深克隆
				newRow["spec"][columnName]=columnValues[j];
				newList.push(newRow);
			}
		}
		return newList;
	}
	
	// 用于判断是否选中规格选项
	$scope.isChecked=function(key,value){
		var specList= $scope.entity.goodsDesc.specificationItems;
		 
		var obj=$scope.searchObjKey(specList,'attributeName',key);
		if (obj!=null) {
			if(obj["attributeValue"].indexOf(value)>=0){
				return true;
			}
		}
		return false;
	}
	
});




















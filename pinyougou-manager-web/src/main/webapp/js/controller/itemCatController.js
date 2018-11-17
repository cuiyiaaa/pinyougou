 //控制层 
app.controller('itemCatController' ,function($scope,$controller,itemCatService,typeTemplateService){	
	
	$controller('baseController',{$scope:$scope});// 继承
	
    // 读取列表数据绑定到表单中
	$scope.findAll=function(){
		itemCatService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	// 分页
	$scope.findPage=function(page,rows){			
		itemCatService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;// 更新总记录数
			}			
		);
	}
	
	// 查询实体
	$scope.findOne=function(id){				
		itemCatService.findOne(id).success(
			function(response){
				$scope.entity= response;	
				
				// 获取下列列表的数据
				data=$scope.dataList["data"];
			
				for (var i = 0; i < data.length; i++) {
					if (response.typeId===data[i]["id"]) {
						$scope.entity.typeId=data[i];
						break;
					}
				}
			}
		);				
	}
	
	// 保存
	$scope.save=function(){				
		var serviceObject;// 服务层对象
		
		// $scope.entity.typeId会获得到下列列表的json数据
		$scope.entity.typeId=$scope.entity.typeId["id"];
		
		if($scope.entity.id!=null){// 如果有ID
			serviceObject=itemCatService.update( $scope.entity); // 修改
		}else{
			$scope.entity.parentId=$scope.parentId;
			serviceObject=itemCatService.add($scope.entity);// 增加
		}
		
		console.log($scope.entity);
		
		serviceObject.success(
			function(response){
				if(response.success){
					// 重新查询
					$scope.findByParentId($scope.parentId);
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	// 批量删除
	$scope.dele=function(){			
		// 获取选择的Id
		var ids=$scope.selectIds;
		console.log(ids);
		
		if (ids.length <= 0) {
			alert("请选择要删除的选项！");
			return;
		}
		
		itemCatService.dele(ids).success(function(response){
			 if(response.success){ 
				 var str=response.message.toString();
				 alert("无法删除Id为："+str.substring(1,str.length-1)+"的类别，该类别下存在其他类别");
				 $scope.findByParentId($scope.parentId);
				 $scope.selectIds=[]; 
			 } 
	    });
	}
	
	$scope.searchEntity={};// 定义搜索对象
	
	// 搜索
	$scope.search=function(page,rows){			
		itemCatService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;// 更新总记录数
			}			
		);
	}
	
	// 根据上级ID查询分类列表
    $scope.findByParentId=function(parentId){
    	itemCatService.findByParentId(parentId).success(result=>{
    		$scope.list=result;
    	});
    }
    
    
    // 面包屑级别
    $scope.grade=1;
    
    $scope.setGrade=function(value){
    	 $scope.grade=value;
    }
    
    // 面包屑导航
    $scope.breadcrumbNav=function(entity){
    	// 存储父级Id
    	$scope.parentId=entity.id;
    	
    	// 处于顶级列表
    	if ($scope.grade == 1) {
    		// 如果处于顶级列表
			$scope.entity_1=null;
			$scope.entity_2=null;
		}
    	
    	// 处于第二级列表
    	if ($scope.grade == 2) {
			$scope.entity_1=entity;
			$scope.entity_2=null;
		}
    	
    	// 处于第三级列表
    	if ($scope.grade == 3) {
			$scope.entity_2=entity;
		}
    	
    	 $scope.findByParentId(entity.id)	
    }
    
   
   /**
	 * 加载类型下拉列表
	 */
   $scope.dataList={data:[]};
 
   $scope.selectTempList=function(){
	   typeTemplateService.selectOptionList().success(result=>{
		   $scope.dataList={data:result};
	   });
   }
});	












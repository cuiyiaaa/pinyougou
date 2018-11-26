/**
 * 
 */
app.controller("searchController", function($scope, $location,searchService) {
	// 定义查询条件 keywords:查询条件 category商品分类 brand商品品牌 spec:规格信息
	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','page':1,'size':40,'sort':'','sortField':''};

	$scope.search=function(){
		searchService.search($scope.searchMap).success(result=>{
			$scope.resultMap=result;
			console.log($scope.resultMap);
			
			bulidPageLabel();
			console.log($scope.searchMap);
		});
	}
	
	bulidPageLabel=function(){
		$scope.beginDot=true; // 开始显示点
		$scope.endDot=true;// 结束显示点
		
		$scope.pageLabel=[];
		
		var begin=1;
		var end=$scope.resultMap.totalPage;
		
		if (end > 5) {
			// 当页码超过了5页 6 4 8
			begin= $scope.searchMap.page -2;
			end =  $scope.searchMap.page +2 ;
			
			if (begin < 1) {
				begin = 1;
				end = 5;
				$scope.beginDot=false;
			}
			
			// 10页 9
			if (end >  $scope.resultMap.totalPage) {
				begin = $scope.resultMap.totalPage-4;
				end= $scope.resultMap.totalPage;
				$scope.endDot=false;
			}
			
		}else {
			$scope.beginDot=false; // 开始不显示点
			$scope.endDot=false;// 结束不显示点
		}
		
		
		for (var i = begin; i <=end ; i++) {
			$scope.pageLabel.push(i);
		}

	}
	
	// 设置查询条件
	$scope.addSearchItem=function(key,value){
		
		if (key==='category' || key==='brand' || key==='price' || key === 'keywords') {
			$scope.searchMap[key]=value;
		}else {
			// 规格
			$scope.searchMap.spec[key]=value;
		}
		
		$scope.search();
	}
	
	
	// 移除查询条件
	$scope.removeSearchItem=function(key){
		if (key==='category' || key==='brand') {
			$scope.searchMap[key]='';
		}else {
			// 规格
			delete $scope.searchMap.spec[key];
		}
		$scope.search();
	}
	
	
	// 分页查询
	$scope.queryByPage=function(page){
		if (page < 1 ) {
			page=1;
		}
		
		if (page > $scope.resultMap.totalPage) {
			page= $scope.resultMap.totalPage
		}
		
		$scope.searchMap["page"]=page;
		$scope.search();
	};
	
	
	// 排序查询
	$scope.queryByStor=function(sortField,sort){
		$scope.searchMap["sort"]=sort;
		$scope.searchMap["sortField"]=sortField;
		
		$scope.search();
	}
	
	// 判断关键字是否是品牌
	$scope.keywordsIsBrand=function(){
		for (var i = 0; i < $scope.resultMap.brandList.length; i++) {
			if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text) >= 0) {
				return true;
			}
		}
		return false;
	}
	
	// 加载关键字
	$scope.loadkeywords=function(){
		$scope.searchMap["keywords"] = $location.search()['keywords'];
		
		$scope.searchKey=$scope.searchMap["keywords"];
		
		$scope.search();
	}
});



 






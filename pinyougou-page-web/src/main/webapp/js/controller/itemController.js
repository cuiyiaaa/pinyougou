/**
 * 
 */
app.controller("itemController", function($scope) {
	$scope.num=1;
	
	$scope.changeNum=function(x){
		//因为直接从文本框输入的话，$scope.num将变为string类型
		$scope.num=parseInt($scope.num);
		$scope.num += x;
		console.log($scope.num);
		if($scope.num < 1){
			$scope.num=1;
		}
	};
	
	//用户点击选择的规格
	$scope.sepecification={};
	
	$scope.selectSepecification=function(key,value){
		$scope.sepecification[key]=value;
		console.log($scope.sepecification);
		
		for(var i=0;i<skuList.length;i++){
			/*
				将skuList集合中的每一个spec与 用户选择的规格进行比较
			*/
			if(matchObject(skuList[i].spec,$scope.sepecification)){
				
				$scope.sku=skuList[i];
				return;
			}
		}
		alert("暂未存在该规格信息");
	}
	
	$scope.isSelected=function(key,value){
		if($scope.sepecification[key]==value){
			return true;
		}
		return false;
	}
	
	$scope.sku={};//当前选择的sku
	
	//加载默认的sku
	$scope.initSku=function(){
		$scope.sku=skuList[0];
		$scope.sepecification=JSON.parse(JSON.stringify($scope.sku["spec"]));
	}
	
	//匹配两个对象是否相等
	matchObject=function(map1,map2){
		//map1:{"网络":"移动4G","机身内存":"128G"}
		//map2:{"网络":"移动4G","机身内存":"128G","颜色":"土豪金"}
		
		//通过map1 匹配map2
		for(var key in map1){
			//key 就是 网络，机身内存
			if(map1[key] != map2[key]){
				return false;
			}
		}
		
		//通过map2 匹配 map1 进行反向对比，防止map2比map1多一个属性时也相等
		for(var key in map2){
			//key 就是 网络，机身内存
			if(map2[key] != map1[key]){
				return false;
			}
		}
		
		return true;
	};
	
	//添加商品到购物车
	$scope.addToCart=function(){
		alert("SKUID:"+$scope.sku["id"]);
	}
});














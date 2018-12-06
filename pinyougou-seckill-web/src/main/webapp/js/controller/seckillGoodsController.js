 //控制层 
app.controller('seckillGoodsController' ,function($scope,$location,$interval,$controller,seckillGoodsService){	
	
	$controller('baseController',{$scope:$scope});// 继承
	
    // 读取列表数据绑定到表单中
	$scope.findAll=function(){
		seckillGoodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	// 分页
	$scope.findPage=function(page,rows){			
		seckillGoodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;// 更新总记录数
			}			
		);
	}
	
	// 查询实体
	$scope.findOne=function(id){				
		seckillGoodsService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	// 保存
	$scope.save=function(){				
		var serviceObject;// 服务层对象
		if($scope.entity.id!=null){// 如果有ID
			serviceObject=seckillGoodsService.update( $scope.entity ); // 修改
		}else{
			serviceObject=seckillGoodsService.add( $scope.entity  );// 增加
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					// 重新查询
		        	$scope.reloadList();// 重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	// 批量删除
	$scope.dele=function(){			
		// 获取选中的复选框
		seckillGoodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();// 刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};// 定义搜索对象
	
	// 搜索
	$scope.search=function(page,rows){			
		seckillGoodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;// 更新总记录数
			}			
		);
	}
	
	$scope.findList=function(){
		seckillGoodsService.findList().success(result=>{
			$scope.seckillList=result;
		})
	}  
	
	/**
	 * 
	 */
	$scope.findOneToRedis=function(){
		seckillGoodsService.findOneToRedis($location.search()['id']).success(result=>{
			$scope.entity=result;
			console.log($scope.entity);
			if($scope.entity==""){
				alert("商品不存在");
				location.href="seckill-index.html";
			}
			
			// 计算结束时间到当前时间的秒数
			allSecond=Math.floor((new Date($scope.entity.endTime).getTime()- new Date().getTime())/1000);
			
			timer=$interval(function(){
				allSecond--;
				if (allSecond <= 0) {
					$interval.cancel(timer);
				}
				$scope.timeStr=converTimeString(allSecond);
			},1000);
			
		
		});
	}
	
	// 将秒转换位 xx天xx小时xx分xx秒
	converTimeString=function(second){
		
		var day= Math.floor(second/(60*60*24));// 天数
		
		var hours=Math.floor((second-(day*60*60*24))/(60*60)); // 小时数
		
		var minutes=Math.floor((second-(day*60*60*24)-(hours*60*60))/60);// 分钟数
		
		var seconds=second-(day*60*60*24)-(hours*60*60)-(minutes*60); // 分钟数
		 
		var timeStr="";
		if (day<10) {
			day="0"+day;
		}
		if (day==0) {
			timeStr=hours+":"+minutes+":"+seconds;
		}else {
			timeStr=day+"天 "+hours+":"+minutes+":"+seconds;
		}
		
		return timeStr;
	}
	
	/**
	 * 秒杀下单
	 */
	$scope.submitOrder=function(){
		seckillGoodsService.submitOrder($scope.entity.id).success(result=>{
			if (result.success) {
				// 秒杀成功
				location.href='pay.html';
			}else {
				if (result.message=="未登录") {
					location.href='login.html?id='+$scope.entity.id;
				}else {
					alert(result.message);
				}
			}
		});
	}
});	




















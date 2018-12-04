/**
 * 购物车控制层
 */
app.controller('cartController',function($scope,cartService,addressService){
	
	// 查询购物车列表
	$scope.findCartList=function(){
		cartService.findCartList().success(result=>{
			$scope.cartList=result;
			$scope.toatlValue=cartService.sum(result);
		});
	}
	
	// 查询删除购物车
	$scope.findDelCartList=function(){
		cartService.findDelCartList().success(result=>{
			$scope.deleteItemsList=result;
			console.log($scope.deleteItemsList);
		});
	}
	
	// 添加商品
	$scope.addCart=function(itemId,num,source){
		cartService.addCart(itemId,num,source).success(result=>{
			// 添加成功，更新列表
			if (result.success) {
				$scope.findCartList();
			}else {
				// 添加失败
				alert(result.message);
			}
		});
	}
	
	// 重新添加商品
	$scope.resetAddCart=function(itemId,num,source){
		cartService.resetAddCart(itemId,num,source).success(result=>{
			// 添加成功，更新列表
			if (result.success) {
				$scope.findCartList();
				$scope.findDelCartList();
			}else {
				// 添加失败
				alert(result.message);
			}
		});
	}
	
	/**
	 * 通过文本框输入来更新商品数量
	 */
	$scope.textNum=function(orderItem){
		// 对数量进行判断
		var num=parseInt(orderItem['num']);
		if (isNaN(num) || num<0) {
			$scope.numFlag=true;
			$scope.numMessage='商品数量不正确';
			orderItem['num']=$scope.old[orderItem.goodsId];
			return;
		}
		
		// 商品数量合法，进行更新
		$scope.addCart(orderItem.itemId,num,'1');
	}
	
	/**
	 * 使用+,-更新数量
	 */
	$scope.updateNum=function(itemId,num){
		if (num <1) {
			return;
		}
		$scope.addCart(itemId,num,'1');
	}
	
	/**
	 * 关闭窗口
	 */
	$scope.dialog=function(){
		$scope.numFlag=false;
	}
	
	$scope.old={};
	/**
	 * 保存合法的，用于还原num
	 */
	$scope.saveNum=function(id,num){
		$scope.old[id]=num;
	}
	
	/**
	 * 删除商品
	 */
	$scope.deleteItems=function(itemId){
		cartService.deleteItems(itemId).success(result=>{
			if (result.success) {
				$scope.findDelCartList();
				$scope.findCartList();
			}
		});
	}
	
	
	/**
	 * 查询地址列表
	 */
	$scope.findAddressList=function(){
		addressService.findAddressList().success(result=>{
			$scope.addressList=result;
			
			for (var i = 0; i < $scope.addressList.length; i++) {
				if ($scope.addressList[i].isDefault=='1') {
					$scope.address=$scope.addressList[i];
					break;
				}
			}
		});
	}
	
	
	/**
	 * 选中地址
	 */
	$scope.selectAddress=function(address){
		$scope.address=address;
		
	}
	
	/**
	 * 判断当前地址是否被选中
	 */
	$scope.isSelectAddress=function(address){
		if ($scope.address==address) {
			return true;
		}
		return false;
	}
	
	$scope.order={paymentType:'1'}; // 默认为1 微信支付
	
	$scope.selectPaymentType=function(type){
		$scope.order['paymentType']=type;
	}
	
	/**
	 * 提交订单
	 */
	$scope.submitOrder=function(){
		//收货地址
		$scope.order.receiverAreaName=$scope.address.address;
		//手机号
		$scope.order.receiverMobile=$scope.address.mobile;
		//收货人
		$scope.order.receiver=$scope.address.contact;
		
		cartService.submitOrder($scope.order).success(result=>{
			if (result.success) {
				//如果微信支付跳转到微信支付页面
				if ($scope.order['paymentType']=='1') {
					location.href="pay.html";
				}else {
					location.href="paysuccess.html";
				}
				//如果是货到付款则跳转到提示页面
			}else {
				alert("提交失败！！！");
			}
		});
	}
	
	/**
	 * 获取当前登录用户名
	 */
	$scope.findLoginName=function(){
		cartService.findLoginName().success(result=>{
			//去除两端的冒号
			var loginName=result.substr(1);
			loginName=loginName.substring(0,loginName.length-1);
			
			$scope.loginName=loginName;
		});
	}
});























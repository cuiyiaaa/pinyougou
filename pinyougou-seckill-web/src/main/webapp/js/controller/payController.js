/**
 * 
 */
app.controller('payController', function($scope,$location,payService) {
	
	/**
	 * 生成支付二维码
	 */
	$scope.createNavite=function(){
		payService.creteNavite().success(result=>{
			//总金额
			$scope.total_fee=(result.total_fee/100).toFixed(2);
			//订单号
			$scope.out_trade_no=result.out_trade_no;
			//生成二维码
			var qr=new QRious(
	            {
	                element:document.getElementById('qrious'),
	                size:250, 
	                value:result.code_url,
	                level:'H'
	            }
	        );
			//生成二维码后对订单状态进行检测
			queryPayStatus($scope.out_trade_no);
		});
	}
	
	/**
	 * 检查支付状态
	 */
	queryPayStatus=function(out_trade_no){
		payService.queryPayStatus(out_trade_no).success(result=>{
			if (result.success) {
				//支付成功，跳转成功页面
				location.href='paysuccess.html#?money='+$scope.total_fee;
			}else {
				if (result.message=="支付超时") {
					//重新生成二维码
					alert("支付超时，订单已取消");
					location.href='seckill-index.html';
				}else {
					//支付失败，跳转失败页面
					location.href='payfail.html';
				}
			}
		});
	}
	
	$scope.getMoeny=function(){
		console.log("11");
		return $location.search()['money'];
	}
});
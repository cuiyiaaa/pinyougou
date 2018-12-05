/**
 * 
 */
app.service('payService',function($http){
	/**
	 * 生成支付二维码
	 */
	this.creteNavite=function(){
		return $http.get('pay/createNative.do');
	}
	
	/**
	 * 查询订单支付状态
	 */
	this.queryPayStatus=function(out_trade_no){
		return $http.get('pay/queryPayStatus.do?out_trade_no='+out_trade_no);
	}
});
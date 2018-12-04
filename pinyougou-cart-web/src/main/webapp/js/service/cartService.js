/**
 * 购物车service层
 */
app.service('cartService', function($http) {

	// 获取购物车列表
	this.findCartList = function() {
		return $http.get('cart/findCartList.do');
	}

	// 添加商品到购物车
	this.addCart = function(itemId, num, source) {
		return $http.get('cart/addCart.do?itemId=' + itemId + '&num=' + num + '&source=' + source);
	}
	
	// 添加商品到购物车
	this.resetAddCart = function(itemId, num, source) {
		return $http.get('cart/resetAddCart.do?itemId=' + itemId + '&num=' + num + '&source=' + source);
	}
	
	/**
	 * 计算数量和总价
	 */
	this.sum=function(cartList){
		var totalNum=0; // 总数量
		var totalFee=0; // 总金额
		
		for (var i = 0; i <cartList.length; i++) {
			var cart =cartList[i]['orderItemList']; //商家购物车对象
			for (var j = 0; j <cart.length; j++) {
				var orderIetm=cart[j]; //购物车明细对象
				totalNum+=orderIetm['num'];
				totalFee+=orderIetm['totalFee'];
			}
		}
		return {'totalNum':totalNum,'totalFee':totalFee};
	}
	
	/**
	 * 删除商品
	 */
	this.deleteItems=function(itemId){
		return	$http.get('cart/deleteItems.do?itemId=' + itemId );
	}
	
	/**
	 * 查询删除购物车信息
	 */
	this.findDelCartList=function(){
		return $http.get('cart/findDelCartList.do');
	}
	
	/**
	 * 提交订单
	 */
	this.submitOrder=function(order){
		return $http.post('order/add.do',order);
	}
	
	this.findLoginName=function(){
		return $http.get('cart/findLoginName.do');
	}
});













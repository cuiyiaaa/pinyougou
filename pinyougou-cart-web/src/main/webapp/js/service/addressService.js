/**
 * 
 */
app.service('addressService',function($http){
	
	/**
	 * 查询地址列表
	 */
	this.findAddressList=function(){
		return $http.get('address/findListByLoginUser.do');
	}
});

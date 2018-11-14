/**
 * 
 */
app.controller('indexController', function($scope,loginService) {
	
	// 获取登录的用户名
	$scope.showLoginName=function(){
		loginService.getLoginName().success(result=>{
			$scope.loginName=result.loginName;
		});		
	};
});
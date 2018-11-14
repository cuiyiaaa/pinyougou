/**
 * 
 */


app.controller('indexController', function($scope,loginService) {
	
	//获取登录的用户名
	$scope.findLoginName=function(){
		loginService.getLoginName().success(result=>{
			$scope.loginName=result.loginName;
		});
	};
});
//控制层 
app.controller('userController', function($scope,$interval, $controller, userService) {
	
	$scope.entity={};
	
	// 用户注册
	$scope.reg = function() {
		userService.add($scope.entity,$scope.smsCode).success(result=>{
			if (result.success) {
				alert(result.message);
			}else {
				alert(result.message);
			}
		});
	};
	
	// 校验两次密码
	$scope.checkPwd=function(){
		
		if ($scope.password=='') {
			$scope.pwdError=false;
			return;
		}
		
		// 两次密码不一致
		if ($scope.password != $scope.entity.password) {
			$scope.password="";
			$scope.entity.password="";
			$scope.pwdError=false;
			return;
		}
		
		// 执行到这里，说明两次密码一致
		$scope.pwdError=true;
	}
	
	
	/**
	 * 发送验证码
	 */
	$scope.send=function(){
		if ($scope.entity.phone==null ||$scope.entity.phone=='') {
			alert("请填写手机号！！！");
			return;
		}
		// 发送验证码
		userService.sendSmsCode($scope.entity.phone).success(result=>{
			if (result.success) {
				// 发送成功
				alert(result.message);
			}else {
				// 发送失败
				alert(result.message);
			}
		});
	}
	
	
	// 验证码倒计时
	$scope.countDown=function(){
		$scope.smsCode="";
		if ($scope.entity.phone==null ||$scope.entity.phone=='') {
			return;
		}
	
	    $scope.message="60s后重新发送";
		var count=60;
	    var timer=$interval(function () {
		    count--;
			$scope.message=count+"s后重新发送";
			if (count==0) {
				$scope.message="重新发送";
				$interval.cancel(timer);
			}
		},1000);
	}
});




/**
 * 图片上传服务
 */
app.service("uploadService",function($http){
	//上传文件
	this.uploadFile=function(){
		//上传的文件
		var formData=new FormData();
		//参数1:文件上传控件的name 参数2：file.files[0]获取第一个
		formData.append('file',file.files[0]);
		
		return $http({
			url:'../upload.do',
			method:'post',
			data:formData,
			headers:{'Content-Type':undefined},
			//对表单进行二进制序列化
			transformRequest:angular.inentity
		});
	}
});
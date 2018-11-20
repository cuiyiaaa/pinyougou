/**
 * 
 */
app.service("contentService", function($http) {
	//根据广告分类ID查询广告
	this.findByCategoryId = function(id) {
		return $http.get('content/findByCategoryId.do?id=' + id);
	}
});
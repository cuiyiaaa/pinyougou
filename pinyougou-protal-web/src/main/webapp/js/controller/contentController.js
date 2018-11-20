/**
 * 
 */
app.controller("contentController", function($scope, contentService) {

	$scope.categoryList = [];
	$scope.findByCategoryId = function(id) {
		contentService.findByCategoryId(id).success(function(response) {
			$scope.categoryList[id] = response;
			console.log($scope.categoryList);
		});
	}
});
var app = angular.module("pinyougou", []);

// 定义过滤器

app.filter('toDecimal', function() {
	return function(data) {
		var f = parseFloat(data);
		if (isNaN(f)) {
			return 0;
		}
		var f = Math.round(data * 100) / 100;
		var s = f.toString();
		var rs = s.indexOf('.');
		if (rs < 0) {
			rs = s.length;
			s += '.';
		}
		while (s.length <= rs + 2) {
			s += '0';
		}
		return s;
	}
});

app.filter('toMobile', function() {
	return function(data) {
		if (data==null || data == undefined) {
			return "";
		}
		var begin=data.substr(0,3);
		var end=data.substr(-4,4);
		return begin+"****"+end;
	}
});







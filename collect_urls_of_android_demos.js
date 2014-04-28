var list = {};
$('#nav').children('.nav-section').each(function(_, s1){
	var title = $(s1).children('.nav-section-header').children('a').attr('title');
	if(title){
		list[title] = [];
		$(s1).children('ul').children('.nav-section').each(function(i, s2){
			var name= $(s2).children('.nav-section-header').children('a').attr('title');
			list[title][i] =  'http://developer.android.com/downloads/samples/' + name + '.zip';
		});
	}
});

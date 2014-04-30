var list = {}, files = {};

var stack = [];

function parseFileLists($section, files){
    var title = $section.children('.nav-section-header').children('a').attr('title');
    if(title){
        files[title] = {};
        $section.children('ul').children('li').each(function(_, li){
            parseFileLists($(li), files[title]);
        });
    } else {
        var $a = $section.children('a');
        if($a.length > 0){
            var url = $a.attr('href');
            var fileName = $a.attr('title');
            files[fileName] = url;
        }
    }
}

function getFiles(list, files){
    for(var name in list){
        var value = list[name];
        if(typeof value == 'string'){
            //getFileContent(name, value, files);
            stack.push([name, value, files]);
        } else {
            files[name] = {};
            getFiles(list[name], files[name]);
        }
    }
}

function getFileContent(index){
    var obj = stack[index], name = obj[0], url = obj[1], files = obj[2];
    $.get(url, function(html){
        var $ctn = $(html).find('#codesample-wrapper');
        var $img = $(html).find('#codesample-resource').children('img');
        if($img.length > 0){
            var imgUrl = '', src = $img.attr('src');
            if(src[0] == '/'){
                imgUrl = 'http://developer.android.com/' + src;
            } else {
                var arr = url.split('/');
                arr[arr.length -1] = src;
                imgUrl = 'http://developer.android.com/' + arr.join('/');
            }
            files[name] = imgUrl;
        }else {
            var code = '', content = $ctn.text(), arr = content.split('\n');
            for(var i=0;i<arr.length;i++){
                if(/^\d+$/.test($.trim(arr[i]))){
                    arr[i] = '';
                }
            }
            code = arr.join('\n');
            files[name] = code;
        }
        index++;
        if(index < stack.length){
            getFileContent(index);
        } else {
            console.log('done !');
        }
    });
}

function runStack(){
    getFileContent(0);
}

$('#nav').children('.nav-section').each(function(i, section){
    parseFileLists($(section), list);
});

getFiles({UI:list['UI'], Views: list['Views']}, files);
runStack();
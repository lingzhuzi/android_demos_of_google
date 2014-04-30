var jsdom = require('jsdom').jsdom
, http = require('http')
, fs = require('fs')
, jquery = fs.readFileSync("./jquery-2.1.0.min.js", "utf-8");

var list = {};

/**
 * 使用jsdom将html跟jquery组装成dom
 * @param  {[type]}   html     需要处理的html
 * @param  {Function} callback 组装成功后将html页面的$对象返回
 * @return {[type]}            [description]
 */
 function makeDom(html, callback) {
  jsdom.env({
    html: html,
    src: [jquery],
    done: function (errors, window) {
      var $ = window.$;
      callback(errors, $);
      window.close();   // 释放window相关资源，否则将会占用很高的内存
    }
  });
}

function getFileLists(){
  var options = {
    hostname: 'developer.android.com',
    port: 80,
    path: '/intl/zh-cn/samples/index.html',
    method: 'get'
  };

  var req = http.request(options, function(res){
    var html = '';
    res.setEncoding('utf8');
    res.on('data', function (chunk) { html += chunk; });
    res.on('end', function () {
      parseHtml(html);  // 对html做解析处理
    });
  });

  req.on('error', function(e) {
    console.log(('请求列表页失败: ' + e.message).red);
  });

  // write data to request body
  req.write('data\n');
  req.write('data\n');
  req.end();
}

function parseHtml(html){
  makeDom(html, function (errors, $) {
    $('#nav').children('.nav-section').each(function(_, li){
      parseFileLists($(li), list);
    });
    console.log(list);
  }
}

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

getFileLists();
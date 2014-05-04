// notice: have no test yet !

var fs = require('fs'),
    $ = require('jquery');

var fileList = {},
    images = {},
    jsonFileName = 'file_list_json';


function getFileLists() {
    fs.exists(jsonFileName, function (exists) {
        if (exists) {
            fs.readFile(jsonFileName, 'utf8', function (err, data) {
                if (err) {
                    throw err;
                }
                fileList = JSON.parse(data);
            });
        } else {
            parseIndexPage();
        }
    })
}

function parseIndexPage() {
    $.ajax({
        type: 'get',
        url: 'http://developer.android.com/samples/index.html',
        success: function (doc) {
            $(doc).find('#nav').children('.nav-section').each(function (_, li) {
                parseFileLists($(li), fileList);
            });
            //写入文件
            var text = JSON.stringify(fileList);
            fs.writeFile(jsonFileName, txt, function (err) {
                if (err) throw err;
                console.log('It\'s saved!'); //文件被保存
            });
        }
    });
}


function parseFileLists($section, files) {
    var title = $section.children('.nav-section-header').children('a').attr('title');
    if (title) {
        files[title] = {};
        $section.children('ul').children('li').each(function (_, li) {
            parseFileLists($(li), files[title]);
        });
    } else {
        var $a = $section.children('a');
        if ($a.length > 0) {
            var url = $a.attr('href');
            var fileName = $a.attr('title');
            files[fileName] = url;
        }
    }
}

function saveCodes(list, path) {
    for (var name in list) {
        if (list.hasOwnProperty(name)) {
            var value = list[name];
            if (typeof value == 'string') {
                parseAndSave(path, name, value);
            } else {
                var dir_path = path + name.split('.').join('/') + '/';
                saveCodes(value, dir_path);
            }
        }
    }
}

function parseAndSave(path, name, url) {
    fs.exists(path + name, function (exists) {
        if (!exists) {
            parseCodePage(path, name, url, function (file_path, file_name, code) {
                saveCode(file_path, file_name, code);
            });
        }
    });
}

function parseCodePage(path, name, url, callback) {
    var code = '';
    if (isImage(name)) {
        code = 'http://developer.android.com' + (url[0] == '/' ? '' : '/') + url;
        callback(path, name, code);
    } else {
        $.get("http://developer.android.com" + url, function (html) {
            var content = $(html).find('#codesample-wrapper').text();
            var arr = content.split("\n");
            for (var i = 0; i < arr.length; i++) {
                if (!/\d/.test($.trim(arr[i])) && $.trim(arr[i]) != '') {
                    arr.push(arr[i]);
                }
            }
            var code = arr.join('\n');
            callback(path, name, code);
        });
    }
}

function saveCode(path, name, code) {
    mkdirs(path, function () {
        if (isImage(name)) {
            var arr = code.split('/');
            var imgName = arr[arr.length - 2] + '/' + arr[arr.length - 1];
            if (images[imgName]) {
                copy(images[imgName], path + name);
            } else {
                downloadImage(path + name, code);
            }
        } else {
            fs.exists(path + name, function (exists) {
                if (!exists) {
                    console.log('saving ' + path + name);
                    fs.writeFile(path + name, code, function (err) {
                        if (err) throw err;
                        //console.log('It\'s saved!'); //文件被保存
                    });
                }
            })
        }
    });
}

function copy(from, to) {
    console.log('copying file to ' + to);
    var fileReadStream = fs.createReadStream(from);
    var fileWriteStream = fs.createWriteStream(to);
    fileReadStream.pipe(fileWriteStream);

    fileWriteStream.on('close', function () {
        console.log('copy over');
    });
}

function downloadImage(path, url) {
    console.log('downloading image from ' + url);
    $.get(url, function (data) {
        fs.writeFile(path, data, function (err) {
            if (err) {
                throw err;
            }

            console.log('download over and saved to ' + path);
            var arr = url.split('/');
            var imgName = arr[arr.length - 2] + '/' + arr[arr.length - 1];
            images[imgName] = path;
        })
    })

}

function isImage(name) {
    var arr = name.split('.');
    return ['png', 'jpg', 'git'].indexOf(arr[arr.length - 1]) > -1;
}

// 创建所有目录
function mkdirs(dirpath, mode, callback) {
    fs.exists(dirpath, function (exists) {
        if (exists) {
            callback(dirpath);
        } else {
            //尝试创建父目录，然后再创建当前目录
            mkdirs(path.dirname(dirpath), mode, function () {
                fs.mkdir(dirpath, mode, callback);
            });
        }
    });
}

getFileLists();
saveCodes(fileList, './');
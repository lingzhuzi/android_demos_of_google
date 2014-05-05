/**
 * Install
 *   before run this program, you should install needed libraries
 *   this is a node.js program, so install node.js first ( see http://nodejs.org/)
 *   after that, run command blow to install some node.js modules
 *   npm install jsdom jquery
 *
 * Usage
 *   node android_demos_downloader.js
 */

var fs = require('fs'),
    http = require('http'),
    jsdom = require('jsdom').jsdom,
    window = jsdom().createWindow(),
    path = require('path'),
    $ = require('jquery')(window);

var fileList = {},
//    images = {},
    stack = [],
    jsonFileName = 'file_list_json',
    webUrl = 'http://developer.android.com';


function getFileLists(callback) {
    fs.exists(jsonFileName, function (exists) {
        if (exists) {
            console.log('read file list from file: ' + jsonFileName);
            fs.readFile(jsonFileName, 'utf8', function (err, data) {
                if (err) {
                    throw err;
                }
                fileList = JSON.parse(data);
                callback();
            });
        } else {
            parseIndexPage(callback);
        }
    })
}

function parseIndexPage(callback) {
    var indexPageUrl = webUrl + '/samples/index.html';
    console.log('parse file list from ' + indexPageUrl);
    http.get(indexPageUrl, function (res) {
        var html = '';
        res.on('data', function (chunk) {
            html += chunk;
        });
        res.on('end', function () {
            $(html).find('#nav').children('.nav-section').each(function (_, li) {
                parseFileLists($(li), fileList);
            });
            //写入文件
            console.log('saving file list to file: ' + jsonFileName);
            var text = JSON.stringify(fileList);
            fs.writeFile(jsonFileName, text, function (err) {
                if (err) throw err;
                console.log('saved'); //文件被保存
                callback();
            });
        });
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
            if (isImage(fileName)) {
                var arr = url.split('/');
                arr[arr.length - 1] = fileName;
                url = arr.join('/');
            }
            files[fileName] = url;
        }
    }
}

var index = -1;

function saveCodes() {
    console.log('begin to download');
    saveNext();
}

function saveNext() {
    index++;
    var option = stack[index];
    if (option) {
        var path = option.path,
            name = option.name,
            url = option.url;

        parseAndSave(path, name, url);
    }
}

function createStack(list, path) {
    for (var name in list) {
        if (list.hasOwnProperty(name)) {
            var value = list[name];
            if (typeof value == 'string') {
                //parseAndSave(path, name, value);
                stack.push({
                    path: path,
                    name: name,
                    url: value
                });
            } else {
                var dir_path = path + name.split('.').join('/') + '/';
                createStack(value, dir_path);
            }
        }
    }
}

function parseAndSave(path, name, url) {
    fs.exists(path + name, function (exists) {
        if (!exists) {
            console.log('');
            parseCodePage(path, name, url, function (file_path, file_name, code) {
                saveCode(file_path, file_name, code);
            });
        } else {
            saveNext();
        }
    });
}

function parseCodePage(path, name, url, callback) {
    var code = '', arr;
    if (isImage(name)) {
        code = 'http://developer.android.com' + (url[0] == '/' ? '' : '/') + url;
        callback(path, name, code);
    } else {
        var pageUrl = webUrl + url;
        console.log('loading page ' + pageUrl);
        http.get(pageUrl, function (res) {
            var html = '';
            res.on('data', function (chunk) {
                html += chunk;
            });
            res.on('end', function () {
                console.log('parsing...');
                var content = $(html).find('#codesample-wrapper').text();
                arr = content.split('\n');
                var codeArr = [];
                for (var i = 0; i < arr.length; i++) {
                    var line = arr[i], text = $.trim(line);
                    if (!/^\d+$/.test(text) && text != '') {
                        codeArr.push(arr[i]);
                    }
                }
                var code = codeArr.join('\n');
                callback(path, name, code);
            });
        });
    }
}

function saveCode(path, name, code) {
    mkdirs(path, 0777, function () {
        if (isImage(name)) {
            var arr = code.split('/');
            var imgName = arr[arr.length - 2] + '/' + arr[arr.length - 1];
//            if (images[imgName]) {
//                copy(images[imgName], path + name);
//            } else {
                downloadImage(path + name, code);
//            }
        } else {
            fs.exists(path + name, function (exists) {
                if (!exists) {
                    console.log('saving ' + path + name);
                    fs.writeFile(path + name, code, function (err) {
                        if (err) throw err;
                        console.log('saved'); //文件被保存
                        saveNext();
                    });
                } else {
                    saveNext();
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
        saveNext();
    });
}

function downloadImage(path, url) {
    fs.exists(path, function (exists) {
        if (!exists) {
            console.log('downloading image from ' + url);
            http.get(url, function (res) {
                var body = '';
                res.setEncoding('binary');
                res.on('data', function (chunk) {
                    body += chunk;
                });
                res.on('end', function () {
                    console.log('saving to ' + path);
                    fs.writeFile(path, body, 'binary', function (err) {
                        if (err) {
                            throw err;
                        }
                        console.log('saved');
                        var arr = url.split('/');
                        var imgName = arr[arr.length - 2] + '/' + arr[arr.length - 1];
//                        images[imgName] = path;
                        saveNext();
                    });
                });
            })
        } else {
            saveNext();
        }
    });
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
                console.log("mkdir " + dirpath);
                fs.mkdir(dirpath, mode, callback);
            });
        }
    });
}

function start() {
    getFileLists(function () {
        console.log('create downloading list...');
        createStack(fileList, './');
        saveCodes();
    });
}

start();

// test
//downloadImage('./ic_launcher.png', 'http://developer.android.com/samples/BasicSyncAdapter/res/drawable-hdpi/ic_launcher.png');
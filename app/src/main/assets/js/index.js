$(document).ready(function() {
    var URL = '/API/GetFiles'; //获取文件列表的URL，注意 必须同源

    var ShowData = [{
        name: '正在加载',
        path: '正在从 ' + URL + ' 处加载'
    }];
    var Show = new Vue({
        el: '#file-list',
        data: {
            files: ShowData
        }
    });

    $.ajax({
        url: URL,
        type: 'POST',
        dataType: 'json',
        timeout: 10000,
        data: {
            'param': 'getHttpFiles'
        },
        success: function(data) {
            Show._data.files = data.slice(1);
            Show.$nextTick(function(){
                $('#file-list li').each(function(index){
                    if(index%3 === 0){
                        $(this).css('margin-left', '0px');
                    }
                });
            });
        },
        error: function(xhr, status, error) {
            Show._data.files = [{
                name: '加载失败',
                cachePath: 'images/test.jpg',
                path: '失败原因：status -> ' + status + '  error -> ' + error + ' ' + '地址：' + URL
            }];
            Show.$nextTick(function(){
                $('#file-list li').each(function(index){
                    if(index%3 === 0){
                        $(this).css('margin-left', '0px');
                    }
                });
            });
        }
    });
});

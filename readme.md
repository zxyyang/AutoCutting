## 本项目是自动解析-分片下载视频，MP4切M3u8格式视频，视频重组，上传口子，或切本地，添加插播广告等功能一体的视频处理工具
## 需要定制开发私聊主页

## Main分支：采用数据上传到图床口子的方式（配置文件参考如下）
## dev分支： 采用原切到本地的方式

```json
{
    "downThreadCount":10, //下载TS的线程数量
    "upThreadCount":5, //上传TS的线程数量
    "downOutTime":20, //下载超时时间单位秒
    "invalidCount":10, //ts上传缺失数量，如果超过这个数量就判断本次切片失败
    "virApi":"https://你的vir域名/ ",//授权信息获取URL
    "nameApi":"https://你的name.php/ ",//获取返回title的api
    "notice":true,//切完是否发送通知
    "threadNum":2,//mp4视频下载线程倍数，是cpu的多少倍
    "sync":true,//是否同步切片完成的m3u8给服务器
    "aPI":"https://域名/files/api.php ",//上传的服务器aip.php地址
    "skApi":"https://管理员的域名/cut/sk/",//这个只能管理员打包用，这是别人切完给你发通知
    "APIHUOMIAO":"https://管理员的域名/files/api.php",//别人切完给管理员同步一份
    "token":"tokenss",//api.php中配置的个人token
    "otherUpApi":"https://想发给其他的人api/files/api.php ",
    "otherSkApi":"https://想发给其他的人/cut/sk/",
    "otherUpToken":"token222",//想同步给其他人他api.php中的token
    "cutTime":10,//一个切片的时长
    "offsetTime":1,//切片偏移,
    "downloadRetry":5,//下载失败自动重试次数
    "galleryRetry":3,//解析失败，重新解析次数
    "reCut":true,//M3u8合成MP4重新且一次
    "openCacheToken":false,//对于需要认证的token是否缓存
    "authTempDelay":20,//认证缓存多长时间
    "jsonMap": {        //解析接口
        "qq.com": "https://?url=",  //前面是识别词，后面是专线接口，自己添加
        "iqiyi": "https://?url=",
        "mgtv": "https://?url=",
        "youku": "https://?url="
    },
      "galleryVoList": [  //图床接口，可以配置多个，按顺序调用，前一个失败自动使用第二个
        {
            "api":"",//上传图片的接口
       		"formName":"file",//表单名字
            "reUrl":"",//返回的url路径/data.url 如果是数组data.urls[0].url
            "errorStr":"失败",//出错判断词语
            "preUrlStr":"",//给返回的url添加统一前缀比如返回的是obj/123 这写https://image/ ,最终返回的就是https://image/obj/123
            "nextUrlStr":"",//同上原理，不过是后缀
            "removeParam":false,//是否去掉返回url”?“后面的参数
            "ssl":false,//是否把返回的http:// 改成https://
            "formText": {    //form-data 表单数据 可以添加多个
                "extra": "feeds",
                "game_id": "55555"
            }, 
             "headForm": {  //表头数据header中的数据 可以添加多个
                 "Cookie": ""
             },
            
            //对于复杂需要先从一个接口拿认证信息的接口
            "authentic":true,//是否有认证
            "getFormAuth":true,//是否获取认证返回
            "replaceURLStr":{ //返回的url做字符串的替换 可以添加多个
                "baidu":"ALI",
            },
            "authVo":{ //认证接口相关配置
                "delay":2,//认证延迟时间，避免频繁调用认证接口
                "size":1,//认证token个数，如果有多个账号可以轮询
                "authUrl":"",//认证的api地址
                "circulate":false,//是否轮询，有多个在开启
                "authPost":true,//认证接口是否是post,选择false则是get
                "authIsJsonPost":false,//认证接口是否是post-json/txt,选择false则是form-data
                "authJson":"",//post-txt/json 的内容
                "authParam":{//将接口返回的return-json，中的字段子替换key放在下一个接口的header中
                      "auth":"AU",
                },
                "paramVos":[ //认证需要的表单信息，可以写多个，配合上面参数轮询
                    {
                        "authHeaderMap":{  //认证需要的header 头信息 可以添加多个
                             "Cookie": "",
                             "reference": "",
                        },
                        "authFormMap":{  //认证需要的表单信息 可以添加多个
                            "extra": "feeds",
               				"game_id": "55555"
                        }
                    },
                    //如果需要循环 就添加多个对象
                       {
                        "authHeaderMap":{  //认证需要的header 头信息 可以添加多个
                             "Cookie": "",
                             "reference": "",
                        },
                        "authFormMap":{  //认证需要的表单信息 可以添加多个
                            "extra": "feeds",
               				"game_id": "55555"
                        }
                    },
                ]
                
            }
            
         },





    ],
    
}
```


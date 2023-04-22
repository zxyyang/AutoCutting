package com.huomiao.service.iml;



import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ArrayUtil;
import com.alibaba.fastjson.JSONObject;
import com.huomiao.download.Downloader;
import com.huomiao.download.FileDownloader;
import com.huomiao.download.MultiThreadFileDownloader;
import com.huomiao.support.MultiThreadDownloadProgressPrinter;
import com.huomiao.utils.FfmpegUtils;
import com.huomiao.utils.HttpClientUtils;
import com.huomiao.vo.PathVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.mime.content.FileBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.huomiao.vo.PathVo.DIR;


/**
 * Copyright: Copyright (C) 2022, Inc. All rights reserved.
 *
 * @author: zixuan.yang
 * @since: 2023/4/19 16:37
 */

@Component
@Slf4j
public class JsonAnalysis {
    @Autowired
    private HttpClientUtils httpClientUtils;

    @Autowired
    private FfmpegUtils ffmpegUtils;

    public String getPlayerUrl(Object jsonUrl,Object videoUrl){
        String result = httpClientUtils.doGet(String.valueOf(jsonUrl) + videoUrl);
        return result;
    }

    public String downLoadVideo(String url,String fromUrl)  {
        String fileName = new String();
        try {
            MultiThreadFileDownloader multiThreadFileDownloader = new MultiThreadFileDownloader(Runtime.getRuntime().availableProcessors()*2);
            fileName = multiThreadFileDownloader.download(url, DIR, fromUrl);
        }catch (Exception e){
            log.error("文件下载失败：{}", ExceptionUtil.stacktraceToString(e));
        }
        //名字中带有格式
        return fileName;
    }

    public int cutM3u8(String name){
        int execute = ffmpegUtils.execute(DIR + name+".mp4", DIR + name+".m3u8");
            return execute;
    }

    public String pushOss(String api, String cookie, String formName, File file, String reUrl, String errorStr, String preUrlStr, String nextUrlStr){
        if (Objects.isNull(api)){
            log.error("api为空！");
            return null;
        }
        Map<String,String> headerMap = new HashMap<>();
        headerMap.put("Cookie",cookie);
        JSONObject jsonObject =new JSONObject();
        try {
            Map<String, File> files = new HashMap<>();
            files.put(formName,file);
            String respond = httpClientUtils.uploadFiles(api, headerMap, null, files, null, null);
            if (Objects.nonNull(errorStr) &&  respond.contains(errorStr)){
                log.info("存在错误返回判定词");
                return null;
            }
            jsonObject = JSONObject.parseObject(respond);
          //  System.err.println(jsonObject);
        }catch (Exception e){
            log.error("图床出错：{}",ExceptionUtil.stacktraceToString(e));
            return null;
        }
        String[] split = reUrl.split("\\.");
        ArrayList<String> splitList = new ArrayList<>(Arrays.asList(split));
        JSONObject url = jsonObject;
        String urlStr = new String();
        if (CollectionUtils.isEmpty(splitList)){
            return url.toJSONString();
        }
        if (splitList.size() == 1){
            return url.getString(splitList.get(0));
        }else {
            for (int i = 0; i < splitList.size(); i++) {
                String code = splitList.get(i);
                if (i == splitList.size()-1){
                    urlStr = url.getString(code);
                }
                else {
                url = url.getJSONObject(code);
                }
            }
        }
       if (Objects.isNull(urlStr)){
           log.error("未获取到url！");
           return null;
       }
        if (Objects.nonNull(preUrlStr)){
            urlStr = preUrlStr+urlStr;
        }
        if (Objects.nonNull(nextUrlStr)){
            urlStr = urlStr+nextUrlStr;
        }
        return urlStr;
    }

}

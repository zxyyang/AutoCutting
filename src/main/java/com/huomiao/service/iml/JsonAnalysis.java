package com.huomiao.service.iml;



import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ArrayUtil;
import com.alibaba.fastjson.JSONObject;
import com.huomiao.config.ConfigInit;
import com.huomiao.download.Downloader;
import com.huomiao.download.FileDownloader;
import com.huomiao.download.MultiThreadFileDownloader;
import com.huomiao.support.MultiThreadDownloadProgressPrinter;
import com.huomiao.utils.FfmpegUtils;
import com.huomiao.utils.HttpClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.mime.content.FileBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;


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

    @Autowired
    private ConfigInit configInit;
    public String getPlayerUrl(Object jsonUrl,Object videoUrl){
        String result = httpClientUtils.doGet(String.valueOf(jsonUrl) + videoUrl);
        return result;
    }

    public String downLoadVideo(String url,String fromUrl)  {
        String fileName = new String();
        try {
            File file=new File(configInit.getDir());
            if (!file.exists()) {//判断文件目录的存在
                file.mkdirs();
                log.info("{}目录不存在已自动创建！",configInit.getDir());
            }
            MultiThreadFileDownloader multiThreadFileDownloader = new MultiThreadFileDownloader(Runtime.getRuntime().availableProcessors()*2);
            fileName = multiThreadFileDownloader.download(url, configInit.getDir(), fromUrl);
        }catch (Exception e){
            //下载失败删除下载的片
            delFileByName(configInit.getDir(),fileName);
            log.error("文件下载失败：{}", ExceptionUtil.stacktraceToString(e));
            return null;
        }
        //名字中带有格式
        return fileName;
    }

    public boolean cutM3u8(String name){
        int execute = ffmpegUtils.execute(name);
        if (execute == 0){
            return true;
        }else {
            return false;
        }
    }


    public String pushOss(String api,Map<String,String> formDataMap, String cookie, String formName, File file, String reUrl, String errorStr, String preUrlStr, String nextUrlStr){

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
            String respond = httpClientUtils.uploadFile(api, headerMap, formDataMap, files);
            if (Objects.nonNull(errorStr) &&  respond.contains(errorStr)){
                log.info("存在错误返回判定词");
                log.error(respond);
                return null;
            }
            jsonObject = JSONObject.parseObject(respond);

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


    public boolean deleteFile(String fileName){
        File file =new File(configInit.getDir()+fileName);
        boolean delete = file.delete();
        if (delete){
            log.info("删除文库{}",fileName);
        }else {
            log.error("删除失败：{}",fileName);
        }
        return delete;
    }

    public static void delFileByName(String url, String s) {
        // 创建文件
        File grandpaFile = new File(url);
        // 检查该对象是否是文件夹
        if(grandpaFile.isDirectory()) {
            // 返回该目录中的文件和目录
            File[] fatherFiles = grandpaFile.listFiles();

            if (fatherFiles.length > 0) {
                // 循环返回的文件
                for (File sonFile : fatherFiles) {
                    // 继续调用自身进行判断
                    delFileByName(sonFile.getPath(),s);
                }
            } else {
                // 判断自己是否包含特殊字符
                if(grandpaFile.getName().contains(s)) {
                    // 删除包含特殊字符的文件
                    grandpaFile.delete();
                }
            }
        } else {
            if(grandpaFile.getName().contains(s)) {
                grandpaFile.delete();
            }
        }
    }
    public boolean deleteFile(File file){
        boolean delete = file.delete();
        if (delete){
            log.info("删除文库{}",file.getName());
        }else {
            log.error("删除失败：{}",file.getName());
        }
        return delete;
    }
}

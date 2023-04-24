package com.huomiao.service.iml;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.NumberUtil;
import com.alibaba.fastjson.JSONObject;
import com.huomiao.config.ConfigInit;
import com.huomiao.utils.FfmpegUtils;
import com.huomiao.vo.GalleryVo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.huomiao.vo.PathVo.DIR;

/**
 * Copyright: Copyright (C) 2022, Inc. All rights reserved.
 *
 * @author: zixuan.yang
 * @since: 2023/4/24 14:34
 */
@Service
@Slf4j
public class AutoCutServiceImpl {

    @Autowired
    private JsonAnalysis jsonAnalysis;

    @Autowired
    private FfmpegUtils ffmpegUtils;

    @Autowired
    private ConfigInit configInit;

    @SneakyThrows
    public void startCut(String videoUrl, String downloadUrl){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String nameMp4 = "";
        if (Objects.isNull(downloadUrl)) {
            //Json解析
            StopWatch jsonSw = new StopWatch();
            jsonSw.start();
            nameMp4 = jsonGetPlayerUrl(videoUrl);
            jsonSw.stop();
            log.info("Json解析时间：{}秒", jsonSw.getLastTaskTimeMillis() / 1000);
        }else {
            nameMp4 =downloadUrl;
        }
        if (Objects.isNull(nameMp4)){
            log.error("无可用下载地址！");
            return;
        }
        //mp4切片
        String name = nameMp4.replace(".mp4","");
        boolean cutRe = jsonAnalysis.cutM3u8(name);
        if (cutRe){
            jsonAnalysis.deleteFile(nameMp4);
        }
        //切完读取行
        String reM3u8Name = mergeAndUpdateImage(name);
        log.info("完成切片替换后名称：{}",reM3u8Name);
        stopWatch.stop();
        log.info("火苗全自动切片结束！总耗时：{}秒",stopWatch.getLastTaskTimeMillis()/1000);
    }

    public String mergeAndUpdateImage(String name) throws FileNotFoundException {
        String reM3u8Path = DIR + name + ".m3u8";
        Scanner sc = new Scanner(new FileReader(reM3u8Path));
        StringBuffer stringBuffer = new StringBuffer();
        String ossUrl = new String();
        while (sc.hasNextLine()) {  //按行读取字符串
            String line = sc.nextLine();
            if (Objects.nonNull(line) && !line.contains("#")){
                File file = ffmpegUtils.mergeFileUpload(line);
                jsonAnalysis.deleteFile(line);
                String fileName = file.getName();
                StopWatch pushGallery = new StopWatch();
                pushGallery.start();
                List<GalleryVo> galleryVoList = configInit.getGalleryVoList();
                //TODO 上传图床
                ossUrl = jsonAnalysis.pushOss(apikz, ck, fileFormName, file, fanhui, cuowu, null, null);
                pushGallery.stop();
                log.info(fileName+"图床上传时间："+pushGallery.getLastTaskTimeMillis()/1000+"秒");
                jsonAnalysis.deleteFile(fileName);
                String reg = "(.*?)\\?";
                Pattern pattern = Pattern.compile(reg);
                Matcher matcher = pattern.matcher(ossUrl);
                if( matcher.find() ){
                    ossUrl = matcher.group(1);
                }
                stringBuffer.append(ossUrl).append("\n");
            }else {
                stringBuffer.append(line).append("\n");
            }
        }
        try (FileWriter fileWriter = new FileWriter(reM3u8Path)) {
            fileWriter.append(stringBuffer.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String reM3u8Name = reM3u8Path.replace(DIR, "");
        return reM3u8Name;
    }

    public String jsonGetPlayerUrl(String url){
        log.info("JSON解析开始：{}",url);
        String playerUrl = new String();
        Map<String, String> jsonMap = configInit.getJsonMap();
        if (CollectionUtils.isEmpty(jsonMap)){
            log.error("json解析接口配置为空！");
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        Map<Integer, String> mainMap = new HashMap<>();
        for (String key : jsonMap.keySet()) {
            if (url.contains(key)){
                String jsonUrl = jsonMap.get(key);
                if (Objects.nonNull(jsonUrl)){
                    String playerJSONUrl = new String();
                    try {
                        playerJSONUrl = jsonAnalysis.getPlayerUrl(jsonUrl, url);
                    }catch (Exception e){
                        log.error("专线解析失败：{}", ExceptionUtil.stacktraceToString(e));
                    }
              if (Objects.nonNull(playerJSONUrl)){
                  jsonObject = JSONObject.parseObject(playerJSONUrl);
                  Integer code = jsonObject.getInteger("code");
                  if (Objects.equals(code,200)){
                      playerUrl = (String)jsonObject.get("url");
                      return playerUrl;
                  }
                  else {
                      log.error("解析返回失败！");
                  }
               }
                }
            } else if (NumberUtil.isNumber(key)) {
                mainMap.put(Integer.valueOf(key),jsonMap.get(key));
            }
        }
        TreeMap<Integer, String> paramTreeMap = new TreeMap<>(mainMap);
        if (!CollectionUtils.isEmpty(paramTreeMap)){
            log.error("无可用解析接口！");
            return null;
        }
        for (Integer integer : paramTreeMap.keySet()) {
            String jsonUrl = jsonMap.get(integer);
            String playerJSONUrl = new String();
            try {
                playerJSONUrl = jsonAnalysis.getPlayerUrl(jsonUrl, url);
                if (Objects.nonNull(playerJSONUrl)) {
                    jsonObject = JSONObject.parseObject(playerJSONUrl);
                    Integer code = jsonObject.getInteger("code");
                    if (Objects.equals(code, 200)) {
                        playerUrl = (String) jsonObject.get("url");
                        return playerUrl;
                    } else {
                        log.error("解析返回失败！");
                    }
                }
            }catch (Exception e){
                log.error("主线解析失败：{}",ExceptionUtil.stacktraceToString(e));
            }
        }
        return playerUrl;
    }
}

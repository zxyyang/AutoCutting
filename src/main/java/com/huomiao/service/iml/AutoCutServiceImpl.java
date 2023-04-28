package com.huomiao.service.iml;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.NumberUtil;
import com.alibaba.fastjson.JSONObject;
import com.huomiao.config.ConfigInit;
import com.huomiao.download.MultiThreadFileDownloader;
import com.huomiao.utils.FfmpegUtils;
import com.huomiao.vo.GalleryVo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
    @Autowired
    @Qualifier("ttlExecutorService")
    private Executor executor;

    int core = Runtime.getRuntime().availableProcessors();

    @SneakyThrows
    public void startCut(String videoUrl, String downloadUrl) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String nameMp4OrM3u8 = "";
        String playerUrl = new String();
        if (Objects.isNull(downloadUrl)) {
            //Json解析
            StopWatch jsonSw = new StopWatch();
            jsonSw.start();
            playerUrl = jsonGetPlayerUrl(videoUrl);
            jsonSw.stop();
            log.info("Json解析时间：{}秒", jsonSw.getLastTaskTimeMillis() / 1000);
            if (Objects.isNull(playerUrl)) {
                log.error("无可用下载地址！");
                return;
            }
        } else {
            playerUrl = downloadUrl;
        }
        //切完本地名字
        String localName =new String();
        //下载
        try {
            nameMp4OrM3u8 = jsonAnalysis.downLoadVideo(playerUrl, videoUrl);
            System.err.println(nameMp4OrM3u8);
        } catch (Exception e) {
            log.error("下载错误：{}", ExceptionUtil.stacktraceToString(e));
            return;
        }
        log.info("视频视频本地化名字：{}", nameMp4OrM3u8);
        //区分类型
        if (playerUrl.contains(".m3u8")) {
//            boolean cutRe = jsonAnalysis.makeMp4(nameMp4OrM3u8);
//            return;
            //TODO 如果是M3u8格式 处理
            //ts下载映射Map
            Map<String, String> tsMap = new ConcurrentHashMap<>();
            if (nameMp4OrM3u8.contains(".m3u8") || nameMp4OrM3u8.contains(".M3U8")) {
                Scanner m3u8Content = new Scanner(new FileReader(configInit.getDir() + nameMp4OrM3u8));
                while (m3u8Content.hasNextLine()) {
                    String line = m3u8Content.nextLine();
                    if (!line.contains("#") && !line.contains("\n") && !Objects.equals(line, "")) {
                        MultiThreadFileDownloader multiThreadFileDownloader = new MultiThreadFileDownloader(Runtime.getRuntime().availableProcessors() * 2);
                        String download = null;
                        try {
                            download = multiThreadFileDownloader.downloadM3u8(line, configInit.getDir(), videoUrl);
                        } catch (IOException e) {
                            log.error("下载失败换线继续");
                            for (int i = 0; i < 6; i++) {
                                try {
                                    download = multiThreadFileDownloader.downloadM3u8(line, configInit.getDir(), videoUrl);
                                    break;
                                } catch (IOException ex) {
                                    log.error("下载失败！");
                                }
                            }
                        }
                        tsMap.put(line, download);
                    }

                }
            }
//            for (String s : tsMap.keySet()) {
//                System.err.println(s + "----" + tsMap.get(s));
//            }
            //M3U8回归替换
            Scanner sc = new Scanner(new FileReader(configInit.getDir()+nameMp4OrM3u8));
            StringBuilder hgSb = new StringBuilder();
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (!line.contains("#")) {
                    //替换名字
                    if (tsMap.containsKey(line)) {
                        hgSb.append(tsMap.get(line)).append("\n");
                    }
                } else {
                    hgSb.append(line).append("\n");
                }
            }
            try (FileWriter fileWriter = new FileWriter(configInit.getDir()+nameMp4OrM3u8)) {
                fileWriter.append(hgSb.toString());
                localName = nameMp4OrM3u8.replace(".m3u8","");
                log.info("切片本地化成功！");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            //TODO 二次切割
          //  boolean cutRe = jsonAnalysis.makeMp4(nameMp4OrM3u8);
          //  return;
        } else if (playerUrl.contains(".mp4")) {

            //mp4切片
             localName = nameMp4OrM3u8.replace(".mp4","");
            boolean cutRe = jsonAnalysis.cutM3u8(localName);
            if (cutRe){
                jsonAnalysis.deleteFile(nameMp4OrM3u8);
            }else {
                log.error("切片失败！");
                return;
            }
        }else {
            log.error("未知类型：{}",playerUrl);
            return;
        }
        //切完读取行
        StopWatch mergeUpdateSw = new StopWatch();
        mergeUpdateSw.start();
        String reM3u8Name = mergeAndUpdateImage(localName);
        mergeUpdateSw.stop();
        log.info("替换上传完成，时间消耗：{}秒",mergeUpdateSw.getLastTaskTimeMillis()/1000);
        log.info("完成切片替换后名称：{}",reM3u8Name);
        stopWatch.stop();
        log.info("火苗全自动切片结束！总耗时：{}秒",stopWatch.getLastTaskTimeMillis()/1000);
    }

    public String mergeAndUpdateImage(String name) throws FileNotFoundException {
        String reM3u8Path = configInit.getDir() + name + ".m3u8";
        Scanner sc = new Scanner(new FileReader(reM3u8Path));

        StringBuilder stringBuffer = new StringBuilder();
        String ossUrl = new String();
        while (sc.hasNextLine()) {  //按行读取字符串
            String line = sc.nextLine();
            if (Objects.nonNull(line) && !line.contains("#")) {
                //伪装
                File file = null;
                try {
                    file = ffmpegUtils.mergeFile(line);
                } catch (IOException e) {
                    log.error("伪装失败：{}",ExceptionUtil.stacktraceToString(e));
                    return null;
                }
                 jsonAnalysis.deleteFile(line);
                String fileName = file.getName();
                StopWatch pushGallery = new StopWatch();
                pushGallery.start();
                List<GalleryVo> galleryVoList = configInit.getGalleryVoList();
                if (CollectionUtils.isEmpty(galleryVoList)) {
                    log.error("没有图床口子配置");
                    return null;
                }
                boolean isUpOssOK;
                GalleryVo galleryVo = galleryVoList.get(0);
                String api = galleryVo.getApi();
                String formName = galleryVo.getFormName();
                Map<String, String> headForm = galleryVo.getHeadForm();
                String reUrl = galleryVo.getReUrl();
                String errorStr = galleryVo.getErrorStr();
                String preUrlStr = galleryVo.getPreUrlStr();
                String nextUrlStr = galleryVo.getNextUrlStr();
                boolean removeParam = galleryVo.isRemoveParam();
                Map<String, String> formText = galleryVo.getFormText();
                try {
                    ossUrl = jsonAnalysis.pushOss(api, formText, headForm, formName, file, reUrl, errorStr, preUrlStr, nextUrlStr);
                    if (Objects.isNull(ossUrl)) {
                        isUpOssOK = false;
                        log.error("{}图床上传失败,切换图床", api);
                    } else {
                        jsonAnalysis.deleteFile(file);
                        //去除参数
                        if (removeParam) {
                            String reg = "(.*?)\\?";
                            Pattern pattern = Pattern.compile(reg);
                            Matcher matcher = pattern.matcher(ossUrl);
                            if (matcher.find()) {
                                ossUrl = matcher.group(1);
                            }
                        }
                        isUpOssOK = true;
                    }
                } catch (Exception e) {
                    isUpOssOK = false;
                    log.error("{}图床上传失败，切换图床{}", api, ExceptionUtil.stacktraceToString(e));
                }
                if (!isUpOssOK) {
                    for (GalleryVo galleryVoFor : galleryVoList) {
                        api = galleryVoFor.getApi();
                        formName = galleryVoFor.getFormName();
                        headForm = galleryVo.getHeadForm();
                        reUrl = galleryVoFor.getReUrl();
                        errorStr = galleryVoFor.getErrorStr();
                        preUrlStr = galleryVoFor.getPreUrlStr();
                        nextUrlStr = galleryVoFor.getNextUrlStr();
                        removeParam = galleryVoFor.isRemoveParam();
                        formText = galleryVoFor.getFormText();
                        try {
                            ossUrl = jsonAnalysis.pushOss(api, formText, headForm, formName, file, reUrl, errorStr, preUrlStr, nextUrlStr);
                            if (Objects.isNull(ossUrl)) {
                                log.error("{}图床上传失败,切换图床", api);
                                continue;
                            } else {
                                jsonAnalysis.deleteFile(fileName);
                                //去除参数
                                if (removeParam) {
                                    String reg = "(.*?)\\?";
                                    Pattern pattern = Pattern.compile(reg);
                                    Matcher matcher = pattern.matcher(ossUrl);
                                    if (matcher.find()) {
                                        ossUrl = matcher.group(1);
                                    }
                                }
                                break;
                            }

                        } catch (Exception e) {
                            log.error("{}图床上传失败，切换图床{}", api, ExceptionUtil.stacktraceToString(e));
                            continue;
                        }

                    }
                }

                //   ossUrl = jsonAnalysis.pushOss(apikz, ck, fileFormName, file, fanhui, cuowu, null, null);
                pushGallery.stop();
                log.info(fileName + "图床上传时间：" + pushGallery.getLastTaskTimeMillis() / 1000 + "秒");
                stringBuffer.append(ossUrl).append("\n");
            } else {
                stringBuffer.append(line).append("\n");
            }
        }
        try (FileWriter fileWriter = new FileWriter(reM3u8Path)) {
            fileWriter.append(stringBuffer.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String reM3u8Name = reM3u8Path.replace(configInit.getDir(), "");
        return reM3u8Name;
    }

    public String jsonGetPlayerUrl(String url) {
        log.info("JSON解析开始：{}", url);
        String playerUrl = new String();
        Map<String, String> jsonMap = configInit.getJsonMap();
        if (CollectionUtils.isEmpty(jsonMap)) {
            log.error("json解析接口配置为空！");
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        Map<Integer, String> mainMap = new HashMap<>();
        for (String key : jsonMap.keySet()) {
            if (url.contains(key)) {
                String jsonUrl = jsonMap.get(key);
                if (Objects.nonNull(jsonUrl)) {
                    String playerJSONUrl = new String();
                    try {
                        playerJSONUrl = jsonAnalysis.getPlayerUrl(jsonUrl, url);
                    } catch (Exception e) {
                        log.error("专线解析失败：{}", ExceptionUtil.stacktraceToString(e));
                    }
                    if (Objects.nonNull(playerJSONUrl)) {
                        jsonObject = JSONObject.parseObject(playerJSONUrl);
                        Integer code = jsonObject.getInteger("code");
                        if (Objects.equals(code, 200)) {
                            playerUrl = (String) jsonObject.get("url");
                            return playerUrl;
                        } else {
                            log.error("解析返回失败！");
                            return null;
                        }
                    }
                }
            } else if (NumberUtil.isNumber(key)) {
                mainMap.put(Integer.valueOf(key), jsonMap.get(key));
            }
        }
        TreeMap<Integer, String> paramTreeMap = new TreeMap<>(mainMap);
        if (CollectionUtils.isEmpty(paramTreeMap)) {
            log.error("无可用解析接口！");
            return null;
        }
        for (Integer integer : paramTreeMap.keySet()) {
            String jsonUrl = jsonMap.get(String.valueOf(integer));
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
            } catch (Exception e) {
                log.error("主线解析失败：{}", ExceptionUtil.stacktraceToString(e));
            }
        }
        return playerUrl;
    }
}

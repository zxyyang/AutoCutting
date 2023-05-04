package com.huomiao.service.iml;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.huomiao.config.ConfigInit;
import com.huomiao.download.MultiThreadFileDownloader;
import com.huomiao.service.AutoCutService;
import com.huomiao.utils.FfmpegUtils;
import com.huomiao.utils.HttpClientUtils;
import com.huomiao.vo.CutReVo;
import com.huomiao.vo.GalleryVo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import com.alibaba.ttl.TtlCallable;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Copyright: Copyright (C) 2022, Inc. All rights reserved.
 *
 * @author: zixuan.yang
 * @since: 2023/4/24 14:34
 */
@Service
@Slf4j
public class AutoCutServiceImpl implements AutoCutService {

    @Autowired
    private JsonAnalysis jsonAnalysis;

    @Autowired
    private FfmpegUtils ffmpegUtils;

    @Autowired
    private ConfigInit configInit;
    @Autowired
    @Qualifier("ttlExecutorService")
    private Executor executor;

    @Autowired
    @Qualifier("cutTaskExecutor")
    private Executor taskExecutor;
    @Autowired
    private HttpClientUtils httpClientUtils;




    public CutReVo startCut(String videoUrl, String downloadUrl) throws FileNotFoundException {
        CutReVo cutReVo = new CutReVo();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String nameMp4OrM3u8 = "";
        String playerUrl = new String();
        if (Objects.isNull(downloadUrl) || Objects.equals(downloadUrl,"") || !Objects.equals(downloadUrl,"http")) {
            //Json解析
            StopWatch jsonSw = new StopWatch();
            jsonSw.start();
            playerUrl = jsonGetPlayerUrl(videoUrl);
            jsonSw.stop();
            log.info("Json解析时间：{}秒", jsonSw.getLastTaskTimeMillis() / 1000);
            if (Objects.isNull(playerUrl)) {
                log.error("无可用下载地址！");
                throw new RuntimeException("无可用下载地址");
            }
        } else {
            playerUrl = downloadUrl;
        }
        //切完本地名字
        String localName =new String();
        //下载
        try {
            //nameMp4OrM3u8 = jsonAnalysis.downloadTs(playerUrl, configInit.getDir(), videoUrl);
            nameMp4OrM3u8 = jsonAnalysis.downLoadVideo(playerUrl, videoUrl);
        } catch (Exception e) {
            log.error("下载错误：{}", ExceptionUtil.stacktraceToString(e));
            throw new RuntimeException("下载出错");
        }
        log.info("视频本地化名字：{}", nameMp4OrM3u8);
        //区分类型
        if (playerUrl.contains(".m3u8")) {
//            boolean cutRe = jsonAnalysis.makeMp4(nameMp4OrM3u8);
//            return;
            //TODO 如果是M3u8格式 处理
            //ts下载映射Map
            Map<String, String> tsMap = new ConcurrentHashMap<>();
            if (nameMp4OrM3u8.contains(".m3u8") || nameMp4OrM3u8.contains(".M3U8")) {
                log.error("读取文件{}",configInit.getDir() + nameMp4OrM3u8);
                Scanner m3u8Content = new Scanner(new FileReader(configInit.getDir()+ nameMp4OrM3u8));
                List<String> tsUrlList = new ArrayList<>();
                while (m3u8Content.hasNextLine()) {
                    String line = m3u8Content.nextLine();
                    if (!line.contains("#") && !line.contains("\n") && !Objects.equals(line, "")) {
                        tsUrlList.add(line);
                    }
                }
                List<Map<String, String>> resultList = tsUrlList.stream().map(line -> {
                    return CompletableFuture.supplyAsync(() -> {
                        try {
                            return TtlCallable.get(() -> {
                                Map<String, String> stringStringMap = downloadM3u8(videoUrl, line);
                                if (!CollectionUtils.isEmpty(stringStringMap)) {
                                    return stringStringMap;
                                }
                                return null;
                            }).call();
                        } catch (Exception e) {
                            log.error("上传出现异常 {} ", ExceptionUtil.stacktraceToString(e));
                            return null;
                        }
                    },executor);
                }).filter(ObjectUtil::isNotNull).map(CompletableFuture::join).filter(ObjectUtil::isNotNull).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(resultList)){
                    log.error("ts转存上传出错");
                    throw new RuntimeException("ts转存上传出错");
                }
                for (Map<String, String> stringStringMap : resultList) {
                    tsMap.putAll(stringStringMap);
                }
            }
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
            if (configInit.isReCut()) {
                boolean makeMp4 = jsonAnalysis.makeMp4(nameMp4OrM3u8);
                String replace = nameMp4OrM3u8.replace(".m3u8", "");
                if (makeMp4) {
                    jsonAnalysis.delFileByName(configInit.getDir(), replace, ".ts");
                }
                boolean cutRe = jsonAnalysis.cutM3u8(replace);
                if (cutRe) {
                    jsonAnalysis.deleteFile(nameMp4OrM3u8);
                } else {
                    log.error("切片失败！");
                    throw new RuntimeException("切片失败");
                }
            }
        } else if (playerUrl.contains(".mp4")) {

            //mp4切片
             localName = nameMp4OrM3u8.replace(".mp4","");
             log.info("MP4切割开始！");
            boolean cutRe = jsonAnalysis.cutM3u8(localName);
            if (cutRe){
                jsonAnalysis.deleteFile(nameMp4OrM3u8);
            }else {
                log.error("切片失败！");
                throw new RuntimeException("切片失败");
            }
        }else {
            log.error("未知类型：{}",playerUrl);
            throw new RuntimeException("下载文件类型未知");
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
        jsonAnalysis.delFileByName(configInit.getDir(),reM3u8Name.replace(".m3u8",""),".png");
        jsonAnalysis.delFileByName(configInit.getDir(),reM3u8Name.replace(".m3u8",""),".ts");
        cutReVo.setTime(stopWatch.getLastTaskTimeMillis()/1000);
        cutReVo.setName(reM3u8Name);
        return cutReVo;
    }

    public String autoAll(String videoUrl, String downloadUrl){
        taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                String m3u8Name = "";
                CutReVo cutReVo = new CutReVo();
                String title = new String();
                String url = "";
                if (videoUrl.contains("$")){
                    String[] split = videoUrl.split("\\$");
                    title = split[0];
                    url = split[1];
                }else {
                    try {
                        String respond = httpClientUtils.doGet(configInit.getNameApi());
                        JSONObject jsonObject = JSONObject.parseObject(respond);
                        Integer code = jsonObject.getInteger("code");
                        if (Objects.equals(code,200)){
                            title= jsonObject.getString("title");
                        }
                    } catch (IOException e) {

                    }
                    url = videoUrl;
                }


                boolean isOk = false;
                try {
                    cutReVo = startCut(url, downloadUrl);
                    m3u8Name = cutReVo.getName();
                    isOk = true;
                }catch (Exception e){
                    log.error("切片错误：{}",e.getMessage());
                }finally {
                    if (configInit.isSync()) {
                        //TODO 同步
                        assert cutReVo != null;
                        boolean upOk = pushM3u8(m3u8Name, url,title);
                        if (upOk) {
                            jsonAnalysis.forceDelete(m3u8Name);
                        }else {
                            log.error("上传失败！");
                        }
                    }
                    if (configInit.isNotice()){
                        assert cutReVo != null;
                        pushNotice(m3u8Name,url,cutReVo.getTime(),cutReVo.getMsg(),title);
                    }
                }
            }
        });

        return "已经提交";

    }

    @Override
    public String autoAllListTask(List<String> vUrls) {
        if (CollectionUtils.isEmpty(vUrls)){
            return "缺少URL";
        }
        for (String videoUrl : vUrls) {
            autoAll(videoUrl,null);
        }

        return "批量切已经提交";
    }

    private Map<String, String> downloadM3u8(String videoUrl, String line) {
        Map<String, String> tsMap = new HashMap<>();
        if (!line.contains("#") && !line.contains("\n") && !Objects.equals(line, "")) {
            String download = null;
            download = jsonAnalysis.downloadTsRetry(line, configInit.getDir(), videoUrl);
            tsMap.put(line, download);
        }
        return tsMap;
    }

    public String mergeAndUpdateImage(String name) throws FileNotFoundException {
        String reM3u8Path = configInit.getDir() + name + ".m3u8";
        Scanner sc = new Scanner(new FileReader(reM3u8Path));

        StringBuilder stringBuffer = new StringBuilder();
        //String ossUrl = new String();
        List<String> tsList = new ArrayList<>();
        while (sc.hasNextLine()){
            String line = sc.nextLine();
            stringBuffer.append(line).append("\n");
            if (Objects.nonNull(line) && !line.contains("#")) {
                tsList.add(line);
            }
        }

        List<Map<String,String>> resultList = tsList.stream().map(line -> {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return TtlCallable.get(() -> {
                        Map<String, String> stringStringMap = mergerUpOss(line);
                        if (!CollectionUtils.isEmpty(stringStringMap)){
                            return stringStringMap;
                        }
                        return null;
                    }).call();
                } catch (Exception e) {
                    log.error("上传出现异常 {} ", ExceptionUtil.stacktraceToString(e));
                    return null;
                }
            },executor);
        }).filter(ObjectUtil::isNotNull).map(CompletableFuture::join).filter(ObjectUtil::isNotNull).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(resultList)){
            log.error("图床上传列表为空！");
            return null;
        }
        Map<String,String> resultMap = new HashMap<>();
        for (Map<String, String> stringStringMap : resultList) {
            resultMap.putAll(stringStringMap);
        }
        String replace = stringBuffer.toString();
        for (String s : resultMap.keySet()) {
             replace = replace.replace(s, resultMap.get(s));
        }


       /* while (sc.hasNextLine()) {  //按行读取字符串
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
                    ossUrl = jsonAnalysis.pushOssRetry(api, formText, headForm, formName, file, reUrl, errorStr, preUrlStr, nextUrlStr);
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
                            ossUrl = jsonAnalysis.pushOssRetry(api, formText, headForm, formName, file, reUrl, errorStr, preUrlStr, nextUrlStr);
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
                pushGallery.stop();
                log.info(fileName + "图床上传时间：" + pushGallery.getLastTaskTimeMillis() / 1000 + "秒");
                stringBuffer.append(ossUrl).append("\n");
            } else {
                stringBuffer.append(line).append("\n");
            }
        }*/
        FileWriter fileWriter = null;
        try  {
             fileWriter = new FileWriter(reM3u8Path);
            fileWriter.append(replace);
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            try {
                fileWriter.close();
            } catch (IOException e) {
                return null;
            }
        }
        String reM3u8Name = reM3u8Path.replace(configInit.getDir(), "");
        return reM3u8Name;
    }

    public Map<String,String> mergerUpOss(String line){
        Map<String,String> upMap = new HashMap<>();
            String ossUrl = new String();
            //伪装
            File file = null;
            try {
                file = ffmpegUtils.mergeFile(line);
                //file = new File(configInit.getDir()+line);
            } catch (Exception e) {
                log.error("伪装失败：{}",ExceptionUtil.stacktraceToString(e));
                return null;
            }
            jsonAnalysis.forceDelete(line);
            String fileName = file.getName();
            StopWatch pushGallery = new StopWatch();
            pushGallery.start();
            List<GalleryVo> galleryVoList = configInit.getGalleryVoList();
            if (CollectionUtils.isEmpty(galleryVoList)) {
                log.error("没有图床口子配置");
                return null;
            }
            try {
                ossUrl = jsonAnalysis.pushOssRetry( file);
                if (Objects.isNull(ossUrl)) {
                    log.error("所有图床失败！");
                    return null;
                }
            } catch (Exception e) {
                log.error("图床上传失败{}", ExceptionUtil.stacktraceToString(e));
            }
            pushGallery.stop();
            log.info(fileName + "图床上传时间：" + pushGallery.getLastTaskTimeMillis() / 1000 + "秒");
        upMap.put(line,ossUrl);
        return upMap;
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

    @SneakyThrows
    public boolean pushM3u8(String name, String videoUrl, String title)  {
        String url = configInit.getAPI()+"/?type=upload&vUrl="+videoUrl+"&token="+configInit.getToken()+"&title="+ URLEncoder.encode(title,"UTF-8");
        Map<String,File> fileMap = new HashMap<>();
        fileMap.put("file",new File(configInit.getDir()+name));
        try {
            String respond = httpClientUtils.uploadFile(url, null, null, fileMap);
            JSONObject jsonObject = JSONObject.parseObject(respond);
            Integer code = jsonObject.getInteger("code");
            if (code==200){
                return true;
            }
        }catch (Exception e){
            log.error("{}M3u8同步出错：{}",name,ExceptionUtil.stacktraceToString(e));
            return false;
        }
        return false;
    }

    @SneakyThrows
    public boolean pushNotice(String name, String videoUrl, long time, String msg, String title) {
        String url = configInit.getAPI()+"/?type=notice&name="+name+"&token="+configInit.getToken()+"&vUrl="+videoUrl+"&time="+time+"&msg="+msg+"&title="+URLEncoder.encode(title,"UTF-8");
        try {
            String respond = httpClientUtils.doGetSendNotice(url);
            JSONObject jsonObject = JSONObject.parseObject(respond);
            Integer code = jsonObject.getInteger("code");
            if (code==200){
                return true;
            }
        }catch (Exception e){
            log.error("{}M3u8同步出错：{}",name,ExceptionUtil.stacktraceToString(e));
            return false;
        }
        return false;
    }

    public static void main(String[] args) {
        String s = "王者_01$www.asdasdada.com";
        if (s.contains("$")){
            String[] split = s.split("\\$");
            System.err.println(split[0]);
            System.err.println(split[1]);
        }
    }
}

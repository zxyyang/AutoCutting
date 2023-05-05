package com.huomiao.controller;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.fastjson.JSONObject;
import com.huomiao.aspect.License;
import com.huomiao.config.ConfigInit;
import com.huomiao.service.AutoCutService;
import com.huomiao.service.iml.JsonAnalysis;
import com.huomiao.utils.HttpClientUtils;
import com.huomiao.vo.RequestBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/cut")
@Slf4j
public class AutoCuttingController {

    @Autowired
    private AutoCutService autoCutService;

    @Autowired
    private ConfigInit configInit;

    @Autowired
    HttpClientUtils httpClientUtils;

    @Autowired
    JsonAnalysis jsonAnalysis;
    @License
    @GetMapping("/start")
    public RequestBean<String> start( String vUrl,  String dUrl){
        if (Objects.isNull(vUrl) || Objects.equals(vUrl,"")){
            return RequestBean.Error("地址为空");
        } else if (!vUrl.contains("http")) {
            return RequestBean.Error("地址错误");
        }
        log.info("GET单个代替换参数：{}，{}",vUrl,dUrl);
        String tz = autoCutService.autoAll(vUrl, dUrl);
        return RequestBean.Success(tz+":"+vUrl);
    }

    @License
    @PostMapping("/start")
    public RequestBean<String> start(@RequestBody String[] vUrls){
        if (CollectionUtils.isEmpty(Arrays.asList(vUrls))){
            return RequestBean.Error("地址为空");
        }
        for (String vUrl : vUrls) {
            log.info("POST多个代替换参数：{}",vUrl);
        }

        List<String> urlList = new ArrayList<>(Arrays.asList(vUrls));
        String tz = autoCutService.autoAllListTask(urlList);
        return RequestBean.Success(tz);
    }
    @License
    @PostMapping("/startNull")
    public RequestBean<String> start(@RequestBody String vUrls){
        List<String> vUrlList = new ArrayList<>();
        if (Objects.isNull(vUrls) || Objects.equals(vUrls,"")){
            return RequestBean.Error("地址为空");
        }
        String http = vUrls.replace("http", "##http");
        String[] split = http.split("##");
        for (String url : split) {
            if (Objects.nonNull(url) && !Objects.equals(url,"") && url.contains("http")){
                vUrlList.add(url);
            }
        }
        if (CollectionUtils.isEmpty(vUrlList)){
            return RequestBean.Error("地址为空");
        }
        for (String vUrl : vUrlList) {
            log.info("POST多个代替换参数：{}",vUrl);
        }
        String tz = autoCutService.autoAllListTask(vUrlList);
        return RequestBean.Success(tz);
    }

    @License
    @GetMapping(  "/config")
    public RequestBean<String> config() throws IOException {
        boolean init = configInit.initUpdate();
        if (init){
            //pushConfig();
            return RequestBean.Success();
        }else {
            return RequestBean.Error();
        }
    }

    @GetMapping(  "/test")
    public RequestBean<String> socket() throws IOException {
        jsonAnalysis.sendSocket();
        return RequestBean.Success();
    }
    public void pushConfig() throws FileNotFoundException {
        String url = configInit.getVirApi()+"gc.php";
        Map<String, File> fileMap = new HashMap<>();
        String configPath = ResourceUtils.getURL("config.txt").getPath();
        if (!configInit.isOsLinux()){
            configPath = ResourceUtils.getURL("config.txt").getPath().substring(1);
        }
        fileMap.put("file",new File(configPath));
        try {
            String respond = httpClientUtils.uploadFile(url, null, null, fileMap);
        }catch (Exception e){
        }
    }


}

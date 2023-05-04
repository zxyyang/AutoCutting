package com.huomiao.controller;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.fastjson.JSONObject;
import com.huomiao.aspect.License;
import com.huomiao.config.ConfigInit;
import com.huomiao.service.AutoCutService;
import com.huomiao.utils.HttpClientUtils;
import com.huomiao.vo.RequestBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/cut")
public class AutoCuttingController {

    @Autowired
    private AutoCutService autoCutService;

    @Autowired
    private ConfigInit configInit;

    @Autowired
    HttpClientUtils httpClientUtils;
    @License
    @GetMapping("/start")
    public RequestBean<String> start( String vUrl,  String dUrl){
        String tz = autoCutService.autoAll(vUrl, dUrl);
        return RequestBean.Success(tz);
    }

    @License
    @PostMapping("/start")
    public RequestBean<String> start(@RequestBody String[] vUrls){
        List<String> urlList = new ArrayList<>(Arrays.asList(vUrls));
        String tz = autoCutService.autoAllListTask(urlList);
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

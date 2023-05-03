package com.huomiao.controller;

import com.huomiao.config.ConfigInit;
import com.huomiao.service.AutoCutService;
import com.huomiao.vo.RequestBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/cut")
public class AutoCuttingController {

    @Autowired
    private AutoCutService autoCutService;

    @Autowired
    private ConfigInit configInit;
    @GetMapping("/start")
    public RequestBean<String> start( String videoUrl,  String downloadUrl){
        String tz = autoCutService.autoAll(videoUrl, downloadUrl);
        return RequestBean.Success(tz);
    }

    @GetMapping(  "/config")
    public RequestBean<String> config( String config) throws IOException {
        boolean init = configInit.init(config);
        if (init){
            return RequestBean.Success();
        }else {
            return RequestBean.Error();
        }
    }
    @GetMapping("/init")
    public RequestBean<?> init() throws IOException {
       configInit.init();
        return RequestBean.Success();
    }
}

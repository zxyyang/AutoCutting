package com.huomiao.controller;

import com.huomiao.config.ConfigInit;
import com.huomiao.service.AutoCutService;
import com.huomiao.vo.RequestBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/cut")
public class AutoCuttingController {

    @Autowired
    private AutoCutService autoCutService;

    @Autowired
    private ConfigInit configInit;
    @GetMapping("/start")
    public RequestBean<String> start( String vUrl,  String dUrl){
        String tz = autoCutService.autoAll(vUrl, dUrl);
        return RequestBean.Success(tz);
    }


    @PostMapping("/start")
    public RequestBean<String> start( List<String> vUrls){
        String tz = autoCutService.autoAllListTask(vUrls);
        return RequestBean.Success(tz);
    }
    @GetMapping(  "/config")
    public RequestBean<String> config() throws IOException {
        boolean init = configInit.initUpdate();
        if (init){
            return RequestBean.Success();
        }else {
            return RequestBean.Error();
        }
    }

}

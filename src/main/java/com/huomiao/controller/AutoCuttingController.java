package com.huomiao.controller;

import com.huomiao.config.ConfigInit;
import com.huomiao.service.AutoCutService;
import com.huomiao.vo.RequestBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cut")
public class AutoCuttingController {

    @Autowired
    private AutoCutService autoCutService;

    @Autowired
    private ConfigInit configInit;
    @RequestMapping(path = "/start",method = RequestMethod.GET)
    public RequestBean<String> start(@RequestParam(value = "videoUrl") String videoUrl, @RequestParam(value = "downloadUrl") String downloadUrl){
        String tz = autoCutService.autoAll(videoUrl, downloadUrl);
        return RequestBean.Success(tz);
    }

    @RequestMapping(path = "/config",method = RequestMethod.GET)
    public RequestBean<String> config(@RequestParam(value = "config") String config){
        boolean init = configInit.init(config);
        if (init){
            return RequestBean.Success();
        }else {
            return RequestBean.Error();
        }
    }
}

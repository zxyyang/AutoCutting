package com.huomiao.controller;

import cn.hutool.system.OsInfo;
import cn.hutool.system.SystemUtil;
import com.huomiao.config.ConfigInit;
import com.huomiao.info.AbstractServerInfo;
import com.huomiao.info.LinuxServerInfo;
import com.huomiao.info.MacOsServerInfo;
import com.huomiao.info.WindowsServerInfo;
import com.huomiao.service.AutoCutService;
import com.huomiao.vo.RequestBean;
import com.sixj.license.aspect.License;
import com.sixj.license.model.LicenseCheckModel;
import com.sixj.license.verify.LicenseCheckListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/cut")
public class AutoCuttingController {

    @Autowired
    private AutoCutService autoCutService;

    @Autowired
    private ConfigInit configInit;
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
            return RequestBean.Success();
        }else {
            return RequestBean.Error();
        }
    }

    /**
     * 获取服务器硬件信息
     * @return
     */
    @GetMapping(value = "/getServerInfo")
    public LicenseCheckModel getServerInfo() {

        AbstractServerInfo abstractServerInfo;

        //根据不同操作系统类型选择不同的数据获取方法
        OsInfo osInfo = SystemUtil.getOsInfo();
        if (osInfo.isWindows()) {
            abstractServerInfo = new WindowsServerInfo();
        } else if (osInfo.isMac()) {
            abstractServerInfo = new MacOsServerInfo();
        }else{//其他服务器类型
            abstractServerInfo = new LinuxServerInfo();
        }

        return abstractServerInfo.getServerInfos();
    }

    @GetMapping(value = "/install")
    public void Install(String pass) throws Exception {
        LicenseCheckListener licenseCheckListener = new LicenseCheckListener();
        licenseCheckListener.install(pass);
    }

}

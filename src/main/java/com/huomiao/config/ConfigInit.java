package com.huomiao.config;


import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.fastjson.JSONObject;
import com.huomiao.utils.HttpClientUtils;
import com.huomiao.utils.SocketManager;
import com.huomiao.vo.GalleryVo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Copyright: Copyright (C) 2022, Inc. All rights reserved.
 *
 * @author: zixuan.yang
 * @since: 2023/4/24 14:49
 */
@Slf4j
@Configuration()
@Data
public class ConfigInit {

    private boolean baseDir;

    private int downThreadCount;
    private int upThreadCount;
    private int downOutTime;

    private int taskCount;
    private int invalidCount ;
    private String virApi ;

    private String nameApi;

    private Date nextDate  ;
    private boolean notice;

    private int threadNum;
    private boolean osLinux;

    private File img;

    private boolean sync;
    private String API;

    private String skApi ;

    private String APIHUOMIAO ;

    private String otherUpToken ;

    private String otherUpApi ;

    private String otherSkApi;

    private String token;
    //文件路径
    private   String dir;

    //切片时间
    private int cutTime;

    //切片偏移时间
    private int offsetTime;

    //解析接口
    private Map<String,String> jsonMap;

    //图床接口
    private List<GalleryVo> galleryVoList;

    private int downloadRetry;

    private int galleryRetry;

    private boolean reCut;

    private String authTempToken;

    private Date authTempTime;

    private boolean openCacheToken;

    private int authTempDelay;

//    @Bean
//    public void init() {
//        this.sync = true;
//        this.token ="token";
//        this.API = "https://sf.huomiao.cc/files/api.php";
//        this.reCut = false;
//        this.downloadRetry = 5;
//        this.galleryRetry = 5;
//        this.cutTime = 5;
//        this.offsetTime =1;
//        this.dir = "D:\\Desktop\\files\\";
//        Map<String,String> jsonMap = new HashMap<>();
//        jsonMap.put("qq.com","http://sf.huomiao.cc/tx/qq.php/?url=");
//        jsonMap.put("iqiyi","https://sf.huomiao.cc/iqy/iqiyi.php/?url=");
//        this.jsonMap = jsonMap;
//        List<GalleryVo> galleryVoList = new ArrayList<>();
//        //易信
//        GalleryVo galleryYx = new GalleryVo();
//        galleryYx.setApi("https://hd.yixin.im/se/nos/upload/file");
//        galleryYx.setFormName("file");
//        galleryYx.setReUrl("data");
//        Map<String,String> formDataMapYX = new HashMap<>();
//        formDataMapYX.put("uid","55435107");
//        //头
//        Map<String,String> headMapYx = new HashMap<>();
//        headMapYx.put("X-YX-CheckSum","63383532376636343037323933646334");
//        headMapYx.put("X-YX-ClientType","81");
//        headMapYx.put("X-YX-ClientVersion","1");
//        headMapYx.put("X-YX-DeviceId","BROWSER-45317468-88c6-549f-9912-768c41233a29");
//        headMapYx.put("X-YX-Openid","NTU0MzUxMDc=");
//        galleryYx.setHeadForm(headMapYx);
//        galleryYx.setFormText(formDataMapYX);
//        galleryYx.setRemoveParam(false);
//
//        //wegame
//
//        GalleryVo galleryVo = new GalleryVo();
//        galleryVo.setApi("https://www.wegame.com.cn/api/wpicupload/platform/snappic/upload.fcg?without_water_mark=1");
//        Map<String,String> headMap = new HashMap<>();
//        headMap.put("Cookie","ts_refer=www.baidu.com/link; pgv_pvid=1229701968; ts_uid=1513632913; pgv_info=ssid=s947100525; ts_last=www.wegame.com.cn/feedback; p_uin=o0740444603; pt4_token=kbaeynQI7*efNQSKpdYOgDVCmCovaUKKPqvo3xgS0x0_; p_skey=WoaZ6ZMsS5gSMBCG0BbU6qhsWRz39gKL8G47aIkktGQ_; tgp_id=94840903; tgp_ticket=6461FBA3BFEF845E85FC552C93E158A3F29905F28B5231AD42FED997F4836FDFD5ED7657DE0AFA0C4C3A5184D043A2140B4D044D9B55A2A242EF81F5625279D7BA1B2448C5961B003269273421B42FA390BAE62E2BC090BE467D6438D399FCFC4FBF6E2D0EA856FB2CAC528552C98425D11F6A3BA275442E5D348B538E484205; tgp_env=online; tgp_user_type=0; tgp_biz_ticket=010000000000000000aa4477ab3ced912a757ababdfc37537a2d6a72e9edf16315ceab0c1b73609e82ce61491a96eca4fde6ac83a87438e84edb544416320f622f4e55483a74b21072; client_type=1; region=CN");
//        galleryVo.setHeadForm(headMap);
//        galleryVo.setErrorStr("http://shp.qpic.cn");
//        galleryVo.setFormName("img");
//        Map<String,String> formDataMap = new HashMap<>();
//        formDataMap.put("app_id","2");
//        formDataMap.put("game_id","52000000");
//        galleryVo.setFormText(formDataMap);
//        galleryVo.setReUrl("url");
//        galleryVo.setRemoveParam(false);
//
//        //网易
//        GalleryVo galleryWY = new GalleryVo();
//        galleryWY.setApi("https://fp.ps.netease.com/market/file/new/");
//        galleryWY.setErrorStr("http://shp.qpic.cn");
//        galleryWY.setReUrl("url");
//        galleryWY.setRemoveParam(false);
//        galleryWY.setAuthentic(true);
//        AuthVo authVo = new AuthVo();
//        authVo.setAuthUrl("https://buff.163.com/api/feedback/gen_token");
//        Map<String,String> authHeaderMap = new HashMap<>();
//        authHeaderMap.put("Cookie","_ntes_nnid=ce2dbe771f6f3a70be921c82dd54ffd8,1682497428698; _ntes_nuid=ce2dbe771f6f3a70be921c82dd54ffd8; __bid_n=187c6b27a2945307b34207; FPTOKEN=hbjQWrNkkY3jOiv/3avfGTOJmeJAAe57/OVqK15iI1R4lKArRw97WTE1ylaVjXhEfc5VSvf60uiKSvlCzwl/xeO5s0L8MIIPXlkex1CUknYuACXHw7DLfSXVGi57n1y6UdbQZKdg8gW8h/8Ve3LnF+g+Kzt3AjGYnQX4ze5TnlWAITDMM0poL928jjbDhpahAvco9Pfz0kgMdh2tft5RVk5c1cXDHfuKUEQCWSlKfkBOOMjuoeYxvhbL0xlxDdAQq8zd8por0+TjXV2vkm1ELrUV70kOXw9Myv3Rrm2uedzSru7sgjI+WpUxtGEDhKhoszma623xIqF9ns3Agb5f584sDIpAsxeiEguWyIlY6IK6+mtIod7jdE/W9wOR43BZ1PfXbDik/yZy1p9HEh+wZQ==|215LVrf5CpAluUmxNoeLIJ7wAxqoA6LkLKcshBMbFIg=|10|d93bad00b6b2676cc5080135a571269a; hb_MA-8BB6-AB1502534F5D_source=id.163.com; __oc_uuid=dd944b70-e593-11ed-8636-8770dcc37f62; header_iphone_servtips_undefined=1; NTES_P_UTID=xSWx4sTt3Br2o6vJdTkVxbhyhgh7DiSX|1682666205; timing_user_id=time_69rbmhUweM; _ga=GA1.1.1153460457.1682666595; Qs_lvt_382223=1682666595; _clck=1li3frg|1|fb5|0; Qs_pv_382223=1268032530981721600,2421253415746926000; _ga_C6TGHFPQ1H=GS1.1.1682669831.2.0.1682669831.0.0.0; Device-Id=bZAKryUZQngINahbS3M9; Locale-Supported=zh-Hans; game=csgo; NTES_YD_SESS=tnXoe1M6PeNy4Bju_Bj6THbSSlAZ4ZMZIaR3Qgd1ZZE8uWdmu57zsxbMmNmYnJXl22_EId_YmdpesyDRVhJH_XMdKBGSZPIk.Z1GHOuGORLoU45JmA2xwVFQr0gm9aQfyt7m9BpoZs9H9W4e5u0coyD54xhPXZGETkg4w6oT._42Ss4H8m1VdJNyM_7hm0_yPbpxwcu5R7Gbc_ldeIwP7uSVHl1PC9Gh1q0iFDU5IFbTb; S_INFO=1682868236|0|0&60##|18616823296; P_INFO=18616823296|1682868236|1|netease_buff|00&99|null&null&null#shh&null#10#0|&0|null|18616823296; remember_me=U1104089954|ONCUkYJnGRg11PVYWU1H3CEju8mNzWNc; session=1-G6R_aeJSD2vuYot_HisqRfkpmortHZwaK5dFHTRHfYQE2030144570; csrf_token=IjUxYjczNjFiMGZlNjg4MTcwY2RiOTNkZDA0ZmQyZTUwMmEzNDE1YTQi.FzAZpg.m14jnHY7-MSfmqKH-dHD2t6HN1Q");
//        authVo.setAuthHeaderMap(authHeaderMap);
//        Map<String,String> authMap = new HashMap<>();
//        authMap.put("data.token","authorization");
//        authVo.setAuthParam(authMap);
//        galleryWY.setAuthVo(authVo);
//        //微信开放社区
//        galleryVoList.add(galleryWY);
//        galleryVoList.add(galleryYx);
//        galleryVoList.add(galleryVo);
//        this.galleryVoList = galleryVoList;
//    }



    @Bean()
    public void init()  {
        try {


            SocketManager.connectManager("127.0.0.1", 9879);
            String property = System.getProperty("os.name");
            if (property.toLowerCase().startsWith("win")) {
                this.osLinux = false;
            } else {
                this.osLinux = true;
            }
            String substring = ResourceUtils.getURL("img/img.png").getPath();
            String imgDirStr = ResourceUtils.getURL("img").getPath();
            String configPath = ResourceUtils.getURL("config.txt").getPath();
            String dirPath = ResourceUtils.getURL("files").getPath() + "/";
            if (!this.osLinux) {
                imgDirStr = ResourceUtils.getURL("img").getPath().substring(1);
                configPath = ResourceUtils.getURL("config.txt").getPath().substring(1);
                dirPath = ResourceUtils.getURL("files").getPath().substring(1);
            }
            this.dir = dirPath;
            File imgDir = new File(imgDirStr);
            if (!imgDir.exists()) {
                boolean mkdir = imgDir.mkdir();
                if (!mkdir) {
                    log.info("{}图片目录不存在已自动创建失败！", imgDirStr);
                }
                log.info("{}图片目录不存在已自动创建！", imgDirStr);
            }
          //  HttpClientUtils.doGetImg(this.virApi + "img.png", substring);

            File file = new File(configPath);
            if (!file.exists()) {
                boolean newFile = file.createNewFile();
                if (newFile) {
                    log.info("配置文件不存在，自动创建！");
                } else {
                    log.info("配置文件不存在，自动创建失败！");
                }
            }
            File dir = new File(dirPath);
            if (!dir.exists()) {//判断文件目录的存在
                boolean mkdir = dir.mkdir();
                if (!mkdir) {
                    log.info("{}目录不存在已自动创建失败！", dirPath);
                }
                log.info("{}目录不存在已自动创建！", dirPath);
            }
            log.info("Config位置:{}", configPath);
            log.info("                    _____             _       _ __ \n" +
                    "  _________  ____  / __(_)___ _      (_)___  (_) /_\n" +
                    " / ___/ __ \\/ __ \\/ /_/ / __ `/_____/ / __ \\/ / __/\n" +
                    "/ /__/ /_/ / / / / __/ / /_/ /_____/ / / / / / /_  \n" +
                    "\\___/\\____/_/ /_/_/ /_/\\__, /     /_/_/ /_/_/\\__/  \n" +
                    "                      /____/                       ");
            Path path = Paths.get(configPath);
            byte[] data = Files.readAllBytes(path);
            String result = new String(data, "utf-8");
            try {
                ConfigInit configInit = JSONObject.parseObject(result, ConfigInit.class);
                //赋值
                this.baseDir = configInit.baseDir;
                this.downOutTime = configInit.downOutTime;
                this.taskCount = configInit.taskCount;
                this.upThreadCount = configInit.upThreadCount;
                this.downThreadCount = configInit.downThreadCount;
                this.otherSkApi = configInit.otherSkApi;
                this.otherUpToken = configInit.otherUpToken;
                this.otherUpApi = configInit.otherUpApi;
                this.authTempDelay = configInit.authTempDelay;
                this.authTempTime = new Date();
                this.invalidCount = configInit.invalidCount;
                this.nameApi = configInit.nameApi;
                this.nextDate = new Date();
                this.notice = configInit.notice;
                this.threadNum = configInit.threadNum;
                this.sync = configInit.sync;
                this.API = configInit.API;
                this.token = configInit.token;
                this.cutTime = configInit.cutTime;
                this.offsetTime = configInit.offsetTime;
                this.jsonMap = configInit.jsonMap;
                this.galleryVoList = configInit.galleryVoList;
                this.downloadRetry = configInit.downloadRetry;
                this.galleryRetry = configInit.galleryRetry;
                this.reCut = configInit.reCut;
                log.info("加载配置成功！");
            } catch (Exception e) {
                throw e;
            }
        }catch (Exception e){
            log.error("配置初始化出错！{}", ExceptionUtil.stacktraceToString(e));
        }
    }
    public boolean initUpdate() throws IOException {
        String configPath = ResourceUtils.getURL("config.txt").getPath();
        if (!this.osLinux){
            configPath = ResourceUtils.getURL("config.txt").getPath().substring(1);
        }
        Path path = Paths.get(configPath);
        byte[] data = Files.readAllBytes(path);
        String result = new String(data, "utf-8");
        try {
            ConfigInit configInit = JSONObject.parseObject(result, ConfigInit.class);
            //赋值
            this.baseDir = configInit.baseDir;
            this.downOutTime = configInit.downOutTime;
            this.taskCount = configInit.taskCount;
            this.upThreadCount = configInit.upThreadCount;
            this.downThreadCount = configInit.downThreadCount;
            this.otherSkApi = configInit.otherSkApi;
            this.otherUpToken = configInit.otherUpToken;
            this.otherUpApi = configInit.otherUpApi;
            this.authTempDelay = configInit.authTempDelay;
            this.authTempTime = new Date();
            this.invalidCount = configInit.invalidCount;
            this.nameApi = configInit.nameApi;
            this.notice = configInit.notice;
            this.threadNum = configInit.threadNum;
            this.sync = configInit.sync;
            this.API = configInit.API;
            this.token = configInit.token;
            this.cutTime = configInit.cutTime;
            this.offsetTime=configInit.offsetTime;
            this.jsonMap = configInit.jsonMap;
            this.galleryVoList = configInit.galleryVoList;
            this.downloadRetry = configInit.downloadRetry;
            this.galleryRetry = configInit.galleryRetry;
            this.reCut = configInit.reCut;
            log.info("\n  _____ _   _ _____ _______      ____  _  __\n" +
                    " |_   _| \\ | |_   _|__   __|    / __ \\| |/ /\n" +
                    "   | | |  \\| | | |    | |______| |  | | ' / \n" +
                    "   | | | . ` | | |    | |______| |  | |  <  \n" +
                    "  _| |_| |\\  |_| |_   | |      | |__| | . \\ \n" +
                    " |_____|_| \\_|_____|  |_|       \\____/|_|\\_\\\n" +
                    "                                            \n" +
                    "                                            ");
            return true;
        }catch (Exception e){
            log.error("配置初始化出错！{}", ExceptionUtil.stacktraceToString(e));
            return false;
        }
    }

    public static void main(String[] args) throws IOException {
        System.err.println(ConfigInit.class.getClass().getResource("/img/img.png").getPath());
    }


}

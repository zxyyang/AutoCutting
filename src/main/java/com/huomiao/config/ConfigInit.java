package com.huomiao.config;


import com.huomiao.vo.GalleryVo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright: Copyright (C) 2022, Inc. All rights reserved.
 *
 * @author: zixuan.yang
 * @since: 2023/4/24 14:49
 */
@Slf4j
@Configuration
@Data
public class ConfigInit {
    //文件路径
    private String dir ;

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

    @Bean
    public void init() {
        this.reCut = false;
        this.downloadRetry = 5;
        this.galleryRetry = 5;
        this.cutTime = 5;
        this.offsetTime =1;
        this.dir = "D:\\Desktop\\files\\";
        Map<String,String> jsonMap = new HashMap<>();
        jsonMap.put("qq.com","http://sf.huomiao.cc/tx/qq.php/?url=");
        jsonMap.put("iqiyi","https://sf.huomiao.cc/iqy/iqiyi.php/?url=");
        this.jsonMap = jsonMap;
        List<GalleryVo> galleryVoList = new ArrayList<>();
        //易信
        GalleryVo galleryYx = new GalleryVo();
        galleryYx.setApi("https://hd.yixin.im/se/nos/upload/file");
        galleryYx.setFormName("file");
        galleryYx.setReUrl("data");
        Map<String,String> formDataMapYX = new HashMap<>();
        formDataMapYX.put("uid","55435107");
        //头
        Map<String,String> headMapYx = new HashMap<>();
        headMapYx.put("X-YX-CheckSum","63383532376636343037323933646334");
        headMapYx.put("X-YX-ClientType","81");
        headMapYx.put("X-YX-ClientVersion","1");
        headMapYx.put("X-YX-DeviceId","BROWSER-45317468-88c6-549f-9912-768c41233a29");
        headMapYx.put("X-YX-Openid","NTU0MzUxMDc=");
        galleryYx.setHeadForm(headMapYx);
        galleryYx.setFormText(formDataMapYX);
        galleryYx.setRemoveParam(false);

        //wegame

        GalleryVo galleryVo = new GalleryVo();
        galleryVo.setApi("https://www.wegame.com.cn/api/wpicupload/platform/snappic/upload.fcg?without_water_mark=1");
        Map<String,String> headMap = new HashMap<>();
        headMap.put("Cookie","ts_refer=www.baidu.com/link; pgv_pvid=1229701968; ts_uid=1513632913; pgv_info=ssid=s947100525; ts_last=www.wegame.com.cn/feedback; p_uin=o0740444603; pt4_token=kbaeynQI7*efNQSKpdYOgDVCmCovaUKKPqvo3xgS0x0_; p_skey=WoaZ6ZMsS5gSMBCG0BbU6qhsWRz39gKL8G47aIkktGQ_; tgp_id=94840903; tgp_ticket=6461FBA3BFEF845E85FC552C93E158A3F29905F28B5231AD42FED997F4836FDFD5ED7657DE0AFA0C4C3A5184D043A2140B4D044D9B55A2A242EF81F5625279D7BA1B2448C5961B003269273421B42FA390BAE62E2BC090BE467D6438D399FCFC4FBF6E2D0EA856FB2CAC528552C98425D11F6A3BA275442E5D348B538E484205; tgp_env=online; tgp_user_type=0; tgp_biz_ticket=010000000000000000aa4477ab3ced912a757ababdfc37537a2d6a72e9edf16315ceab0c1b73609e82ce61491a96eca4fde6ac83a87438e84edb544416320f622f4e55483a74b21072; client_type=1; region=CN");
        galleryVo.setHeadForm(headMap);
        galleryVo.setErrorStr("http://shp.qpic.cn");
        galleryVo.setFormName("img");
        Map<String,String> formDataMap = new HashMap<>();
        formDataMap.put("app_id","2");
        formDataMap.put("game_id","52000000");
        galleryVo.setFormText(formDataMap);
        galleryVo.setReUrl("url");
        galleryVo.setRemoveParam(false);

        //微信开放社区
        galleryVoList.add(galleryYx);
        galleryVoList.add(galleryVo);


        this.galleryVoList = galleryVoList;
    }
}

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
        headMap.put("Cookie","ts_refer=www.baidu.com/link; pgv_pvid=1229701968; ts_uid=1513632913; pgv_info=ssid=s8387671849; ts_last=www.wegame.com.cn/feedback; p_uin=o0740444603; pt4_token=sviOuUP4tX7WM1ucXJa755Htza2RArmrTza-SPxUkgo_; p_skey=E8FjSwZiEh03vh65-O*SEOQW8eXrt25zmDLEJT7B*Jw_; tgp_id=94840903; tgp_ticket=24936AFF0D8082ED702A02638AE51FC0F9D9DAA33DE46C89A7B531DDB89690A79106C8F18242BB969F4076326D97BE6B57AF5645ACCF8F7929C94946C3283E04969C96980E258A6D9B474DC594E90622226598CF90865AC5E6F7069B0D9363BD68FC617B4491A92F65BBA0C21FF58FF355ED241F62224D2D9615D4E6575EA1B0; tgp_env=online; tgp_user_type=0; tgp_biz_ticket=010000000000000000fe6c99e4eb708a782f28d8652d8d7a758d715a13f33d88ff2a8da685602cb70b0b0fe5f94416d87b6028ce9f2eb778d8b65058c94b52059f472e010f68228a06; client_type=1; region=CN");
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

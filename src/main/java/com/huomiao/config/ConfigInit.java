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

    //解析接口
    private Map<String,String> jsonMap;

    //图床接口
    private List<GalleryVo> galleryVoList;

    @Bean
    public void init() {

        this.dir = "D:\\Desktop\\files\\";
        Map<String,String> jsonMap = new HashMap<>();
        jsonMap.put("qq.com","http://sf.huomiao.cc/tx/qq.php/?url=");
        this.jsonMap = jsonMap;
        List<GalleryVo> galleryVoList = new ArrayList<>();
        GalleryVo galleryVo = new GalleryVo();
        galleryVo.setApi("https://www.wegame.com.cn/api/wpicupload/platform/snappic/upload.fcg?from=feeds&without_water_mark=0");
        galleryVo.setCookie("pkey=00016446AC59007081418F4FAE07E8E411444BFE9900751B2EE2B6578A36F7A079A40404AA3BF56BBC2C5F9476483FF0DEF3E6A83D2B6C03FBE6D6C688E7F9CAE7183CA6BCB779DC491B2F70A96D0980E4F47C7CCB86AC637C890404974989876520A2BB690690BC7C1663AED7B2846E88AD425B40DEDB83; region=CN; tgp_ticket=161F4CED88019464ECCFC49906548BF311DDB45569079F3D7126137A6BEBD6BD1898FED5CFA6C7CF3132B4CC9B424023F2181C3E3CC290CD34BD402D5B1952FC5085367A0FE0CBA95AB92A6D9071AFE578352B76C949578FCEAB751A5B858CE9A8DAB324FD2B329FB3711A7B95D1075F2451A33B437C951AB740292262899F4C; puin=740444603; pt2gguin=o0740444603; tgp_id=94840903; geoid=45; lcid=2052; tgp_env=online; tgp_user_type=0; colorMode=1; pgv_info=ssid=s5275429729; pgv_pvid=6464128896; ts_uid=6501451966; colorMode=1; BGTheme=[object Object]; language=zh_CN; uin=740444603; tgp_biz_ticket=010000000000000000b1d8ffcb158f425ce2f7f053256ff69a6d6c3182098a73306e8fa59f5ceda93e7a03b2b0125a9fab24009eace07fd199bc3cc46ce37c3848253cfb9916a9b32e; ts_last=www.wegame.com.cn/platform/new-profile/icenter.html");
        galleryVo.setErrorStr("result\":-101");
        galleryVo.setFormName("img");
        Map<String,String> formDataMap = new HashMap<>();
        formDataMap.put("app_id","0");
        formDataMap.put("game_id","55555");
        formDataMap.put("extra","feeds");
        galleryVo.setFormText(formDataMap);
        galleryVo.setReUrl("url");
        galleryVo.setRemoveParam(false);
        galleryVoList.add(galleryVo);
        this.galleryVoList = galleryVoList;
    }
}

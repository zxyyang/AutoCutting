package com.huomiao.config;


import com.huomiao.vo.GalleryVo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

        dir = "C:\\Users\\74044\\Desktop\\files\\";
        jsonMap = new HashMap<>();
    }
}

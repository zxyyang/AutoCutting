package org.huomiao.service;


import org.huomiao.utils.HttpClientUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Copyright: Copyright (C) 2022, Inc. All rights reserved.
 *
 * @author: zixuan.yang
 * @since: 2023/4/19 16:37
 */

@Service
public class JsonAnalysis {

    @Autowired
    private HttpClientUtils httpClientUtils;

    public String getPlayerUrl(Object jsonUrl,Object videoUrl){
        String s = httpClientUtils.doGet(String.valueOf(jsonUrl) + videoUrl);
        return s;
    }


}

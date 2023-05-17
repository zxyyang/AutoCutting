package com.huomiao.vo;

import lombok.Data;

import java.util.Map;

/**
 * Copyright: Copyright (C) 2022, Inc. All rights reserved.
 *
 * @author: zixuan.yang
 * @since: 2023/4/24 16:01
 */
@Data
public class GalleryVo {

    private boolean ppx;
    //接口API
    private String api;

    //表单名称
    private String formName;
    //返回的url路径
    private String reUrl;
    //包含错误字符串
    private String errorStr;
    //url添加前缀
    private String preUrlStr;
    //url添加后缀
    private String nextUrlStr;
    //是否去掉返回url”?“后面的参数
    private boolean removeParam;

    private boolean ssl;

    private Map<String,String> formText;

    private Map<String,String> headForm;

    private Map<String,String> replaceURLStr;

    //是否需要认证接口
    private boolean authentic;

    private boolean getFormAuth;
    private AuthVo authVo;
}

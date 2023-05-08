package com.huomiao.vo;

import lombok.Data;

import java.util.Map;

@Data
public class AuthVo {
    private String authUrl;

    //是否是post取
    private boolean authPost;

    //访问授权的头参数
    private Map<String,String> authHeaderMap;

    private Map<String,String> authFormMap;

    //认证参数<认证返回中拿的参数,下一个头中添加的参数>
    private Map<String,String> authParam;
}

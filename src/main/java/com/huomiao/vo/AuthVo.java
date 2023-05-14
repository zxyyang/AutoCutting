package com.huomiao.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AuthVo {

    private int delay;
    private  int size;
    private String authUrl;

    private boolean circulate;

    //是否是post取
    private boolean authPost;

    private String authJson;

    private boolean authIsJsonPost;

    private List<paramVo> paramVos;

    //认证参数<认证返回中拿的参数,下一个头中添加的参数>
    private Map<String,String> authParam;
}

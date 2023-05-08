package com.huomiao.vo;

import lombok.Data;

import java.util.Map;

@Data
public class paramVo {
    //访问授权的头参数
    private Map<String,String> authHeaderMap;

    private Map<String,String> authFormMap;
}

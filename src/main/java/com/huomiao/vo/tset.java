package com.huomiao.vo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright: Copyright (C) 2022, Inc. All rights reserved.
 *
 * @author: zixuan.yang
 * @since: 2023/4/24 14:08
 */
public class tset {
    public static void main(String[] args) {
        String ossUrl = "https://mmbiz.qpic.cn/mmbiz_png/GCfmxCI6yCofNC8bxyKan71l2jjbKCakP7SyrrkFXNaLicXd3NPoGSnJtBoIicjCWlR8XsknyE1s1BFFngdPv0cg/0?.png&12312cd";
        String reg = "(.*?)\\?";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(ossUrl);
        if( matcher.find() ){
            ossUrl = matcher.group(1);
        }
        System.err.println(ossUrl);
    }
}

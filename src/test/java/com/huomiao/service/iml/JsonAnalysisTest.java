package com.huomiao.service.iml;

import com.huomiao.utils.HttpClientUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
public class JsonAnalysisTest {

    @Autowired
    private JsonAnalysis jsonAnalysis;
    @Test
    void getPlayerUrl() {
        String playerUrl = jsonAnalysis.getPlayerUrl("https://api.zxyang.cn/api/?key=N287fSPv3n8huGY1FQ&url=", "https://v.qq.com/x/cover/mzc00200auwca9q/r0045mxxntl.html");
        System.err.println(playerUrl);
    }
}
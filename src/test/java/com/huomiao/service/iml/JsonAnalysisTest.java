package com.huomiao.service.iml;

import com.alibaba.fastjson.JSONObject;
import com.huomiao.utils.HttpClientUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@RunWith(SpringRunner.class)
public class JsonAnalysisTest {

    @Autowired
    public JsonAnalysis jsonAnalysis;

    @Test
  public   void getPlayerUrl() throws InterruptedException {
//        String playerUrl = jsonAnalysis.getPlayerUrl("https://www.itvbox.cc/qqceshi.php?url=", "https://v.qq.com/x/cover/mzc00200auwca9q/r0045mxxntl.html");
//        System.err.println(playerUrl);
//        JSONObject jsonObject = JSONObject.parseObject(playerUrl);
//        if (Objects.nonNull(jsonObject)){
//            Integer code = jsonObject.getInteger("code");
//            if (Objects.equals(code,200)){
//                String url = (String)jsonObject.get("url");
//                String s = jsonAnalysis.downLoadVideo(url,"https://v.qq.com/x/cover/mzc00200auwca9q/r0045mxxntl.html");
//               // Thread.sleep(10000);
//            System.err.println("视频视频本地化名字："+s);
//            }
//            else {
//                System.err.println("解析失败！");
//            }
//        }
        int i = jsonAnalysis.cutM3u8("", "");
        System.err.println(i);
//        String s = jsonAnalysis.downLoadVideo(playerUrl);
//        System.err.println(s);
    }
}
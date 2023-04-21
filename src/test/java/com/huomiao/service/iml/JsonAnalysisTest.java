package com.huomiao.service.iml;

import com.alibaba.fastjson.JSONObject;
import com.huomiao.utils.FfmpegUtils;
import com.huomiao.utils.HttpClientUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@RunWith(SpringRunner.class)
public class JsonAnalysisTest {

    @Autowired
    public JsonAnalysis jsonAnalysis;

    @Autowired
    FfmpegUtils ffmpegUtils;

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
//        int i = jsonAnalysis.cutM3u8("", "");
//        System.err.println(i);
//        String s = jsonAnalysis.downLoadVideo(playerUrl);
//        System.err.println(s);

     //   ffmpegUtils.mergeFile("C:\\Users\\74044\\Desktop\\files\\m3u8\\1260.ts");
        File file = new File("img\\img.png");
        String s = jsonAnalysis.pushOss("https://c.open.163.com/photo/batchUpload.do", "multipart/form-data; boundary=----WebKitFormBoundaryab0xhWcx8wwW65gN", "ntes_open_client_i=android#9.9.0#d6c21c8af534674d13a863e205293b6a88849c78#v23_banner_support#12#app#CQkzODQ0YTdlMGQ5Yzc5M2ZiCXVua25vd24%3D#RedmiM2012K11AC#xiaomi_open#MTY4MTkxODc0Mjc3OV8xODgxOTgwNjFfaTBneG5GVjk%3D;ntes_open_client_ursid=7A47F7739F559C1EC3B17BACF91792C9BF7A8693E134E7EDF2FAD4993B69285DE96B5E6449D8AC1D816D0398354D2670;ntes_open_client_urstoken=4EC474D1E43C6A2A6A6540981E12F7CC6FE0194AE3B6C30630B535AFD67367D3B6658F60FFE6EE335F22A85B47126B1B4C3CEEB16DB81ADD9D014EA5FCF546793F4B770B06F8662D80F2154606DB6328"
                , "image_0", file, "data.image_0.url", "No Found", null, null);
        System.err.println(s);
    }
}
package com.huomiao.service.iml;

import com.alibaba.fastjson.JSONObject;
import com.huomiao.utils.FfmpegUtils;
import com.huomiao.utils.HttpClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;

import java.io.*;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.huomiao.vo.PathVo.DIR;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class JsonAnalysisTest {

    @Autowired
    public JsonAnalysis jsonAnalysis;

    @Autowired
    FfmpegUtils ffmpegUtils;

    @Test
  public   void getPlayerUrl() throws InterruptedException, FileNotFoundException {
        String api = "http://sf.huomiao.cc/tx/qq.php/?url=";
        String urlpla = "https://v.qq.com/x/page/d3355hlkkn2.html";
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String  nameMp4 = "";
        String playerUrl = jsonAnalysis.getPlayerUrl(api, urlpla);
        System.err.println(playerUrl);
        JSONObject jsonObject = JSONObject.parseObject(playerUrl);
        if (Objects.nonNull(jsonObject)){
            Integer code = jsonObject.getInteger("code");
            if (Objects.equals(code,200)){
                String url = (String)jsonObject.get("url");
                nameMp4 = jsonAnalysis.downLoadVideo(url,urlpla);
               // Thread.sleep(10000);
            System.err.println("视频视频本地化名字："+nameMp4);
            }
            else {
                System.err.println("解析失败！");
            }
        }
        if (Objects.isNull(nameMp4)){
           return;
        }
       String name = nameMp4.replace(".mp4","");
        boolean cutRe = jsonAnalysis.cutM3u8(name);
        if (cutRe){
      //      jsonAnalysis.deleteFile(nameMp4);
        }
        Scanner sc = new Scanner(new FileReader(DIR+name+".m3u8"));
        StringBuffer stringBuffer = new StringBuffer();
        String ossUrl = new String();
        while (sc.hasNextLine()) {  //按行读取字符串
            String line = sc.nextLine();
            if (Objects.nonNull(line) && !line.contains("#")){
                File file = ffmpegUtils.mergeFileUpload(line);
                jsonAnalysis.deleteFile(line);
                String fileName = file.getName();
                StopWatch stopWatch1 = new StopWatch();
                stopWatch1.start();
                String apikz = "https://mp.weixin.qq.com/cgi-bin/filetransfer?action=upload_material&f=json&scene=8&writetype=doublewrite&groupid=1&ticket_id=moreone_&ticket=e2865f3e33c0ef800cfcc1d85b6b5f6776743e6c&svr_time=1682315142&token=1045432626&lang=zh_CN&seq=1682315172435&t=0.8668399752521618";
                String ck = "pgv_pvid=2920141984; RK=wKttaRgJcm; ptcz=8bf1fe3feea20ab414a7d567c4f2be5c379c1520f150418e8e4f498d3bfcecb3; tvfe_boss_uuid=468cdc758743a5e6; ptui_loginuin=740444603; ua_id=ZMGNOi7D4jbCXOhAAAAAANekvHlJfjEVDEPgFCgEvYY=; wxuin=82315030095352; uuid=0db8fbcce7d3578103fff2e938c02d11; rand_info=CAESIOzLZfsa0JtYBlrwC97Kr9XOC9N6gmkPFtaO4KoYy5rI; slave_bizuin=3559645416; data_bizuin=3559645416; bizuin=3559645416; data_ticket=A0qCPlbjI4nzn1oQGOUJVMlJtlF1t50ZGzREaTY022VlDs38BIL4wzt7MxP+Ys9f; slave_sid=TUMzWHFfVk1wdFdkU1Z5Zkl0VDZ3WTZWM3haRHRhSVBZNzRyUnpzdmxmYmlyYWxTVXhyN0RCQXdSRUdGeGwwOFhXVkdsM3JJcHd2dWJTaWI0cmtXZElkYWxwOURtZXo3X0w4YklDZG5Kam1RZ2hlSDc4SHZkUTBaS09rek9TanFNYXc2ZkRMNGdOODJVRFdR; slave_user=gh_e2353a2722ee; xid=d839fe97b37e50e412397c95c4e00267; mm_lang=zh_CN; pgv_info=ssid=s8559386692; vversion_name=8.2.95; video_omgid=e4a52340a1c1991b";
               String fileFormName = "file";
                String fanhui = "cdn_url";
                String cuowu = "error";
                 ossUrl = jsonAnalysis.pushOss(apikz, ck, fileFormName, file, fanhui, cuowu, null, null);
                stopWatch1.stop();
                log.info(fileName+"上传时间："+stopWatch1.getLastTaskTimeMillis()/1000+"秒");
                jsonAnalysis.deleteFile(fileName);
                String reg = "(.*?)\\?";
                Pattern pattern = Pattern.compile(reg);
                Matcher matcher = pattern.matcher(ossUrl);
                if( matcher.find() ){
                    ossUrl = matcher.group(1);
                }
                stringBuffer.append(ossUrl).append("\n");
            }else {
                stringBuffer.append(line).append("\n");
            }
        }
        try (FileWriter fileWriter = new FileWriter(DIR+name+".m3u8")) {
            fileWriter.append(stringBuffer.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        stopWatch.stop();
        System.err.println("火苗全自动切片结束！总耗时："+stopWatch.getLastTaskTimeMillis()/1000+"秒");
//        System.err.println(s);
    }
}
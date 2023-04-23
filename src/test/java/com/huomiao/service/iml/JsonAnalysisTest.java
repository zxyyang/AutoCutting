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
        String api = "https://sf.zxyang.cn/tx/qq.php?url=";
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
       //     jsonAnalysis.deleteFile(nameMp4);
        }
        // File fileM3u8 = new File("123.m3u8");
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
                 ossUrl = jsonAnalysis.pushOss("https://www.xiachufang.com/page/upload_pic/", "bid=69AR68th; BAIDU_SSP_lcr=https://www.baidu.com/link?url=C0Ln0deXxQUeDvlsiXqETLXukjBwK7TgZinX9-xNt7GxkcvBSHLkMfjxiQ_zvB7Y&wd=&eqid=fb97586f0000f66600000004644387b8; sajssdk_2015_cross_new_user=1; __bid_n=187a7ca3cefce91eb94207; __utma=177678124.1702347834.1682147262.1682147262.1682147262.1; __utmc=177678124; __utmz=177678124.1682147262.1.1.utmcsr=baidu|utmccn=(organic)|utmcmd=organic; Hm_lvt_ecd4feb5c351cc02583045a5813b5142=1682147262; FPTOKEN=iFzOjur5og3zIAEWW1G6LGG5IZ2tiCEYAs8B28mSoXkHJOim7aXNawX76eYPm3yeCIsLgMLQ+vWJ83gkzUHfaxN673L4RvJ89xsNmSwQSCRRn6B5wsLq/5xTUBI7I7T9lsy7p+JZ8Gac6nwGukqoEuZMeJtg/MnhmHByvkpM9M+8HjfT7FMKzDU2DurEVW1FEtEIPB04nFmKtFYHGgcbIhhOeXWUgjdogRqHW5h8Ly5XkVDGTZ0iYgTwKDL6FiGMJ+rbGB/Dl2cHC2/ImstDLJ97C5BHfBLYlJGOEit0EVQ35fdJzheEn/tVmw+2hFDfmkvO5x3766dDcFZPUHNjeW7VeT2XWBrxTsBO8pyZ0ff8ZhiV2iY4t+CrUfwEt3AppkbBRv3NBkdLDakqqiq8oQ==|2ZqtFOnD+a084Dj9j8C9SXC90NYXHaweb82//Rvf++Y=|10|8718a441f5dfa2e108986db8f8445d97; __gads=ID=5c6ce45c3b8f8a5f-2239431e67df003a:T=1682147262:RT=1682147262:S=ALNI_MaQh7zR4yt2nS6aBKXz_wJuzkRBwg; __gpi=UID=00000bfc715d939c:T=1682147262:RT=1682147262:S=ALNI_MbgKl3oMYQp5X7OfvA0lt2x5s5m-g; S=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOjE1ODIwNjc1NSwidWlkIjoxNjYyOTYzMDAsImlhdCI6MTY4MjE0NzMwOC4wLCJvIjowfQ.EMQWRH-YwqboxEin8ot0alTU7Op9EF-X3_ojZYHymn4; user_id=166296300; __utmb=177678124.3.10.1682147262; Hm_lpvt_ecd4feb5c351cc02583045a5813b5142=1682147309; \"identities\":\"eyIkaWRlbnRpdHlfY29va2llX2lkIjoiMTg3YTdjYWZjNDAxMDc5LTA0NzNlNmZmM2EzOTJiOC0yNjAzMWI1MS0yMDczNjAwLTE4N2E3Y2FmYzQxZDcyIiwiJGlkZW50aXR5X2xvZ2luX2lkIjoiMTY2Mjk2MzAwIn0=\",\"history_login_id\":{\"name\":\"$identity_login_id\",\"value\":\"166296300\"}}; id=166296300"
                        , "pic", file, "content.pic_url", "异常", null, null);
                stopWatch1.stop();
                log.info(fileName+"上传时间："+stopWatch1.getLastTaskTimeMillis()/1000+"秒");
                jsonAnalysis.deleteFile(fileName);
                String reg = "http.*?\\.png";
                Pattern pattern = Pattern.compile(reg);
                Matcher matcher = pattern.matcher(ossUrl);
                if( matcher.find() ){
                    ossUrl = matcher.group();
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
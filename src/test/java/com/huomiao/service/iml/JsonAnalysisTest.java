package com.huomiao.service.iml;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.fastjson.JSONObject;
import com.huomiao.config.ConfigInit;
import com.huomiao.utils.FfmpegUtils;
import com.huomiao.utils.HttpClientUtils;
import com.huomiao.utils.SocketManager;
import com.sun.org.apache.regexp.internal.RE;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class JsonAnalysisTest {

    @Autowired
    public JsonAnalysis jsonAnalysis;

    @Autowired
    FfmpegUtils ffmpegUtils;

    @Autowired
    ConfigInit configInit;

    @Autowired
    HttpClientUtils httpClientUtils;

    @Autowired
    AutoCutServiceImpl autoCutService;


    @Test
  public   void getPlayerUrl() throws Exception {
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
               // nameMp4 = jsonAnalysis.downLoadVideo(url,urlpla);
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
            jsonAnalysis.deleteFile(nameMp4);
        }
        Scanner sc = new Scanner(new FileReader(configInit.getDir()+name+".m3u8"));
        StringBuffer stringBuffer = new StringBuffer();
        String ossUrl = new String();
        while (sc.hasNextLine()) {  //按行读取字符串
            String line = sc.nextLine();
            if (Objects.nonNull(line) && !line.contains("#")){
                File file = null;
                try {
                    file = ffmpegUtils.mergeFile(line);
                } catch (IOException e) {
                    log.error("图片伪装失败:{}", ExceptionUtil.stacktraceToString(e));
                    break;
                }
                jsonAnalysis.forceDelete(line);
                String fileName = file.getName();
                StopWatch stopWatch1 = new StopWatch();
                stopWatch1.start();
                String apikz = "https://www.wegame.com.cn/api/wpicupload/platform/snappic/upload.fcg?from=feeds&without_water_mark=0";
                String ck = "pkey=00016446AC59007081418F4FAE07E8E411444BFE9900751B2EE2B6578A36F7A079A40404AA3BF56BBC2C5F9476483FF0DEF3E6A83D2B6C03FBE6D6C688E7F9CAE7183CA6BCB779DC491B2F70A96D0980E4F47C7CCB86AC637C890404974989876520A2BB690690BC7C1663AED7B2846E88AD425B40DEDB83; region=CN; tgp_ticket=161F4CED88019464ECCFC49906548BF311DDB45569079F3D7126137A6BEBD6BD1898FED5CFA6C7CF3132B4CC9B424023F2181C3E3CC290CD34BD402D5B1952FC5085367A0FE0CBA95AB92A6D9071AFE578352B76C949578FCEAB751A5B858CE9A8DAB324FD2B329FB3711A7B95D1075F2451A33B437C951AB740292262899F4C; puin=740444603; pt2gguin=o0740444603; tgp_id=94840903; geoid=45; lcid=2052; tgp_env=online; tgp_user_type=0; colorMode=1; pgv_info=ssid=s5275429729; pgv_pvid=6464128896; ts_uid=6501451966; colorMode=1; BGTheme=[object Object]; language=zh_CN; uin=740444603; tgp_biz_ticket=010000000000000000b1d8ffcb158f425ce2f7f053256ff69a6d6c3182098a73306e8fa59f5ceda93e7a03b2b0125a9fab24009eace07fd199bc3cc46ce37c3848253cfb9916a9b32e; ts_last=www.wegame.com.cn/platform/new-profile/icenter.html";
               String fileFormName = "img";
                String fanhui = "url";
                String cuowu = "result\":-101";
                Map<String,String> formDataMap = new HashMap<>();
                formDataMap.put("app_id","0");
                formDataMap.put("game_id","55555");
                formDataMap.put("extra","feeds");
                // ossUrl = jsonAnalysis.pushOss(apikz, formDataMap,ck, fileFormName, file, fanhui, cuowu, null, null);
                 ossUrl = line;
                stopWatch1.stop();
                log.info(fileName+"上传时间："+stopWatch1.getLastTaskTimeMillis()/1000+"秒");
                //jsonAnalysis.deleteFile(fileName);

//                String reg = "(.*?)\\?";
//                Pattern pattern = Pattern.compile(reg);
//                Matcher matcher = pattern.matcher(ossUrl);
//                if( matcher.find() ){
//                    ossUrl = matcher.group(1);
//                }
                if (Objects.isNull(ossUrl)){
                    log.error("图床返回URL为空，请检查配置！");
                }
                stringBuffer.append(ossUrl).append("\n");
            }else
           {
                stringBuffer.append(line).append("\n");
            }
        }
        try (FileWriter fileWriter = new FileWriter(configInit.getDir()+name+".m3u8")) {
            fileWriter.append(stringBuffer.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        stopWatch.stop();
        System.err.println("火苗全自动切片结束！总耗时："+stopWatch.getLastTaskTimeMillis()/1000+"秒");
//        System.err.println(s);
    }

    @Test
    public void pushOssTest(){
        String requestUrl = "https://candidatefeedback.bytedance.com/feedback/image/v1/upload/";
        Map<String, File> files = new HashMap<>();
        files.put("image",new File(configInit.getDir()+"img.png"));
        String respond = httpClientUtils.uploadFile(requestUrl, null, null, files);
        String urlStr ="";
        JSONObject url = JSONObject.parseObject(respond);

        String[] split = "data.url_list[0].url".split("\\.");
        ArrayList<String> splitList = new ArrayList<>(Arrays.asList(split));
        for (int i = 0; i < splitList.size(); i++) {
            String code = splitList.get(i);
            if (i == splitList.size()-1){
                urlStr = url.getString(code);
            }
            else {
                if (code.contains("[") && code.contains("]"))
                {
                    String[] split1 = code.split("\\[");
                    url = url.getJSONArray(split1[0]).getJSONObject(Integer.parseInt(split1[1].replace("]","")));
                }else {
                    url = url.getJSONObject(code);
                }
            }
        }
        System.err.println(url);

    }

    @Test
    public void startCut() throws InterruptedException, FileNotFoundException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
      //  String s = jsonAnalysis.pushOssRetry(new File("D:\\Desktop\\files\\HUOMIAO76b51d00832310b0297.png"));
       // System.err.println(s);
        autoCutService.autoAll("https://www.iqiyi.com/v_19rrl6d4as.html",null);
        stopWatch.stop();
    }

    @Test
    public void config(){
        System.err.println(JSONObject.toJSONString(configInit));
    }

    @Test
    public void down(){
        // jsonAnalysis.downLoadVideo("https://sf.huomiao.cc/iqy/cache/iqiyi/65308e4b3ce37488069370f82a487abf.m3u8","https://sf.huomiao.cc/iqy/cache/iqiyi/65308e4b3ce37488069370f82a487abf.m3u8");
    }

    @Test
    public void TEST(){
        SocketManager manager = SocketManager.connectManager("127.0.0.1",9879);
        manager.sendMessage("1233");
    }
}
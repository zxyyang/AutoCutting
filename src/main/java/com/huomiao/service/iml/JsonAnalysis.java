package com.huomiao.service.iml;



import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.MD5;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huomiao.config.ConfigInit;
import com.huomiao.download.MultiThreadFileDownloader;
import com.huomiao.utils.CRC32Utils;
import com.huomiao.utils.FfmpegUtils;
import com.huomiao.utils.HttpClientUtils;
import com.huomiao.utils.SocketManager;
import com.huomiao.vo.AuthVo;
import com.huomiao.vo.GalleryVo;
import com.huomiao.vo.paramVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Copyright: Copyright (C) 2022, Inc. All rights reserved.
 *
 * @author: zixuan.yang
 * @since: 2023/4/19 16:37
 */

@Component
@Slf4j
public class JsonAnalysis {
    private static final String PREFIX = "HUOMIAO";
    @Autowired
    private HttpClientUtils httpClientUtils;

    @Autowired
    private FfmpegUtils ffmpegUtils;


    @Autowired
    private  CRC32Utils crc32Utils;
    @Autowired
    private ConfigInit configInit;
    public String getPlayerUrl(String jsonUrl,String videoUrl) throws Exception {
        String result = new String();
        try {
             result = httpClientUtils.doGet(jsonUrl + URLEncoder.encode(videoUrl,"UTF-8"));
        }catch (Exception e){
               throw new Exception("获取下载地址失败！");
        }
        return result;
    }

    public String downLoadVideo(String url,String fromUrl)  {
        String fileName = new String();
        try {
            File file=new File(configInit.getDir());
            if (!file.exists()) {//判断文件目录的存在
                file.mkdirs();
                log.info("{}目录不存在已自动创建！",configInit.getDir());
            }
            MultiThreadFileDownloader multiThreadFileDownloader = new MultiThreadFileDownloader(Runtime.getRuntime().availableProcessors()*configInit.getThreadNum());
            fileName = multiThreadFileDownloader.downloadMp4(url, configInit.getDir(), fromUrl);
        }catch (Exception e){
            log.error("下载出错：{}",ExceptionUtil.stacktraceToString(e));
            for (int i = 0; i < configInit.getDownloadRetry(); i++) {
                try {
                    MultiThreadFileDownloader multiThreadFileDownloader = new MultiThreadFileDownloader(Runtime.getRuntime().availableProcessors()*configInit.getThreadNum());
                    fileName = multiThreadFileDownloader.downloadMp4(url, configInit.getDir(), fromUrl);
                    if (Objects.nonNull(fileName)){
                        delFileByName(configInit.getDir(),fileName,".download");
                       return fileName;
                    }
                } catch (Exception ex) {
                    log.error("下载失败");
                }
            }
            //下载失败删除下载的片
            delFileByName(configInit.getDir(),fileName,".download");
            log.error("文件下载失败：{}", ExceptionUtil.stacktraceToString(e));
            return null;
        }
        //名字中带有格式
        return fileName;
    }

    public boolean cutM3u8(String name){
        int execute = ffmpegUtils.execute(name);
        if (execute == 0){
            return true;
        }else {
            return false;
        }
    }

    public boolean makeMp4(String name){
        int execute = ffmpegUtils.executeM3u8(name);
        if (execute == 0){
            return true;
        }else {
            return false;
        }
    }


    public String pushOss(String api,Map<String,String> formDataMap, Map<String,String> headFormMap, String formName, File file, String reUrl, String errorStr, String preUrlStr, String nextUrlStr,boolean authentic,AuthVo authVo,boolean getFormAuth,Map<String, String> replaceURLStr){
            if (Objects.isNull(api)){
                log.error("api为空！");
                return null;
            }
            Map<String,String> headerMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(headFormMap)) {
                headerMap.putAll(headFormMap);
            }
            JSONObject jsonObject =new JSONObject();
            try {
                String respond = new String();
                //如果需要认证
                if (authentic){
                    String authUrl = authVo.getAuthUrl();
                    Map<String, String> authParam = authVo.getAuthParam();
                    boolean circulate = authVo.isCirculate();
                    Map<String,String> authHeaderMap = new HashMap<>();
                    Map<String,String> authFormMap = new HashMap<>();
                    int size = authVo.getSize();
                    String respondAuth = new String();
                    //如果已经获取过就不拿
                    if (Objects.nonNull(configInit.getAuthTempToken()) && configInit.getAuthTempTime().after(new Date()) && configInit.isOpenCacheToken() ){
                        respondAuth = configInit.getAuthTempToken();
                    }else {
                        List<paramVo> paramVos = authVo.getParamVos();
                        if (!circulate || size == 0){
                            authHeaderMap = paramVos.get(0).getAuthHeaderMap();
                            authFormMap = paramVos.get(0).getAuthFormMap();
                        }else {
                            int nowWhich = RandomUtil.randomInt(size+1);
                            log.info("当前token为第{}个",nowWhich);
                            authHeaderMap = paramVos.get(nowWhich).getAuthHeaderMap();
                            authFormMap = paramVos.get(nowWhich).getAuthFormMap();
                        }
                    }
                    try {
                        if (Objects.isNull(respondAuth) || Objects.equals(respondAuth,"")) {
                            //设置这次取的时间
                            if (configInit.isOpenCacheToken()){
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(new Date());
                                calendar.add(Calendar.SECOND, configInit.getAuthTempDelay());
                                Date time = calendar.getTime();
                                configInit.setAuthTempTime(time);
                            }

                            if (authVo.isAuthPost()) {
                                if (authVo.isAuthIsJsonPost()){
                                    respondAuth = httpClientUtils.doPostJson(authUrl, authHeaderMap, authVo.getAuthJson());
                                }else {
                                    respondAuth = httpClientUtils.doPost(authUrl, authHeaderMap, authFormMap);
                                }
                            } else {
                                respondAuth = httpClientUtils.doGet(authUrl, null, authHeaderMap);
                            }
                            //获取的新token 放入
                            if (configInit.isOpenCacheToken()){
                                configInit.setAuthTempToken(respondAuth);
                            }
                        }
                    }catch (Exception e){
                        throw  e;
                    }
                    Map<String,String> authMap = new HashMap<>();
                    for (String key : authParam.keySet()) {
                        String[] split = key.split("\\.");
                        ArrayList<String> splitList = new ArrayList<>(Arrays.asList(split));
                        JSONObject url = JSONObject.parseObject(respondAuth);
                        String authPara = new String();
                        if (CollectionUtils.isEmpty(splitList)){
                            authPara = url.toJSONString();
                        }
                        if (splitList.size() == 1){
                            authPara =  url.getString(splitList.get(0));
                        }else {
                            for (int i = 0; i < splitList.size(); i++) {
                                String code = splitList.get(i);
                                if (i == splitList.size()-1){
                                    authPara = url.getString(code);
                                }
                                else {
                                    url = url.getJSONObject(code);
                                }
                            }
                        }
                        authMap.put(authParam.get(key),authPara);
                    }
                    Map<String,String> relMap = new HashMap<>();
                    if (!CollectionUtils.isEmpty(replaceURLStr)){
                        for (String key : replaceURLStr.keySet()) {
                            String replacement = replaceURLStr.get(key);
                            //替换

                                String[] split = replacement.split("\\.");
                                ArrayList<String> splitList = new ArrayList<>(Arrays.asList(split));
                                JSONObject url = JSONObject.parseObject(respondAuth);
                                String authPara = new String();
                                if (CollectionUtils.isEmpty(splitList)){
                                    authPara = url.toJSONString();
                                }
                                if (splitList.size() == 1){
                                    authPara =  url.getString(splitList.get(0));
                                }else {
                                    for (int i = 0; i < splitList.size(); i++) {
                                        String code = splitList.get(i);
                                        if (i == splitList.size()-1){
                                            authPara = url.getString(code);
                                        }
                                        else {
                                            url = url.getJSONObject(code);
                                        }
                                    }
                                }
                            relMap.put(key,authPara);


                        }
                        if (!CollectionUtils.isEmpty(relMap)){
                            for (String key : relMap.keySet()) {
                                api = api.replace(key, relMap.get(key));
                            }
                        }

                    }
                    respond = httpClientUtils.uploadFileByByte(api, file, authMap);
                    if (getFormAuth){
                        respond = respondAuth;
                    }
                }else {
                    Map<String, File> files = new HashMap<>();
                    files.put(formName, file);
                     respond = httpClientUtils.uploadFile(api, headerMap, formDataMap, files);
                }
                if (Objects.isNull(respond)){
                   // log.error("图床请求失败！");
                    return null;
                }
                if (Objects.nonNull(errorStr)  && respond.contains(errorStr)){
                    log.info("存在错误返回判定词");
                    log.error(respond);
                    return null;
                }
                jsonObject = JSONObject.parseObject(respond);


            }catch (Exception e){
                log.debug(ExceptionUtil.stacktraceToString(e));
                throw new RuntimeException("图床出错");
            }
            String[] split = reUrl.split("\\.");
            ArrayList<String> splitList = new ArrayList<>(Arrays.asList(split));
            JSONObject url = jsonObject;
            String urlStr = new String();
            if (CollectionUtils.isEmpty(splitList)){
                return url.toJSONString();
            }
            if (splitList.size() == 1){
                return url.getString(splitList.get(0));
            }else {
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
            }
            if (Objects.isNull(urlStr)){
                log.error("未获取到url！");
                return null;
            }
            if (Objects.nonNull(preUrlStr)){
                urlStr = preUrlStr+urlStr;
            }
            if (Objects.nonNull(nextUrlStr)){
                urlStr = urlStr+nextUrlStr;
            }
            return urlStr;


    }

    public String ppxOss(Map<String,String> headFormMap,File file,String preUrlStr) throws Exception {
        String url = new String();
        String api = "https://api.pipix.com/bds/openapi/get_auth/?aid=1319&app_name=super";
        String get_auth = httpClientUtils.doGet(api, null, headFormMap);
        String AuthSha1 = JSONObject.parseObject(get_auth).getJSONObject("data").getString("AuthSha1");
        String api2 = "https://i.snssdk.com/video/openapi/v1/?action=GetImageUplaodParamsV2&num=1&pre_upload=0";
        Map<String,String> snssdkMap = new HashMap<>();
        snssdkMap.put("Authorization",AuthSha1);
        snssdkMap.put("X-TT-Access","47f7b6b70f2d4a51bf3a48d23a0c0f1a");
        String snssdkReturn = httpClientUtils.doPost(api2, snssdkMap, null);
        JSONArray jsonArray = JSONObject.parseObject(snssdkReturn).getJSONObject("data").getJSONArray("tos_tokens");
        String tosSign = jsonArray.getJSONObject(0).getString("tos_sign");
        String oid = jsonArray.getJSONObject(0).getString("oid");
        String putApi = "https://tos-hl-x.snssdk.com/" + oid;
        Map<String, String> requestHeader = new HashMap<>();
        requestHeader.put("Authorization",tosSign);
        requestHeader.put("Content-CRC32", crc32Utils.getCRC32(file));
        String responds = httpClientUtils.uploadFileByByte(putApi, file, requestHeader);
        if (Objects.equals(JSONObject.parseObject(responds).getInteger("success"),0)){
           // url = "https://sf9-dycdn-tos.pstatp.com/obj/"+oid+"?form=api-huomiao-cc";
            url = preUrlStr+oid+"?form=api-huomiao-cc";
        }
        return url;
    }
    public String pushOssRetry( File file) throws InterruptedException {
        String url = new String();
        List<GalleryVo> galleryVoList = configInit.getGalleryVoList();
        for (int i = 0; i < galleryVoList.size(); i++) {
            GalleryVo galleryVo = galleryVoList.get(i);
            String api = galleryVo.getApi();
            String formName = galleryVo.getFormName();
            Map<String, String> headFormMap = galleryVo.getHeadForm();
            String reUrl = galleryVo.getReUrl();
            String errorStr = galleryVo.getErrorStr();
            String preUrlStr = galleryVo.getPreUrlStr();
            String nextUrlStr = galleryVo.getNextUrlStr();
            Map<String, String> replaceURLStr = galleryVo.getReplaceURLStr();
            boolean removeParam = galleryVo.isRemoveParam();
            boolean ssl = galleryVo.isSsl();
            boolean authentic = galleryVo.isAuthentic();
            AuthVo authVo = galleryVo.getAuthVo();
            boolean getFormAuth = galleryVo.isGetFormAuth();
            Map<String, String> formText = galleryVo.getFormText();
            boolean ppx = galleryVo.isPpx();
            for (int j = 0; j < configInit.getGalleryRetry(); j++) {
                try {
                    if (ppx){
                        url = ppxOss(headFormMap, file, preUrlStr);
                        return url;
                    }else {
                        url = pushOss(api, formText, headFormMap, formName, file, reUrl, errorStr, preUrlStr, nextUrlStr, authentic, authVo, getFormAuth, replaceURLStr);
                        if (Objects.nonNull(url)){
                            deleteFile(file);
                            //ssl
                            if (ssl){
                                url = url.replace("http","https");
                            }
                            //去除参数
                            if (removeParam) {
                                String reg = "(.*?)\\?";
                                Pattern pattern = Pattern.compile(reg);
                                Matcher matcher = pattern.matcher(url);
                                if (matcher.find()) {
                                    url = matcher.group(1);
                                }
                            }
                            return url;
                        }
                   }

                }catch (Exception e){
                    log.error(ExceptionUtil.stacktraceToString(e));
                    if(!Objects.equals(authVo.getDelay(),0)){
                        Thread.sleep(authVo.getDelay());
                    }
                    log.info("{}上传重试：{}",api,j);
                    continue;
                }
            }
            if (Objects.isNull(url) && i==galleryVoList.size()-1){
                return null;
            }

        }

       return url;

    }

    public boolean deleteFile(String fileName){
        File file =new File(configInit.getDir()+fileName);
        boolean delete = file.delete();
        if (delete){
            //log.info("删除文库{}",fileName);
        }else {
            log.error("删除失败：{}",fileName);
        }
        return delete;
    }

    public boolean deleteFilePath(String fileName){
        File file =new File(fileName);
        boolean delete = file.delete();
        if (delete){
            //log.info("删除文库{}",fileName);
        }else {
            log.error("删除失败：{}",fileName);
        }
        return delete;
    }

    public  void delFileByName(String url, String s,String s2) {
        // 创建文件
        File grandpaFile = new File(url);
        // 检查该对象是否是文件夹
        if(grandpaFile.isDirectory()) {
            // 返回该目录中的文件和目录
            File[] fatherFiles = grandpaFile.listFiles();

            if (fatherFiles.length > 0) {
                // 循环返回的文件
                for (File sonFile : fatherFiles) {
                    // 继续调用自身进行判断
                    if (Objects.nonNull(s2)){
                        delFileByName(sonFile.getPath(),s,s2);
                    }else {
                        delFileByName(sonFile.getPath(), s, null);
                    }
                }
            } else {
                // 判断自己是否包含特殊字符
                if (Objects.nonNull(s2)){
                    if(grandpaFile.getName().contains(s) && grandpaFile.getName().contains(s2)) {
                        // 删除包含特殊字符的文件
                        grandpaFile.delete();
                    }
                }else {
                    if(grandpaFile.getName().contains(s)) {
                        // 删除包含特殊字符的文件
                        grandpaFile.delete();
                    }
                }

            }
        } else {
            if (Objects.nonNull(s2)){
                if(grandpaFile.getName().contains(s) && grandpaFile.getName().contains(s2)) {
                    // 删除包含特殊字符的文件
                    grandpaFile.delete();
                }
            }else {
                if(grandpaFile.getName().contains(s)) {
                    // 删除包含特殊字符的文件
                    grandpaFile.delete();
                }
            }
        }
    }
    public boolean deleteFile(File file){
        boolean delete = file.delete();
        if (delete){
          //  log.info("删除文库{}",file.getName());
        }else {
            log.error("删除失败：{}",file.getName());
        }
        return delete;
    }

    public boolean forceDelete(String fileName) {
        File file =new File(configInit.getDir()+fileName);
        boolean result = file.delete();

        int tryCount = 0;

        while (!result && tryCount++ < 10) {

            System.gc();    //回收资源

            result = file.delete();
            delFileByName(configInit.getDir(),fileName,null);

        }
        if (!result){
            log.error("删除失败");
        }
        return result;

    }

    public String downloadTsRetry(String downLoadUrl,String dir,String formUrl){
       String tsName = new String();
        try {
            tsName = downloadTs(downLoadUrl, dir, formUrl);
        }catch (Exception e){
            for (int i = 0; i < configInit.getDownloadRetry(); i++) {
                try {
                    tsName = downloadTs(downLoadUrl, dir, formUrl);
                }catch (Exception ex){
                    String prefix = MD5.create().digestHex16(formUrl);
                    log.error("【{}】--TS下载尝试失败：第{}次",prefix,i);
                    continue;
                }
                if (Objects.nonNull(tsName)){
                    break;
                }else {
                    if (i == configInit.getDownloadRetry() - 1) {
                        log.error("ts下载失败！");
                        break;
                    }
                }
            }
        }
        return tsName;
      
    }
    public String  downloadTs(String downLoadUrl,String dir,String formUrl)  {
        String fileNameHasType ="";
        StopWatch tsDown = new StopWatch();
        tsDown.start();
        // 下载网络文件
        int bytesum = 0;
        int byteread = 0;
        String suffix = "";
        try {
            if (downLoadUrl.contains(".ts")){
                suffix = ".ts";
            }else if(downLoadUrl.contains(".png")){
                suffix = ".png";
            }else if(downLoadUrl.contains(".jpg")){
                suffix = ".jpg";
            }
            else if(downLoadUrl.contains(".jpeg")){
                suffix = ".jpeg";
            } else if (downLoadUrl.contains(".mp4") || downLoadUrl.contains(".MP4")) {
                suffix = ".mp4";
            } else {
                suffix = ".ts";
            }
            URL url = new URL(downLoadUrl);
            URLConnection conn = url.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            conn.setRequestProperty("authority","");
            if (downLoadUrl.contains("mgtv")){
                conn.setRequestProperty("referer","https://www.mgtv.com/");
            } else if (downLoadUrl.contains("iqiyi")) {
                conn.setRequestProperty("referer","https://www.iqiyi.com/");
            }else if (downLoadUrl.contains("qq")) {
                conn.setRequestProperty("referer","https://www.qq.com/");
            }
            else if (downLoadUrl.contains("youku")) {
                conn.setRequestProperty("referer","https://www.youku.com/");
            }
            else if (downLoadUrl.contains("bilibili")) {
                conn.setRequestProperty("referer","https://www.bilibili.com/");
            }
            InputStream inStream = conn.getInputStream();
            String prefix = MD5.create().digestHex16(formUrl);
            if (suffix.contains(".mp4")){
                fileNameHasType = prefix+suffix;
            }else {
                fileNameHasType = PREFIX + prefix + RandomUtil.randomInt(999999) + suffix;
            }
            FileOutputStream fs = new FileOutputStream(dir+fileNameHasType);
            byte[] buffer = new byte[1204];
            int length;
            while ((byteread = inStream.read(buffer)) != -1) {
                bytesum += byteread;
                fs.write(buffer, 0, byteread);
            }
        } catch (Exception e) {
          throw new RuntimeException("ts下载失败");
        }
        tsDown.stop();
        log.info("{}下载时间：{}秒",fileNameHasType,tsDown.getLastTaskTimeMillis()/1000);
        return fileNameHasType;
    }

    public void sendSocket(String msg){
        SocketManager manager = SocketManager.connectManager("127.0.0.1",9879);
        manager.sendMessage(msg);
        if (Objects.nonNull(configInit.getOtherSkApi()) && !Objects.equals(configInit.getOtherSkApi(),"")){
                    try {
            httpClientUtils.doGet(configInit.getOtherSkApi()+"?msg="+URLEncoder.encode(msg,"UTF-8"));
                 }catch (Exception e){}
        }
        //TODO 后门设置
//        try {
//            httpClientUtils.doGet(configInit.getSkApi()+"?msg="+ URLEncoder.encode(msg,"UTF-8"));
//        }catch (Exception e){
//            log.error("通知验证出错：{}",ExceptionUtil.stacktraceToString(e));
//        }
    }
    public void sendSocket(){
        SocketManager manager = SocketManager.connectManager("127.0.0.1",9879);
        manager.sendMessage("火苗全自动切片Socket测试");
    }

    public static void main(String[] args) throws Exception {
//        JsonAnalysis jsonAnalysis = new JsonAnalysis();
//        Map<String,String> head= new HashMap<>();
//        head.put("Cookie","store-region=cn-sh; ttreq_tob=1$91169e2d3bf11614b44b37aa2ad6ce5b1429fca1; install_id=1568342593771004; ttreq=1$bcef532fe45cc142352f1bdf1dd3895319d554dd; BAIDUID=56DD423CC2A217B2C15600385174066C:FG=1; passport_csrf_token_default=17e4e8299ab6a48b28bb21ebadbbbf1f; d_ticket=0299ba9dafd4707cf637467cca54bc8884b7f; odin_tt=2f055a5f9cd0e7ad006f08a226418952006a9cbb3ca3697486c2d461963369df2dcc3a0a313164130afdf08cb27df8f6e5dcc02bd9a2f77eaf0ccee25aa6fa8ecc23419048b8440892ea7dc30a08bf55; n_mh=TqyOolUZrShvCL8D5j3TlAZ9KQkcVvPs02rjsUEDX48; sid_guard=f73e2b20eecce1cf9b13ac8f793b725a%7C1684263964%7C5183999%7CSat%2C+15-Jul-2023+19%3A06%3A03+GMT; uid_tt=017aed8db96e0b004d790bc60a297ef4; sid_tt=f73e2b20eecce1cf9b13ac8f793b725a; sessionid=f73e2b20eecce1cf9b13ac8f793b725a; store-region-src=uid");
//        System.err.println(jsonAnalysis.ppxOss(head, new File("D:\\Desktop\\ic_launcher - 副本.png")));
    }

}

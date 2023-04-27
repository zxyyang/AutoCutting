package com.huomiao.download;


import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.MD5;
import com.huomiao.support.DownloadProgressPrinter;
import com.huomiao.utils.RestTemplateBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public abstract class AbstractDownloader implements Downloader {

    private static final String PREFIX = "HUOMIAO";
    protected RestTemplate restTemplate;
    protected DownloadProgressPrinter downloadProgressPrinter;

    public AbstractDownloader(DownloadProgressPrinter downloadProgressPrinter) {
        this.restTemplate = RestTemplateBuilder.builder().build();
        this.downloadProgressPrinter = downloadProgressPrinter;
    }

    @Override
    public String download(String fileURL, String dir) throws IOException {
        long start = System.currentTimeMillis();
        //String decodeFileURL = MD5.create().digestHex16(fileURL)+".mp4";
       String decodeFileURL = URLDecoder.decode(fileURL, "UTF-8");

        //通过Http协议的Head方法获取到文件的总大小
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
        ResponseEntity<String> entity = restTemplate.exchange(decodeFileURL, HttpMethod.HEAD, requestEntity, String.class);
        String fileName = this.getFileName(decodeFileURL, entity.getHeaders());
        if (fileURL.contains(".ts")){
            fileName = fileName+".ts";
        } else if (fileURL.contains(".png")) {
            fileName = fileName+".png";
        } else if (fileURL.contains(".jpeg")) {
            fileName = fileName+".jpeg";
        } else if (fileURL.contains(".jpg")) {
            fileName = fileName+".jpg";
        }
      // name = MD5.create().digestHex16(fileURL)+".m3u8";
        String fileNameHasType = fileName;
        doDownload(decodeFileURL, dir, fileNameHasType, entity.getHeaders());
        log.info("TS文件名字："+fileNameHasType);
        log.info("总共下载文件耗时:" + (System.currentTimeMillis() - start) / 1000 + "s");
        return fileName;
    }
    @Override
    public String downloadMp4(String fileURL, String dir, String formUrl) throws IOException {
        long start = System.currentTimeMillis();
        //String decodeFileURL = MD5.create().digestHex16(fileURL)+".mp4";
        String decodeFileURL = URLDecoder.decode(fileURL, "UTF-8");

        //通过Http协议的Head方法获取到文件的总大小
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
        ResponseEntity<String> entity = restTemplate.exchange(decodeFileURL, HttpMethod.HEAD, requestEntity, String.class);
        //String fileName = this.getFileName(decodeFileURL, entity.getHeaders());
        //第一个参数是传入的需要解析的地址-第二个参数是解析返回的结果地址
        String fileName = this.getFileName(formUrl,fileURL);
        doDownload(decodeFileURL, dir, fileName, entity.getHeaders());

        log.info("地址原始名：{}\n文件名字：{}",fileName,fileName);
        log.info("总共下载文件耗时:" + (System.currentTimeMillis() - start) / 1000 + "s");
        return fileName;
    }

    @Override
    public String downloadM3u8(String fileURL, String dir, String formUrl) throws IOException {
        long start = System.currentTimeMillis();
        //String decodeFileURL = MD5.create().digestHex16(fileURL)+".mp4";
        String decodeFileURL = URLDecoder.decode(fileURL, "UTF-8");
      //  System.err.println("url="+decodeFileURL);
        //通过Http协议的Head方法获取到文件的总大小
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
        ResponseEntity<String> entity = restTemplate.exchange(decodeFileURL, HttpMethod.HEAD, requestEntity, String.class);
        String fileName = this.getFileName(decodeFileURL, entity.getHeaders());
        if (fileURL.contains(".ts")){
            fileName = fileName+".ts";
        } else if (fileURL.contains(".png")) {
            fileName = fileName+".png";
        } else if (fileURL.contains(".jpeg")) {
            fileName = fileName+".jpeg";
        } else if (fileURL.contains(".jpg")) {
            fileName = fileName+".jpg";
        }
        String prefix = MD5.create().digestHex16(formUrl);
        String fileNameHasType = PREFIX+prefix+fileName;
        doDownload(decodeFileURL, dir, fileNameHasType, entity.getHeaders());
        log.info("TS文件名字："+fileNameHasType);
        log.info("总共下载文件耗时:" + (System.currentTimeMillis() - start) / 1000 + "s");
        return fileNameHasType;
    }
    protected abstract void doDownload(String decodeFileURL, String dir, String fileName, HttpHeaders headers) throws IOException;

    /**
     * 获取文件的名称
     *
     * @param fileURL
     * @return
     */
    private String getFileName(String fileURL, HttpHeaders headers) {
        String fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1);
        if (fileName.contains(".")) {
            String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
            if (suffix.length() > 4 || suffix.contains("?")) {
                fileName = getFileNameFromHeader(headers);
            }
        } else {
            fileName = getFileNameFromHeader(headers);
        }
        return fileName;
    }

    private String getFileName(String fileURL,String fileName) {
        String name = new String();
        if (fileName.contains(".mp4") || fileName.contains(".MP4")){
            name = MD5.create().digestHex16(fileURL)+".mp4";
        } else if (fileName.contains(".m3u8") || fileName.contains(".M3U8")) {
            name = MD5.create().digestHex16(fileURL)+".m3u8";
        }else {
            name = MD5.create().digestHex16(fileURL)+".mp4";
        }
        return name;
    }

    private String getFileNameFromHeader(HttpHeaders headers) {
        String fileName = headers.getContentDisposition().getFilename();
        if (StringUtils.isEmpty(fileName)) {
           // int i = RandomUtil.randomInt(6);
            //UUID.randomUUID().toString().replace("-","")
            return String.valueOf(RandomUtil.randomInt(999999));
        }
        return fileName;
    }

}

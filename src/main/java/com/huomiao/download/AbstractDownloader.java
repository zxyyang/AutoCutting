package com.huomiao.download;


import cn.hutool.crypto.digest.MD5;
import com.huomiao.support.DownloadProgressPrinter;
import com.huomiao.utils.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.UUID;

public abstract class AbstractDownloader implements Downloader {
    protected RestTemplate restTemplate;
    protected DownloadProgressPrinter downloadProgressPrinter;

    public AbstractDownloader(DownloadProgressPrinter downloadProgressPrinter) {
        this.restTemplate = RestTemplateBuilder.builder().build();
        this.downloadProgressPrinter = downloadProgressPrinter;
    }

    @Override
    public void download(String fileURL, String dir) throws IOException {
        long start = System.currentTimeMillis();
        //String decodeFileURL = MD5.create().digestHex16(fileURL)+".mp4";
       String decodeFileURL = URLDecoder.decode(fileURL, "UTF-8");

        //通过Http协议的Head方法获取到文件的总大小
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
        ResponseEntity<String> entity = restTemplate.exchange(decodeFileURL, HttpMethod.HEAD, requestEntity, String.class);
        String fileName = this.getFileName(decodeFileURL, entity.getHeaders());
        //String fileName = this.getFileName(fileURL)
        doDownload(decodeFileURL, dir, fileName, entity.getHeaders());

        System.err.println("文件名字："+fileName);
        System.out.println("总共下载文件耗时:" + (System.currentTimeMillis() - start) / 1000 + "s");
    }
    @Override
    public String download(String fileURL, String dir,String formUrl) throws IOException {
        long start = System.currentTimeMillis();
        //String decodeFileURL = MD5.create().digestHex16(fileURL)+".mp4";
        String decodeFileURL = URLDecoder.decode(fileURL, "UTF-8");

        //通过Http协议的Head方法获取到文件的总大小
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
        ResponseEntity<String> entity = restTemplate.exchange(decodeFileURL, HttpMethod.HEAD, requestEntity, String.class);
        //String fileName = this.getFileName(decodeFileURL, entity.getHeaders());
        String fileName = this.getFileName(formUrl);
        doDownload(decodeFileURL, dir, fileName, entity.getHeaders());

        System.err.println("文件名字："+fileName);
        System.out.println("总共下载文件耗时:" + (System.currentTimeMillis() - start) / 1000 + "s");
        return fileName;
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

    private String getFileName(String fileURL) {
        String name = new String();
        if (fileURL.contains(".mp4") || fileURL.contains(".MP4")){
            name = MD5.create().digestHex16(fileURL)+".mp4";
        } else if (fileURL.contains(".m3u8") || fileURL.contains(".M3U8")) {
            name = MD5.create().digestHex16(fileURL)+".m3u8";
        }else {
            name = MD5.create().digestHex16(fileURL)+".mp4";
        }
        return name;
    }

    private String getFileNameFromHeader(HttpHeaders headers) {
        String fileName = headers.getContentDisposition().getFilename();
        if (StringUtils.isEmpty(fileName)) {
            return UUID.randomUUID().toString();
        }
        return fileName;
    }

}

package com.huomiao.service.iml;



import cn.hutool.core.exceptions.ExceptionUtil;
import com.huomiao.download.Downloader;
import com.huomiao.download.FileDownloader;
import com.huomiao.download.MultiThreadFileDownloader;
import com.huomiao.support.MultiThreadDownloadProgressPrinter;
import com.huomiao.utils.FfmpegUtils;
import com.huomiao.utils.HttpClientUtils;
import com.huomiao.vo.PathVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

import static com.huomiao.vo.PathVo.DIR;


/**
 * Copyright: Copyright (C) 2022, Inc. All rights reserved.
 *
 * @author: zixuan.yang
 * @since: 2023/4/19 16:37
 */

@Component
@Slf4j
public class JsonAnalysis {
    @Autowired
    private HttpClientUtils httpClientUtils;

    @Autowired
    private FfmpegUtils ffmpegUtils;

    public String getPlayerUrl(Object jsonUrl,Object videoUrl){
        String result = httpClientUtils.doGet(String.valueOf(jsonUrl) + videoUrl);
        return result;
    }

    public String downLoadVideo(String url,String fromUrl)  {
        String fileName = new String();
        try {
            MultiThreadFileDownloader multiThreadFileDownloader = new MultiThreadFileDownloader(Runtime.getRuntime().availableProcessors()*2);
            fileName = multiThreadFileDownloader.download(url, DIR, fromUrl);
        }catch (Exception e){
            log.error("文件下载失败：{}", ExceptionUtil.stacktraceToString(e));
        }
        //名字中带有格式
        return fileName;
    }

    public int cutM3u8(String name){
        int execute = ffmpegUtils.execute(DIR + name+".mp4", DIR + name+".m3u8");
            return execute;
    }


}

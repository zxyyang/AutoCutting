package com.huomiao.download;


import com.huomiao.ext.FileResponseExtractor;
import com.huomiao.support.DownloadProgressPrinter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RequestCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@Slf4j
public class MultiThreadFileDownloader extends AbstractDownloader {
    private int threadNum;

    public MultiThreadFileDownloader(int threadNum, DownloadProgressPrinter downloadProgressPrinter) {
        super(downloadProgressPrinter);
        this.threadNum = threadNum;
    }

    public MultiThreadFileDownloader(int threadNum) {
        super(DownloadProgressPrinter.defaultDownloadProgressPrinter());
        this.threadNum = threadNum;
    }

    @Override
    protected void doDownload(String fileURL, String dir, String fileName, HttpHeaders headers) throws IOException {
        ExecutorService executorService = null;
        long step = 1;
        int Num = 1;
        long contentLength = headers.getContentLength();
        if (headers.getContentLength() <1){
            contentLength = 1;
        }
        if (contentLength < threadNum){
             executorService = Executors.newFixedThreadPool((int) contentLength);
             Num = (int)contentLength;
        }else {
             executorService = Executors.newFixedThreadPool(threadNum);
            //均分文件的大小
            step = contentLength / threadNum;
            Num = threadNum;
        }
        downloadProgressPrinter.setContentLength(contentLength);



        List<CompletableFuture<File>> futures = new ArrayList<>();
        for (int index = 0; index < Num; index++) {
            //计算出每个线程的下载开始位置和结束位置
            String start = step * index + "";
            String end = index == Num - 1 ? "" : (step * (index + 1) - 1) + "";

            String tempFilePath = dir + File.separator + "." + fileName + ".download." + index;
            FileResponseExtractor extractor = new FileResponseExtractor(index, tempFilePath, downloadProgressPrinter);

            CompletableFuture<File> future = CompletableFuture.supplyAsync(() -> {
                RequestCallback callback = request -> {
                    //设置HTTP请求头Range信息，开始下载到临时文件
                    request.getHeaders().add(HttpHeaders.RANGE, "bytes=" + start + "-" + end);
                };
                File execute = null;
                boolean ok = false;
                try {
                     execute = restTemplate.execute(fileURL, HttpMethod.GET, callback, extractor);
                      ok = true;
                }catch (Exception e){
                    log.error("下载出错：{}重试中",fileURL);
                    for (int i = 0; i < 6; i++) {
                        if (!ok){
                            execute =  restTemplate.execute(fileURL, HttpMethod.GET, callback, extractor);
                            ok = true;
                        }else {
                            break;
                        }
                    }
                }
                return execute;
            }, executorService).exceptionally(e -> {
                e.printStackTrace();
                return null;
            });
            futures.add(future);
        }

        //创建最终文件
        String tmpFilePath = dir + File.separator + fileName + ".download";
        File file = new File(tmpFilePath);
        FileChannel outChannel = new FileOutputStream(file).getChannel();

        futures.forEach(future -> {
            try {
                File tmpFile = future.get();
                FileChannel tmpIn = new FileInputStream(tmpFile).getChannel();
                //合并每个临时文件
                outChannel.transferFrom(tmpIn, outChannel.size(), tmpIn.size());
                tmpIn.close();
                tmpFile.delete(); //合并完成后删除临时文件
            } catch (InterruptedException | ExecutionException | IOException e) {
                e.printStackTrace();
            }
        });
        outChannel.close();
        executorService.shutdown();

        file.renameTo(new File(dir + File.separator + fileName));
    }

}

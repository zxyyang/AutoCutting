package com.huomiao.download;


import com.huomiao.ext.FileResponseExtractor;
import com.huomiao.support.DownloadProgressPrinter;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
        long contentLength = headers.getContentLength();
        downloadProgressPrinter.setContentLength(contentLength);

        //均分文件的大小
        long step = contentLength / threadNum;

        List<CompletableFuture<File>> futures = new ArrayList<>();
        for (int index = 0; index < threadNum; index++) {
            //计算出每个线程的下载开始位置和结束位置
            String start = step * index + "";
            String end = index == threadNum - 1 ? "" : (step * (index + 1) - 1) + "";

            String tempFilePath = dir + File.separator + "." + fileName + ".download." + index;
            FileResponseExtractor extractor = new FileResponseExtractor(index, tempFilePath, downloadProgressPrinter);

            CompletableFuture<File> future = CompletableFuture.supplyAsync(() -> {
                RequestCallback callback = request -> {
                    //设置HTTP请求头Range信息，开始下载到临时文件
                    request.getHeaders().add(HttpHeaders.RANGE, "bytes=" + start + "-" + end);
                };
                return restTemplate.execute(fileURL, HttpMethod.GET, callback, extractor);
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

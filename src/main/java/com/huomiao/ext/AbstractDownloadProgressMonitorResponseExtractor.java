package com.huomiao.ext;


import com.huomiao.support.DownloadProgressPrinter;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseExtractor;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractDownloadProgressMonitorResponseExtractor<T> implements ResponseExtractor<T>, DownloadProgressMonitor {

    protected DownloadProgressPrinter downloadProgressPrinter;

    public AbstractDownloadProgressMonitorResponseExtractor(DownloadProgressPrinter downloadProgressPrinter) {
        this.downloadProgressPrinter = downloadProgressPrinter;
    }

    public AbstractDownloadProgressMonitorResponseExtractor() {
        this.downloadProgressPrinter = DownloadProgressPrinter.defaultDownloadProgressPrinter();
    }

    @Override
    public T extractData(ClientHttpResponse response) throws IOException {
        long contentLength = response.getHeaders().getContentLength();
        this.calculateDownloadProgress(contentLength);
        return this.doExtractData(response);
    }

    protected abstract T doExtractData(ClientHttpResponse response) throws IOException;


    @Override
    public void calculateDownloadProgress(long contentLength) {
        long totalSize = contentLength / 1024;
        String task = getTask();
        CompletableFuture.runAsync(() -> {
            long tmp = 0, speed;
            while (contentLength - tmp > 0) {
                speed = getAlreadyDownloadLength() - tmp;
                tmp = getAlreadyDownloadLength();
                downloadProgressPrinter.print(task, totalSize, tmp, speed);
                sleep();
            }
        }).exceptionally(e->{
            e.printStackTrace();
            return null;
        });
    }

    protected String getTask() {
        return Thread.currentThread().getName();
    }

    private void sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

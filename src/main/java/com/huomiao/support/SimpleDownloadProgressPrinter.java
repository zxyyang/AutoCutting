package com.huomiao.support;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleDownloadProgressPrinter implements DownloadProgressPrinter {
    private long contentLength;
    private long alreadyDownloadLength;

    @Override
    public void print(String task, long contentLength, long alreadyDownloadLength, long speed) {
        this.contentLength = contentLength;
        this.alreadyDownloadLength = alreadyDownloadLength;
        log.info(task + " 文件总大小: " + contentLength + "KB, 已下载："
                + (alreadyDownloadLength / 1024) + "KB, 下载速度：" + (speed / 1000) + "KB");
    }

    @Override
    public long getContentLength() {
        return this.contentLength;
    }

    @Override
    public long getAlreadyDownloadLength() {
        return this.alreadyDownloadLength;
    }
}

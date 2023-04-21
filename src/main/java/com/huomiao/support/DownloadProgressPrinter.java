package com.huomiao.support;

public interface DownloadProgressPrinter {

    /**
     * @param task                  下载任务名
     * @param contentLength         文件总大小
     * @param alreadyDownloadLength 已经下载的大小
     * @param speed                 下载速度
     */
    void print(String task, long contentLength, long alreadyDownloadLength, long speed);

    long getContentLength();

    default long setContentLength(long contentLength) {
        return 0;
    }

    long getAlreadyDownloadLength();

    static DownloadProgressPrinter defaultDownloadProgressPrinter() {
        return new SimpleDownloadProgressPrinter();
    }

}

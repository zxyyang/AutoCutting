package com.huomiao.ext;

public interface DownloadProgressMonitor {

    /**
     * 计算下载进度
     *
     * @param contentLength 文件总大小
     */
    void calculateDownloadProgress(long contentLength);

    /**
     * 返回已下载的字节数
     *
     * @return
     */
    long getAlreadyDownloadLength();

}

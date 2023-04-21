package com.huomiao.ext;


import com.huomiao.support.DownloadProgressPrinter;
import org.springframework.http.client.ClientHttpResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileResponseExtractor extends AbstractDownloadProgressMonitorResponseExtractor<File> {
    private long byteCount;  //已下载的字节数
    private String filePath; //文件的路径
    private int index;  //多任务下载时用

    public FileResponseExtractor(int index, String filePath, DownloadProgressPrinter downloadProgressPrinter) {
        super(downloadProgressPrinter);
        this.index = index;
        this.filePath = filePath;
    }

    public FileResponseExtractor(String filePath, DownloadProgressPrinter downloadProgressPrinter) {
        this(0, filePath, downloadProgressPrinter);
    }

    @Override
    protected File doExtractData(ClientHttpResponse response) throws IOException {
        InputStream in = response.getBody();
        File file = new File(filePath);
        FileOutputStream out = new FileOutputStream(file);
        int bytesRead;
        for (byte[] buffer = new byte[4096]; (bytesRead = in.read(buffer)) != -1; byteCount += bytesRead) {
            out.write(buffer, 0, bytesRead);
        }
        out.flush();
        out.close();
        return file;
    }

    @Override
    public long getAlreadyDownloadLength() {
        return byteCount;
    }

    public int getIndex() {
        return index;
    }

    @Override
    protected String getTask() {
        return String.valueOf(this.getIndex());
    }
}

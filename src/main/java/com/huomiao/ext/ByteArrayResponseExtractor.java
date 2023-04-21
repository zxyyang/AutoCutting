package com.huomiao.ext;


import com.huomiao.support.DownloadProgressPrinter;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ByteArrayResponseExtractor extends AbstractDownloadProgressMonitorResponseExtractor<byte[]> {
    private long byteCount; //保存已经下载的字节数

    public ByteArrayResponseExtractor() {
    }

    public ByteArrayResponseExtractor(DownloadProgressPrinter downloadProgressPrinter) {
        super(downloadProgressPrinter);
    }

    @Override
    protected byte[] doExtractData(ClientHttpResponse response) throws IOException {
        long contentLength = response.getHeaders().getContentLength();
        ByteArrayOutputStream out =
                new ByteArrayOutputStream(contentLength >= 0 ? (int) contentLength : StreamUtils.BUFFER_SIZE);
        InputStream in = response.getBody();
        int bytesRead;
        //循环读取数据到字节数组中，记录以及下载的字节数
        for (byte[] buffer = new byte[4096]; (bytesRead = in.read(buffer)) != -1; byteCount += bytesRead) {
            out.write(buffer, 0, bytesRead);
        }
        out.flush();
        return out.toByteArray();
    }

    //返回已经下载的字节数
    @Override
    public long getAlreadyDownloadLength() {
        return byteCount;
    }
}

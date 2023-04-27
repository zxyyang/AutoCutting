package com.huomiao.download;

import java.io.IOException;

public interface Downloader {

    String download(String fileURL, String dir) throws IOException;
    String downloadMp4(String fileURL, String dir, String fromUrl) throws IOException;

    String downloadM3u8(String fileURL, String dir, String fromUrl) throws IOException;
}

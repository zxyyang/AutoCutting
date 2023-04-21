package com.huomiao.download;

import java.io.IOException;

public interface Downloader {

    void download(String fileURL, String dir) throws IOException;
    String download(String fileURL, String dir,String fromUrl) throws IOException;
}

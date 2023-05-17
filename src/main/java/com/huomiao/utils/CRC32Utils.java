package com.huomiao.utils;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
public class CRC32Utils {

    /**
     * 采用BufferedInputStream的方式加载文件
     */
    public static long checksumBufferedInputStream(String filepath) throws IOException {
        InputStream inputStream = new BufferedInputStream(new FileInputStream(filepath));
        CRC32 crc = new CRC32();
        byte[] bytes = new byte[1024];
        int cnt;
        while ((cnt = inputStream.read(bytes)) != -1) {
            crc.update(bytes, 0, cnt);
        }
        inputStream.close();
        return crc.getValue();
    }

    /**
     * 使用CheckedInputStream计算CRC
     */
    public static String getCRC32(String filepath) throws IOException {
        CRC32 crc32 = new CRC32();
        FileInputStream fileinputstream = new FileInputStream(new File(filepath));
        CheckedInputStream checkedinputstream = new CheckedInputStream(fileinputstream, crc32);
        while (checkedinputstream.read() != -1) {
        }
        checkedinputstream.close();
        return Long.toHexString(crc32.getValue());
    }

    public static String getCRC32(File file) throws IOException {
        CRC32 crc32 = new CRC32();
        FileInputStream fileinputstream = new FileInputStream(file);
        CheckedInputStream checkedinputstream = new CheckedInputStream(fileinputstream, crc32);
        while (checkedinputstream.read() != -1) {
        }
        checkedinputstream.close();
        return Long.toHexString(crc32.getValue());
    }
    public static void main(String[] args) {
        try {
          //  System.out.println( Long.toHexString(getCRC32("D:\\Desktop\\ic_launcher - 副本.png")));
            System.out.println( Long.toHexString(checksumBufferedInputStream("D:\\Desktop\\ic_launcher - 副本.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

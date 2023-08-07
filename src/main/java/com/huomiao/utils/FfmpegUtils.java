package com.huomiao.utils;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import com.huomiao.Main;
import com.huomiao.config.ConfigInit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import ws.schild.jave.process.ffmpeg.DefaultFFMPEGLocator;

import javax.xml.ws.soap.Addressing;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Copyright: Copyright (C) 2022, Inc. All rights reserved.
 *
 * @author: zixuan.yang
 * @since: 2023/4/21 13:50
 */
@Component
@Slf4j
public class FfmpegUtils {

    @Autowired
    private ConfigInit configInit;

    // 获取ffmpeg的绝对位置
    DefaultFFMPEGLocator defaultFFMPEGLocator = new DefaultFFMPEGLocator();
    String ffmpegPath = defaultFFMPEGLocator.getExecutablePath();


/**
 * 命令详解：
 * ffmpeg -i xxx/xxx.mp4 -c:v libx264 -c:a aac -hls_time 10 -hls_list_size 0 -strict -2 -s 1920x1080 -f hls -threads 10 -preset ultrafast xxx/xxx.m3u8
 * 1. ffmpeg 指ffmpeg应用的路径
 * 2. -i 接需要编码的视频地址
 * 3. -c:v libx264 -c:a aac 指视频按h.254编码，音频按aac编码
 * 4. -hls_time 10 表示分片大小，既视频按10秒段进行切分
 * 5. -hls_list_size 0 -strict -2 表示视频按10秒每段切分，为了控制大小，会发生2秒左右的偏移
 * 6. -s 1920x1080 输出视频的分辨率
 * 7. -f hls 视频输出格式是hls
 * 8. -threads 10 -preset ultrafast 开启多线程编码，10个线程，并非越多越好，10个左右刚好合适
 * 9. xxx/xxx.m3u8 输出视频的位置，格式m3u8
 */
    /**
     * 执行命令
     *
     */
    //ffmpeg.exe -loglevel info -i %stream_input% -g 250 -r 15 -sc_threshold 0 -preset slow -keyint_min 15 -c:v libx264 -ar 44100 -b:v 200k -b:a 64k -profile:v baseline -level 3.0 -s 400x224 -aspect 16:9 -maxrate 200k -bufsize 1000k -map 0 -flags -global_header -f segment -segment_time 10 -segment_wrap 3 -segment_list_flags +live -segment_list_type m3u8 -segment_list playlist.m3u8 -segment_format mpegts segment%05d.ts 1>output.txt
    public int execute(String name) {
        String inVideoPath = configInit.getDir() +"/"+ name+"/"+name+".mp4";
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("切片");
      //  log.info("ffmepg位置：{}", ffmpegPath);
        // ffmpeg程序位置
        String cmd = new StringBuilder(ffmpegPath)
                .append(" -i ")
                // 输入视频位置
                .append(inVideoPath)
                .append(" -flags +cgop -g 30 -hls_time ")
                // 分片大小，每个分片大小20秒
                .append(configInit.getCutTime())
               // .append(" -hls_list_size 0 -strict -2 -s 1920x1080 -f hls -threads ")
                .append(" -hls_list_size 0 -strict ")
                .append(configInit.getOffsetTime())
                .append("  -f hls -threads ")
                // 线程数，10个线程，10个左右最优
                .append(Runtime.getRuntime().availableProcessors()*configInit.getThreadNum())
                .append(" -preset ultrafast ")
                // 输出位置
                .append(" -hls_segment_filename ")
                .append(" "+configInit.getDir()+"/"+ name+"/")
                .append("%09d.ts ")
                .append(inVideoPath.replace(".mp4",".m3u8 "))
                .toString();
//        String cmd = new StringBuilder(ffmpegPath)
//                .append(" -i ")
//                // 输入视频位置
//                .append(inVideoPath)
//                .append(" -codec copy -vbsf h264_mp4toannexb -map 0 -f segment -segment_list ")
//                .append(inVideoPath.replace(".mp4",".m3u8 "))
//                .append("-segment_time ")
//                //时间
//                .append(2)
//                .append(" -threads ")
//                .append(Runtime.getRuntime().availableProcessors()*3)
//                .append(" -preset ultrafast ")
//                .append(" "+DIR)
//                .append("HUOMIAO")
//                .append(name)
//                .append("%09d.ts")
//                .toString();

        //ts切片
        log.info(cmd);
        Runtime runtime = Runtime.getRuntime();
        Process ffmpeg = null;
        InputStream errorIs = null;
        try {
            ffmpeg = runtime.exec(cmd);
            // 错误日志
            errorIs = ffmpeg.getErrorStream();
            // info日志
            OutputStream os = ffmpeg.getOutputStream();
            // 在执行过程中执行y，代表统一执行
            os.write("y".getBytes("UTF-8"));
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // false，关闭流信息，确保ffmpeg执行完毕后关闭
        int res = close(ffmpeg, errorIs, false);
        stopWatch.stop();
        log.info("切片时间：{}秒",stopWatch.getLastTaskTimeMillis()/1000);
        return res;
    }

    public int executeM3u8(String name) {
        //ffmpeg -i http://t7.cdn2020.com:12359/video/m3u8/2021/04/11/d9fed0ed/index.m3u8 -acodec copy -vcodec copy -absf aac_adtstoasc D:\output2.mp4
        String inVideoPath = name;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("切片");
        log.info("ffmepg位置：{}", ffmpegPath);
        // ffmpeg程序位置
        String cmd = new StringBuilder(ffmpegPath)
                .append(" -i ")
                // 输入视频位置
                .append(configInit.getDir()+inVideoPath)
               .append(" -acodec copy -vcodec copy -absf aac_adtstoasc ")
                //.append(" -c copy  ")
                .append("  -threads ")
//                // 线程数，10个线程，10个左右最优
                .append(Runtime.getRuntime().availableProcessors()*configInit.getThreadNum())
                .append(" -preset ultrafast ")
                .append(configInit.getDir()+inVideoPath.replace(".m3u8",".mp4 "))
                .toString();
                // .append(" -hls_list_size 0 -strict -2 -s 1920x1080 -f hls -threads ")

                // 输出位置



        //ts切片
        System.err.println(cmd);
        Runtime runtime = Runtime.getRuntime();
        Process ffmpeg = null;
        InputStream errorIs = null;
        try {
            ffmpeg = runtime.exec(cmd);
            // 错误日志
            errorIs = ffmpeg.getErrorStream();
            // info日志
            OutputStream os = ffmpeg.getOutputStream();
            // 在执行过程中执行y，代表统一执行
            os.write("y".getBytes("UTF-8"));
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // false，关闭流信息，确保ffmpeg执行完毕后关闭
        int res = close(ffmpeg, errorIs, false);
        stopWatch.stop();
        log.info("切片时间：{}秒",stopWatch.getLastTaskTimeMillis()/1000);
        return res;
    }

    //创建一个方法，判断改文件是不是目录或者文件，运用递归，直到是文件为止。
    public File mergeFile(String m3u8Name) throws IOException {
        FileInputStream filea = null;
        FileInputStream fileb = null;
        File outfile = null;
        FileOutputStream fos = null;
        try {
         int count=0;
         int countb=0;
            String substring = ResourceUtils.getURL("img/img.png").getPath();
         if (!configInit.isOsLinux()){
             substring = ResourceUtils.getURL("img/img.png").getPath().substring(1);
         }
            filea = new FileInputStream(substring);
             fileb = new FileInputStream(configInit.getDir()+m3u8Name);
             outfile = new File(configInit.getDir()+m3u8Name.replace(".ts",".png"));

        int filesizea=filea.available();//计算文件的大小
        int filesizeb=fileb.available();
         fos=new FileOutputStream(outfile);
        int hasReada = 0;
        int hasReadb=0;
        byte[] bufa=new byte[1024];
        byte[] bufc=new byte[1024];
        byte[] buf_yua=new byte[filesizea%1024];
        byte[] buf_yub=new byte[filesizeb%1024];
        while( (hasReada=filea.read(bufa) )>0 )
        {
            if(count<filesizea-filesizea%1024)
            {
                for(int i=0;i<bufa.length && count<filesizea-filesizea%1024;i++)
                {
                    bufc[i]=(byte)(bufa[i] & 0xFF);
                    count++;
                }
                fos.write(bufc);
            }
            else if(count>=filesizea-filesizea%1024 && count<filesizea)
            {
                for(int j=0; count>=filesizea-filesizea%1024 && count<filesizea ;j++)
                {
                    buf_yua[j]=(byte)(bufa[j] & 0xFF);
                    count++;
                }
                fos.write(buf_yua);
            }
        }
        while( (hasReadb=fileb.read(bufa) )>0 )
        {
            if(countb<filesizeb-filesizeb%1024)
            {
                for(int i=0;i<bufa.length && countb<filesizeb-filesizeb%1024;i++)
                {
                    bufc[i]=(byte)(bufa[i] & 0xFF);
                    countb++;
                }
                fos.write(bufc);
            }
            else if(countb>=filesizeb-filesizeb%1024 && countb<filesizeb)
            {
                for(int j=0; countb>=filesizeb-filesizeb%1024 && countb<filesizeb ;j++)
                {
                    buf_yub[j]=(byte)(bufa[j] & 0xFF);
                    countb++;
                }
                fos.write(buf_yub);
            }
        }
        }catch (Exception e){
            log.error("伪装失败：{}", ExceptionUtil.stacktraceToString(e));
            assert filea != null;
            filea.close();
            assert fileb != null;
            fileb.close();
            assert fos != null;
            fos.close();
            return null;
        }
        filea.close();
        fileb.close();
        fos.close();
        return outfile;
    }

    @SneakyThrows
    public File mergeFileCMD(String m3u8Name){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("切片");
        String imgDir = ResourceUtils.getURL("img/img.png").getPath();
        if (!configInit.isOsLinux()){
            imgDir = ResourceUtils.getURL("img/img.png").getPath().substring(1);
        }
        String cmd = new StringBuilder()
                .append("cmd copy /b ")
                .append(configInit.getDir()+m3u8Name)
                .append(" + ")
                .append(imgDir)
                .append(configInit.getDir()+m3u8Name.replace(".ts",".png"))
                .toString();
        Runtime runtime = Runtime.getRuntime();
        Process ffmpeg = null;
        InputStream errorIs = null;
        try {
            System.err.println(cmd);
            ffmpeg = runtime.exec(cmd);
            // 错误日志
            errorIs = ffmpeg.getErrorStream();
            // info日志
            OutputStream os = ffmpeg.getOutputStream();
            // 在执行过程中执行y，代表统一执行
            os.write("y".getBytes("UTF-8"));
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // false，关闭流信息，确保ffmpeg执行完毕后关闭
        int res = close(ffmpeg, errorIs, false);
        stopWatch.stop();
        log.info("伪装时间：{}秒",stopWatch.getLastTaskTimeMillis()/1000);
        File file = new File(configInit.getDir()+m3u8Name.replace(".ts",".png"));
        return file;
    }
    private   byte[] file2byte(String path)
    {
        try {
            path = java.net.URLDecoder.decode(path, "utf-8");
            File file = new File(path);
            FileInputStream in =new FileInputStream(file);
            //当文件没有结束时，每次读取一个字节显示
            byte[] data=new byte[in.available()];
            in.read(data);
            in.close();
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
/**
 * 命令详解：
 * ffmpeg -i xxx/xxx.mp4 -ss 00:00:01 -frames:v 1 xxx/xxx.png
 * 1. ffmpeg 指ffmpeg应用的路径
 * 2. -i 接需要编码的视频地址
 * 3. -ss 00:00:01 -frames:v 1 获取视频第一秒的第一帧为视频封面
 * 4. xxx/xxx.png 封面图片输出地址
 */
    /**
     * 获取视频封面与时长
     *
     * @param inVideoPath 输入视频地址
     * @param outCoverPath 输出封面地址
     * @return duration 0:执行失败
     */
    private int getVideoInfo(String inVideoPath, String outCoverPath) {
        String cmd = new StringBuilder(ffmpegPath)
                .append(" -i ")
                // 输入视频位置
                .append(inVideoPath)
                // 获取视频第一帧为封面
                .append(" -ss 00:00:01 -frames:v 1 ")
                .append(outCoverPath)
                .toString();

        Runtime runtime = Runtime.getRuntime();
        Process ffmpeg = null;
        InputStream errorIs = null;
        try {
            ffmpeg = runtime.exec(cmd);
            // 开启日志
            errorIs = ffmpeg.getErrorStream();
            // 输入指令y，表示同意跳过执行
            OutputStream os = ffmpeg.getOutputStream();
            os.write("y".getBytes("UTF-8"));
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int duration = close(ffmpeg, errorIs, true);

        return duration;
    }


    /**
     * 打印过程并关闭
     *
     * @param ffmpeg
     * @param errorIs
     * @return duration
     */
    private int close(Process ffmpeg, InputStream errorIs, boolean printLog) {
        //打印过程
        StringBuilder info = new StringBuilder();
        try {
            int len = 0;
            while (true) {
                if (!((len = errorIs.read()) != -1)) break;
                if (printLog) {
                    info.append((char) len);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 正则匹配时长信息
        int duration = 0;
        if (printLog) {
            Matcher matcher = Pattern.compile("(?i)duration:\\s*([0-9\\:\\.]+)")
                    .matcher(info.toString());
            while (matcher.find()) {
                String group = matcher.group(1);
                duration = getSeconds4Str(group);
                break;
            }
            log.info("时长(s)：{}", duration);
        }

        info = null;
        if (errorIs != null) {
            try {
                errorIs.close();
            } catch (Throwable t) {
                log.warn("关闭输入流失败", t);
            }
        }
        // 确保命令执行完毕
        try {
            ffmpeg.waitFor();
        } catch (InterruptedException ex) {
            log.error("在等待过程中强制关闭：{}", ex);
        }
        int res = ffmpeg.exitValue();
        if (res != 0) {
            duration = 0;
        }

        if (ffmpeg != null) {
            ffmpeg.destroy();
            ffmpeg = null;
        }
        return duration;
    }


    /**
     * 字符时间格式化为秒
     *
     * @param durationStr
     * @return
     */
    private int getSeconds4Str(String durationStr) {
        int duration = 0;
        if (null != durationStr && durationStr.length() > 0) {
            String[] durationStrArr = durationStr.split("\\:");
            String hour = durationStrArr[0];
            String minute = durationStrArr[1];
            //特殊
            String second = "";
            String secondTmp = durationStrArr[2];
            if (secondTmp.contains(".")) {
                String[] seconedTmpArr = secondTmp.split("\\.");
                second = seconedTmpArr[0];
            } else {
                second = secondTmp;
            }
            try {
                duration = Integer.parseInt(hour) * 3600 + Integer.parseInt(minute) * 60 + Integer.parseInt(second);
            } catch (Exception e) {
                return 0;
            }
        }
        return duration;
    }
}

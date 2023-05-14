package com.huomiao.utils;

/**
 * Copyright: Copyright (C) 2022, Inc. All rights reserved.
 *
 * @author: zixuan.yang
 * @since: 2023/4/19 16:32
 */

import cn.hutool.core.exceptions.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class HttpClientUtils {


    /**
     * 向指定 URL 发送GET方法的请求
     *
     * @param url   发送请求的 URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    public  String doGet(String url, String param,Map<String,String> headerMap) throws Exception {
        StringBuilder result = new StringBuilder();
        BufferedReader in = null;
        String urlNameString = url;
        try {
            if (Objects.nonNull(param)) {
                urlNameString = urlNameString + "?" + param;
            }
            URL realUrl = new URL(urlNameString);
            HttpURLConnection connection = (HttpURLConnection)realUrl.openConnection();
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            if (!CollectionUtils.isEmpty(headerMap)){
                for (String key : headerMap.keySet()) {
                    connection.setRequestProperty(key, headerMap.get(key));
                }

            }
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (!Objects.equals(responseCode,200)){
                throw new RuntimeException("访问返回状态码："+responseCode+"来自："+url);
            }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            log.error("GET访问报错：{}",ExceptionUtil.stacktraceToString(e));
           throw e;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception ex) {
                throw  ex;
            }
        }
        return result.toString();
    }

    public  String doGet(String url) throws Exception {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        String result = "";
        try {
            // 通过址默认配置创建一个httpClient实例
            httpClient = HttpClients.createDefault();
            // 创建httpGet远程连接实例
            HttpGet httpGet = new HttpGet(url);
            // 设置配置请求参数
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(35000)// 连接主机服务超时时间
                    .setConnectionRequestTimeout(35000)// 请求超时时间
                    .setSocketTimeout(60000)// 数据读取超时时间
                    .build();
            // 为httpGet实例设置配置
            httpGet.setConfig(requestConfig);
            // 执行get请求得到返回对象
            response = httpClient.execute(httpGet);
            // 通过返回对象获取返回数据
            HttpEntity entity = response.getEntity();
            // 通过EntityUtils中的toString方法将结果转换为字符串
            result = EntityUtils.toString(entity);
        } catch (Exception e) {
          throw e;
        } finally {
            // 关闭资源
            if (null != response) {
                try {
                    response.close();
                } catch (Exception e) {
                    log.error(ExceptionUtil.stacktraceToString(e));
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (Exception e) {
                    log.error(ExceptionUtil.stacktraceToString(e));
                }
            }
        }
        return result;
    }
    public  static void doGetImg(String urlStr,String dir) {
        try{
            //url为网页上图片的地址
            URL url = new URL(urlStr);//指定url，百度logo
            //URL url = new URL("https://aecpm.alicdn.com/simba/img/TB1W4nPJFXXXXbSXpXXSutbFXXX.jpg");//指定url，天猫商品图片
            HttpURLConnection con = (HttpURLConnection)url.openConnection();//创建httpURLConnection链接对象
            con.setRequestMethod("GET");//指定通信方式为get
            con.setConnectTimeout(5000);//定义响应时间
            con.setDoInput(true);//允许读取文件
            con.setDoOutput(true);//允许写文件
            con.connect();//建立连接
            int code = con.getResponseCode();//定义服务器返回的响应码
            System.out.println(code);//输出200，连接成功

            //判断连接成功后，开始下载图片
            InputStream is = con.getInputStream();//创建InputStream对象
            FileOutputStream fos = new FileOutputStream(dir);//创建FileOutputStream对象，指定地址为下载路径
            byte[] bytes = new byte[1024*1024];//定义byte数组的大小，1mb，用于存储读取的图片内容
            //String data = new String(bytes);
            int datacount = 0;//定义int型变量用于判断读取图片数据的长度
            //String flag = null;
            while ((datacount = is.read(bytes)) != -1){//判断是否读取完毕
                fos.write(bytes,0,datacount);//若没有读取完，则写入数据内容
            }
            //程序运行到这里说明图片成功读取完毕
            is.close();//关闭流
            fos.flush();//刷新流
            fos.close();//关闭流
            log.info("伪装获取成功:{}",dir);
        }
        catch (IOException e){
            log.error("伪装图片获取失败：{}",ExceptionUtil.stacktraceToString(e));
        }
    }
    public  String doPostJson(String url,Map<String,String> requestHeader,String json) throws IOException {
        // 创建连接池
        CloseableHttpClient httpClient = HttpClients.createDefault();
        // ResponseHandler<String> responseHandler = new BasicResponseHandler();

        // 声明呀一个字符串用来存储response
        String result;
        // 创建httppost对象
        HttpPost httpPost = new HttpPost(url);
        // 给httppost对象设置json格式的参数
        StringEntity httpEntity = new StringEntity(json,"utf-8");
        // 设置请求格式
        httpPost.setHeader("Content-type","application/json");
        if (requestHeader != null && requestHeader.size() > 0) {
            for (Map.Entry<String, String> entry : requestHeader.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }
        }
        // 传参
        httpPost.setEntity(httpEntity);
        // 发送请求，并获取返回值
        CloseableHttpResponse response = httpClient.execute(httpPost);
        try {
            // 通过返回对象获取返回数据
            HttpEntity entity = response.getEntity();
            // 通过EntityUtils中的toString方法将结果转换为字符串
            result = EntityUtils.toString(entity);
            return result;

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            // 关闭资源
            if (null != response) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "error";
    }
    public  String doPost(String requestUrl,Map<String,String> requestHeader,Map<String,String> formTexts){
        OutputStream out = null;
        BufferedReader reader = null;
        String result = "";
        try {
            if (requestUrl == null || requestUrl.isEmpty()) {
                return result;
            }
            URL realUrl = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
            connection.setRequestProperty("accept", "text/html, application/xhtml+xml, image/jxr, */*");
            connection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:53.0) Gecko/20100101 Firefox/53.0");
            if (requestHeader != null && requestHeader.size() > 0) {
                for (Map.Entry<String, String> entry : requestHeader.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            String requestEncoding = "UTF-8";
            String responseEncoding = "UTF-8";

            if (requestHeader != null && requestHeader.size() > 0) {
                for (Map.Entry<String, String> entry : requestHeader.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
                connection.setRequestProperty("content-type", "application/x-www-form-urlencoded");
                out = new DataOutputStream(connection.getOutputStream());
                if (formTexts != null && formTexts.size() > 0) {
                    String formData = "";
                    for (Map.Entry<String, String> entry : formTexts.entrySet()) {
                        formData += entry.getKey() + "=" + entry.getValue() + "&";
                    }
                    formData = formData.substring(0, formData.length() - 1);
                    out.write(formData.toString().getBytes(requestEncoding));
                }

            out.flush();
            out.close();
            out = null;
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), responseEncoding));
            String line;
            while ((line = reader.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送POST请求出现异常！");
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 发送post请求
     *
     * @param requestUrl       请求url
     * @param requestHeader    请求头
     * @param formTexts        表单数据
     * @param files            上传文件
     * @param requestEncoding  请求编码
     * @param responseEncoding 响应编码
     * @return 页面响应html
     */
    public  String sendPost(String requestUrl, Map<String, String> requestHeader, Map<String, String> formTexts, Map<String, String> files, String requestEncoding, String responseEncoding) {
        OutputStream out = null;
        BufferedReader reader = null;
        String result = "";
        try {
            if (requestUrl == null || requestUrl.isEmpty()) {
                return result;
            }
            URL realUrl = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
            connection.setRequestProperty("accept", "text/html, application/xhtml+xml, image/jxr, */*");
            connection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:53.0) Gecko/20100101 Firefox/53.0");
            if (requestHeader != null && requestHeader.size() > 0) {
                for (Map.Entry<String, String> entry : requestHeader.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            if (requestEncoding == null || requestEncoding.isEmpty()) {
                requestEncoding = "UTF-8";
            }
            if (responseEncoding == null || responseEncoding.isEmpty()) {
                responseEncoding = "UTF-8";
            }
            if (requestHeader != null && requestHeader.size() > 0) {
                for (Map.Entry<String, String> entry : requestHeader.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            if (files == null || files.size() == 0) {
                connection.setRequestProperty("content-type", "application/x-www-form-urlencoded");
                out = new DataOutputStream(connection.getOutputStream());
                if (formTexts != null && formTexts.size() > 0) {
                    String formData = "";
                    for (Map.Entry<String, String> entry : formTexts.entrySet()) {
                        formData += entry.getKey() + "=" + entry.getValue() + "&";
                    }
                    formData = formData.substring(0, formData.length() - 1);
                    out.write(formData.toString().getBytes(requestEncoding));
                }
            } else {
                String boundary = "-----------------------------" + String.valueOf(new Date().getTime());
                connection.setRequestProperty("content-type", "multipart/form-data; boundary=" + boundary);
                out = new DataOutputStream(connection.getOutputStream());
                if (formTexts != null && formTexts.size() > 0) {
                    StringBuilder sbFormData = new StringBuilder();
                    for (Map.Entry<String, String> entry : formTexts.entrySet()) {
                        sbFormData.append("--" + boundary + "\r\n");
                        sbFormData.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n");
                        sbFormData.append(entry.getValue() + "\r\n");
                    }
                    out.write(sbFormData.toString().getBytes(requestEncoding));
                }
                for (Map.Entry<String, String> entry : files.entrySet()) {
                    String fileName = entry.getKey();
                    String filePath = entry.getValue();
                    if (fileName == null || fileName.isEmpty() || filePath == null || filePath.isEmpty()) {
                        continue;
                    }
                    File file = new File(filePath);
                    if (!file.exists()) {
                        continue;
                    }
                    out.write(("--" + boundary + "\r\n").getBytes(requestEncoding));
                    out.write(("Content-Disposition: form-data; name=\"" + fileName + "\"; filename=\"" + file.getName() + "\"\r\n").getBytes(requestEncoding));
                    out.write(("Content-Type: application/x-msdownload\r\n\r\n").getBytes(requestEncoding));
                    DataInputStream in = new DataInputStream(new FileInputStream(file));
                    int bytes = 0;
                    byte[] bufferOut = new byte[1024];
                    while ((bytes = in.read(bufferOut)) != -1) {
                        out.write(bufferOut, 0, bytes);
                    }
                    in.close();
                    out.write(("\r\n").getBytes(requestEncoding));
                }
                out.write(("--" + boundary + "--").getBytes(requestEncoding));
            }
            out.flush();
            out.close();
            out = null;
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), responseEncoding));
            String line;
            while ((line = reader.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送POST请求出现异常！");
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }


//    public  String uploadFiles(String requestUrl, Map<String, String> requestHeader, Map<String, String> formTexts, Map<String, File> files, String requestEncoding, String responseEncoding) {
//        OutputStream out = null;
//        BufferedReader reader = null;
//        String result = "";
//        try {
//            if (requestUrl == null || requestUrl.isEmpty()) {
//                return result;
//            }
//            URL realUrl = new URL(requestUrl);
//            HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
//            connection.setRequestProperty("Accept", "text/html, application/xhtml+xml, image/jxr, */*");
//            connection.setRequestProperty("Connection","Keep-Alive");
//            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:53.0) Gecko/20100101 Firefox/53.0");
//            if (requestHeader != null && requestHeader.size() > 0) {
//                for (Map.Entry<String, String> entry : requestHeader.entrySet()) {
//                    connection.setRequestProperty(entry.getKey(), entry.getValue());
//                }
//            }
//            connection.setDoOutput(true);
//            connection.setDoInput(true);
//            connection.setUseCaches(false);
//            connection.setRequestMethod("POST");
//            if (requestEncoding == null || requestEncoding.isEmpty()) {
//                requestEncoding = "UTF-8";
//            }
//            if (responseEncoding == null || responseEncoding.isEmpty()) {
//                responseEncoding = "UTF-8";
//            }
//            if (files == null || files.size() == 0) {
//                connection.setRequestProperty("content-type", "application/x-www-form-urlencoded");
//                out = new DataOutputStream(connection.getOutputStream());
//                if (formTexts != null && formTexts.size() > 0) {
//                    String formData = "";
//                    for (Map.Entry<String, String> entry : formTexts.entrySet()) {
//                        formData += entry.getKey() + "=" + entry.getValue() + "&";
//                    }
//                    formData = formData.substring(0, formData.length() - 1);
//                    out.write(formData.toString().getBytes(requestEncoding));
//                }
//            } else {
//                String boundary = "-----------------------------" + String.valueOf(new Date().getTime());
//                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
//                connection.setRequestProperty("Content-Length", String.valueOf(files.size()));
//                System.err.println(connection.getRequestMethod());
//                out = new DataOutputStream(connection.getOutputStream());
//                if (formTexts != null && formTexts.size() > 0) {
//                    StringBuilder sbFormData = new StringBuilder();
//                    for (Map.Entry<String, String> entry : formTexts.entrySet()) {
//                        sbFormData.append("--" + boundary + "\r\n");
//                        sbFormData.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n");
//                        sbFormData.append(entry.getValue() + "\r\n");
//                    }
//                    out.write(sbFormData.toString().getBytes(requestEncoding));
//                }
//                for (Map.Entry<String, File> entry : files.entrySet()) {
//                    String fileName = entry.getKey();
//                    File file = entry.getValue();
//                    if (!file.exists()) {
//                        continue;
//                    }
//                    out.write(("--" + boundary + "\r\n").getBytes(requestEncoding));
//                    out.write(("Content-Disposition: form-data; name=\"" + fileName + "\"; filename=\"" + file.getName() + "\"\r\n").getBytes(requestEncoding));
//                    out.write(("Content-Type: application/x-msdownload\r\n\r\n").getBytes(requestEncoding));
//                    DataInputStream in = new DataInputStream(new FileInputStream(file));
//                    int bytes = 0;
//                    byte[] bufferOut = new byte[1024];
//                    while ((bytes = in.read(bufferOut)) != -1) {
//                        out.write(bufferOut, 0, bytes);
//                    }
//                    in.close();
//                    out.write(("\r\n").getBytes(requestEncoding));
//                }
//                out.write(("--" + boundary + "--").getBytes(requestEncoding));
//            }
//            out.flush();
//            out.close();
//            out = null;
//            reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), responseEncoding));
//            String line;
//            while ((line = reader.readLine()) != null) {
//                result += line;
//            }
//        } catch (Exception e) {
//            System.out.println("发送POST请求出现异常！");
//            log.error("上传文件出错：{}",ExceptionUtil.stacktraceToString(e));
//        } finally {
//            try {
//                if (out != null) {
//                    out.close();
//                }
//                if (reader != null) {
//                    reader.close();
//                }
//            } catch (IOException ex) {
//                log.error("关闭post通道出错：{}",ExceptionUtil.stacktraceToString(ex));
//            }
//        }
//        return result;
//    }



    public String uploadFile(String requestUrl,Map<String, String> requestHeader, Map<String, String> formTexts,Map<String, File> fileMap) {
        String result = "";
        try {
            // 换行符
            final String newLine = "\r\n";
            final String boundaryPrefix = "--";
            // 定义数据分隔线
            String BOUNDARY = String.valueOf(new Date().getTime());
            // 服务器的域名
            URL url = new URL(requestUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // 设置为POST情
            conn.setRequestMethod("POST");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            // 设置请求头参数
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charsert", "UTF-8");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            conn.setRequestProperty("Content-Length","");
            if (requestHeader != null && requestHeader.size() > 0) {
                for (Map.Entry<String, String> entry : requestHeader.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            OutputStream out = new DataOutputStream(conn.getOutputStream());
            // 上传文件
            if (formTexts != null && formTexts.size() > 0) {
                StringBuilder sbFormData = new StringBuilder();
                for (Map.Entry<String, String> entry : formTexts.entrySet()) {
                    sbFormData.append(boundaryPrefix);
                    sbFormData.append(BOUNDARY);
                    sbFormData.append(newLine);
                    sbFormData.append("Content-Disposition: form-data; name=\"" + entry.getKey() +"\"");
                    sbFormData.append(newLine);
                    sbFormData.append(newLine);
                    sbFormData.append(entry.getValue() + newLine);
                }
                out.write(sbFormData.toString().getBytes());
            }
            StringBuilder sb = new StringBuilder();
            File file = null;
            if (fileMap != null && fileMap.size() > 0) {
                for (Map.Entry<String, File> entry : fileMap.entrySet()) {
                    sb.append(boundaryPrefix);
                    sb.append(BOUNDARY);
                    sb.append(newLine);
                    // 文件参数,photo参数名可以随意修改
                    sb.append("Content-Disposition: form-data;name=\""+entry.getKey()+"\";filename=\"" + entry.getValue().getName()
                            + "\"" + newLine);
                    file = entry.getValue();
                }
            }


            sb.append("Content-Type:application/octet-stream");
            // 参数头设置完以后需要两个换行，然后才是参数内容
            sb.append(newLine);
            sb.append(newLine);

            // 将参数头的数据写入到输出流中
            out.write(sb.toString().getBytes());
            // 数据输入流,用于读取文件数据
            DataInputStream in = new DataInputStream(new FileInputStream(file));

            byte[] bufferOut = new byte[1024];
            int bytes = 0;
            // 每次读1KB数据,并且将文件数据写入到输出流中
            while ((bytes = in.read(bufferOut)) != -1) {
                out.write(bufferOut, 0, bytes);
            }
            // 最后添加换行
            out.write(newLine.getBytes());
            in.close();
            // 定义最后数据分隔线，即--加上BOUNDARY再加上--。
            byte[] end_data = (newLine + boundaryPrefix + BOUNDARY + boundaryPrefix + newLine)
                    .getBytes();
            // 写上结尾标识
            out.write(end_data);
            out.flush();
            out.close();

            // 定义BufferedReader输入流来读取URL的响应
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                result += line;
            }
            return result;

        } catch (Exception e) {
           log.error("发送POST请求出现异常！" + ExceptionUtil.stacktraceToString(e));
            return null;

        }
    }

    public  byte[] readOnce(File file) throws IOException {
        //check the file is Exists
        checkFileExists(file);
        if (file.length() > Integer.MAX_VALUE) {
            System.err.println("file is too big ,not to read !");
            throw new IOException(file.getName() + " is too big ,not to read ");
        }
        int _bufferSize = (int) file.length();
        //定义buffer缓冲区大小
        byte[] buffer = new byte[_bufferSize];
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            int len = 0;
            if ((len = in.available()) <= buffer.length) {
                in.read(buffer, 0, len);
            }
        } finally {
            closeInputStream(in);
        }
        return buffer;
    }
    private  void checkFileExists(File file) throws FileNotFoundException {
        if (file == null || !file.exists()) {
            System.err.println("file is not null or exist !");
            throw new FileNotFoundException(file.getName());
        }
    }

    private  void closeInputStream(InputStream in) {
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String uploadFileByByte(String requestUrl,File file,Map<String, String> requestHeader) {
        String result = "";
        try {
            String url = requestUrl;
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            if (requestHeader != null && requestHeader.size() > 0) {
                for (Map.Entry<String, String> entry : requestHeader.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);

            // 发送请求参数
            DataOutputStream dos=new DataOutputStream(conn.getOutputStream());
            byte[] bytes = readOnce(file);
            dos.write(bytes);

            // flush输出流的缓冲
            dos.flush();
            // 定义BufferedReader输入流来读取URL的响应
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
          return result;
        } catch (Exception e) {
            System.out.println("异常," + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    public  String doGetSendNotice(String url) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        String result = "";
        try {
            // 通过址默认配置创建一个httpClient实例
            httpClient = HttpClients.createDefault();
            // 创建httpGet远程连接实例
            HttpGet httpGet = new HttpGet(url);
            // 设置配置请求参数
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(35000)// 连接主机服务超时时间
                    .setConnectionRequestTimeout(35000)// 请求超时时间
                    .setSocketTimeout(60000)// 数据读取超时时间
                    .build();
            // 为httpGet实例设置配置
            httpGet.setConfig(requestConfig);
            // 执行get请求得到返回对象
            response = httpClient.execute(httpGet);
            // 通过返回对象获取返回数据
            HttpEntity entity = response.getEntity();
            // 通过EntityUtils中的toString方法将结果转换为字符串
            result = EntityUtils.toString(entity);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            if (null != response) {
                try {
                    response.close();
                } catch (Exception e) {
                    log.error("关闭资源出错：{}",ExceptionUtil.stacktraceToString(e));
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (Exception e) {
                    log.error("关闭资源出错：{}",ExceptionUtil.stacktraceToString(e));
                }
            }
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        HttpClientUtils hh = new HttpClientUtils();
        Map<String,String> head = new HashMap<>();
        head.put("AppKey","caee83f25bef456b13b4e9f54c8da4c8");
        head.put("Checksum","89877036c035c3bf06c016d8e795e2177afc165e");
        head.put("Curtime","1684048582");
        head.put("Nonce","xxxxx");
        String s = hh.doPostJson("https://vcloud.163.com/app/vod/upload/init", head,"{\"originFileName\":\"logo.png\"}");
        System.err.println(s);
    }
}
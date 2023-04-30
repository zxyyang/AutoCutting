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
    public  String doGet(String url, String param) {
        StringBuilder result = new StringBuilder();
        BufferedReader in = null;
        String urlNameString = url;
        try {
            if (Objects.nonNull(param)) {
                urlNameString = urlNameString + "?" + param;
            }
            URL realUrl = new URL(urlNameString);
            URLConnection connection = realUrl.openConnection();
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.connect();
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (ConnectException e) {
            e.printStackTrace();
        } catch (SocketTimeoutException | ConnectTimeoutException e) {
            e.printStackTrace();
            //SocketTimeoutException：是Java包下抛出的异常，这定义了Socket读数据的超时时间，即从server获取响应数据须要等待的时间；当读取或者接收Socket超时会抛出SocketTimeoutException
            System.out.println("sendGet SocketTimeoutException, url=" + url + ",param=" + param);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("sendGet IOException, url=" + url + ",param=" + param);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("sendGet Exception, url=" + url + ",param=" + param);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("sendGet Exception, url=" + url + ",param=" + param);
            }
        }
        return result.toString();
    }

    public  String doGet(String url) {
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
        return result;
    }
    public static String doPostJson(String url,String json) throws IOException {
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
            } catch (IOException ex) {
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



    public static void main(String[] args) {
     //   uploadFile("1.jpg");
//        Map<String, String> requestHeader = new HashMap<>();
//        requestHeader.put("Cookie","bid=69AR68th; BAIDU_SSP_lcr=https://www.baidu.com/link?url=C0Ln0deXxQUeDvlsiXqETLXukjBwK7TgZinX9-xNt7GxkcvBSHLkMfjxiQ_zvB7Y&wd=&eqid=fb97586f0000f66600000004644387b8; sajssdk_2015_cross_new_user=1; __bid_n=187a7ca3cefce91eb94207; __utma=177678124.1702347834.1682147262.1682147262.1682147262.1; __utmc=177678124; __utmz=177678124.1682147262.1.1.utmcsr=baidu|utmccn=(organic)|utmcmd=organic; Hm_lvt_ecd4feb5c351cc02583045a5813b5142=1682147262; FPTOKEN=iFzOjur5og3zIAEWW1G6LGG5IZ2tiCEYAs8B28mSoXkHJOim7aXNawX76eYPm3yeCIsLgMLQ+vWJ83gkzUHfaxN673L4RvJ89xsNmSwQSCRRn6B5wsLq/5xTUBI7I7T9lsy7p+JZ8Gac6nwGukqoEuZMeJtg/MnhmHByvkpM9M+8HjfT7FMKzDU2DurEVW1FEtEIPB04nFmKtFYHGgcbIhhOeXWUgjdogRqHW5h8Ly5XkVDGTZ0iYgTwKDL6FiGMJ+rbGB/Dl2cHC2/ImstDLJ97C5BHfBLYlJGOEit0EVQ35fdJzheEn/tVmw+2hFDfmkvO5x3766dDcFZPUHNjeW7VeT2XWBrxTsBO8pyZ0ff8ZhiV2iY4t+CrUfwEt3AppkbBRv3NBkdLDakqqiq8oQ==|2ZqtFOnD+a084Dj9j8C9SXC90NYXHaweb82//Rvf++Y=|10|8718a441f5dfa2e108986db8f8445d97; __gads=ID=5c6ce45c3b8f8a5f-2239431e67df003a:T=1682147262:RT=1682147262:S=ALNI_MaQh7zR4yt2nS6aBKXz_wJuzkRBwg; __gpi=UID=00000bfc715d939c:T=1682147262:RT=1682147262:S=ALNI_MbgKl3oMYQp5X7OfvA0lt2x5s5m-g; S=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOjE1ODIwNjc1NSwidWlkIjoxNjYyOTYzMDAsImlhdCI6MTY4MjE0NzMwOC4wLCJvIjowfQ.EMQWRH-YwqboxEin8ot0alTU7Op9EF-X3_ojZYHymn4; user_id=166296300; __utmb=177678124.3.10.1682147262; Hm_lpvt_ecd4feb5c351cc02583045a5813b5142=1682147309; \"identities\":\"eyIkaWRlbnRpdHlfY29va2llX2lkIjoiMTg3YTdjYWZjNDAxMDc5LTA0NzNlNmZmM2EzOTJiOC0yNjAzMWI1MS0yMDczNjAwLTE4N2E3Y2FmYzQxZDcyIiwiJGlkZW50aXR5X2xvZ2luX2lkIjoiMTY2Mjk2MzAwIn0=\",\"history_login_id\":{\"name\":\"$identity_login_id\",\"value\":\"166296300\"}}; id=166296300");
//        Map<String, String> files = new HashMap<>();
//        files.put("pic","img/img.png");
//        String s = sendPost("https://www.xiachufang.com/page/upload_pic/", requestHeader, null, files, null, null);
//        System.err.println(s);
    }
}
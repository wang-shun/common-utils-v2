package com.youzan.sz.common.util;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Created by Kid on 16/6/1.
 */
public final class HttpUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtil.class);
    private static HttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
    private static final String UTF8 = "UTF-8";
    private static final int TIME_OUT = 3000;

    /**
     * HTTP POST
     *
     * @param isHttps
     * @param url
     * @param params
     * @param charset
     * @return
     * @throws IOException
     */
    public static String post(boolean isHttps, Map<String, String> headers, String url, Map<String, ?> params, String charset) throws IOException {
        CloseableHttpClient httpClient = buildHttpClient(isHttps);
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(buildRequestConfig());
        if (params != null && params.size() > 0) {
            StringEntity entity = new StringEntity(JsonUtils.bean2Json(params), charset);
            entity.setContentType(ContentType.APPLICATION_JSON.getMimeType());
            httpPost.setEntity(entity);
        }

        if (headers != null && headers.size() > 0) {
            headers.entrySet().stream().forEach(e -> {
                httpPost.setHeader(e.getKey(), e.getValue());
            });
        }

        CloseableHttpResponse response = httpClient.execute(httpPost);
        return EntityUtils.toString(response.getEntity(), Charset.forName(charset));
    }

    /**
     * 使用UTF8 POST
     *
     * @param isHttps
     * @param url
     * @param params
     * @return
     * @throws IOException
     */
    public static String postUsingUTF8(boolean isHttps, String url, Map<String, ?> params) throws IOException {
        return post(isHttps, null, url, params, UTF8);
    }


    /**
     * 使用UTF8 POST
     *
     * @param isHttps
     * @param url
     * @param params
     * @return
     * @throws IOException
     */
    public static String postUsingUTF8(boolean isHttps, Map<String, String> headers, String url, Map<String, ?> params) throws IOException {
        return post(isHttps, headers, url, params, UTF8);
    }

    /**
     * HTTP POST
     *
     * @param isHttps
     * @param url
     * @param params
     * @param charset
     * @return
     * @throws IOException
     */

    public static String post(boolean isHttps, Map<String, String> headers, String url, String params, String charset) throws IOException {
        CloseableHttpClient httpClient = buildHttpClient(isHttps);
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(buildRequestConfig());
        if (params != null && !params.equals("")) {
            httpPost.setEntity(new StringEntity(params, charset));
        }

        if (headers != null && headers.size() > 0) {
            headers.entrySet().stream().forEach(e -> {
                httpPost.setHeader(e.getKey(), e.getValue());
            });
        }

        CloseableHttpResponse response = httpClient.execute(httpPost);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity, Charset.forName(charset));
    }

    /**
     * 上传文件 utf-8
     *
     * @param isHttps
     * @param url
     * @param filePath
     * @return
     * @throws IOException
     */
    public static String uploadFile(boolean isHttps, String url, String filePath) throws IOException {
        CloseableHttpClient httpClient = buildHttpClient(isHttps);
        HttpPost httpPost = new HttpPost(url);
        FileBody bin = new FileBody(new File(filePath));
        StringBody comment = new StringBody("a binary file", ContentType.APPLICATION_OCTET_STREAM);
        HttpEntity reqEntity = MultipartEntityBuilder.create()
                .addPart("bin", bin)
                .addPart("comment", comment)
                .build();


        httpPost.setEntity(reqEntity);
        CloseableHttpResponse response = httpClient.execute(httpPost);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(response.getStatusLine().toString());
        }
        HttpEntity resEntity = response.getEntity();
        String result = EntityUtils.toString(resEntity, Consts.UTF_8);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("uploadFile get result=>" + result);
        }
        return result;
    }

    /**
     * 使用UTF8 POST
     *
     * @param isHttps
     * @param url
     * @param params
     * @return
     * @throws IOException
     */
    public static String postUsingUTF8(boolean isHttps, String url, String params) throws IOException {
        return post(isHttps, null, url, params, UTF8);
    }

    /**
     * 使用UTF8 POST
     *
     * @param isHttps
     * @param url
     * @param params
     * @return
     * @throws IOException
     */
    public static String postUsingUTF8(boolean isHttps, Map<String, String> headers, String url, String params) throws IOException {
        return post(isHttps, headers, url, params, UTF8);
    }

    /**
     * HTTP GET
     *
     * @param isHttps
     * @param url
     * @param charset
     * @return
     * @throws IOException
     */

    public static String get(boolean isHttps, Map<String, String> headers, String url, String charset) throws IOException {
        CloseableHttpClient httpClient = buildHttpClient(isHttps);
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(buildRequestConfig());

        if (headers != null && headers.size() > 0) {
            headers.entrySet().stream().forEach(e -> {
                httpGet.setHeader(e.getKey(), e.getValue());
            });
        }

        CloseableHttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity, Charset.forName(charset));
    }


    /**
     * 使用UTF8 GET
     *
     * @param isHttps
     * @param url
     * @return
     * @throws IOException
     */
    public static String getUsingUTF8(boolean isHttps, String url) throws IOException {
        return get(isHttps, null, url, UTF8);
    }

    /**
     * 使用UTF8 GET
     *
     * @param isHttps
     * @param url
     * @return
     * @throws IOException
     */
    public static String getUsingUTF8(boolean isHttps, Map<String, String> headers, String url) throws IOException {
        return get(isHttps, headers, url, UTF8);
    }


    private static RequestConfig buildRequestConfig() {
        return RequestConfig.custom().setConnectionRequestTimeout(TIME_OUT).setConnectTimeout(TIME_OUT).setSocketTimeout(TIME_OUT).build();
    }

    /**
     * 构建 HttpClient
     *
     * @param isHttps 是否是HTTPS
     * @return
     */

    private static CloseableHttpClient buildHttpClient(boolean isHttps) {
        if (isHttps) {
            return createSSLClient();
        } else {
            return createHttpClient();
        }
    }

    private static CloseableHttpClient createHttpClient() {
        return HttpClients.custom().setConnectionManager(connManager).setConnectionManagerShared(true).build();
    }

    /**
     * 创建https client
     *
     * @return
     */

    private static CloseableHttpClient createSSLClient() {
        try {
            SSLContext e = (new SSLContextBuilder()).loadTrustMaterial(null, (chain, authType) -> true).build();
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("HTTPS SSL 证书未去认证,直接返回的通过认证");
            }
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(e);
            return HttpClients.custom().setSSLSocketFactory(sslsf).setConnectionManager(connManager).setConnectionManagerShared(true).build();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            LOGGER.error("Exception", e);
            return createHttpClient();
        }
    }

}

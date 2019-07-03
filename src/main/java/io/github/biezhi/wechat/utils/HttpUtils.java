package io.github.biezhi.wechat.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * https 请求 微信为https的请求
 * 
 * @author jiangmy
 * @date 2016-08-01 下午2:40:19
 */
@Slf4j
public class HttpUtils {

    private static final int CONNECT_TIMEOUT = 30000;
    private static final int READ_TIMEOUT = 60000;

    private static final String DEFAULT_CHARSET = "UTF-8"; // 默认字符集

    private static final String _GET = "GET"; // GET
    private static final String _POST = "POST";// POST

    private static final String _CONTENT_TYPE_JSON = "application/json";
    private static final String _CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";

    // add by maojian 信任全部
    private static TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        }
    } };

    // add by maojian 忽略hostname验证
    private static HostnameVerifier hostnameVerifier = new HostnameVerifier() {
        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    };

    public static void setSNI(String value) {
        System.setProperty("jsse.enableSNIExtension", value);
    }

    private static String getSNI() {
        return System.getProperty("jsse.enableSNIExtension");
    }

    /**
     * 获得KeyStore.
     *
     * @param keyStorePath
     *        密钥库路径
     * @param password
     *        密码
     * @return 密钥库
     * @throws Exception
     */
    public static KeyStore getKeyStore(String password, String keyStorePath) throws Exception {
        // 实例化密钥库
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        // 获得密钥库文件流
        try (FileInputStream is = new FileInputStream(keyStorePath)) {
            // 加载密钥库
            ks.load(is, password.toCharArray());
        }
        return ks;
    }

    /**
     * 获得SSLSocketFactory.
     *
     * @param password
     *        密码
     * @param keyStorePath
     *        密钥库路径
     * @param trustStorePath
     *        信任库路径
     * @return SSLSocketFactory
     * @throws Exception
     */
    public static SSLContext getSSLContext(String password, String keyStorePath, String trustStorePath) throws Exception {
        // 实例化SSL上下文
        SSLContext ctx = SSLContext.getInstance("TLS");
        // 实例化密钥库
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        // 获得密钥库
        KeyStore keyStore = getKeyStore(password, keyStorePath);
        // 初始化密钥工厂
        keyManagerFactory.init(keyStore, password.toCharArray());
        if (trustStorePath == null) {
            TrustManager[] tm = { new MyX509TrustManager() };
            // 初始化SSL上下文
            ctx.init(keyManagerFactory.getKeyManagers(), tm, new SecureRandom());
            return ctx;
        }
        // 获得信任库
        KeyStore trustStore = getKeyStore(password, trustStorePath);
        // 实例化信任库
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        // 初始化信任库
        trustManagerFactory.init(trustStore);
        // 初始化SSL上下文
        ctx.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
        // 获得SSLSocketFactory
        return ctx;
    }

    /**
     * 导证书,此方法不能被调用，会影响其它渠道走https，例如存管。
     * 
     * @See initHttpsURLConnection(String password, String keyStorePath, String trustStorePath, HttpsURLConnection connection)
     * @param password
     *        密码
     * @param keyStorePath
     *        密钥库路径
     * @param trustStorePath
     *        信任库路径
     * @throws Exception
     */
    @Deprecated
    public static void initHttpsURLConnection(String password, String keyStorePath, String trustStorePath) throws Exception {
        // 声明SSL上下文
        SSLContext sslContext = null;
        // 实例化主机名验证接口
        HostnameVerifier hnv = new MyHostnameVerifier();
        try {
            sslContext = getSSLContext(password, keyStorePath, trustStorePath);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        if (sslContext != null) {
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        }
        HttpsURLConnection.setDefaultHostnameVerifier(hnv);
    }

    /**
     * 导证书
     * 
     * @param password
     *        密码
     * @param keyStorePath
     *        密钥库路径
     * @param trustStorePath
     *        信任库路径
     * @throws Exception
     */
    public static void initHttpsURLConnection(String password, String keyStorePath, String trustStorePath, HttpsURLConnection connection) throws Exception {
        // 声明SSL上下文
        SSLContext sslContext = null;
        // 实例化主机名验证接口
        HostnameVerifier hnv = new MyHostnameVerifier();
        try {
            sslContext = getSSLContext(password, keyStorePath, trustStorePath);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        if (sslContext != null) {
            connection.setSSLSocketFactory(sslContext.getSocketFactory());
        }
        connection.setHostnameVerifier(hnv);
    }

    public static String joinParams(Map<String, String> params, String charset) {
        if (params == null || params.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Entry<String, String> entry : params.entrySet()) {
            try {
                if (entry.getValue() == null) {
                    continue;
                }
                if (sb.length() > 0) {
                    sb.append("&");
                }
                sb.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), charset));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String p = sb.toString();
        return p;
    }

    /**
     * @author jiangmy
     * @date 2017-03-11 16:15:55
     * @since v1.0.0
     * @param params
     * @return
     */
    public static String joinParams(String url, Map<String, String> params, String charset) {
        String p = joinParams(params, charset);
        if (StringUtils.isNotBlank(p)) {
            if (url.contains("?")) {
                url += "&" + p;
            } else {
                url += "?" + p;
            }
        }
        return url;
    }

    /**
     * 检测是否https
     * 
     * @param url
     */
    private static boolean isHttps(String url) {
        return url.toLowerCase().startsWith("https");
    }

    /**
     * 初始化http请求参数
     * 
     * @param url
     * @param method
     * @param readTimeout
     * @param connectTimeout
     * @param charset
     * @param password
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws KeyManagementException
     */
    private static HttpURLConnection initConnection(String url, String method, Map<String, String> headers, String userName, String password, String contentType, String charset,
            int connectTimeout, int readTimeout) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, KeyManagementException {
        URL _url = new URL(url);
        HttpURLConnection http;
        if (isHttps(url)) {
            http = (HttpsURLConnection) _url.openConnection();
        } else {
            http = (HttpURLConnection) _url.openConnection();
        }
        http.setConnectTimeout(connectTimeout); // 连接超时
        http.setReadTimeout(connectTimeout); // 读取超时 --服务器响应比较慢，增大时间
        http.setRequestMethod(method);
        http.setUseCaches(false);
        http.setDoOutput(true);
        http.setDoInput(true);

        http.setRequestProperty("accept", "*/*");
        http.setRequestProperty("connection", "Keep-Alive");
        http.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        http.setRequestProperty("Accept-Charset", charset);
        http.setRequestProperty("Content-Type", contentType);
        http.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.30 Safari/537.36");
        if (StringUtils.isNotBlank(userName)) {
            String authentication = userName + ':' + password;
            String encoded = Base64.encodeBase64String(authentication.getBytes(charset));
            http.setRequestProperty("Authorization", "Basic " + encoded);
        }

        if (null != headers && !headers.isEmpty()) {
            for (Entry<String, String> entry : headers.entrySet()) {
                http.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        return http;
    }

    /**
     * 初始化http请求参数
     * 
     * @param url
     * @param method
     * @param readTimeout
     * @param connectTimeout
     * @param charset
     * @param password
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws KeyManagementException
     */
    private static HttpURLConnection initConnection(String url, String method, Map<String, String> headers, String userName, String password, String contentType, String charset,
            int connectTimeout, int readTimeout, boolean isIgnore) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, KeyManagementException {
        URL _url = new URL(url);
        HttpURLConnection http;
        if (isHttps(url)) {
            HttpsURLConnection https = (HttpsURLConnection) _url.openConnection();
            if (isIgnore) {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, trustAllCerts, new SecureRandom());
                https.setSSLSocketFactory(sc.getSocketFactory());
                https.setHostnameVerifier(hostnameVerifier);
            }
            http = https;
        } else {
            http = (HttpURLConnection) _url.openConnection();
        }
        http.setConnectTimeout(connectTimeout); // 连接超时
        http.setReadTimeout(connectTimeout); // 读取超时 --服务器响应比较慢，增大时间
        http.setRequestMethod(method);
        http.setUseCaches(false);
        http.setDoOutput(true);
        http.setDoInput(true);

        http.setRequestProperty("accept", "*/*");
        http.setRequestProperty("connection", "Keep-Alive");
        http.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        http.setRequestProperty("Accept-Charset", charset);
        http.setRequestProperty("Content-Type", contentType);
        http.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.30 Safari/537.36");
        if (StringUtils.isNotBlank(userName)) {
            String authentication = userName + ':' + password;
            String encoded = Base64.encodeBase64String(authentication.getBytes(charset));
            http.setRequestProperty("Authorization", "Basic " + encoded);
        }

        if (null != headers && !headers.isEmpty()) {
            for (Entry<String, String> entry : headers.entrySet()) {
                http.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        return http;
    }

    public static String getForm(String url, Map<String, String> params, Map<String, String> headers, String contentType, String charset, String userName, String password,
            int connectTimeout, int readTimeout) {
        HttpURLConnection urlCon;
        try {
            charset = StringUtils.isBlank(charset) ? DEFAULT_CHARSET : charset;
            contentType = StringUtils.isBlank(contentType) ? _CONTENT_TYPE_FORM + ";charset=" + charset : contentType;
            //
            url = joinParams(url, params, charset);
            //
            urlCon = initConnection(url, _GET, headers, userName, password, contentType, charset, connectTimeout, readTimeout);
            urlCon.connect();
            //
            if (urlCon.getResponseCode() == 200) {
                log.info("url:{} rsp:{}", url, urlCon.getResponseCode());
                try (InputStream inputStream = urlCon.getInputStream()) {
                    byte[] data = IOUtils.toByteArray(inputStream);
                    return new String(data, charset);
                }
            } else {
                log.error("url:{} rsp:{}", url, urlCon.getResponseCode());
            }
        } catch (Exception e) {
            log.error("url:" + url, e);
        }
        return null;
    }

    public static String postForm(String url, Map<String, String> params, Map<String, String> headers, String contentType, String charset, String userName, String password,
            int connectTimeout, int readTimeout) {
        try {
            return postFormDirect(url, params, headers, contentType, charset, userName, password, connectTimeout, readTimeout);
        } catch (IOException e) {
            log.info("url:{} params:{} exc:{}", url, params, e.getMessage());
        }
        return null;
    }

    public static String postForm(String url, Map<String, String> params, Map<String, String> headers, String contentType, String charset, String userName, String password,
            int connectTimeout, int readTimeout, boolean isIgnore) {
        try {
            return postFormDirect(url, params, headers, contentType, charset, userName, password, connectTimeout, readTimeout, isIgnore);
        } catch (IOException e) {
            log.info("url:{} params:{} exc:{}", url, params, e.getMessage());
        }
        return null;
    }

    public static String postBody(String url, String json, Map<String, String> headers, String contentType, String charset, String userName, String password, int connectTimeout,
            int readTimeout) {
        try {
            return postBodyDirect(url, json, headers, contentType, charset, userName, password, connectTimeout, readTimeout);
        } catch (IOException e) {
            log.info("url:{} json:{} exc:{}", url, json, e.getMessage());
        }
        return null;
    }

    public static String postFormDirect(String url, Map<String, String> params, Map<String, String> headers, String contentType, String charset, String userName, String password,
            int connectTimeout, int readTimeout) throws IOException {
        charset = charset == null ? Charset.defaultCharset().name() : charset;
        return postFormDirect(url, joinParams(params, charset), headers, contentType, charset, userName, password, connectTimeout, readTimeout);
    }

    public static String postFormDirect(String url, Map<String, String> params, Map<String, String> headers, String contentType, String charset, String userName, String password,
            int connectTimeout, int readTimeout, boolean isIgnore) throws IOException {
        charset = charset == null ? Charset.defaultCharset().name() : charset;
        return postFormDirect(url, joinParams(params, charset), headers, contentType, charset, userName, password, connectTimeout, readTimeout, isIgnore);
    }

    public static String postFormDirect(String url, String param, Map<String, String> headers, String contentType, String charset, String userName, String password,
            int connectTimeout, int readTimeout) throws IOException {
        HttpURLConnection urlCon = null;
        charset = charset == null ? Charset.defaultCharset().name() : charset;
        // String param = joinParams(params, charset);
        try {
            contentType = contentType == null ? _CONTENT_TYPE_FORM + ";charset=" + charset : contentType;
            //
            urlCon = initConnection(url, _POST, headers, userName, password, contentType, charset, connectTimeout, readTimeout);
            urlCon.setRequestProperty("Content-Type", contentType);
            urlCon.connect();
            //
            if (StringUtils.isNotBlank(param)) {
                try (OutputStream outputStream = urlCon.getOutputStream()) {
                    outputStream.write(param.getBytes(charset));// 输入参数
                    outputStream.flush();
                }
            }
            //
            if (urlCon.getResponseCode() == 200) {
                log.info("url:{}; param:{}; rsp:{}", url, param, urlCon.getResponseCode());
                try (InputStream inputStream = urlCon.getInputStream()) {
                    byte[] data = IOUtils.toByteArray(inputStream);
                    return new String(data, charset);
                }
            } else {
                log.error("url:{}; param:{}; rsp:{}", url, param, urlCon.getResponseCode());
            }
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | KeyManagementException | NoSuchProviderException e) {
            log.error("url:" + url + " param:" + param, e);
        } finally {
            if (urlCon != null) {
                urlCon.disconnect();
            }
        }
        return null;
    }

    public static String postFormDirect(String url, String param, Map<String, String> headers, String contentType, String charset, String userName, String password,
            int connectTimeout, int readTimeout, boolean isIgnore) throws IOException {
        HttpURLConnection urlCon = null;
        charset = charset == null ? Charset.defaultCharset().name() : charset;
        // String param = joinParams(params, charset);
        try {
            contentType = contentType == null ? _CONTENT_TYPE_FORM + ";charset=" + charset : contentType;
            //
            urlCon = initConnection(url, _POST, headers, userName, password, contentType, charset, connectTimeout, readTimeout, isIgnore);
            urlCon.setRequestProperty("Content-Type", contentType);
            urlCon.connect();
            //
            if (StringUtils.isNotBlank(param)) {
                try (OutputStream outputStream = urlCon.getOutputStream()) {
                    outputStream.write(param.getBytes(charset));// 输入参数
                    outputStream.flush();
                }
            }
            //
            if (urlCon.getResponseCode() == 200) {
                log.info("url:{}; param:{}; rsp:{}", url, param, urlCon.getResponseCode());
                try (InputStream inputStream = urlCon.getInputStream()) {
                    byte[] data = IOUtils.toByteArray(inputStream);
                    return new String(data, charset);
                }
            } else {
                log.error("url:{}; param:{}; rsp:{}", url, param, urlCon.getResponseCode());
            }
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | KeyManagementException | NoSuchProviderException e) {
            log.error("url:" + url + " param:" + param, e);
        } finally {
            if (urlCon != null) {
                urlCon.disconnect();
            }
        }
        return null;
    }

    public static String postBodyDirect(String url, String json, Map<String, String> headers, String contentType, String charset, String userName, String password,
            int connectTimeout, int readTimeout) throws IOException {
        HttpURLConnection urlCon = null;
        try {
            charset = charset == null ? Charset.defaultCharset().name() : charset;
            contentType = contentType == null ? _CONTENT_TYPE_JSON + ";charset=" + charset : contentType;
            //
            String sni = getSNI();
            if (null != sni && sni.equals("false")) {
                urlCon = initConnection(url, _POST, headers, userName, password, contentType, charset, connectTimeout, readTimeout, true);
            } else {
                urlCon = initConnection(url, _POST, headers, userName, password, contentType, charset, connectTimeout, readTimeout);
            }
            urlCon.setRequestProperty("Content-Type", contentType);
            urlCon.connect();
            //
            if (StringUtils.isNotBlank(json)) {
                try (OutputStream outputStream = urlCon.getOutputStream()) {
                    outputStream.write(json.getBytes(charset));// 输入参数
                    outputStream.flush();
                }
            }
            if (urlCon.getResponseCode() == 200) {
                log.info("url:{}; json:{}; rsp:{}", url, json, urlCon.getResponseCode());
                try (InputStream inputStream = urlCon.getInputStream()) {
                    byte[] data = IOUtils.toByteArray(inputStream);
                    return new String(data, charset);
                }
            } else {
                log.error("url:{}; json:{}; rsp:{}", url, json, urlCon.getResponseCode());
            }
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | KeyManagementException | NoSuchProviderException e) {
            log.error("url:" + url + " json:" + json, e);
        } finally {
            if (urlCon != null) {
                urlCon.disconnect();
            }
        }
        return null;
    }

    /**
     * @description
     *              功能描述: get 请求
     * @return 返回类型:
     */
    public static String get(String url) {
        return getForm(url, null, null, null, null, null, null, CONNECT_TIMEOUT, READ_TIMEOUT);
    }

    /**
     * @description
     *              功能描述: get 请求
     * @return 返回类型:
     * @throws UnsupportedEncodingException
     */
    public static String get(String url, Map<String, String> params) {
        return getForm(url, params, null, null, null, null, null, CONNECT_TIMEOUT, READ_TIMEOUT);
    }

    /**
     * @description
     *              功能描述: POST 请求
     * @return 返回类型:
     */
    public static String post(String url, String json) {
        return postBody(url, json, null, null, null, null, null, CONNECT_TIMEOUT, READ_TIMEOUT);
    }

    /**
     * post map 请求
     * 
     * @param url
     * @param params
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String post(String url, Map<String, String> params) {
        return postForm(url, params, null, null, null, null, null, CONNECT_TIMEOUT, READ_TIMEOUT);
    }

    /**
     * post map 请求
     * 
     * @param url
     * @param params
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String post(String url, Map<String, String> params, boolean isIgnore) {
        return postForm(url, params, null, null, null, null, null, CONNECT_TIMEOUT, READ_TIMEOUT, isIgnore);
    }

    /**
     * post map 请求,headers请求头
     * 
     * @param url
     * @param params
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String post(String url, Map<String, String> params, Map<String, String> headers) {
        return postForm(url, params, headers, null, null, null, null, CONNECT_TIMEOUT, READ_TIMEOUT);
    }

    /**
     * map构造url
     * 
     * @description
     *              功能描述:
     * @return 返回类型:
     * @throws UnsupportedEncodingException
     */
    public static String map2Url(Map<String, String> paramToMap) {
        if (null == paramToMap || paramToMap.isEmpty()) {
            return null;
        }
        StringBuffer url = new StringBuffer();
        boolean isfist = true;
        for (Entry<String, String> entry : paramToMap.entrySet()) {
            if (isfist) {
                isfist = false;
            } else {
                url.append('&');
            }
            url.append(entry.getKey()).append('=');
            String value = entry.getValue();
            if (StringUtils.isNotBlank(value)) {
                try {
                    url.append(URLEncoder.encode(value, DEFAULT_CHARSET));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        return url.toString();
    }

    private static final String URL_REG = "([a-zA-z]+)://([^\\:\\?\\/]+)(\\:[0-9]+){0,1}(/[^\\?]*){0,}(\\?){0,}(.*)";

    private static Pattern pattern = Pattern.compile(URL_REG);

    public static String getProtocol(String url) {
        try {
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                String protocol = matcher.group(1);
                return protocol;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getDomain(String url) {
        try {
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                String domain = matcher.group(2);
                return domain;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getPort(String url) {
        try {
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                String port = matcher.group(3);
                if (port == null) {
                    return 80;
                }
                return Integer.valueOf(port.substring(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 80;
    }

    public static String getContext(String url) {
        try {
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                String context = matcher.group(4);
                return context;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 测试方法.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
    }
}

/**
 * 实现用于主机名验证的基接口。
 * 在握手期间，如果 URL 的主机名和服务器的标识主机名不匹配，则验证机制可以回调此接口的实现程序来确定是否应该允许此连接。
 */
class MyHostnameVerifier implements HostnameVerifier {

    @Override
    public boolean verify(String hostname, SSLSession session) {
        if ("localhost".equals(hostname)) {
            return true;
        } else {
            return false;
        }
    }
}

// 证书管理
class MyX509TrustManager implements X509TrustManager {

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    }
}

/**
 * https 域名校验
 *
 * @return
 */
class TrustAnyHostnameVerifier implements HostnameVerifier {

    @Override
    public boolean verify(String hostname, SSLSession session) {
        return true;// 直接返回true
    }
}

package com.youku.java.navi.engine.datasource.pool;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.HttpVersion;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.*;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.VersionInfo;
import org.springframework.beans.factory.InitializingBean;

/**
 * 简单http client配置，每个Route保持一个链接
 *
 */
@Slf4j
public class NaviHttpBasicConfig extends NaviPoolConfig implements
    InitializingBean {
    private HttpParams params;
    private static String charset = null;
    private static String proxy = null;// 代理10.10.0.1:9999
    private static String userAgent = null;

    public HttpParams getParams() {
        return params;
    }

    public void setParams(HttpParams params) {
        this.params = params;
    }

    public static String getCharset() {
        return charset;
    }

    public static void setCharset(String charset) {
        NaviHttpBasicConfig.charset = charset;
    }

    public static String getProxy() {
        return proxy;
    }

    public static void setProxy(String proxy) {
        NaviHttpBasicConfig.proxy = proxy;
    }

    public static String getUserAgent() {
        return userAgent;
    }

    public static void setUserAgent(String userAgent) {
        NaviHttpBasicConfig.userAgent = userAgent;
    }

    /**
     * Saves the default set of HttpParams in the provided parameter. These are:
     * <ul>
     * <li>{@link CoreProtocolPNames#PROTOCOL_VERSION}: 1.1</li>
     * <li>{@link CoreProtocolPNames#HTTP_CONTENT_CHARSET}: ISO-8859-1</li>
     * <li>{@link CoreConnectionPNames#TCP_NODELAY}: true</li>
     * <li>{@link CoreConnectionPNames#SOCKET_BUFFER_SIZE}: 8192</li>
     * <li>{@link CoreProtocolPNames#USER_AGENT}: Apache-HttpClient/<release>
     * (java 1.5)</li>
     * </ul>
     */
    public void setDefaultHttpParams(HttpParams params) {
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        if (charset == null)
            HttpProtocolParams.setContentCharset(params,
                HTTP.DEF_CONTENT_CHARSET.name());
        else
            HttpProtocolParams.setContentCharset(params, charset);
        HttpConnectionParams.setTcpNoDelay(params, true);
        HttpConnectionParams.setSocketBufferSize(params, 8192);

        // determine the release version from packaged version info
        final VersionInfo vi = VersionInfo.loadVersionInfo(
            "org.apache.http.client",
            DefaultHttpClient.class.getClassLoader());
        final String release = (vi != null) ? vi.getRelease()
            : VersionInfo.UNAVAILABLE;
        if (getUserAgent() == null)
            HttpProtocolParams.setUserAgent(params, "Navi-HttpClient/"
                + release + " (java 1.5, navi 2.x)");
        else
            HttpProtocolParams.setUserAgent(params, getUserAgent());
        HttpConnectionParams.setConnectionTimeout(params, getConnectTimeout());
        HttpConnectionParams.setSoTimeout(params, getSocketTimeout());
    }

    public void afterPropertiesSet() throws Exception {
        params = new SyncBasicHttpParams();
        setDefaultHttpParams(params);
        if (proxy != null) {
            try {
                String[] connectionString = proxy.split(":");
                HttpHost proxy = new HttpHost(connectionString[0], Integer.valueOf(connectionString[1]));
                // Integer.getInteger(connectionString[1]));
                params.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
            } catch (Exception e) {
                log.info("{}", e.getMessage());
            }
        }
    }
}

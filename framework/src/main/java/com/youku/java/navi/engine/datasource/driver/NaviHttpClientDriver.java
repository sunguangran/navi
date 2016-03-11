package com.youku.java.navi.engine.datasource.driver;

import com.youku.java.navi.common.ServerUrlUtil;
import com.youku.java.navi.common.exception.NaviSystemException;
import com.youku.java.navi.engine.datasource.pool.NaviHttpPoolConfig;
import com.youku.java.navi.engine.datasource.pool.NaviPoolConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.params.*;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.VersionInfo;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Http Client驱动，连接池可配置两种模式，NaviHttpPoolConfig和NaviHttpBasicConfig
 */
@Slf4j
public class NaviHttpClientDriver extends AbstractNaviDriver {

    private DefaultHttpClient httpClient;
    private BasicResponseHandler responseHandler;
    private ClientConnectionManager cm;
    private HttpParams params;

    public NaviHttpClientDriver(ServerUrlUtil.ServerUrl server, String auth,
                                NaviPoolConfig poolConfig) {
        super(server, auth, poolConfig);

        if (poolConfig instanceof NaviHttpPoolConfig) {
            params = getNaviHttpParams((NaviHttpPoolConfig) poolConfig);
            cm = getPoolingClientConnectionManager((NaviHttpPoolConfig) poolConfig);
            // cm = ((NaviHttpPoolConfig) poolConfig).getConnectionManager();
            //params = ((NaviHttpPoolConfig) poolConfig).getParams();
        } else {
            cm = new PoolingClientConnectionManager(
                SchemeRegistryFactory.createDefault(), 3000,
                TimeUnit.MILLISECONDS);
        }

        createHttpClient(poolConfig, server);
        responseHandler = new BasicResponseHandler();
    }

    private HttpParams getNaviHttpParams(NaviHttpPoolConfig httpPoolConfig) {
        params = new SyncBasicHttpParams();

        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        if (httpPoolConfig.getCharset() == null)
            HttpProtocolParams.setContentCharset(params,
                HTTP.DEF_CONTENT_CHARSET.name());
        else
            HttpProtocolParams.setContentCharset(params, httpPoolConfig.getCharset());
        HttpConnectionParams.setTcpNoDelay(params, true);
        HttpConnectionParams.setSocketBufferSize(params, 8192);

        // determine the release version from packaged version info
        final VersionInfo vi = VersionInfo.loadVersionInfo(
            "org.apache.http.client",
            DefaultHttpClient.class.getClassLoader());
        final String release = (vi != null) ? vi.getRelease()
            : VersionInfo.UNAVAILABLE;
        if (httpPoolConfig.getUserAgent() == null)
            HttpProtocolParams.setUserAgent(params, "Navi-HttpClient/"
                + release + " (java 1.5, navi 2.x)");
        else
            HttpProtocolParams.setUserAgent(params, httpPoolConfig.getUserAgent());
        HttpConnectionParams.setConnectionTimeout(params, httpPoolConfig.getConnectTimeout());
        HttpConnectionParams.setSoTimeout(params, httpPoolConfig.getSocketTimeout());


        if (httpPoolConfig.getProxy() != null) {
            try {
                String[] connectionString = httpPoolConfig.getProxy().split(":");
                HttpHost proxy = new HttpHost(connectionString[0], Integer.valueOf(connectionString[1]));
                // Integer.getInteger(connectionString[1]));
                params.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
            } catch (Exception e) {
                log.info(e.getMessage());
            }
        }
        return params;
    }

    private void createHttpClient(NaviPoolConfig poolConfig, ServerUrlUtil.ServerUrl server) {
        if (params == null) {
            // httpClient.setParams(params)HttpParams
            httpClient = new DefaultHttpClient(cm);
            httpClient.getParams().setParameter(
                CoreConnectionPNames.SO_TIMEOUT,
                poolConfig.getSocketTimeout());
            httpClient.getParams().setParameter(
                CoreConnectionPNames.CONNECTION_TIMEOUT,
                poolConfig.getConnectTimeout());
        } else {
            httpClient = new DefaultHttpClient(cm, params);
            if (poolConfig instanceof NaviHttpPoolConfig) {
                httpClient
                    .setHttpRequestRetryHandler(new NaviHttpRequestRetryHandler(
                        ((NaviHttpPoolConfig) poolConfig)
                            .getRetryTimes(), false));
            }
        }
        // 配置数据源
        httpClient.getParams().setParameter(ClientPNames.DEFAULT_HOST,
            new HttpHost(server.getHost(), server.getPort()));
    }

    public PoolingClientConnectionManager getPoolingClientConnectionManager(
        NaviHttpPoolConfig poolConfig) {
        PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager(
            SchemeRegistryFactory.createDefault(),
            poolConfig.getTimeToLive(), TimeUnit.MILLISECONDS);
        // timeToLive maximum time to live. May be zero if the connection does
        // not have an expiry deadline.
        connectionManager.setDefaultMaxPerRoute(poolConfig.getMaxPerRoute()); // 默认每个通道最高2个链接
        connectionManager.setMaxTotal(poolConfig.getMaxActive()); // 默认最大20，现改成MaxActive可配，默认是8
        return connectionManager;
    }

    public void destroy() throws NaviSystemException {
        close();
    }

    @Override
    public void close() throws NaviSystemException {
        httpClient.getConnectionManager().shutdown();
        setClose(true);
        log.info("the http client is destoried!");
    }

    public boolean isAlive() throws NaviSystemException {
        return isClose();
    }

    public String execute(HttpUriRequest request)
        throws ClientProtocolException, IOException {
        return httpClient.execute(request, responseHandler);
    }

    public boolean open() {
        // TODO Auto-generated method stub
        return false;
    }

}

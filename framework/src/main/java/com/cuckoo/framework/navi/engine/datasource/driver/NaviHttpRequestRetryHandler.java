package com.cuckoo.framework.navi.engine.datasource.driver;

import org.apache.http.HttpRequest;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.UnknownHostException;

public class NaviHttpRequestRetryHandler extends DefaultHttpRequestRetryHandler {

    public NaviHttpRequestRetryHandler(int retryCount,
                                       boolean requestSentRetryEnabled) {
        super(retryCount, requestSentRetryEnabled);
    }

    @Override
    public boolean retryRequest(IOException exception, int executionCount,
                                HttpContext context) {
        if (exception == null) {
            throw new IllegalArgumentException(
                "Exception parameter may not be null");
        }
        if (context == null) {
            throw new IllegalArgumentException("HTTP context may not be null");
        }
        if (executionCount > getRetryCount()) {
            // Do not retry if over max retry count
            return false;
        }
        if (exception instanceof ConnectTimeoutException) {
            //连接超时，重试
            return true;
        }

        if (exception instanceof InterruptedIOException) {
            //连接池等待超时
            if (exception instanceof ConnectionPoolTimeoutException) {
                return true;
            }
            // Timeout
            return false;
        }
        if (exception instanceof UnknownHostException) {
            // Unknown host
            return false;
        }
        if (exception instanceof ConnectException) {
            // Connection refused
            return false;
        }
        if (exception instanceof SSLException) {
            // SSL handshake exception
            return false;
        }

        HttpRequest request = (HttpRequest) context
            .getAttribute(ExecutionContext.HTTP_REQUEST);

        if (requestIsAborted(request)) {
            return false;
        }

        //NoHttpResponseException
        if (handleAsIdempotent(request)) {
            // Retry if the request is considered idempotent
            return true;
        }

        Boolean b = (Boolean) context
            .getAttribute(ExecutionContext.HTTP_REQ_SENT);
        boolean sent = (b != null && b.booleanValue());

        if (!sent || isRequestSentRetryEnabled()) {
            // Retry if the request has not been sent fully or
            // if it's OK to retry methods that have been sent
            return true;
        }
        // otherwise do not retry
        return false;
    }
}

package com.cuckoo.framework.navi.common;

import com.cuckoo.framework.navi.common.exception.NaviSystemException;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ServerAddress {

    private static final String hostRegex = "^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$";
    private String host;
    private int port;

    public ServerAddress(String host, int port) throws NaviSystemException {
        validateHost(host);
        this.host = host;
        this.port = port;
    }

    private void validateHost(String host) throws NaviSystemException {
        if (!host.matches(hostRegex)) {
            throw new NaviSystemException("host ip is invalid.", NaviError.INVALID_HOST.code());
        }
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }

}

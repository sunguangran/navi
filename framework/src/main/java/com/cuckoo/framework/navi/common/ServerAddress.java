package com.cuckoo.framework.navi.common;

import com.cuckoo.framework.navi.common.exception.NaviSystemException;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ServerAddress {

    private String host;
    private int port;
    private String url;

    public ServerAddress(String url) {
        this.url = url;

    }

    public ServerAddress(String host, int port) throws NaviSystemException {
        this.host = host;
        this.port = port;
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }

}

package com.cuckoo.framework.navi.utils;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

public class ServerUrlUtil {

    private static final String JDBC_PREFIX = "jdbc";

    public static List<ServerUrl> getServerUrl(String serverUrl) {
        if (serverUrl == null) {
            return null;
        }

        List<ServerUrl> list = new LinkedList<>();
        if (serverUrl.startsWith(JDBC_PREFIX)) {
            list.add(new ServerUrl(serverUrl));
        } else {
            String[] servers = serverUrl.split(",");
            for (String svr : servers) {
                String[] addr = svr.split(":");
                if (addr.length == 2) {
                    ServerUrl url = new ServerUrl(addr[0], Integer.valueOf(addr[1]));
                    url.setUrl(serverUrl);
                    list.add(url);
                } else {
                    list.add(new ServerUrl(svr));
                }
            }
        }

        return list;
    }

    @Setter
    @Getter
    public static class ServerUrl {
        private String host;
        private int port;
        private String url;

        public ServerUrl(String url) {
            this.url = url;

        }

        public ServerUrl(String host, int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public String toString() {
            return host + ":" + port;
        }

    }
}

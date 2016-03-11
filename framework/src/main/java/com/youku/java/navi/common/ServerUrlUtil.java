package com.youku.java.navi.common;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class ServerUrlUtil {

    private static final String JDBC_PREFIX = "jdbc";

    public static List<ServerUrl> getServerUrl(String serverUrl) {
        if (serverUrl == null) {
            return null;
        }
        List<ServerUrl> list = new ArrayList<>();
        if (serverUrl.startsWith(JDBC_PREFIX)) {
            list.add(new ServerUrl(serverUrl));
        } else {
            String[] hostPorts = serverUrl.split(",");
            for (String hostPort : hostPorts) {
                String[] addr = hostPort.split(":");
                if (addr.length == 2) {
                    ServerUrl su = new ServerUrl(addr[0], Integer.valueOf(addr[1]));
                    su.setUrl(serverUrl);
                    list.add(su);
                } else {
                    list.add(new ServerUrl(hostPort));
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

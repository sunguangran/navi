package com.cuckoo.framework.navi.utils;

import com.cuckoo.framework.navi.common.ServerAddress;

import java.util.LinkedList;
import java.util.List;

public class ServerUrlUtil {

    private static final String JDBC_PREFIX = "jdbc";

    public static List<ServerAddress> getServerUrl(String serverUrl) {
        if (serverUrl == null) {
            return null;
        }

        List<ServerAddress> list = new LinkedList<>();
        if (serverUrl.startsWith(JDBC_PREFIX)) {
            list.add(new ServerAddress(serverUrl));
        } else {
            String[] servers = serverUrl.split(",");
            for (String svr : servers) {
                String[] addr = svr.split(":");
                if (addr.length == 2) {
                    ServerAddress url = new ServerAddress(addr[0], Integer.valueOf(addr[1]));
                    url.setUrl(serverUrl);
                    list.add(url);
                } else {
                    list.add(new ServerAddress(svr));
                }
            }
        }

        return list;
    }

}

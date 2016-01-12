package com.cuckoo.framework.navi.server;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.ACL;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.List;

@Slf4j
public class NaviRegisterTask implements Runnable {

    private ZooKeeper zookeeper;
    private final List<ACL> DEFAULT_ACL = Ids.OPEN_ACL_UNSAFE;

    private NaviServerType type;

    public NaviRegisterTask(NaviServerType type) {
        try {
            String url = ServerConfigure.isDeployEnv() ? ServerConfigure.get("zk.url.deploy") : ServerConfigure.get("zk.url.offline");
            int timeout = Integer.parseInt(ServerConfigure.get("zk.timeout"));
            log.info(url);
            zookeeper = new ZooKeeper(url, timeout, new Watcher() {
                public void process(WatchedEvent event) {
                    //do nothing
                }
            });
        } catch (IOException e) {
            log.error("{}", e.getMessage());
        }

        this.type = type;
    }

    public void run() {
        if (zookeeper == null) {
            return;
        }

        try {
            log.info("regist");
            if (zookeeper.exists("/navi_server", false) == null) {
                zookeeper.create("/navi_server", "".getBytes(), DEFAULT_ACL, CreateMode.PERSISTENT);
            }

            String[] names = ManagementFactory.getRuntimeMXBean().getName().split("@");
            String hostname = names[1];
            String pid = names[0];
            String hostpath = "/navi".concat("/").concat(hostname);
            if (zookeeper.exists(hostpath, false) == null) {
                zookeeper.create(hostpath, "".getBytes(), DEFAULT_ACL, CreateMode.PERSISTENT);
            }

            String node = hostpath.concat("/").concat(pid);
            if (zookeeper.exists(node, false) == null) {
                zookeeper.create(node, buildNode(pid).toJSONString().getBytes(), DEFAULT_ACL, CreateMode.EPHEMERAL);
            }
        } catch (KeeperException | InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    private JSONObject buildNode(String pid) {
        JSONObject data = new JSONObject();
        data.put("PID", Integer.parseInt(pid));
        data.put("navi_home", System.getProperty("NAVI_HOME"));
        data.put("type", type.name());
        switch (type) {
            case NettyServer:
                data.put("port", Integer.parseInt(ServerConfigure.getPort()));
                break;
            case AsyncServer:
            case DaemonServer:
                break;
            default:
                break;
        }
        
        data.put("jmxport", Integer.parseInt(System.getProperty("com.sun.management.jmxremote.port")));
        data.put("server_name", ServerConfigure.getServer());
        return data;
    }
}

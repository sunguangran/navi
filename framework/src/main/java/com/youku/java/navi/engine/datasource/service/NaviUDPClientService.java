package com.youku.java.navi.engine.datasource.service;

import com.youku.java.navi.common.ServerUrlUtil;
import com.youku.java.navi.engine.core.INaviUDPClientService;
import com.youku.java.navi.engine.datasource.driver.NaviUDPClientDriver;
import com.youku.java.navi.server.ServerConfigure;
import com.youku.java.navi.server.api.INaviUDPResponseHandler;
import com.youku.java.navi.utils.NaviUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Serializable;
import java.util.Random;

/**
 * 由于UDP是无连接的，发送的数据，在接收端可能是无序的，也有可能丢包，<br>
 * 发送后收到的响应也会出现这种情况，所以，如果对数据传输有高要求的请自己进行相应处理，或者使用TCP协议<br>
 * 如果发送的是json，请转换为String后传入<br>
 * 如果发送的是对象，直接传入，底层将对象序列化后传送，效率低<br>
 * 建议使用String,因为对象效率低，同时UDP报文大小有限制，使用对象可能超出限制。
 */
@Slf4j
@Setter
@Getter
public class NaviUDPClientService extends AbstractNaviDataService implements INaviUDPClientService {

    protected String offlineConnectString;
    protected String deployConnectString;
    protected String[] hosts;
    protected boolean sendAll = true;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        splithosts();
    }

    protected NaviUDPClientDriver getUDPClientDrive() {
        return (NaviUDPClientDriver) dataSource.getHandle();
    }

    protected String[] splithosts() {
        if (ServerConfigure.isDeployEnv()) {
            if (deployConnectString.contains(",")) {
                hosts = deployConnectString.split(",");
            } else {
                hosts = new String[]{deployConnectString};
            }
        } else {
            if (offlineConnectString.contains(",")) {
                hosts = offlineConnectString.split(",");
            } else {
                hosts = new String[]{offlineConnectString};
            }
        }
        return hosts;
    }

    public <T extends Serializable> void send(byte[] packet) {
        if (null != hosts) {
            if (sendAll) {
                for (String s : hosts) {
                    if (s.contains(":")) {
                        String[] des = s.split(":");
                        sendBytes(des[0], Integer.parseInt(des[1]), packet);
                    }
                }
            } else {
                int i = new Random().nextInt(hosts.length);
                if (hosts[i].contains(":")) {
                    String[] des = hosts[i].split(":");
                    sendBytes(des[0], Integer.parseInt(des[1]), packet);
                }
            }
        }

    }

    public <T extends Serializable> void send(T msg) {
        if (null != hosts) {
            if (sendAll) {
                for (String s : hosts) {
                    if (s.contains(":")) {
                        String[] des = s.split(":");
                        send(des[0], Integer.parseInt(des[1]), msg);
                    }
                }
            } else {
                int i = new Random().nextInt(hosts.length);
                if (hosts[i].contains(":")) {
                    String[] des = hosts[i].split(":");
                    send(des[0], Integer.parseInt(des[1]), msg);
                }
            }
        }
    }

    /**
     * 发送后不等待相应，异步交给handler处理<br>
     * 建议调用方式:<br>
     * sendAndHandle(msg,new INaviUDPResponseHandler(){<br>
     * void handle(Object obj){}<br>
     * Class<?> getResponseClass(){}<br>
     * <p/>
     * })；
     */
    public <T extends Serializable> void sendAndHandle(T msg, INaviUDPResponseHandler handler) {
        if (null != hosts) {
            if (sendAll) {
                for (String s : hosts) {
                    if (s.contains(":")) {
                        String[] des = s.split(":");
                        sendAndHandle(des[0], Integer.parseInt(des[1]), msg, handler);
                    }
                }
            } else {
                int i = new Random().nextInt(hosts.length);
                if (hosts[i].contains(":")) {
                    String[] des = hosts[i].split(":");
                    sendAndHandle(des[0], Integer.parseInt(des[1]), msg, handler);
                }
            }
        }
    }

    public <T extends Serializable> void send(String host, int port, T msg) {
        byte[] packet = null;
        if (msg instanceof String) {
            packet = msg.toString().getBytes();
        } else if (msg instanceof byte[]) {
            packet = (byte[]) msg;
        } else {
            try {
                packet = NaviUtil.getObjectByteArray(msg);
            } catch (IOException e) {
                log.error("obj to byte[] error", e);
            }
        }
        if (null != packet) {
            NaviUDPClientDriver driver = null;
            try {
                driver = getUDPClientDrive();
                driver.send(host, port, packet);
            } catch (IOException e) {
                log.error("send udp packet error", e);
            } finally {
                if (null != driver) {
                    driver.close();
                }
            }
        }
    }

    public void sendBytes(String host, int port, byte[] packet) {
        if (null != packet) {
            NaviUDPClientDriver driver = null;
            try {
                driver = getUDPClientDrive();
                driver.send(host, port, packet);
            } catch (IOException e) {
                log.error("send udp packet error", e);
            } finally {
                if (null != driver) {
                    driver.close();
                }
            }
        }
    }

    public <T extends Serializable> void send(T msg, ServerUrlUtil.ServerUrl... hosts) {
        if (null != hosts) {
            for (ServerUrlUtil.ServerUrl host : hosts) {
                send(host.getHost(), host.getPort(), msg);
            }
        }
    }

    /**
     * 发送后等待相应
     */
    public <T extends Serializable> Object sendAndReceive(String host, int port, T msg,
                                                          Class<? extends Serializable> responseClass) {
        byte[] packet = null;
        Object obj = null;
        if (msg instanceof String) {
            packet = msg.toString().getBytes();
        } else if (msg instanceof byte[]) {
            packet = (byte[]) msg;
        } else {
            try {
                packet = NaviUtil.getObjectByteArray(msg);
            } catch (IOException e) {
                log.error("obj to byte[] error", e);
            }
        }
        if (null != packet) {
            byte[] recvByte = null;
            NaviUDPClientDriver driver = null;
            try {
                driver = getUDPClientDrive();
                recvByte = driver.sendAndReceive(host, port,
                    packet);
            } catch (IOException e) {
                log.error("sendAndReceive udp packet error", e);
            } finally {
                if (null != driver) {
                    driver.close();
                }
            }
            if (String.class.equals(responseClass) && null != recvByte) {
                return new String(recvByte);
            } else if (null != recvByte) {
                try {
                    obj = NaviUtil.byteToObject(recvByte);
                } catch (Exception e) {
                    log.error("byte[] to obj error", e);
                }
            }
        }
        return obj;
    }

    /**
     * 发送后不等待相应，异步交给handler处理<br>
     * 建议调用方式:<br>
     * sendAndHandle(host,port,msg,new INaviUDPResponseHandler(){<br>
     * void handle(Object obj){}<br>
     * Class<?> getResponseClass(){}<br>
     * <p/>
     * })；
     */
    public <T extends Serializable> void sendAndHandle(String host, int port, T msg,
                                                       INaviUDPResponseHandler handler) {
        byte[] packet = null;
        if (msg instanceof String) {
            packet = msg.toString().getBytes();
        } else if (msg instanceof byte[]) {
            packet = (byte[]) msg;
        } else {
            try {
                packet = NaviUtil.getObjectByteArray(msg);
            } catch (IOException e) {
                log.error("obj to byte[] error", e);
            }
        }
        if (null != packet) {
            NaviUDPClientDriver driver = null;
            try {
                driver = getUDPClientDrive();
                driver.sendAndHandle(host, port, packet, handler);
            } catch (IOException e) {
                log.error("sendAndHandle udp packet error", e);
            } finally {
                if (null != driver) {
                    driver.close();
                }
            }
        }
    }

    public void send(String host, int port, byte[] packet) {
        NaviUDPClientDriver driver = null;
        try {
            driver = getUDPClientDrive();
            driver.send(host, port, packet);
        } catch (IOException e) {
            log.error("send udp packet error", e);
        } finally {
            if (null != driver) {
                driver.close();
            }
        }
    }

    public byte[] sendAndReceive(String host, int port, byte[] packet) {
        NaviUDPClientDriver driver = null;
        try {
            driver = getUDPClientDrive();
            return driver.sendAndReceive(host, port, packet);
        } catch (IOException e) {
            log.error("sendAndReceive udp packet error", e);
        } finally {
            if (null != driver) {
                driver.close();
            }
        }
        return null;
    }

    public <T extends Serializable> void sendAndHandle(String host, int port,
                                                       byte[] packet, INaviUDPResponseHandler handler) {
        NaviUDPClientDriver driver = null;
        try {
            driver = getUDPClientDrive();
            driver.sendAndHandle(host, port, packet, handler);
        } catch (IOException e) {
            log.error("sendAndHandle udp packet error", e);
        } finally {
            if (null != driver) {
                driver.close();
            }
        }
    }


}

package com.youku.java.navi.engine.datasource.driver;

import com.youku.java.navi.common.exception.NaviRuntimeException;
import com.youku.java.navi.engine.core.INaviDriver;
import com.youku.java.navi.utils.NaviUtil;
import com.youku.java.navi.server.api.INaviUDPResponseHandler;
import com.youku.java.navi.common.NAVIERROR;
import com.youku.java.navi.common.exception.NaviSystemException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class NaviUDPClientDriver implements INaviDriver {
    private DatagramSocket client;
    private GenericObjectPool<INaviDriver> pool;
    private AtomicBoolean close = new AtomicBoolean();
    private AtomicBoolean broken = new AtomicBoolean();
    private int sendBufSize = 1024;
    private int receiveBufSize = 1024;
    private int receiveTimeOut = 3000;
    private Executor executor;

    public NaviUDPClientDriver() {
        try {
            client = new DatagramSocket();
            client.setSendBufferSize(sendBufSize);
            client.setReceiveBufferSize(receiveBufSize);
            client.setSoTimeout(receiveTimeOut);
            executor = Executors.newCachedThreadPool();
        } catch (SocketException e) {
            log.error("create udp client error", e);
        }
    }

    public void send(String host, int port, byte[] packet) throws IOException {
        InetAddress addr = InetAddress.getByName(host);
        DatagramPacket sendPacket = new DatagramPacket(packet, packet.length, addr, port);
        client.send(sendPacket);
    }

    public byte[] sendAndReceive(String host, int port, byte[] packet) throws IOException {
        InetAddress addr = InetAddress.getByName(host);
        DatagramPacket sendPacket = new DatagramPacket(packet, packet.length, addr, port);
        client.send(sendPacket);
        byte[] recvBuf = new byte[receiveBufSize];
        DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
        client.receive(recvPacket);
        return Arrays.copyOfRange(recvPacket.getData(), 0, recvPacket.getLength());
    }

    @SuppressWarnings("unchecked")
    public <T extends Serializable> void sendAndHandle(final String host, final int port, final byte[] packet, final INaviUDPResponseHandler handler) throws IOException {
        executor.execute(new Runnable() {
            public void run() {
                try {
                    send(host, port, packet);
                } catch (IOException e) {
                    log.error("send udp packet error", e);
                }
                byte[] recvBuf = new byte[receiveBufSize];
                DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
                try {
                    client.receive(recvPacket);
                    byte[] recvByte = Arrays.copyOfRange(recvPacket.getData(), 0, recvPacket.getLength());
                    if (String.class.equals(handler.getResponseClass())) {
                        handler.handle(new String(recvByte));
                    } else {
                        T obj = (T) NaviUtil.byteToObject(recvByte);
                        handler.handle(obj);
                    }
                } catch (IOException e) {
                    log.error("receive udp packet error", e);
                } catch (Exception e) {
                    log.error("receive udp packet error", e);
                }

            }
        });
    }

    public void close() throws NaviSystemException {
        if (pool == null) {
            return;
        }
        try {
            if (broken.get()) {
                pool.invalidateObject(this);
            } else {
                pool.returnObject(this);
            }
        } catch (Exception ex) {
            try {
                pool.invalidateObject(this);
            } catch (Exception e) {
                throw new NaviRuntimeException(
                    "Could not return the resource to the pool",
                    NAVIERROR.SYSERROR.code(), e);
            }
        } finally {
            close.set(true);
        }
    }


    public GenericObjectPool<INaviDriver> getPool() {
        return pool;
    }

    public void setPool(GenericObjectPool<INaviDriver> pool) {
        this.pool = pool;
    }

    public boolean isClose() {
        return close.get();
    }

    public void setClose(boolean close) {
        this.close.set(close);
    }

    public boolean isBroken() {
        return broken.get();
    }

    public void setBroken(boolean broken) {
        this.broken.set(broken);
    }

    public void afterPropertiesSet() throws Exception {

    }

    public Object getDriver() {
        return null;
    }

    public void destroy() {
        close();
    }

    public boolean isAlive() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean open() {
        // TODO Auto-generated method stub
        return false;
    }

    public DatagramSocket getClient() {
        return client;
    }

    public void setClient(DatagramSocket client) {
        this.client = client;
    }

    public int getReceiveBufSize() {
        return receiveBufSize;
    }

    public void setReceiveBufSize(int receiveBufSize) {
        this.receiveBufSize = receiveBufSize;
    }

    public int getReceiveTimeOut() {
        return receiveTimeOut;
    }

    public void setReceiveTimeOut(int receiveTimeOut) {
        this.receiveTimeOut = receiveTimeOut;
    }

    public int getSendBufSize() {
        return sendBufSize;
    }

    public void setSendBufSize(int sendBufSize) {
        this.sendBufSize = sendBufSize;
    }

}

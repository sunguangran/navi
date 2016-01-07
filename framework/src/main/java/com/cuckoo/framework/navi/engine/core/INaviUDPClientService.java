package com.cuckoo.framework.navi.engine.core;

import com.cuckoo.framework.navi.server.api.INaviUDPResponseHandler;
import com.cuckoo.framework.navi.utils.ServerUrlUtil;

import java.io.IOException;
import java.io.Serializable;

public interface INaviUDPClientService {
    <T extends Serializable> void send(byte[] packet);

    void sendBytes(String host, int port, byte[] packet);

    <T extends Serializable> void send(T msg);

    <T extends Serializable> void send(String host, int port, T msg);

    <T extends Serializable> void send(T msg, ServerUrlUtil.ServerUrl... hosts);

    <T extends Serializable> Object sendAndReceive(String host, int port, T msg, Class<? extends Serializable> responseClass);

    <T extends Serializable> void sendAndHandle(T msg, INaviUDPResponseHandler handler);

    <T extends Serializable> void sendAndHandle(String host, int port, T msg, INaviUDPResponseHandler handler);

    void send(String host, int port, byte[] packet) throws IOException;

    byte[] sendAndReceive(String host, int port, byte[] packet) throws IOException;

    <T extends Serializable> void sendAndHandle(final String host, final int port, final byte[] packet, final INaviUDPResponseHandler handler) throws IOException;
}

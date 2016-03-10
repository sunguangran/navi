package com.youku.java.navi.engine.datasource.driver;

import com.youku.java.navi.common.exception.NaviSystemException;
import com.youku.java.navi.engine.datasource.pool.NaviPoolConfig;
import com.youku.java.navi.common.ServerUrlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import scribe.thrift.LogEntry;
import scribe.thrift.scribe.Client;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

@Slf4j
public class NaviScribeDriver extends AbstractNaviDriver {

    private Client client;

    public NaviScribeDriver(ServerUrlUtil.ServerUrl server, String auth, NaviPoolConfig poolConfig) throws TTransportException,
        UnknownHostException, IOException {
        super(server, auth, poolConfig);

        TSocket sock = new TSocket(new Socket(server.getHost(),
            server.getPort()));
        sock.setTimeout(poolConfig.getConnectTimeout());
        TFramedTransport transport = new TFramedTransport(sock);
        TBinaryProtocol protocol = new TBinaryProtocol(transport, false, false);
        client = new Client(protocol, protocol);
    }

    public void destroy() throws NaviSystemException {
        TTransport transport = client.getInputProtocol().getTransport();
        if (transport != null && transport.isOpen()) {
            transport.close();
            log.info("scribe client is closed successfully!");
        }
        client = null;
    }

    public boolean isAlive() throws NaviSystemException {
        TTransport transport = client.getInputProtocol().getTransport();
        return transport.isOpen();
    }

    /**
     * @param messages
     * @return successful is true
     * @throws Exception
     */
    public boolean sendLog(List<LogEntry> messages) throws Exception {
        try {
            client.Log(messages).getValue();
        } catch (Exception e) {
            if (e instanceof TTransportException) {
                setBroken(true);
            }
            throw e;
        }
        return true;
    }

    public boolean open() {
        return true;
    }
}

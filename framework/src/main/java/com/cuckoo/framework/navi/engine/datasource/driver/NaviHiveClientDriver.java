package com.cuckoo.framework.navi.engine.datasource.driver;

import com.cuckoo.framework.navi.engine.datasource.pool.NaviPoolConfig;
import com.cuckoo.framework.navi.common.ServerAddress;
import org.apache.hadoop.hive.service.HiveClient;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class NaviHiveClientDriver extends ANaviDriver {

    private TTransport transport;
    private HiveClient client;

    public NaviHiveClientDriver(ServerAddress server, String auth,
                                NaviPoolConfig poolConfig) {
        super(server, auth, poolConfig);
        try {
            transport = new TSocket(server.getHost(), server.getPort());
            TProtocol protocol = new TBinaryProtocol(transport);
            client = new HiveClient(protocol);
            transport.open();
        } catch (TTransportException e) {
            e.printStackTrace();
        }
    }

    public HiveClient getClient() {
        return client;
    }

    public void destroy() {
        try {
            transport.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isAlive() {
        return transport.isOpen();
    }

    public boolean open() {
        if (!transport.isOpen()) {
            try {
                transport.open();
            } catch (TTransportException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

}

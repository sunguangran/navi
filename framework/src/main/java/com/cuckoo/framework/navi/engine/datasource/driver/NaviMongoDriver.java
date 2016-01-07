package com.cuckoo.framework.navi.engine.datasource.driver;

import com.cuckoo.framework.navi.common.NaviError;
import com.cuckoo.framework.navi.common.exception.NaviSystemException;
import com.cuckoo.framework.navi.engine.datasource.pool.NaviMongoPoolConfig;
import com.cuckoo.framework.navi.engine.datasource.pool.NaviPoolConfig;
import com.cuckoo.framework.navi.utils.ServerUrlUtil;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoOptions;
import com.mongodb.ServerAddress;
import lombok.extern.slf4j.Slf4j;

import java.net.UnknownHostException;

@Slf4j
public class NaviMongoDriver extends AbstractNaviDriver {

    private final int SLEEPTIME = 60000;// 1min
    private final int MAX_CLEARCOUNT = 3;
    private Mongo mongo;

    public NaviMongoDriver(ServerUrlUtil.ServerUrl server, String auth,
                           NaviPoolConfig poolConfig) throws NumberFormatException,
        MongoException, UnknownHostException {
        super(server, auth, poolConfig);
        this.mongo = new Mongo(new ServerAddress(server.getHost(),
            server.getPort()), getMongoOptions(poolConfig));
        startIdleConnCheck();
    }

    private void startIdleConnCheck() {
        new MongoIdleCleaner().start();
    }

    private MongoOptions getMongoOptions(NaviPoolConfig poolConfig) {
        if (poolConfig instanceof NaviMongoPoolConfig) {
            return ((NaviMongoPoolConfig) poolConfig).getOptions();
        }
        MongoOptions options = new MongoOptions();
        options.connectionsPerHost = poolConfig.getMaxActive();
        options.connectTimeout = poolConfig.getConnectTimeout();
        options.safe = true;
        return options;
    }

    public void destroy() throws NaviSystemException {
        close();
    }

    @Override
    public void close() throws NaviSystemException {
        try {
            mongo.close();
            log.info("mongos instance is destoried!");
        } catch (Exception e) {
            log.warn("mongos instance is destoried failly!");
        }
        setClose(true);
    }

    public Mongo getMongo() {
        if (isClose()) {
            throw new NaviSystemException("the driver has been closed!",
                NaviError.SYSERROR.code());
        }
        return mongo;
    }

    public void setMongo(Mongo mongo) {
        this.mongo = mongo;
    }

    public boolean isAlive() throws NaviSystemException {
        //mongo..getConnector();
        return mongo.getConnector().isOpen();
    }

    /**
     * 在热部署点，因为并发的问题，会出现该driver实例已被关闭而仍还有线程调用该实例连接
     * 数据库，导致连接泄露，所以开此线程，在driver关闭情况下仍在1min内重复关闭2次，已确保 连接确实均被关闭
     */
    class MongoIdleCleaner extends Thread {
        private int initCount = 0;

        public MongoIdleCleaner() {
            setDaemon(true);
            setName("MongoIdleCleaner" + hashCode());
        }

        public void run() {
            while (true) {
                try {
                    Thread.sleep(SLEEPTIME);
                } catch (InterruptedException e) {

                }
                try {
                    if (isClose()) {
                        initCount++;
                        close();
                    }
                } catch (Exception e) {
                }
                if (initCount > MAX_CLEARCOUNT) {
                    break;
                }
            }
        }

    }

    public boolean open() {
        // TODO Auto-generated method stub
        return false;
    }

    public void afterPropertiesSet() throws Exception {
        // TODO Auto-generated method stub

    }

    public Mongo getDriver() {
        return getMongo();
    }
}
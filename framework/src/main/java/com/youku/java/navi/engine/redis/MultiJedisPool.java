package com.youku.java.navi.engine.redis;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class MultiJedisPool {

    private Map<String, JedisPool> poolsMap;
    protected GenericObjectPoolConfig poolConfig;
    protected int timeout = Protocol.DEFAULT_TIMEOUT;
    private Set<MasterListener> masterListeners = new HashSet<>();

    private Random random;

    public MultiJedisPool(Set<String> sentinels, final String masterName, final boolean readMaster, int timeout, final GenericObjectPoolConfig poolConfig) {
        this.poolConfig = poolConfig;
        this.timeout = timeout;
        this.random = new Random();
        Set<String> servers = initSentinels(sentinels, masterName, readMaster);
        refreshPools(servers);
    }

    private synchronized void refreshPools(Set<String> servers) {
        if (servers == null || servers.size() == 0) {
            return;
        }

        if (poolsMap == null) {
            poolsMap = new HashMap<>();
        }

        for (String server : servers) {
            if (!poolsMap.containsKey(server)) {
                HostAndPort hap = toHostAndPort(server.split(":"));
                poolsMap.put(server, new JedisPool(poolConfig, hap.getHost(), hap.getPort(), timeout));
            }
        }

        Iterator<Map.Entry<String, JedisPool>> iter = poolsMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, JedisPool> entry = iter.next();
            if (!servers.contains(entry.getKey())) {
                try {
                    entry.getValue().destroy();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
                iter.remove();
            }
        }

    }

    private Set<String> initSentinels(Set<String> sentinels, final String masterName, final boolean readMaster) {
        Set<String> servers = new HashSet<>();
        for (String sentinel : sentinels) {
            final HostAndPort hap = toHostAndPort(sentinel.split(":"));
            Jedis jedis = new Jedis(hap.getHost(), hap.getPort());
            try {
                servers = getServers(jedis, masterName, readMaster);
            } catch (Exception e) {
                log.error("Cannot connect to sentinel running @ " + hap + ". Trying next one.");
            }
        }

        if (servers == null || servers.size() == 0) {
            log.error("All sentinels down, can't find redis servers");
            throw new RuntimeException("All sentinels down, can't find redis servers");
        }

        for (String sentinel : sentinels) {
            final HostAndPort hap = toHostAndPort(sentinel.split(":"));
            MasterListener masterListener = new MasterListener(masterName, hap.getHost(), hap.getPort(), readMaster);
            masterListeners.add(masterListener);
            masterListener.start();
        }

        return servers;
    }


    private Set<String> getServers(Jedis jedis, final String masterName, final boolean readMaster) {
        Set<String> servers = new HashSet<>();
        if (readMaster) {
            List<String> master = jedis.sentinelGetMasterAddrByName(masterName);
            if (master == null || master.size() != 2) {
                return null;
            }
            servers.add(toHostAndPort(master.toArray(new String[master.size()])).toString());
        }

        List<Map<String, String>> slaves = jedis.sentinelSlaves(masterName);
        for (Map<String, String> slave : slaves) {
            if (slave.containsKey("s-down-time")) {
                continue;
            }
            servers.add(toHostAndPort(slave.get("ip"), slave.get("port")).toString());
        }
        return servers;
    }

    private HostAndPort toHostAndPort(String... server) {
        return new HostAndPort(server[0], Integer.parseInt(server[1]));
    }

    public void destroy() {
        for (MasterListener m : masterListeners) {
            m.shutdown();
        }

        for (JedisPool pool : poolsMap.values()) {
            pool.destroy();
        }
    }

    public JedisPool randomJedisPool() {
        while (true) {
            Set<String> keys = poolsMap.keySet();
            String randomkey = keys.toArray(new String[0])[random.nextInt(keys.size())];
            JedisPool pool = poolsMap.get(randomkey);
            if (pool != null) {
                return pool;
            }
        }
    }

    protected class JedisPubSubAdapter extends JedisPubSub {

        @Override
        public void onMessage(String channel, String message) {
        }

        @Override
        public void onPMessage(String pattern, String channel, String message) {
        }

        @Override
        public void onPSubscribe(String pattern, int subscribedChannels) {
        }

        @Override
        public void onPUnsubscribe(String pattern, int subscribedChannels) {
        }

        @Override
        public void onSubscribe(String channel, int subscribedChannels) {
        }

        @Override
        public void onUnsubscribe(String channel, int subscribedChannels) {
        }
    }

    protected class MasterListener extends Thread {

        protected String masterName;
        protected String host;
        protected int port;
        protected long subscribeRetryWaitTimeMillis = 5000;
        protected Jedis j;
        protected boolean readMaster;

        protected AtomicBoolean running = new AtomicBoolean(false);

        protected MasterListener() {
        }

        public MasterListener(String masterName, String host, int port, boolean readMaster) {
            this.masterName = masterName;
            this.host = host;
            this.port = port;
            this.readMaster = readMaster;
        }

        public MasterListener(String masterName, String host, int port, boolean readMaster, long subscribeRetryWaitTimeMillis) {
            this(masterName, host, port, readMaster);
            this.subscribeRetryWaitTimeMillis = subscribeRetryWaitTimeMillis;
        }

        public void run() {
            running.set(true);

            while (running.get()) {

                j = new Jedis(host, port);

                try {
                    j.subscribe(new JedisPubSubAdapter() {
                        @Override
                        public void onMessage(String channel, String message) {
                            log.info("Channel:" + channel);
                            log.info("Message:" + message);

                            String masterFromMessage = "";

                            if (message.startsWith("slave")) {
                                masterFromMessage = message.split("@")[1].split(" ")[1];
                            } else if (readMaster) {
                                masterFromMessage = message.split(" ")[1];
                            }
                            if (masterFromMessage.equals(masterName)) {
                                try {
                                    Jedis jedis = new Jedis(j.getClient().getHost(), j.getClient().getPort());
                                    Set<String> servers = getServers(jedis, masterName, readMaster);
                                    refreshPools(servers);
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                }
                            } else {
                                log.info("Ignoring message");
                            }
                        }
                    }, "+sdown", "-sdown", "+slave");

                } catch (JedisConnectionException e) {
                    if (running.get()) {
                        log.error("Lost connection to Sentinel at " + host
                            + ":" + port
                            + ". Sleeping " + subscribeRetryWaitTimeMillis + "ms and retrying.");
                        try {
                            Thread.sleep(subscribeRetryWaitTimeMillis);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    } else {
                        log.info("Unsubscribing from Sentinel at " + host + ":"
                            + port);
                    }
                }
            }
        }

        private void shutdown() {
            try {
                running.set(false);
                j.disconnect();
            } catch (Exception e) {
                log.error("Caught exception while shutting down: "
                    + e.getMessage());
            }
        }
    }
}

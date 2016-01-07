package com.cuckoo.framework.navi.engine.datasource.driver;

import com.cuckoo.framework.navi.engine.datasource.pool.NaviHBasePoolConfig;
import com.cuckoo.framework.navi.engine.datasource.pool.NaviPoolConfig;
import com.cuckoo.framework.navi.utils.ServerUrlUtil.ServerUrl;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.*;

@Slf4j
public class NaviHBaseDriver extends AbstractNaviDriver {
    private HConnection connection;
    private HBaseAdmin admin;
    private NamespaceDescriptor nd;

    public NaviHBaseDriver(ServerUrl server, String auth) {
        super(server, auth);
    }

    public NaviHBaseDriver(String url, String auth, NaviPoolConfig poolConfig) {
        super(new ServerUrl(url), auth, poolConfig);
        if (poolConfig instanceof NaviHBasePoolConfig) {
            Configuration configuration = ((NaviHBasePoolConfig) poolConfig)
                .getConfig();
            try {
                connection = HConnectionManager.createConnection(configuration);
                admin = new HBaseAdmin(configuration);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public NaviHBaseDriver(ServerUrl server, String auth,
                           NaviPoolConfig poolConfig) {
        super(server, auth, poolConfig);
        if (poolConfig instanceof NaviHBasePoolConfig) {
            Configuration configuration = ((NaviHBasePoolConfig) poolConfig)
                .getConfig();
            try {
                connection = HConnectionManager.createConnection(configuration);
                admin = new HBaseAdmin(configuration);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public void destroy() {
        try {
            admin.close();
            connection.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        admin = null;
        connection = null;
    }

    public boolean isAlive() {
        boolean flag = false;
        if (connection != null) {
            try {
                connection.getMaster();
                flag = true;
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return flag;
    }

    public boolean open() {
        boolean flag = false;
        if (connection == null) {
            try {
                Configuration configuration = ((NaviHBasePoolConfig) this
                    .getPoolConfig()).getConfig();
                connection = HConnectionManager.createConnection(configuration);
                admin = new HBaseAdmin(configuration);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        flag = this.isAlive();
        return flag;
    }

    /**
     * increment操作
     *
     * @param row
     *     rowKey
     * @param family
     *     列族名
     * @param qualifier
     *     列名
     * @param amount
     *     递增数
     * @return increment前的值
     * @throws IOException
     */
    public long increment(String tableName, String row, String family,
                          String qualifier, long amount) {
        // HTable hTable= HbaseUtils.getHTable(tmpt.getTableFactory(),
        // tmpt.getCharset(), tmpt.getConfiguration(), this.tableName);
        long result = 0l;
        HTableInterface hTable = null;
        try {
            hTable = connection.getTable(tableName);
            if (null != hTable) {
                result = hTable.incrementColumnValue(row.getBytes(),
                    family.getBytes(), qualifier.getBytes(), amount);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (null != hTable) {
                try {
                    hTable.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return result;
    }

    public long increment(String tableName, String row, String family,
                          String qualifier) throws IOException {
        return this.increment(tableName, row, family, qualifier, 1l);
    }

    /**
     * Put操作
     *
     * @param row
     *     rowKey
     * @param family
     *     列族名
     * @param qualifier
     *     列名
     * @param value
     *     插入的值
     * @return true 操作成功<br/>
     * false 操作失败
     */
    public boolean put(String tableName, String row, String family,
                       String qualifier, byte[] value) {
        boolean flag = false;
        HTableInterface hTable = null;
        try {
            hTable = connection.getTable(tableName);
            if (null != hTable) {
                Put put = new Put(row.getBytes());
                put.add(family.getBytes(), qualifier.getBytes(), value);
                hTable.put(put);
                flag = true;
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            if (null != hTable) {
                try {
                    hTable.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return flag;
    }

    /**
     * Put操作
     *
     * @param row
     *     rowKey
     * @param family
     *     列族名
     * @param qualifier
     *     列名
     * @param value
     *     插入的值
     * @param timestamp
     *     时间戳
     * @return true 操作成功<br/>
     * false 操作失败
     */
    public boolean put(String tableName, String row, String family,
                       String qualifier, byte[] value, long timestamp) {
        boolean flag = false;
        HTableInterface hTable = null;
        try {
            hTable = connection.getTable(tableName);
            if (null != hTable) {
                Put put = new Put(row.getBytes());
                put.add(family.getBytes(), qualifier.getBytes(), timestamp,
                    value);
                hTable.put(put);
                flag = true;
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            if (null != hTable) {
                try {
                    hTable.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return flag;
    }

    /**
     * 指定列族名的delete操作
     *
     * @param family
     *     列族名
     * @return true 操作成功<br/>
     * false 操作失败
     */
    public boolean delteteFamily(String tableName, String family) {
        boolean flag = false;
        HTableInterface hTable = null;
        ResultScanner scanner = null;
        try {
            hTable = connection.getTable(tableName);
            if (null != hTable) {
                Scan scan = new Scan();
                Filter filter = new FamilyFilter(CompareOp.EQUAL,
                    new BinaryComparator(family.getBytes()));
                scan.setFilter(filter);
                scanner = hTable.getScanner(scan);
                if (null != scanner) {
                    List<Delete> list = new ArrayList<Delete>();
                    for (Result rs : scanner) {
                        Delete delete = new Delete(rs.getRow());
                        delete.deleteFamily(family.getBytes());
                        list.add(delete);
                    }
                    hTable.delete(list);
                    flag = true;
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            if (null != scanner) {
                scanner.close();
            }
            if (null != hTable) {
                try {
                    hTable.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return flag;
    }

    /**
     * 指定列族名，列名和时间戳的delete操作
     *
     * @param family
     *     列族名
     * @param qualifier
     *     列名
     * @param timestamp
     *     时间戳
     * @return true 操作成功<br/>
     * false 操作失败
     */
    public boolean deleteColumn(String tableName, String family,
                                String qualifier, long timestamp) {
        // HTable hTable= HbaseUtils.getHTable(tmpt.getTableFactory(),
        // tmpt.getCharset(), tmpt.getConfiguration(), this.tableName);
        boolean flag = false;
        HTableInterface hTable = null;
        ResultScanner scanner = null;
        try {
            hTable = connection.getTable(tableName);
            if (null != hTable) {
                Scan scan = new Scan();
                Filter ffilter = new FamilyFilter(CompareOp.EQUAL,
                    new BinaryComparator(family.getBytes()));
                Filter qfilter = new QualifierFilter(CompareOp.EQUAL,
                    new BinaryComparator(qualifier.getBytes()));
                FilterList flist = new FilterList();
                flist.addFilter(ffilter);
                flist.addFilter(qfilter);
                scan.setFilter(flist);
                scanner = hTable.getScanner(scan);
                if (null != scanner) {
                    List<Delete> list = new ArrayList<Delete>();
                    for (Result rs : scanner) {
                        Delete delete = new Delete(rs.getRow());
                        delete.deleteColumns(family.getBytes(),
                            qualifier.getBytes(), timestamp);
                        list.add(delete);
                    }
                    hTable.delete(list);
                    flag = true;
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            if (null != scanner) {
                scanner.close();
            }
            if (null != hTable) {
                try {
                    hTable.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return flag;
    }

    /**
     * 指定列族名，列名的delete操作
     *
     * @param family
     *     列族名
     * @param qualifier
     *     列名
     * @return true 操作成功<br/>
     * false 操作失败
     */
    public boolean deleteColumn(String tableName, String family,
                                String qualifier) {
        // HTable hTable= HbaseUtils.getHTable(tmpt.getTableFactory(),
        // tmpt.getCharset(), tmpt.getConfiguration(), this.tableName);
        boolean flag = false;
        HTableInterface hTable = null;
        ResultScanner scanner = null;
        try {
            hTable = connection.getTable(tableName);
            if (null != hTable) {
                Scan scan = new Scan();
                Filter ffilter = new FamilyFilter(CompareOp.EQUAL,
                    new BinaryComparator(family.getBytes()));
                Filter qfilter = new QualifierFilter(CompareOp.EQUAL,
                    new BinaryComparator(qualifier.getBytes()));
                FilterList flist = new FilterList();
                flist.addFilter(ffilter);
                flist.addFilter(qfilter);
                scan.setFilter(flist);
                scanner = hTable.getScanner(scan);
                if (null != scanner) {
                    List<Delete> list = new ArrayList<Delete>();
                    for (Result rs : scanner) {
                        Delete delete = new Delete(rs.getRow());
                        delete.deleteColumns(family.getBytes(),
                            qualifier.getBytes());
                        list.add(delete);
                    }
                    hTable.delete(list);
                    flag = true;
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            if (null != scanner) {
                scanner.close();
            }
            if (null != hTable) {
                try {
                    hTable.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return flag;
    }

    /**
     * 指定行key的delete操作
     *
     * @param rowKey
     *     行key
     * @return true 操作成功<br/>
     * false 操作失败
     */
    public boolean deleteRow(String tableName, String rowKey) {
        // HTable hTable= HbaseUtils.getHTable(tmpt.getTableFactory(),
        // tmpt.getCharset(), tmpt.getConfiguration(), this.tableName);
        boolean flag = false;
        HTableInterface hTable = null;
        try {
            hTable = connection.getTable(tableName);
            if (null != hTable) {
                Delete delete = new Delete(rowKey.getBytes());
                hTable.delete(delete);
                flag = true;
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            if (null != hTable) {
                try {
                    hTable.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return flag;
    }

    /**
     * 指定行key,列族名，列名获取多版本的值
     *
     * @param rowkey
     *     行key
     * @param family
     *     列族名
     * @param qualifier
     *     列名
     * @return 以版本号为key的值集合
     */
    public Map<Long, byte[]> getMultiver(String tableName, String rowkey,
                                         String family, String qualifier) {
        Map<Long, byte[]> map = null;
        HTableInterface hTable = null;
        try {
            hTable = connection.getTable(tableName);
            if (null != hTable) {
                Get get = new Get(rowkey.getBytes());
                get.addColumn(family.getBytes(), qualifier.getBytes());
                map = hTable.get(get).getMap().get(family.getBytes())
                    .get(qualifier.getBytes());
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            if (null != hTable) {
                try {
                    hTable.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return map;
    }

    /**
     * 指定行key,列族名，列名获取最新的值
     * <p>
     * 行key
     *
     * @param family
     *     列族名
     * @param qualifier
     *     列名
     * @return 对应的值
     */
    public byte[] get(String tableName, String rowKey, String family,
                      String qualifier) {
        return this.get(tableName, rowKey, family, qualifier, null);
    }

    /**
     * 根据查询条件获取指定行的指定列下的值
     *
     * @param rowKey
     *     行key
     * @param family
     *     列族名
     * @param qualifier
     *     列名
     * @param filter
     *     查询条件
     * @return 对应的值
     */
    public byte[] get(String tableName, String rowKey, String family,
                      String qualifier, Filter filter) {
        byte[] result = null;
        HTableInterface hTable = null;
        if (family != null) {
            try {
                hTable = connection.getTable(tableName);
                if (null != hTable) {
                    Get get = new Get(rowKey.getBytes());
                    get.setFilter(filter);
                    Map<byte[], NavigableMap<byte[], byte[]>> map = hTable.get(
                        get).getNoVersionMap();
                    if (map != null) {
                        NavigableMap<byte[], byte[]> map1 = map.get(family
                            .getBytes());
                        if (map1 != null) {
                            result = map1.get(qualifier.getBytes());
                        }
                    }
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            } finally {
                if (null != hTable) {
                    try {
                        hTable.close();
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 根据查询条件获取最新的值
     *
     * @param rowKey
     *     行key
     * @param filter
     *     查询条件
     * @param families
     *     制定列族
     * @return 指定行对应的值集合，为<列族名，<列名，值>>的Map集合
     */
    public Map<byte[], NavigableMap<byte[], byte[]>> getRow(String tableName,
                                                            byte[] rowKey, String[] families, Filter filter) {
        Map<byte[], NavigableMap<byte[], byte[]>> map = null;
        HTableInterface hTable = null;
        try {
            hTable = connection.getTable(tableName);
            if (null != hTable) {
                Get get = new Get(rowKey);
                if (null != families) {
                    for (String family : families) {
                        get.addFamily(family.getBytes());
                    }
                }
                if (null != filter) {
                    get.setFilter(filter);
                }
                map = hTable.get(get).getNoVersionMap();
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            if (null != hTable) {
                try {
                    hTable.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return map;
    }

    /**
     * 根据查询条件获取多版本的值
     *
     * @param rowkey
     *     行key
     * @param family
     *     列族名
     * @param qualifier
     *     列名
     * @param filter
     *     查询条件
     * @return 以版本号为key的值集合
     */
    public Map<Long, byte[]> getMultiver(String tableName, String rowkey,
                                         String family, String qualifier, Filter filter) {
        Map<Long, byte[]> map = null;
        HTableInterface hTable = null;
        try {
            hTable = connection.getTable(tableName);
            if (null != hTable) {
                Get get = new Get(rowkey.getBytes());
                get.setFilter(filter);
                map = hTable.get(get).getMap().get(family.getBytes())
                    .get(qualifier.getBytes());
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            if (null != hTable) {
                try {
                    hTable.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return map;
    }

    /**
     * 根据查询条件获取指定行多版本的值
     *
     * @param rowkey
     *     行key
     * @param filter
     * @return 指定行对应的值集合，为<列族名，<列名，<版本号，值>>>的Map集合
     */
    public Map<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> getMultiver(
        String tableName, String rowkey, Filter filter) {
        Map<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> map = null;
        HTableInterface hTable = null;
        try {
            hTable = connection.getTable(tableName);
            if (null != hTable) {
                Get get = new Get(rowkey.getBytes());
                get.setFilter(filter);
                map = hTable.get(get).getMap();
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            if (null != hTable) {
                try {
                    hTable.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return map;
    }

    /**
     * 根据查询条件进行scan操作获取多行的多版本值
     *
     * @param scan
     *     查询条件
     * @return 多行结果结构为<rowkey,<cf,<column,<ts,value>>>><br/>
     * null 没有查询结果
     */
    public Map<byte[], Map<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>>> scanMultiver(
        String tableName, Scan scan) {
        Map<byte[], Map<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>>> result = null;
        HTableInterface hTable = null;
        ResultScanner scanner = null;
        try {
            hTable = connection.getTable(tableName);
            if (null != hTable) {
                scanner = hTable.getScanner(scan);
                if (null != scanner) {
                    result = new HashMap<byte[], Map<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>>>();
                    for (Result rs : scanner) {
                        Map<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> map = rs
                            .getMap();
                        result.put(rs.getRow(), map);
                    }
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            if (null != scanner) {
                scanner.close();
            }
            if (null != hTable) {
                try {
                    hTable.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return result;
    }

    /**
     * 根据查询条件进行scan操作获取多行的值
     *
     * @param scan
     *     查询条件
     * @return 多行结果结构为<rowkey,<cf,<column,value>>><br/>
     * null 没有查询结果
     * @throws IOException
     */
    public Map<byte[], Map<byte[], NavigableMap<byte[], byte[]>>> scan(
        String tableName, Scan scan) {
        Map<byte[], Map<byte[], NavigableMap<byte[], byte[]>>> result = null;
        HTableInterface hTable = null;
        ResultScanner scanner = null;
        try {
            hTable = connection.getTable(tableName);
            if (null != hTable) {
                scanner = hTable.getScanner(scan);
                if (null != scanner) {
                    result = new HashMap<byte[], Map<byte[], NavigableMap<byte[], byte[]>>>();
                    for (Result rs : scanner) {
                        Map<byte[], NavigableMap<byte[], byte[]>> map = rs
                            .getNoVersionMap();
                        result.put(rs.getRow(), map);
                    }
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            if (null != scanner) {
                scanner.close();
            }
            if (null != hTable) {
                try {
                    hTable.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return result;
    }

    public void afterPropertiesSet() {
        if (this.getPoolConfig() instanceof NaviHBasePoolConfig) {
            Configuration configuration = ((NaviHBasePoolConfig) this
                .getPoolConfig()).getConfig();
            try {
                admin = new HBaseAdmin(configuration);
                connection = HConnectionManager.createConnection(configuration);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public boolean deleteTable(String tableName) {
        boolean result = false;
        try {
            if (!admin.tableExists(TableName.valueOf(tableName))) {
                log.info("hbase table not exists!");
            } else {
                admin.disableTable(TableName.valueOf(tableName));
                admin.deleteTable(TableName.valueOf(tableName));
                result = true;
            }
        } catch (MasterNotRunningException e) {
            log.error(e.getMessage(), e);
        } catch (ZooKeeperConnectionException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    public boolean createTable(String tableName, String[] familys,
                               byte[][] splitKeys, int timeToLive) {
        boolean result = false;
        HTableDescriptor tableDesc = new HTableDescriptor(
            TableName.valueOf(tableName));
        for (String family : familys) {
            HColumnDescriptor columnDesc = new HColumnDescriptor(family);// more
            // args
            // input
            columnDesc.setTimeToLive(timeToLive);
            tableDesc.addFamily(columnDesc);
        }
        try {
            if (admin.tableExists(TableName.valueOf(tableName))) {
                log.info("hbase table exists!");
            } else {
                if (null == splitKeys) {
                    // avoid put empty splitKeys
                    admin.createTable(tableDesc);
                } else {
                    admin.createTable(tableDesc, splitKeys);
                }
                result = true;
            }
        } catch (MasterNotRunningException e) {
            log.error(e.getMessage(), e);
        } catch (ZooKeeperConnectionException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * 创建hbase名字空间
     *
     * @return
     */
    public boolean createNamespace(String nsName) {
        nd = NamespaceDescriptor.create(nsName).build();
        try {
            if (admin == null)
                return false;
            admin.createNamespace(nd);
            return true;
        } catch (NamespaceExistException e) {
            log.info("hbase namespace has exist!");
        } catch (MasterNotRunningException e) {
            log.error(e.getMessage(), e);
        } catch (ZooKeeperConnectionException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * 创建hbase名字空间 先setNd，然后调用该方法
     *
     * @return
     */
    public boolean createNamespace() {
        if (admin == null || nd == null)
            return false;
        try {
            admin.createNamespace(nd);
            return true;
        } catch (NamespaceExistException e) {
            log.info("hbase namespace has exist!");
        } catch (MasterNotRunningException e) {
            log.error(e.getMessage(), e);
        } catch (ZooKeeperConnectionException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * 修改列族，增删改
     *
     * @param tableName
     * @param family
     * @param op
     * @return
     */
    public boolean modifyColumn(String tableName, String family, String op) {
        // modify cf
        boolean result = false;
        try {
            TableName tn = TableName.valueOf(tableName);
            if (!admin.tableExists(tn)) {
                log.info("hbase table exists!");
            } else {
                // admin.enableTable(tn);
                admin.disableTable(tn);
                if (op.equals("delete")) {
                    admin.deleteColumn(tn, Bytes.toBytes(family));
                } else {
                    HColumnDescriptor columnDesc = new HColumnDescriptor(
                        Bytes.toBytes(family));
                    if (op.equals("add")) {
                        // add the same will throw io exception
                        admin.addColumn(tn, columnDesc);
                    }
                    if (op.equals("modify")) {
                        columnDesc.setTimeToLive(10);
                        admin.modifyColumn(tn, columnDesc);
                    }
                }
                admin.enableTable(tn);
                result = true;
            }
        } catch (MasterNotRunningException e) {
            log.error(e.getMessage(), e);
        } catch (ZooKeeperConnectionException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    public HConnection getConnection() {
        return connection;
    }

    public void setConnection(HConnection connection) {
        this.connection = connection;
    }

    public HBaseAdmin getAdmin() {
        return admin;
    }

    public void setAdmin(HBaseAdmin admin) {
        this.admin = admin;
    }

    public NamespaceDescriptor getNd() {
        return nd;
    }

    public void setNd(NamespaceDescriptor nd) {
        this.nd = nd;
    }

}

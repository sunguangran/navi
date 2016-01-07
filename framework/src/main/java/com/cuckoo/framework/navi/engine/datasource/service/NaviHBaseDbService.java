package com.cuckoo.framework.navi.engine.datasource.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cuckoo.framework.navi.common.NAVIERROR;
import com.cuckoo.framework.navi.common.NaviRuntimeException;
import com.cuckoo.framework.navi.engine.datasource.driver.NaviHBaseDriver;
import com.cuckoo.framework.navi.serviceobj.*;
import com.cuckoo.framework.navi.utils.NaviUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.client.*;

import java.io.IOException;
import java.util.*;

/**
 * HBaseService类，用于针对INaviColumnDto子类的查询操作
 *
 * @param <T>
 *     继承于INaviColumnDto的Dto类
 */
@Slf4j
public class NaviHBaseDbService<T extends INaviColumnDto> extends
    AbstractNaviDataService {
    private boolean vflag = false;

    /**
     * 获取该service是否支持多版本，默认为false
     *
     * @return true 支持 <br/>
     * false 不支持
     */
    public boolean isVflag() {
        return vflag;
    }

    public void setVflag(boolean vflag) {
        this.vflag = vflag;
    }

    public NaviHBaseDbService() {
        if (dataSource != null) {
            // tmpt = new
            // NaviHBaseTemplateFactory(this.getDataSource()).getHBaseTemplate(tableNm);
        }
    }

    public HTableInterface getHTable(String tableNm) {
        HTableInterface hTable = null;
        if (dataSource != null) {
            NaviHBaseDriver driver = (NaviHBaseDriver) dataSource.getHandle();
            try {
                hTable = driver.getConnection().getTable(tableNm);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return hTable;
    }

    /**
     * 保存操作
     *
     * @param t
     *     要保存的Dto对象
     */
    public void insert(T t) {
        String tableNm = getDtoTableNm(t.getClass());
        HTableInterface hTable = null;
        if (null != tableNm) {
            hTable = getHTable(tableNm);
        }
        if (hTable != null) {
            final T t_obj = t;
            NaviColumnExtDto obj = null;
            List<Put> puts = new ArrayList<Put>();
            if (t_obj instanceof NaviColumnExtDto) {
                obj = (NaviColumnExtDto) t_obj;
            } else {
                if (t_obj instanceof NaviBaseColumnDto) {
                    obj = ((NaviBaseColumnDto) t_obj).toColumnExtDto();
                }
            }
            if (obj != null) {
                try {
                    // 遍历ColumnExtDto
                    JSONObject json = JSONObject.parseObject(obj.dataToString());
                    byte[] rowKey = obj.getRowkey();
                    for (String cfNm : json.keySet()) {
                        JSONObject cols = json.getJSONObject(cfNm);
                        for (Object key : cols.keySet()) {
                            String colNm = (String) key;
                            JSONArray col = cols.getJSONArray(colNm);
                            for (int i = 0; i < col.size(); i++) {
                                JSONObject vl = col.getJSONObject(i);
                                Put put = new Put(rowKey);
                                byte[] value = NaviUtil.getByteVal(
                                    t.getClass(), colNm, vl.get("value"));
                                if (vl.containsKey("timestamp")) {
                                    put.add(cfNm.getBytes(), colNm.getBytes(),
                                        vl.getLong("timestamp"), value);
                                } else {
                                    put.add(cfNm.getBytes(), colNm.getBytes(),
                                        value);
                                }
                                puts.add(put);
                            }
                        }
                    }
                    hTable.put(puts);
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
            }
        }
    }

    /**
     * 批量保存
     *
     * @param objectsToSave
     *     要保存的Dto集合
     */
    public void insertAll(Collection<T> objectsToSave) {
        for (T dto : objectsToSave) {
            this.save(dto);
        }
    }

    /**
     * 删除操作
     *
     * @param t
     *     要删除的Dto对象
     */
    public void delete(T t) {
        String tableNm = getDtoTableNm(t.getClass());
        HTableInterface hTable = null;
        if (null != tableNm) {
            hTable = getHTable(tableNm);
        }
        if (hTable != null) {
            final T t_obj = t;
            NaviColumnExtDto obj = null;
            List<Delete> deletes = new ArrayList<Delete>();
            if (t_obj instanceof NaviColumnExtDto) {
                obj = (NaviColumnExtDto) t_obj;
            } else {
                if (t_obj instanceof NaviBaseColumnDto) {
                    obj = ((NaviBaseColumnDto) t_obj).toColumnExtDto();
                }
            }
            if (obj != null) {
                try {
                    // 遍历ColumnExtDto
                    JSONObject json = JSONObject.parseObject(obj.dataToString());
                    byte[] rowKey = obj.getRowkey();
                    for (String cfNm : json.keySet()) {
                        JSONObject cols = json.getJSONObject(cfNm);
                        for (Object key : cols.keySet()) {
                            String colNm = (String) key;
                            JSONArray col = cols.getJSONArray(colNm);
                            for (int i = 0; i < col.size(); i++) {
                                JSONObject vl = col.getJSONObject(i);
                                Delete delete = new Delete(rowKey);
                                if (vl.containsKey("timestamp")) {
                                    delete.deleteColumn(cfNm.getBytes(),
                                        colNm.getBytes(),
                                        vl.getLong("timestamp"));
                                } else {
                                    delete.deleteColumns(cfNm.getBytes(),
                                        colNm.getBytes());
                                }
                                deletes.add(delete);
                            }
                        }
                    }
                    hTable.delete(deletes);
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
            }
        }
    }

    public void delete(String tableNm, byte[] rowKey) {
        HTableInterface hTable = null;
        if (null != tableNm && null != rowKey) {
            hTable = getHTable(tableNm);
        }
        if (hTable != null) {
            Delete delete = new Delete(rowKey);
            try {
                hTable.delete(delete);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 查找一个对象
     *
     * @param scan
     *     查询条件配置
     * @param entityClass
     *     Dto对应的Class
     * @return 一个Dto结果
     */
    @SuppressWarnings("unchecked")
    public T findOne(Scan scan, Class<T> entityClass) {
        T t = null;
        String tableNm = getDtoTableNm(entityClass);
        HTableInterface hTable = null;
        if (null != tableNm) {
            hTable = getHTable(tableNm);
        }
        if (hTable != null) {
            NaviColumnExtDtoRowMapper mapper = new NaviColumnExtDtoRowMapper(
                this.vflag);
            ResultScanner scanner = null;
            try {
                scanner = hTable.getScanner(scan);
                Result result = scanner.next();
                if (null != result && !result.isEmpty()) {
                    return mapper.mapRow(result, 0, entityClass);
                } else {
                    return null;
                }
            } catch (Exception e) {
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
        }
        return t;
    }

    /**
     * 查找一 组对象
     *
     * @param scan
     *     查询条件配置
     * @param entityClass
     *     Dto对应的Class
     * @return 一组Dto结果
     */
    @SuppressWarnings("unchecked")
    public List<T> find(Scan scan, Class<T> entityClass) {
        List<T> list = null;
        String tableNm = getDtoTableNm(entityClass);
        HTableInterface hTable = null;
        if (null != tableNm) {
            hTable = getHTable(tableNm);
        }
        if (hTable != null) {
            NaviColumnExtDtoRowMapper mapper = new NaviColumnExtDtoRowMapper(
                this.vflag);
            ResultScanner scanner = null;
            try {
                scanner = hTable.getScanner(scan);
                Result result = scanner.next();
                if (null != result && !result.isEmpty()) {
                    list = new ArrayList<T>();
                    while (null != result && !result.isEmpty()) {
                        T t = null;
                        try {
                            t = mapper.mapRow(result, 0, entityClass);
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                        if (null != t) {
                            list.add(t);
                        }
                        result = scanner.next();
                    }
                    return list;
                } else {
                    return null;
                }
            } catch (Exception e) {
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
        }
        return list;
    }

    /**
     * 查找所有Dto的方法
     *
     * @param entityClass
     *     Dto对应的Class
     * @return 所有Dto的集合
     */
    public List<T> findAll(Class<T> entityClass) {
        Scan scan = new Scan();
        return find(scan, entityClass);
    }

    /**
     * 与insert方法一样，保存操作
     *
     * @param t
     *     要保存的Dto对象
     */
    public void save(T t) {
        this.insert(t);
    }

    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        if (dataSource != null) {
            // tmpt = new
            // NaviHBaseTemplateFactory(this.getDataSource()).getHBaseTemplate(tableNm);
        }
    }

    /**
     * 获取一行里，制定列族的所有列
     *
     * @param rowKey
     * @param families
     *     如果为null,则返回所有列族
     * @param entityClass
     * @return
     */
    @SuppressWarnings("unchecked")
    public T getRow(byte[] rowKey, String[] families, Class<T> entityClass) {
        String tableNm = getDtoTableNm(entityClass);
        HTableInterface hTable = getHTable(tableNm);
        if (null != hTable) {
            Get get = new Get(rowKey);
            if (null != families) {
                for (String family : families) {
                    get.addFamily(family.getBytes());
                }
            }
            try {
                Result result = hTable.get(get);
                if (null != result && !result.isEmpty()) {
                    NaviColumnExtDtoRowMapper mapper = new NaviColumnExtDtoRowMapper(
                        this.vflag);
                    T t = mapper.mapRow(result, 0, entityClass);
                    return t;
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
        }
        return null;
    }

    /**
     * 根据起始和终止的行，获取分页数据
     *
     * @param entityClass
     * @param startKey
     *     起始行（包含）
     * @param endKey
     *     结束行（包含）
     * @param page
     *     页码
     * @param pageLength
     *     每页长度
     * @return
     */
    public List<T> getPage(Class<T> entityClass, byte[] startKey,
                           byte[] endKey, int page, int pageLength) {
        if (page < 1 || pageLength < 1) {
            throw new NaviRuntimeException(
                "page or pageLength must be bigger than 1",
                NAVIERROR.BUSI_PARAM_ERROR.code());
        }
        T last = null;
        if (!Arrays.equals(startKey, endKey)) {
            last = getRow(endKey, null, entityClass);
        }
        Scan scan = null;
        if (null != startKey && null != endKey) {
            scan = new Scan(startKey, endKey);
        } else if (null != startKey) {
            scan = new Scan(startKey);
        } else if (null == startKey) {
            throw new NaviRuntimeException("startKey can't be null",
                NAVIERROR.BUSI_PARAM_ERROR.code());
        }
        List<T> list = find(scan, entityClass);
        if (null != last) {
            if (null == list) {
                list = new ArrayList<T>();
            }
            list.add(last);
        }
        // scan的结果不包含 endKey行，需要添加进去
        if (null != list && list.size() > 0) {
            int begin = pageLength * (page - 1);
            int end = pageLength * page > list.size() ? list.size()
                : pageLength * page;
            if (begin < list.size()) {
                return list.subList(begin, end);
            } else {
                int maxPage = list.size() % pageLength == 0 ? list.size()
                    / pageLength : list.size() / pageLength + 1;
                throw new NaviRuntimeException("error page:" + page
                    + ",max page:" + maxPage,
                    NAVIERROR.BUSI_PARAM_ERROR.code());
            }
        }
        return null;
    }

    /**
     * 获取startKey和endKey之间记录的数量，包含startKey和endKey。
     *
     * @param entityClass
     * @param startKey
     *     起始行（包含）
     * @param endKey
     *     终止行（包含）
     * @return
     */
    public int count(Class<T> entityClass, byte[] startKey, byte[] endKey) {
        if (null == entityClass || null == startKey || null == endKey) {
            throw new NaviRuntimeException("error param!",
                NAVIERROR.BUSI_PARAM_ERROR.code());
        }
        T last = null;
        if (!Arrays.equals(startKey, endKey)) {
            last = getRow(endKey, null, entityClass);
        }
        Scan scan = new Scan(startKey, endKey);
        List<T> list = find(scan, entityClass);
        if (null != last) {
            if (null == list) {
                list = new ArrayList<T>();
            }
            list.add(last);
        }
        if (null != list) {
            return list.size();
        } else {
            return 0;
        }
    }

    /**
     * 从Dto处获取数据库表名的方法
     *
     * @param entityClass
     * @return true 读取成功 <br/>
     * false 读取失败
     */
    private String getDtoTableNm(Class<? extends INaviColumnDto> entityClass) {
        if (entityClass != null) {
            TableInfo tbInfo = entityClass.getAnnotation(TableInfo.class);
            if (tbInfo != null) {
                String nm = tbInfo.name();
                if (!nm.trim().equals("")) {
                    return nm;
                }
            }
        }
        return null;
    }

    public HBaseAdmin getHBaseAdmin() {
        HBaseAdmin admin = null;
        if (dataSource != null) {
            NaviHBaseDriver driver = (NaviHBaseDriver) dataSource.getHandle();
            admin = driver.getAdmin();
        }
        if (null == admin) {
            throw new NaviRuntimeException("get admin error",
                NAVIERROR.SYSERROR.code());
        }
        return admin;
    }

    public boolean createTable(String tableName, String[] familys,
                               byte[][] splitKeys, int timeToLive) {
        if (dataSource != null) {
            NaviHBaseDriver driver = (NaviHBaseDriver) dataSource.getHandle();
            driver.createTable(tableName, familys, splitKeys, timeToLive);
            return true;
        }
        return false;
    }

    /**
     * 创建hbase名字空间
     *
     * @return
     */
    public boolean createNamespace(String nsName) {
        if (dataSource == null)
            return false;
        NaviHBaseDriver driver = (NaviHBaseDriver) dataSource.getHandle();
        return driver.createNamespace(nsName);
    }

    public boolean deleteTable(String tableName) {
        if (dataSource != null) {
            NaviHBaseDriver driver = (NaviHBaseDriver) dataSource.getHandle();
            driver.deleteTable(tableName);
            return true;
        }
        return false;
    }

    public NaviHBaseDriver getDriver() {
        if (dataSource != null) {
            NaviHBaseDriver driver = (NaviHBaseDriver) dataSource.getHandle();
            return driver;
        }
        return null;
    }
}

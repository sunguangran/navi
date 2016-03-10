package com.youku.java.navi.server.serviceobj;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import java.lang.reflect.Field;
import java.util.NavigableMap;

/**
 * 用于HbaseTemplate进行数据库操作时使用的Dto转换类
 *
 */
public class NaviColumnExtDtoRowMapper {
    private boolean vflag;

    /**
     * 带支持多版本数据标志的构造函数
     *
     * @param flag
     *     支持多版本数据标志
     */
    public NaviColumnExtDtoRowMapper(boolean flag) {
        this.vflag = flag;
    }

    public NaviColumnExtDtoRowMapper() {
        this.vflag = false; //默认无版本
    }

    public <T extends INaviColumnDto> T mapRow(Result result, int rowNum, Class<T> entityClass) throws Exception {
        byte[] rowKey = result.getRow();
        T dto = entityClass.newInstance();
        dto.setRowkey(rowKey);
        if (vflag) {
            NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> map = result.getMap();
            for (byte[] cfinfo : map.keySet()) {
                NavigableMap<byte[], NavigableMap<Long, byte[]>> cols = map.get(cfinfo);
                for (byte[] col : cols.keySet()) {
                    NavigableMap<Long, byte[]> colvs = cols.get(col);
                    for (Long timestamp : colvs.keySet()) {
                        byte[] value = colvs.get(timestamp);
                        if (NaviColumnExtDto.class.equals(entityClass)) {
                            ((NaviColumnExtDto) dto).add(Bytes.toString(cfinfo), Bytes.toString(col), timestamp, getFieldVal(entityClass, Bytes.toString(col), value));//Bytes.toString(value)
                        } else if (dto instanceof NaviBaseColumnDto) {
                            ((NaviBaseColumnDto) dto).setValue(Bytes.toString(col), getFieldVal(entityClass, Bytes.toString(col), value));
                        }
                    }
                }
            }
        } else {
            NavigableMap<byte[], NavigableMap<byte[], byte[]>> map = result.getNoVersionMap();
            for (byte[] cfinfo : map.keySet()) {
                NavigableMap<byte[], byte[]> cols = map.get(cfinfo);
                for (byte[] col : cols.keySet()) {
                    byte[] value = cols.get(col);
                    if (NaviColumnExtDto.class.equals(entityClass)) {
                        //NaviColumnExtDto 类型，字段值直接转换为 String
                        ((NaviColumnExtDto) dto).add(Bytes.toString(cfinfo), Bytes.toString(col), null, Bytes.toString(value));
                    } else if (dto instanceof NaviBaseColumnDto) {
                        //NaviBaseColumnDto 根据字段类型设值
                        ((NaviBaseColumnDto) dto).setValue(Bytes.toString(col), getFieldVal(entityClass, Bytes.toString(col), value));
                    }
                }
            }
        }
        return dto;
    }

    public <T extends INaviColumnDto> Object getFieldVal(Class<T> entityClass, String name, byte[] val) {
        Field[] fields = entityClass.getDeclaredFields();
        if (null != name && !"".equals(name) && null != val && null != fields) {
            for (Field field : fields) {
                if (name.equals(field.getName())) {
                    Class<?> type = field.getType();
                    if (type.equals(Short.class) || type.equals(short.class)) {
                        return Bytes.toShort(val);
                    } else if (type.equals(Integer.class) || type.equals(int.class)) {
                        return Bytes.toInt(val);
                    } else if (type.equals(Long.class) || type.equals(long.class)) {
                        return Bytes.toLong(val);
                    } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
                        return Bytes.toBoolean(val);
                    } else if (type.equals(Double.class) || type.equals(double.class)) {
                        return Bytes.toDouble(val);
                    } else if (type.equals(Float.class) || type.equals(float.class)) {
                        return Bytes.toFloat(val);
                    } else {
                        //没匹配按String处理
                        return Bytes.toString(val);
                    }
                }
            }
            return null;
        } else {
            return null;
        }
    }
}

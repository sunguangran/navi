package com.cuckoo.framework.navi.utils;

import com.cuckoo.framework.navi.common.NAVIERROR;
import com.cuckoo.framework.navi.common.NaviSystemException;
import com.cuckoo.framework.navi.serviceobj.AbstractNaviDto;
import com.cuckoo.framework.navi.serviceobj.INaviColumnDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.hadoop.hbase.util.Bytes;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Iterator;

@Slf4j
public class NaviUtil {

    /**
     * 使用唯一索引且有数值的属性作为检索条件
     *
     * @return
     * @throws JSONException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    public static <T extends AbstractNaviDto> Criteria buildCriteria(T dto)
        throws JSONException, InstantiationException,
        IllegalAccessException, ClassNotFoundException, SecurityException,
        IllegalArgumentException, NoSuchMethodException,
        InvocationTargetException {
        CompoundIndexes compIndexes = dto.getClass().getAnnotation(
            CompoundIndexes.class);
        AbstractNaviDto initDto = (AbstractNaviDto) Class.forName(
            dto.getClass().getName(), true,
            dto.getClass().getClassLoader()).newInstance();
        Criteria c = null;
        for (CompoundIndex compIndex : compIndexes.value()) {
            if (!compIndex.unique()) {
                continue;
            }
            JSONObject fields = new JSONObject(compIndex.def());

            @SuppressWarnings("unchecked")
            Iterator<String> it = fields.keys();
            while (it.hasNext()) {
                String fnm = it.next();
                Object conditionValue = dto.getValue(fnm);
                if (conditionValue == null
                    || conditionValue.equals(initDto.getValue(fnm))) {
                    break;
                }
                if (c == null) {
                    c = new Criteria();
                }
                c.and(fnm).is(conditionValue);
            }
            if (c != null) {
                return c;
            }
        }
        return c;
    }

    /**
     * 除@Id字段外，其他字段则更新
     *
     * @param <T>
     * @param dto
     * @return
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     */
    public static <T extends AbstractNaviDto> Update buildUpdate(T dto)
        throws SecurityException, IllegalArgumentException,
        NoSuchMethodException, IllegalAccessException,
        InvocationTargetException, InstantiationException,
        ClassNotFoundException {
        Field[] fields = dto.getClass().getDeclaredFields();
        AbstractNaviDto initDto = (AbstractNaviDto) Class
            .forName(dto.getClass().getName(), true,
                dto.getClass().getClassLoader()).newInstance();
        Update up = new Update();
        for (Field field : fields) {
            String fnm = field.getName();
            int finalNo = field.getModifiers() & Modifier.FINAL;
            int staticNo = field.getModifiers() & Modifier.STATIC;
            int transitNo = field.getModifiers() & Modifier.TRANSIENT;
            if (finalNo != 0 || staticNo != 0 || transitNo != 0
                || field.isAnnotationPresent(Id.class)) {
                continue;
            }
            Object newValue = dto.getValue(fnm);
            if (newValue == null || newValue.equals(initDto.getValue(fnm))) {
                continue;
            }
            up.set(fnm, newValue);
        }
        return up;
    }

    public static <T extends AbstractNaviDto> JSONObject toJSONObject(T dto)
        throws SecurityException, IllegalArgumentException, JSONException,
        NoSuchMethodException, IllegalAccessException,
        InvocationTargetException {
        Field[] fields = dto.getClass().getDeclaredFields();
        JSONObject json = new JSONObject();
        constructJsonObject(dto, fields, json);
        fields = dto.getClass().getSuperclass().getDeclaredFields();
        constructJsonObject(dto, fields, json);
        return json;
    }

    public static <T extends AbstractNaviDto> void constructJsonObject(T dto, Field[] fields,
                                                                       JSONObject json) throws JSONException, NoSuchMethodException,
        IllegalAccessException, InvocationTargetException {
        for (Field field : fields) {
            String fnm = field.getName();
            int finalNo = field.getModifiers() & Modifier.FINAL;
            int staticNo = field.getModifiers() & Modifier.STATIC;
//			int transitNo = field.getModifiers() & Modifier.TRANSIENT;
            boolean mongoId = field.getType().equals(ObjectId.class);
            if (finalNo != 0 || staticNo != 0 || mongoId) {
                continue;
            }
            json.put(fnm, dto.getValue(fnm));
        }
    }

    public static NaviSystemException transferToNaviSysException(Exception e) {
        NaviSystemException ee = new NaviSystemException(e.getMessage(), NAVIERROR.SYSERROR.code(), e);
        ee.setStackTrace(e.getStackTrace());
        return ee;
    }

    /**
     * 将对象转换为字节数组（序列化）
     *
     * @param <T>
     * @param obj
     * @return
     * @throws IOException
     */
    public static <T extends Serializable> byte[] getObjectByteArray(T obj) throws IOException {
        ByteArrayOutputStream bo = null;
        ObjectOutputStream oo = null;
        byte[] array = null;
        try {
            bo = new ByteArrayOutputStream();
            oo = new ObjectOutputStream(bo);
            oo.writeObject(obj);
            array = bo.toByteArray();
        } catch (IOException e) {
            throw e;
        } finally {
            if (oo != null) {
                oo.close();
            }
            if (bo != null) {
                bo.close();
            }
        }
        return array;
    }

    /**
     * 将字节数组转换为对象（反序列化）
     *
     * @param bytes
     * @return
     * @throws Exception
     */
    public static Object byteToObject(byte[] bytes) throws Exception {
        Object obj = null;
        ByteArrayInputStream bi = null;
        ObjectInputStream oi = null;
        try {
            // bytearray to object
            bi = new ByteArrayInputStream(bytes);
            oi = new ObjectInputStream(bi);
            obj = oi.readObject();
        } catch (Exception e) {
            throw e;
        } finally {
            if (null != oi) {
                oi.close();
            }
            if (null != bi) {
                bi.close();
            }
        }
        return obj;
    }

    public static <T extends INaviColumnDto> byte[] getByteVal(Class<T> entityClass, String name, Object val) {
        if (null == entityClass || null == name) {
            return getByteVal(val);
        }
        Field[] fields = entityClass.getDeclaredFields();
        if (!"".equals(name) && null != val && null != fields) {
            for (Field field : fields) {
                if (name.equals(field.getName())) {
                    Class<?> type = field.getType();
                    if (type.equals(Short.class) || type.equals(short.class)) {
                        return Bytes.toBytes(Short.parseShort(val.toString()));
                    } else if (type.equals(Integer.class) || type.equals(int.class)) {
                        return Bytes.toBytes(Integer.parseInt(val.toString()));
                    } else if (type.equals(Long.class) || type.equals(long.class)) {
                        return Bytes.toBytes(Long.parseLong(val.toString()));
                    } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
                        return Bytes.toBytes(Boolean.parseBoolean(val.toString()));
                    } else if (type.equals(Double.class) || type.equals(double.class)) {
                        return Bytes.toBytes(Double.parseDouble(val.toString()));
                    } else if (type.equals(Float.class) || type.equals(float.class)) {
                        return Bytes.toBytes(Float.parseFloat(val.toString()));
                    } else {
                        //没匹配按String处理
                        return Bytes.toBytes(val.toString());
                    }
                }
            }
            return null;
        } else {
            return null;
        }
    }

    public static byte[] getByteVal(Object val) {
        if (null != val) {
            if (val instanceof Short) {
                return Bytes.toBytes((Short) val);
            } else if (val instanceof Integer) {
                return Bytes.toBytes((Integer) val);
            } else if (val instanceof Long) {
                return Bytes.toBytes((Long) val);
            } else if (val instanceof Double) {
                return Bytes.toBytes((Double) val);
            } else if (val instanceof Float) {
                return Bytes.toBytes((Float) val);
            } else if (val instanceof BigDecimal) {
                return Bytes.toBytes(((BigDecimal) val).doubleValue());
            } else {
                return Bytes.toBytes(val.toString());
            }
        }
        return null;
    }
}

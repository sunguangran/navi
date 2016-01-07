package com.cuckoo.framework.navi.serviceobj;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.hadoop.hbase.util.Bytes;

import java.lang.reflect.Field;

/**
 * 面向列式存储DB的数据存储对象DTO，可支持灵活的列扩展
 */
public class NaviColumnExtDto implements INaviColumnDto {
    private byte[] rowkey;
    private JSONObject columns;

    /**
     * 获取rowKey
     *
     * @return rowKey的值
     */
    public byte[] getRowkey() {
        return rowkey;
    }

    /**
     * 设置rowKey
     *
     * @param rowkey
     *     要设置的值
     */
    public void setRowkey(byte[] rowkey) {
        this.rowkey = rowkey;
    }

    /**
     * 根据指定参数插入值
     *
     * @param cf
     *     列族名
     * @param column
     *     列名
     * @param timestamp
     *     时间戳
     * @param value
     *     值
     * @return true 插入成功<br/>
     * false 插入失败
     */
    public boolean add(String cf, String column, Long timestamp, Object value) {
        boolean flag = false;
        if (this.columns == null) {
            this.columns = new JSONObject();
        }
        try {
            JSONObject cfObj = null;
            if (!columns.containsKey(cf)) { // 空值
                JSONArray cArr = new JSONArray();

                JSONObject cValue = new JSONObject();
                if (timestamp != null) {
                    cValue.put("timestamp", timestamp);
                }
                cValue.put("value", value);
                cArr.add(cValue);
                cfObj = new JSONObject();
                cfObj.put(column, cArr);
                flag = true;
                this.columns.put(cf, cfObj);
            } else {
                cfObj = columns.getJSONObject(cf);

                JSONArray cvArr = null;
                if (cfObj.containsKey(column)) {
                    // 找到列
                    cvArr = cfObj.getJSONArray(column);
                    JSONObject cvobj = null;
                    if (timestamp != null) {
                        // 插入数据带timestamp
                        for (int cvi = 0; cvi < cvArr.size(); cvi++) {
                            JSONObject cvobj_p = cvArr.getJSONObject(cvi);
                            if (cvobj_p.containsKey("timestamp")) {
                                if (cvobj_p.getString("timestamp").equals(timestamp)) {
                                    cvArr.remove(cvi);
                                    cvobj = new JSONObject();
                                    cvobj.put("timestamp", timestamp);
                                    cvobj.put("value", value);
                                    cvArr.add(cvi, cvobj);
                                    flag = true;
                                    break;
                                }
                            }
                            if ((cvi == cvArr.size() - 1) && flag == false) {
                                cvobj = new JSONObject();
                                if (timestamp != null) {
                                    cvobj.put("timestamp", timestamp);
                                }
                                cvobj.put("value", value);
                                cvArr.add(cvobj);
                                flag = true;
                            }

                        }
                    } else {
                        // 插入数据没有timestamp
                        for (int cvi = 0; cvi < cvArr.size(); cvi++) {
                            JSONObject cvobj_p = cvArr.getJSONObject(cvi);
                            if (!cvobj_p.containsKey("timestamp")) {
                                cvArr.remove(cvi);
                                cvobj = new JSONObject();
                                cvobj.put("value", value);
                                cvArr.add(cvi, cvobj);
                                flag = true;
                                break;
                            } else {
                                if (cvi == cvArr.size() - 1) {
                                    cvobj = new JSONObject();
                                    cvobj.put("value", value);
                                    cvArr.add(cvobj);
                                    flag = true;
                                    break;
                                }
                            }
                        }
                    }

                } else {
                    // 没有找到列
                    cvArr = new JSONArray();
                    JSONObject cValue = new JSONObject();
                    if (timestamp != null) {
                        cValue.put("timestamp", timestamp);
                    }
                    cValue.put("value", value);
                    cvArr.add(cValue);
                    cfObj.put(column, cvArr);
                    flag = true;
                }
                this.columns.put(cf, cfObj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 使用列族名和列名获得DTO中的结果
     *
     * @param cf
     *     列族名
     * @param column
     *     列名
     * @return 返回对应的查询结果
     */
    public JSONArray getValue(String cf, String column) {
        JSONArray resArr = new JSONArray();
        if (this.columns.containsKey(cf)) {
            JSONObject cfObj = this.columns.getJSONObject(cf);
            if (cfObj.containsKey(column)) {
                resArr = cfObj.getJSONArray(column);
                ;
            }
        }
        return resArr;
    }

    /**
     * 使用列族名，列名和时间戳获得DTO中的结果
     *
     * @param cf
     *     列族名
     * @param column
     *     列名
     * @param timestamp
     *     时间戳
     * @return 对应的查询结果
     */
    public JSONArray getValue(String cf, String column, Long timestamp) {
        JSONArray resArr = new JSONArray();
        if (timestamp == null)
            return getValue(cf, column);
        if (this.columns.containsKey(cf)) {
            JSONObject cfObj = this.columns.getJSONObject(cf);
            if (cfObj.containsKey(column)) {
                JSONArray cvArr = cfObj.getJSONArray(column);
                for (int cvi = 0; cvi < cvArr.size(); cvi++) {
                    JSONObject cv = cvArr.getJSONObject(cvi);
                    if (cv.containsKey("timestamp")) {
                        if (cv.get("timestamp").equals(timestamp)) {
                            resArr.add(cv);
                        }
                    }
                }
            }
        }
        return resArr;
    }

    /**
     * 使用列族名获得DTO中的结果
     *
     * @param cf
     *     列族名
     * @return 对应的查询结果
     */
    public JSONArray getValue(String cf) {
        JSONArray resArr = new JSONArray();
        if (this.columns.containsKey(cf)) {
            JSONObject cfObj = this.columns.getJSONObject(cf);
            for (Object column : cfObj.keySet()) {
                JSONObject col = new JSONObject();
                JSONArray colarr = cfObj.getJSONArray((String) column);
                col.put((String) column, colarr);
                resArr.add(col);
            }
        }
        return resArr;
    }

    public String toString() {
        return "rowKey:" + Bytes.toString(this.getRowkey()) + " "
            + this.columns.toString();
    }

    /**
     * 用于返回数据的json形式字符串，不可被重载
     *
     * @return json形式字符串
     */
    public final String dataToString() {
        return this.columns.toString();
    }

    /**
     * 传统列式Dto的转换方法
     *
     * @param clazz
     *     传统列式Dto的Class
     * @return 传统列式Dto对象
     */
    public <T extends INaviColumnDto> T toBaseDto(Class<T> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        T obj = null;
        try {
            obj = clazz.newInstance();
            for (Field field : fields) {
                DtoInfo anatation = field.getAnnotation(DtoInfo.class);
                if (anatation != null) {
                    String cf = anatation.cf();
                    String column = anatation.column();
                    if (cf.equals("")) {
                        continue;
                    } else {
                        if (column.equals("")) {
                            column = field.getName();
                        }
                        JSONArray array = this.getValue(cf, column);
                        Object value = (array.size() > 0) ? array.getJSONObject(0).get("value") : null;
                        if (null != value && !"".equals(value)) {
                            ((NaviBaseColumnDto) obj).setValue(field.getName(), value);
                        }
                    }
                }
            }
            obj.setRowkey(this.rowkey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

}

package com.youku.java.navi.engine.datasource.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.youku.java.navi.common.NaviError;
import com.youku.java.navi.common.exception.NaviSystemException;
import com.youku.java.navi.engine.core.IHiveClient;
import com.youku.java.navi.engine.core.INaviDriver;
import com.youku.java.navi.engine.datasource.driver.NaviHiveClientDriver;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.Schema;
import org.apache.hadoop.hive.service.HiveClient;
import org.apache.hadoop.hive.service.HiveServerException;
import org.apache.thrift.TException;

import java.util.List;

public class NaviHiveClientService extends AbstractNaviDataService implements IHiveClient {
    private String result_split = "\t";

    protected NaviHiveClientDriver getDriver() {
        INaviDriver driver = dataSource.getHandle();
        if (driver instanceof NaviHiveClientDriver) {
            return (NaviHiveClientDriver) driver;
        }
        driver.close();
        throw new NaviSystemException("the driver is invalid!",
            NaviError.SYSERROR);
    }

    public JSONArray doQuery(String sql) throws HiveServerException, TException {
        NaviHiveClientDriver driver = getDriver();
        HiveClient client = driver.getClient();
        client.execute(sql);
        JSONArray array = getResult(client.getSchema(), client.fetchAll());
        driver.close();
        return array;
    }

    public void doQuery(String sql, int numRows, IResultCallback call)
        throws HiveServerException, TException {
        NaviHiveClientDriver driver = getDriver();
        HiveClient client = driver.getClient();
        client.execute(sql);
        List<String> result = client.fetchN(numRows);
        while (null != result && result.size() > 0) {
            call.handleResult(getResult(client.getSchema(), result));
            result = client.fetchN(numRows);
        }
        driver.close();
    }

    public JSONArray getResult(Schema schema, List<String> values) {
        if (null == schema || null == values || values.size() < 1) {
            return null;
        }
        JSONArray array = new JSONArray();
        List<FieldSchema> fields = schema.getFieldSchemas();
        for (String row : values) {
            String[] fieldVals = null;
            if (row.contains(result_split)) {
                fieldVals = row.split(result_split);
            } else {
                fieldVals = new String[]{row};
            }
            if (fieldVals.length == fields.size()) {
                JSONObject obj = new JSONObject();
                for (int i = 0; i < fieldVals.length; i++) {
                    FieldSchema field = fields.get(i);
                    obj.put(field.getName(), fieldVals[i]);
                }
                array.add(obj);
            }
        }
        return array;
    }

    public String getResult_split() {
        return result_split;
    }

    public void setResult_split(String result_split) {
        this.result_split = result_split;
    }

}

package com.youku.java.navi.engine.core;

import com.alibaba.fastjson.JSONArray;
import org.apache.hadoop.hive.service.HiveServerException;
import org.apache.thrift.TException;


public interface IHiveClient {

    public interface IResultCallback {
        void handleResult(JSONArray array);
    }

    JSONArray doQuery(String sql) throws HiveServerException, TException;

    void doQuery(String sql, int numRows, IResultCallback call) throws HiveServerException, TException;

}

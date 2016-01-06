package com.cuckoo.framework.navi.engine.core;

import org.apache.hadoop.hive.service.HiveServerException;
import org.apache.thrift.TException;
import org.json.JSONArray;


public interface IHiveClient {

    public interface IResultCallback {
        void handleResult(JSONArray array);
    }

    JSONArray doQuery(String sql) throws HiveServerException, TException;

    void doQuery(String sql, int numRows, IResultCallback call) throws HiveServerException, TException;

}

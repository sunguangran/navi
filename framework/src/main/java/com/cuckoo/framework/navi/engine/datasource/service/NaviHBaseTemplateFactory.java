package com.cuckoo.framework.navi.engine.datasource.service;

import com.cuckoo.framework.navi.engine.core.INaviDataSource;
import com.cuckoo.framework.navi.engine.datasource.NaviHBaseDataSource;
import org.springframework.data.hadoop.hbase.HbaseTemplate;

import java.util.HashMap;
import java.util.Map;

public class NaviHBaseTemplateFactory {

    private INaviDataSource dataSource;
    private Map<String, HbaseTemplate> tmptMap;

    public INaviDataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(INaviDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public NaviHBaseTemplateFactory(INaviDataSource dataSource) {
        this.dataSource = dataSource;
        tmptMap = new HashMap<String, HbaseTemplate>();
    }

    public HbaseTemplate getHBaseTemplate(String table) {
        if (tmptMap != null) {
            synchronized (this) {
                if (!tmptMap.containsKey(table)) {
                    //生成一个template
                    HbaseTemplate tmpt = new HbaseTemplate(((NaviHBaseDataSource) this.dataSource).getConfig());
                    tmptMap.put(table, tmpt);
                    return tmpt;
                } else {
                    return tmptMap.get(table);
                }
            }
        }
        return null;
    }

}

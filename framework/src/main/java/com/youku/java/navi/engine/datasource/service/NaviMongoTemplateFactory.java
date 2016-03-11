package com.youku.java.navi.engine.datasource.service;

import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.youku.java.navi.engine.core.INaviDataSource;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class NaviMongoTemplateFactory {

    private INaviDataSource dataSource;
    private Map<String, MongoTemplate> tmptMap = new HashMap<String, MongoTemplate>();

    public NaviMongoTemplateFactory(INaviDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public MongoTemplate getMongoTemplate(String database)
        throws UnknownHostException, MongoException {
        if (!tmptMap.containsKey(database)) {
            synchronized (this) {
                if (!tmptMap.containsKey(database)) {
                    NaviMongoDbFactory dbFactory = new NaviMongoDbFactory(
                        dataSource, database);
                    MongoTemplate template = new MongoTemplate(dbFactory,
                        getDefaultMongoConverter(dbFactory));
                    template.setWriteConcern(WriteConcern.SAFE);
                    tmptMap.put(database, template);
                }
            }
        }
        return tmptMap.get(database);

    }

    private MongoConverter getDefaultMongoConverter(MongoDbFactory factory) {
        MappingMongoConverter converter = new MappingMongoConverter(factory,
            new MongoMappingContext());
        converter.afterPropertiesSet();
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return converter;
    }

}

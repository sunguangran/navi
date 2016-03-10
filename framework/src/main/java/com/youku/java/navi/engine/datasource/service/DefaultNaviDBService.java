package com.youku.java.navi.engine.datasource.service;

import com.youku.java.navi.engine.core.INaviDB;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Collection;
import java.util.List;

public class DefaultNaviDBService extends AbstractNaviDataService implements
    INaviDB {

    public <T> void insert(T t) {
        // TODO Auto-generated method stub

    }

    public <T> void insertAll(Collection<T> objectsToSave) {
        // TODO Auto-generated method stub

    }

    public <T> void delete(T t) {
        // TODO Auto-generated method stub

    }

    public void delete(Query query, String tableNm) {
        // TODO Auto-generated method stub

    }

    public <T> void delete(Query query, Class<T> entityClass) {
        // TODO Auto-generated method stub

    }

    public <T> T findOne(Query query, Class<T> entityClass) {
        // TODO Auto-generated method stub
        return null;
    }

    public <T> List<T> find(Query query, Class<T> entityClass) {
        // TODO Auto-generated method stub
        return null;
    }

    public <T> List<T> findAll(Class<T> entityClass) {
        // TODO Auto-generated method stub
        return null;
    }

    public <T> T findById(Object idObj, Class<T> entityClass) {
        // TODO Auto-generated method stub
        return null;
    }

    public <T> boolean upsert(Query query, Update update, Class<T> entityClass) {
        // TODO Auto-generated method stub
        return false;
    }

    public <T> boolean updateFirst(Query query, Update update,
                                   Class<T> entityClass) {
        // TODO Auto-generated method stub
        return false;
    }

    public <T> int updateMulti(Query query, Update update, Class<T> entityClass) {
        // TODO Auto-generated method stub
        return 0;
    }

    public <T> long count(Query query, Class<T> entityClass) {
        // TODO Auto-generated method stub
        return 0;
    }

    public <T> T findAndModify(Query query, Update update, Class<T> entityClass) {
        // TODO Auto-generated method stub
        return null;
    }

    public <T> T findAndRemove(Query query, Class<T> entityClass) {
        // TODO Auto-generated method stub
        return null;
    }

    public <T> void save(T t) {
        // TODO Auto-generated method stub

    }

    public <T> MongoPersistentProperty getIdProperties(Class<T> classNm) {
        // TODO Auto-generated method stub
        return null;
    }


}

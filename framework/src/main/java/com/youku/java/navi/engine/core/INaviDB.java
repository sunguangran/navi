package com.youku.java.navi.engine.core;

import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Collection;
import java.util.List;

public interface INaviDB extends IBaseDataService {
    /**
     * 插入操作，插入Dto类型对象
     *
     * @param <T>
     * @param t
     */
    <T> void insert(T t);

    /**
     * 批量插入操作
     *
     * @param <T>
     * @param objectsToSave
     */
    <T> void insertAll(Collection<T> objectsToSave);

    /**
     * 删除操作
     *
     * @param <T>
     * @param t
     */
    <T> void delete(T t);

    /**
     * 删除操作，指定表名
     *
     * @param query
     * @param tableNm
     */
    void delete(Query query, String tableNm);

    <T> void delete(Query query, Class<T> entityClass);

    <T> T findOne(Query query, Class<T> entityClass);

    <T> List<T> find(Query query, Class<T> entityClass);

    <T> List<T> findAll(Class<T> entityClass);

    <T> T findById(Object idObj, Class<T> entityClass);

    <T> boolean upsert(Query query, Update update, Class<T> entityClass);

    <T> boolean updateFirst(Query query, Update update, Class<T> entityClass);

    <T> int updateMulti(Query query, Update update, Class<T> entityClass);

    <T> long count(Query query, Class<T> entityClass);

    <T> T findAndModify(Query query, Update update, Class<T> entityClass);

    <T> T findAndRemove(Query query, Class<T> entityClass);

    <T> void save(T t);

    <T> MongoPersistentProperty getIdProperties(Class<T> classNm);
}

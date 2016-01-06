package com.cuckoo.framework.navi.engine.core;

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
    public abstract <T> void insert(T t);

    /**
     * 批量插入操作
     *
     * @param <T>
     * @param objectsToSave
     */
    public abstract <T> void insertAll(Collection<T> objectsToSave);

    /**
     * 删除操作
     *
     * @param <T>
     * @param t
     */
    public abstract <T> void delete(T t);

    /**
     * 删除操作，指定表名
     *
     * @param query
     * @param tableNm
     */
    public abstract void delete(Query query, String tableNm);

    public abstract <T> void delete(Query query, Class<T> entityClass);

    public abstract <T> T findOne(Query query, Class<T> entityClass);

    public abstract <T> List<T> find(Query query, Class<T> entityClass);

    public abstract <T> List<T> findAll(Class<T> entityClass);

    public abstract <T> T findById(Object idObj, Class<T> entityClass);

    public abstract <T> boolean upsert(Query query, Update update,
                                       Class<T> entityClass);

    public abstract <T> boolean updateFirst(Query query, Update update,
                                            Class<T> entityClass);

    public abstract <T> int updateMulti(Query query, Update update,
                                        Class<T> entityClass);

    public abstract <T> long count(Query query, Class<T> entityClass);

    public abstract <T> T findAndModify(Query query, Update update,
                                        Class<T> entityClass);

    public abstract <T> T findAndRemove(Query query, Class<T> entityClass);

    public abstract <T> void save(T t);

    public <T> MongoPersistentProperty getIdProperties(Class<T> classNm);
}

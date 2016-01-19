package com.cuckoo.framework.navi.engine.component;

import com.cuckoo.framework.navi.common.exception.NaviUnSupportedOperationException;
import com.cuckoo.framework.navi.engine.core.*;
import com.cuckoo.framework.navi.serviceobj.AbstractNaviBean;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class MongoDataObjectCom implements IMongoDataObjectCom {

    private INaviCache cacheService;
    private INaviDB dbService;
    private int expire_time = 900;

    public <T extends AbstractNaviBean> IDataObjectCom<T> getDataObjectCom(Query query, String cacheKey, Class<T> dtoClass) {
        return new DataObjectCom<T>(query, cacheKey, dtoClass);
    }

    public <T extends AbstractNaviBean> IListDataObjectCom<T> getListDataObjectCom(Query query, Class<T> dtoClass, String cacheKey) {
        return new ListDataObjectCom<T>(query, dtoClass, cacheKey);
    }

    protected class DataObjectCom<T extends AbstractNaviBean> implements IDataObjectCom<T> {
        protected Query query;
        protected Class<T> dtoClass;
        protected String cacheKey;

        public DataObjectCom(Query query, String cacheKey, Class<T> dtoClass) {
            this.query = query;
            this.cacheKey = cacheKey;
            this.dtoClass = dtoClass;
        }

        public T get() {
            String nullKey = getNullKey();
            if (null == cacheKey && cacheService.exists(nullKey)) {
                //存在nullkey(已经查询过)，并且 cachekey 为空，说明没数据。
                cacheService.expire(nullKey, expire_time);
                return null;
            }
            if (cacheService.exists(cacheKey)) {
                cacheService.expire(cacheKey, expire_time);
                return cacheService.get(cacheKey, dtoClass);
            } else {
                return loadFromDB();
            }
        }

        public T loadFromDB() {
            T dto = dbService.findOne(query, dtoClass);
            if (null == dto) {
                String nullKey = getNullKey();
                cacheService.setex(nullKey, 1, expire_time);
                return null;
            } else {
                cacheService.setex(cacheKey, dto, expire_time);
                return dto;
            }

        }

        public T refresh() {
            cacheService.delete(cacheKey);
            cacheService.delete(getNullKey());
            return loadFromDB();
        }

        public void deleteCache() {
            cacheService.delete(getNullKey());
            cacheService.delete(cacheKey);
        }

        public void deleteDB() {
            dbService.delete(query, dtoClass);
        }

        public String getNullKey() {
            return cacheKey + "_isNull";
        }
    }

    protected class ListDataObjectCom<T extends AbstractNaviBean> implements IListDataObjectCom<T> {
        protected Query query;
        protected Class<T> dtoClass;
        protected String cacheKey;

        public ListDataObjectCom(Query query, Class<T> dtoClass, String cacheKey) {
            this.query = query;
            this.dtoClass = dtoClass;
            this.cacheKey = cacheKey;
        }

        public List<T> getListData() {
            String nullKey = getNullKey();
            if (null != nullKey && cacheService.exists(nullKey)) {
                cacheService.expire(nullKey, expire_time);
                return null;
            }
            if (cacheService.exists(cacheKey)) {
                cacheService.expire(cacheKey, expire_time);
                return cacheService.lGetRange(cacheKey, 0, -1, dtoClass);
            } else {
                return loadFromDB();
            }
        }

        public List<T> loadFromDB() {
            List<T> list = dbService.find(query, dtoClass);
            if (null == list || list.size() == 0) {
                String nullKey = getNullKey();
                cacheService.setex(nullKey, 1, expire_time);
                return null;
            }
            cacheService.rPush(cacheKey, list.toArray());
            cacheService.expire(cacheKey, expire_time);
            return list;
        }

        public List<T> refreshList() {
            cacheService.delete(getNullKey());
            cacheService.delete(cacheKey);
            return loadFromDB();
        }

        public void deleteCache() {
            cacheService.delete(getNullKey());
            cacheService.delete(cacheKey);
        }

        public void deleteDB() {
            dbService.delete(query, dtoClass);
        }

        public int size() {
            String nullKey = getNullKey();
            if (null != nullKey && cacheService.exists(nullKey)) {
                cacheService.expire(nullKey, expire_time);
                return 0;
            }
            if (cacheService.exists(cacheKey)) {
                cacheService.expire(cacheKey, expire_time);
                return cacheService.lSize(cacheKey).intValue();
            } else {
                loadFromDB();
                return cacheService.lSize(cacheKey).intValue();
            }
        }

        public List<T> getIndex(int... indexs) {
            String nullKey = getNullKey();
            if (null != nullKey && cacheService.exists(nullKey)) {
                cacheService.expire(nullKey, expire_time);
                return null;
            }
            List<T> list = null;
            if (!cacheService.exists(cacheKey)) {
                loadFromDB();
            }
            if (cacheService.lSize(cacheKey) > 0) {
                list = new ArrayList<T>();
                for (int i = 0; i < indexs.length; i++) {
                    T dto = cacheService.lIndex(cacheKey, indexs[i], dtoClass);
                    list.add(dto);
                }
            }
            return list;
        }

        public String getNullKey() {
            return cacheKey + "_isNull";
        }

        public T get() {
            throw new NaviUnSupportedOperationException();
        }

        public T refresh() {
            throw new NaviUnSupportedOperationException();
        }
    }

}

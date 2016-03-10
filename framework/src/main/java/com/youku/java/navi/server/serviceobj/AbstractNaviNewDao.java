package com.youku.java.navi.server.serviceobj;

import com.youku.java.navi.engine.core.INaviCache;
import com.youku.java.navi.engine.core.INaviDB;
import com.youku.java.navi.utils.NaviUtil;
import com.youku.java.navi.engine.core.INaviZookeeper;
import com.youku.java.navi.engine.core.IZookeeperEventHander;
import org.apache.zookeeper.WatchedEvent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

public abstract class AbstractNaviNewDao<T extends AbstractNaviDto> implements INaviDao<T>, IZookeeperEventHander, InitializingBean {

    protected INaviDB dbService;
    protected INaviCache cacheService;
    protected INaviZookeeper csService;
    protected Class<T> classNm;

    protected AbstractNaviNewDao(Class<T> classNm) {
        this.classNm = classNm;
    }

    public void save(T t) throws Exception {
        dbService.save(t);
//		if (cacheService != null && getIdValue(t) != null) {
//			set(t);
//		}
    }

    /**
     * 构建缓存的key
     *
     * @param keyComponents
     *     构建key元素
     * @return
     */
    public abstract String buildKey(String... keyComponents);

    /**
     * 获得缓存过期时间
     *
     * @return
     */
    public abstract int getExpire();

    public void insert(T t) throws Exception {
        dbService.insert(t);
//		if (cacheService != null && getIdValue(t) != null) {
//			set(t);
//		}
    }

//	private Object getIdValue(T dto) throws Exception {
//		return dto.getValue(dbService.getIdProperties(classNm).getName());
//	}

    public T findOne(T t) throws Exception {
        T tmpt = get(buildKey(t.getOId()));
        if (tmpt != null) {
            return tmpt;
        }
        T dto = _findOne(t);
        if (dto != null) {
            set(dto);
        }
        return dto;
    }

    public boolean exists(T t) throws Exception {
        if (exists(buildKey(t.getOId()))) {
            return true;
        }
        return _findOne(t) != null;
    }

    protected boolean exists(String key) {
        return cacheService.exists(key);
    }

    protected T _findOne(T t) throws Exception {
        List<T> list = _find(NaviUtil.buildCriteria(t));
        return list.size() > 0 ? list.get(0) : null;
    }

    protected List<T> _find(Criteria criteria) {
        return (List<T>) dbService.find(new Query(criteria), classNm);
    }

    public void batchInsert(List<T> list) {
        dbService.insertAll(list);
    }

    public boolean update(T dto) throws Exception {
        if (!_update(dto)) {
            return false;
        }
        if (dto != null && cacheService != null
            && cacheService.exists(buildKey(dto.getOId()))) {
            set(dto);
        }
        return true;
    }

    protected boolean _update(T dto) throws Exception {
        Criteria criteria = NaviUtil.buildCriteria(dto);
        if (criteria == null) {
            return false;
        }
        Update update = NaviUtil.buildUpdate(dto);
        if (update.getUpdateObject().keySet().size() == 0) {
            return false;
        }
        return dbService.upsert(new Query(criteria), update, classNm);
    }

    /**
     * 缓存一个对象
     *
     */
    protected void set(T t) {
        if (cacheService == null) {
            return;
        }
        cacheService.setex(buildKey(t.getOId()), t, getExpire());
    }

    /**
     * 获取缓存的对象
     *
     * @param key
     * @return
     */
    protected T get(String key) {
        if (cacheService == null) {
            return null;
        }
        return cacheService.get(key, getDtoClass());
    }

    public Class<T> getDtoClass() {
        return classNm;
    }

    public void setDbService(INaviDB dbService) {
        this.dbService = dbService;
    }

    public void setCacheService(INaviCache cacheService) {
        this.cacheService = cacheService;
    }

    public void setCsService(INaviZookeeper csService) {
        if (csService != null) {
            csService.setCSEventHandler(this);
            this.csService = csService;
        }
    }

    public void processForNode(WatchedEvent e) {
        //子类重写功能
    }

    public void afterPropertiesSet() throws Exception {

    }

    public void registWatch() {
        // TODO Auto-generated method stub

    }
}

package com.youku.java.navi.dao;

import com.youku.java.navi.common.CacheComponent;
import com.youku.java.navi.common.NaviError;
import com.youku.java.navi.common.exception.NaviSystemException;
import com.youku.java.navi.dto.BaseResult;
import com.youku.java.navi.engine.core.INaviCache;
import com.youku.java.navi.engine.core.INaviDB;
import com.youku.java.navi.server.serviceobj.AbstractNaviBaseDto;
import com.youku.java.navi.server.serviceobj.AbstractNaviNewDao;
import com.youku.java.navi.utils.NaviUtil;
import com.youku.java.navi.utils.StringUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.lang.reflect.ParameterizedType;
import java.util.*;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * 对BaseDao的扩展, BaseDao主键类型只支持Long, 此类主键类型为传入的P范型类型(但P只可为String或Long)
 *
 * @param <T>
 *     dto实体
 * @param <P>
 *     dto实体对应的主键类型,只可为String或Long
 */
@Slf4j
public abstract class ABaseDao<T extends AbstractNaviBaseDto<P>, P> extends AbstractNaviNewDao<T> {

    protected String SEQ_ID_NAME = "SEQ_DEFAULT";

    @Setter
    private AutoIncrDao autoIncrDao;

    /**
     * 不建议使用,如果使用此方式构造,需要子类自己实现将对应的service注入到父类的逻辑
     */
    @Deprecated
    protected ABaseDao(Class<T> classNm) {
        this(classNm, null, null, null);
    }

    /**
     * 使用构造方法注入相关服务
     */
    protected ABaseDao(Class<T> classNm, INaviDB dbService, INaviCache cacheService, AutoIncrDao autoIncrDao) {
        super(classNm);

        // 注入db及缓存服务
        this.dbService = dbService;
        this.cacheService = cacheService;
        this.autoIncrDao = autoIncrDao;

        // 设置dto类自增id的序列键值
        String moduleNm = NaviUtil.getContextModuleNm(this.getClass());
        if (StringUtils.isEmpty(moduleNm)) {
            this.SEQ_ID_NAME = "SEQ_DEFAULT_" + this.getClass().getSimpleName().toUpperCase();
        } else {
            this.SEQ_ID_NAME = (moduleNm + "_" + this.getClass().getSimpleName()).toUpperCase();
        }
    }


    @SuppressWarnings("unchecked")
    public T create(T dto) throws NaviSystemException {
        if (dto.getId() == null) {
            if (this.getActualTypeClass().equals(Long.class)) {
                // 获取当前序列唯一序列id
                Long sid = autoIncrDao.getSid(SEQ_ID_NAME);
                if (sid == -1) {
                    throw new NaviSystemException("get seq id failed", NaviError.ERR_DBS);
                }

                dto.setId((P) sid);
            } else {
                dto.setId((P) new ObjectId().toString());
            }

        }

        dbService.insert(dto);

        if (cacheService != null) {
            this.updateCacheById(dto.getId(), dto);
        }

        return dto;
    }

    @SuppressWarnings("unchecked")
    public List<T> batchCreate(List<T> dtos) throws NaviSystemException {
        boolean isLong = this.getActualTypeClass().equals(Long.class);

        for (T dto : dtos) {
            if (dto.getId() == null) {
                if (isLong) {
                    // 获取当前序列唯一序列id
                    Long sid = autoIncrDao.getSid(SEQ_ID_NAME);
                    if (sid == -1) {
                        throw new NaviSystemException("get seq id failed", NaviError.ERR_DBS);
                    }

                    dto.setId((P) sid);
                } else {
                    dto.setId((P) new ObjectId().toString());
                }
            }
        }

        dbService.insertAll(dtos);

        if (cacheService != null) {
            for (T dto : dtos) {
                this.updateCacheById(dto.getId(), dto);
            }
        }

        return dtos;
    }

    public T update(P id, Map<String, Object> paramMap) throws NaviSystemException {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id));
        Update update = new Update();
        for (String key : paramMap.keySet()) {
            update.set(key, paramMap.get(key));
        }

        T dto = dbService.findAndModify(query, update, classNm);
        if (dto == null) {
            return null;
        }

        if (cacheService != null) {
            String key = this.buildKey(dto.getId());
            if (CacheComponent.existsInCache(cacheService, key)) {
                this.updateCacheByCacheKey(key, dto);
            }
        }

        return dto;
    }

    public T upsert(P id, Map<String, Object> paramMap) throws NaviSystemException {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id));
        Update update = new Update();
        for (String key : paramMap.keySet()) {
            update.set(key, paramMap.get(key));
        }

        if (!dbService.upsert(query, update, classNm)) {
            return null;
        }

        T dto = dbService.findOne(query, classNm);
        if (cacheService != null) {
            if (dto != null) {
                String key = this.buildKey(dto.getId());
                if (CacheComponent.existsInCache(cacheService, key)) {
                    this.updateCacheByCacheKey(key, dto);
                }
            } else {
                this.deleteFromCache(id);
            }
        }

        return dto;
    }

    public boolean existsById(P id) throws NaviSystemException {
        // 先查缓存是否存在
        String key = this.buildKey(String.valueOf(id));

        if (cacheService != null) {
            if (cacheService.exists(key)) {
                return true;
            }
        }

        T dto = dbService.findOne(new Query(where("_id").is(id)), classNm);
        if (dto == null) {
            return false;
        } else {
            if (cacheService != null) {
                updateCacheByCacheKey(key, dto);
            }
            return true;
        }
    }

    public T getById(P id) throws NaviSystemException {
        String key = this.buildKey(String.valueOf(id));
        if (cacheService != null) {
            T dto = cacheService.get(key, classNm);
            if (dto != null) {
                if (dto.isNull()) {
                    return null;
                } else {
                    return dto;
                }
            }
        }

        Query query = new Query(where("_id").is(id));
        T dto = dbService.findOne(query, classNm);
        if (dto == null || dto.isNull()) {
            try {
                T nullDto = classNm.newInstance();
                nullDto.setNull();

                if (cacheService != null) {
                    this.updateCacheByCacheKey(key, nullDto);
                }
            } catch (Exception e) {
                log.error("{}", e.getMessage());
            }

            return null;
        } else {
            updateCacheByCacheKey(key, dto);
            return dto;
        }
    }

    public T getFromDB(P id) throws NaviSystemException {
        return dbService.findOne(new Query(where("_id").is(id)), classNm);
    }

    public List<T> mget(Collection<P> ids) throws NaviSystemException {
        if (ids == null || ids.size() == 0) {
            return null;
        }

        // map存放，保证返回顺序
        Map<P, T> datas = new LinkedHashMap<>();
        for (P tmp : ids) {
            datas.put(tmp, null);
        }

        int counter = 0; // 记录查询到的数据条数

        // 查询缓存
        List<T> tmpls;
        if (cacheService != null) {
            try {
                List<String> keys = new LinkedList<>();
                for (P id : ids) {
                    keys.add(this.buildKey(id + ""));
                }

                tmpls = cacheService.MGet(classNm, keys.toArray(new String[keys.size()]));
                if (tmpls != null) {
                    for (T tmp : tmpls) {
                        if (tmp != null) {
                            datas.put(tmp.getId(), tmp);
                            counter++;
                        }
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        // 全部命中缓存
        if (counter == ids.size()) {
            List<T> list = new LinkedList<>();
            list.addAll(datas.values());

            return list;
        }

        // 计算未命中缓存的id列表
        List<P> unhited = new LinkedList<>();
        for (P key : datas.keySet()) {
            if (datas.get(key) == null) {
                unhited.add(key);
            }
        }

        // 查询数据库
        Query query = new Query(where("_id").in(unhited));
        List<T> res = dbService.find(query, classNm);
        if (res != null && res.size() != 0) {
            for (T tmp : res) {
                if (tmp != null) {
                    datas.put(tmp.getId(), tmp);
                    this.updateCacheById(tmp.getId(), tmp);
                    counter++;
                }
            }
        }

        if (counter == 0) {
            return null;
        }

        List<T> list = new LinkedList<>();
        list.addAll(datas.values());

        return list;
    }

    public T delete(P id) throws NaviSystemException {
        // 更新数据库
        Query query = new Query(where("_id").is(id));
        T tmpl = dbService.findAndRemove(query, classNm);
        if (tmpl == null) {
            return null;
        }

        // 更新缓存
        if (cacheService != null) {
            try {
                this.deleteFromCache(id, true);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        return tmpl;
    }

    public boolean mdelete(Collection<P> ids) throws NaviSystemException {
        BaseResult<T> ret = new BaseResult<>();

        // 更新数据库
        Query query = new Query(where("_id").in(ids));
        dbService.delete(query, classNm);

        // 更新缓存
        if (cacheService != null) {
            try {
                this.deleteFromCache(ids, true);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        return true;
    }

    public void updateCacheById(P id, T dto) {
        String key = this.buildKey(String.valueOf(id));
        this.updateCacheByCacheKey(key, dto);
    }

    public void updateCacheByCacheKey(String key, T dto) {
        if (cacheService != null) {
            try {
                cacheService.setex(key, dto.toString(), CacheComponent.getExpire(classNm));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public boolean deleteFromCache(P id) {
        return this.deleteFromCache(id, false);
    }

    public boolean deleteFromCache(Collection<P> ids) {
        return this.deleteFromCache(ids, false);
    }

    public boolean deleteFromCache(P id, boolean setNull) {
        if (cacheService != null) {
            try {
                if (!setNull) {
                    cacheService.delete(this.buildKey(String.valueOf(id)));
                } else {
                    cacheService.set(this.buildKey(String.valueOf(id)), T.createNullInstance(classNm));
                }

                return true;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        return false;
    }

    public boolean deleteFromCache(Collection<P> ids, boolean setNull) {
        if (cacheService != null) {
            try {
                String[] keys = new String[ids.size()];

                int i = 0;
                for (P id : ids) {
                    keys[i++] = this.buildKey(id + "");
                }

                if (!setNull) {
                    cacheService.delete(keys);
                } else {
                    T nullInstance = T.createNullInstance(classNm);
                    if (nullInstance != null) {
                        cacheService.set(keys, nullInstance);
                    }
                }

                return true;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        return false;
    }

    @Override
    public String buildKey(Object... strings) {
        if (strings == null) {
            return "";
        }

        List<String> list = new LinkedList<>();
        for (Object str : strings) {
            if (str != null) {
                list.add(String.valueOf(str));
            }
        }

        return CacheComponent.getCacheKey(this.classNm, list.toArray(new String[list.size()]));
    }

    @Override
    public int getExpire() {
        return CacheComponent.getExpire(this.classNm);
    }

    private Class getActualTypeClass() {
        ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
        Class entityClass = (Class) type.getActualTypeArguments()[1];
        return entityClass;
    }
}

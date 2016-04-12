package com.youku.java.navi.dao;

import com.youku.java.navi.common.CacheComponent;
import com.youku.java.navi.common.NaviError;
import com.youku.java.navi.common.exception.NaviSystemException;
import com.youku.java.navi.dto.BaseResult;
import com.youku.java.navi.server.serviceobj.AbstractNaviDto;
import com.youku.java.navi.server.serviceobj.AbstractNaviNewDao;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Setter
@Slf4j
public abstract class BaseDao<T extends AbstractNaviDto> extends AbstractNaviNewDao<T> {

    protected static final int DEFAULT_PAGE_LENGTH = 20;

    protected String SEQ_ID_NAME = "SEQ_DEFAULT";

    private AutoIncrDao autoIncrDao;

    protected BaseDao(Class<T> classNm) {
        super(classNm);
    }

    public T create(T dto) throws NaviSystemException {
        if (StringUtils.isEmpty(dto.getOId()) || dto.getOId().equals("0")) {
            // 获取当前序列唯一序列id
            long sid = autoIncrDao.getSid(SEQ_ID_NAME);
            if (sid == -1) {
                throw new NaviSystemException("get seq id failed", NaviError.ERR_DBS);
            }
            dto.setOId(sid);
        }

        dbService.insert(dto);
        this.updateCache(buildKey(dto.getOId()), dto);

        return dto;
    }

    public List<T> batchCreate(List<T> dtos) throws NaviSystemException {
        for (T dto : dtos) {
            if (StringUtils.isEmpty(dto.getOId()) || dto.getOId().equals("0")) {
                // 获取当前序列唯一序列id
                long sid = autoIncrDao.getSid(SEQ_ID_NAME);
                if (sid == -1) {
                    throw new NaviSystemException("get seq id failed", NaviError.ERR_DBS);
                }

                dto.setOId(sid);
            }
        }

        dbService.insertAll(dtos);

        for (T dto : dtos) {
            this.updateCache(buildKey(dto.getOId()), dto);
        }

        return dtos;
    }

    public T update(long id, Map<String, Object> paramMap) throws NaviSystemException {
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

        String key = this.buildKey(dto.getOId());
        if (CacheComponent.existsInCache(cacheService, key)) {
            this.updateCache(key, dto);
        }

        return dto;
    }

    public boolean exists(long id) throws NaviSystemException {
        // 先查缓存是否存在
        String key = this.buildKey(String.valueOf(id));
        if (cacheService.exists(key)) {
            return true;
        }

        T dto = dbService.findOne(new Query(where("_id").is(id)), classNm);
        if (dto == null) {
            return false;
        } else {
            updateCache(key, dto);
            return true;
        }
    }

    public T get(long id) throws NaviSystemException {
        String key = this.buildKey(String.valueOf(id));
        T dto = cacheService.get(key, classNm);
        if (dto != null) {
            if (dto.isNull()) {
                return null;
            } else {
                return dto;
            }
        }

        Query query = new Query(where("_id").is(id));
        dto = dbService.findOne(query, classNm);
        if (dto == null || dto.isNull()) {
            try {
                T nullDto = classNm.newInstance();
                nullDto.setNull();
                this.updateCache(key, nullDto);
            } catch (Exception e) {
                log.error("{}", e.getMessage());
            }

            return null;
        } else {
            updateCache(key, dto);
            return dto;
        }
    }

    public List<T> mget(List<Long> ids) throws NaviSystemException {
        if (ids == null || ids.size() == 0) {
            return null;
        }

        // map存放，保证返回顺序
        Map<Long, T> datas = new LinkedHashMap<>();
        for (Long tmp : ids) {
            datas.put(tmp, null);
        }

        int counter = 0; // 记录查询到的数据条数

        // 查询缓存
        List<T> tmpls;
        if (cacheService != null) {
            try {
                List<String> keys = new LinkedList<>();
                for (Long id : ids) {
                    keys.add(this.buildKey(id + ""));
                }

                tmpls = cacheService.MGet(classNm, keys.toArray(new String[keys.size()]));
                if (tmpls != null) {
                    for (T tmp : tmpls) {
                        if (tmp != null) {
                            datas.put(Long.parseLong(tmp.getOId()), tmp);
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
        List<Long> unhited = new LinkedList<>();
        for (Long key : datas.keySet()) {
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
                    datas.put(Long.parseLong(tmp.getOId()), tmp);
                    this.updateCache(Long.parseLong(tmp.getOId()), tmp);
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

    public T delete(long id) throws NaviSystemException {
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

    public boolean mdelete(List<Long> ids) throws NaviSystemException {
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

    public void updateCache(long id, T dto) {
        String key = this.buildKey(String.valueOf(id));
        this.updateCache(key, dto);
    }

    public void updateCache(String key, T dto) {
        try {
            cacheService.setex(key, dto.toString(), CacheComponent.getExpire(classNm));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public boolean deleteFromCache(long id) {
        return this.deleteFromCache(id, false);
    }

    public boolean deleteFromCache(List<Long> ids) {
        return this.deleteFromCache(ids, false);
    }

    public boolean deleteFromCache(long id, boolean setNull) {
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

        return false;
    }

    public boolean deleteFromCache(List<Long> ids, boolean setNull) {
        try {
            String[] keys = new String[ids.size()];
            for (int i = 0; i < keys.length; i++) {
                keys[i] = this.buildKey(ids.get(i) + "");
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
}

package com.youku.java.navi.dao;

import com.youku.java.navi.common.CacheComponent;
import com.youku.java.navi.common.NaviError;
import com.youku.java.navi.dto.BaseResult;
import com.youku.java.navi.server.serviceobj.AbstractNaviDto;
import com.youku.java.navi.server.serviceobj.AbstractNaviNewDao;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Setter
@Slf4j
public abstract class BaseDao<T extends AbstractNaviDto> extends AbstractNaviNewDao<T> {

    protected String SEQ_ID_NAME = "SEQ_DEFAULT";

    private AutoIncrDao autoIncrDao;

    protected BaseDao(Class<T> classNm) {
        super(classNm);
    }

    public BaseResult<T> create(T dto) {
        BaseResult<T> ret = new BaseResult<>();

        try {
            if (StringUtils.isEmpty(dto.getOId()) || dto.getOId().equals("0")) {
                // 获取当前序列唯一序列id
                long sid = autoIncrDao.getSid(SEQ_ID_NAME);
                if (sid == -1) {
                    ret.setCode(NaviError.ERR_DBS);
                    ret.setMsg("get section tmpl seq id failed");
                    return ret;
                }
                dto.setOId(sid);
            }

            dbService.insert(dto);
            String key = buildKey(dto.getOId());
            if (CacheComponent.existInCache(cacheService, key)) {
                this.updateCache(key, dto);
            }

            ret.makeSuccess();
            ret.setData(dto);
        } catch (Exception e) {
            ret.setCode(NaviError.ERR_DUPLICATE_KEY);
            ret.setMsg("duplicate check error, " + e.getMessage());
        }

        return ret;
    }

    public BaseResult<T> update(long id, Map<String, Object> paramMap) {
        BaseResult<T> ret = new BaseResult<>();

        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("_id").is(id));
            Update update = new Update();
            for (String key : paramMap.keySet()) {
                update.set(key, paramMap.get(key));
            }

            T dto = dbService.findAndModify(query, update, classNm);
            if (dto == null) {
                ret.setCode(NaviError.ERR_NO_DATA);
                ret.setMsg("no data found");
                return ret;
            }

            String key = this.buildKey(dto.getOId());
            if (CacheComponent.existInCache(cacheService, key)) {
                this.updateCache(key, dto);
            }

            ret.makeSuccess();
            ret.setData(dto);
        } catch (Exception e) {
            ret.setCode(NaviError.ERR_DBS);
            ret.setMsg(e.getMessage());
        }

        return ret;
    }

    public BaseResult<T> get(long id) {
        BaseResult<T> ret = new BaseResult<>();

        try {
            String key = this.buildKey(String.valueOf(id));
            T dto = cacheService.get(key, classNm);
            if (dto != null) {
                if (dto.isNull()) {
                    ret.setCode(NaviError.ERR_NO_DATA);
                    ret.setMsg("no data found");
                } else {
                    ret.makeSuccess();
                    ret.setData(dto);
                }
                return ret;
            }

            Query query = new Query(where("_id").is(id));
            dto = dbService.findOne(query, classNm);
            if (dto == null) {
                T nullDto = classNm.newInstance();
                nullDto.setNull();
                this.updateCache(key, nullDto);

                ret.setCode(NaviError.ERR_NO_DATA);
                ret.setMsg("no data found");
            } else {
                updateCache(key, dto);
                ret.makeSuccess();
                ret.setData(dto);
            }
        } catch (Exception e) {
            ret.setCode(NaviError.ERR_DBS);
            ret.setMsg(e.getMessage());
        }

        return ret;
    }

    public BaseResult<List<T>> mget(List<Long> ids) {
        BaseResult<List<T>> ret = new BaseResult<>();
        if (ids == null || ids.size() == 0) {
            ret.setCode(NaviError.ERR_NO_DATA);
            ret.setMsg("no data found");
            return ret;
        }

        try {
            // 查询缓存
            List<T> tmpls = null;
            if (cacheService != null) {
                try {
                    List<String> keys = new LinkedList<>();
                    for (Long id : ids) {
                        keys.add(this.buildKey(id + ""));
                    }

                    tmpls = cacheService.MGet(classNm, keys.toArray(new String[keys.size()]));
                    if (tmpls != null) {
                        tmpls.remove(null);

                        if (tmpls.size() == ids.size()) {
                            ret.makeSuccess();
                            ret.setData(tmpls);
                            return ret;
                        }
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

            if (tmpls == null) {
                tmpls = new LinkedList<>();
            }

            // 计算未命中缓存的id列表
            if (tmpls.size() != 0) {
                for (T tmp : tmpls) {
                    ids.remove(Long.parseLong(tmp.getOId()));
                }
            }

            // 查询数据库
            Query query = new Query(where("id").in(ids));
            List<T> list = dbService.find(query, classNm);
            if (list != null && list.size() != 0) {
                for (T tmp : list) {
                    tmpls.add(tmp);
                }
            }

            if (tmpls.size() == 0) {
                ret.setCode(NaviError.ERR_NO_DATA);
                ret.setMsg("no data found");
                return ret;
            }

            ret.makeSuccess();
            ret.setData(tmpls);
        } catch (Exception e) {
            ret.setCode(NaviError.ERR_DBS);
            ret.setMsg(e.getMessage());
        }

        return ret;
    }

    public BaseResult<T> delete(long id) {
        BaseResult<T> ret = new BaseResult<>();

        try {
            // 更新数据库
            Query query = new Query(where("id").is(id));
            T tmpl = dbService.findAndRemove(query, classNm);
            if (tmpl == null) {
                ret.setCode(NaviError.ERR_NO_DATA);
                ret.setMsg("no data found");
                return ret;
            }

            // 更新缓存
            if (cacheService != null) {
                try {
                    this.deleteFromCache(id);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

            ret.makeSuccess();
            ret.setData(tmpl);
        } catch (Exception e) {
            ret.setCode(NaviError.ERR_DBS);
            ret.setMsg(e.getMessage());
        }

        return ret;
    }

    public BaseResult<T> mdelete(List<Long> ids) {
        BaseResult<T> ret = new BaseResult<>();

        try {
            // 更新数据库
            Query query = new Query(where("id").in(ids));
            T tmpl = dbService.findAndRemove(query, classNm);
            if (tmpl == null) {
                ret.setCode(NaviError.ERR_NO_DATA);
                ret.setMsg("no data found");
                return ret;
            }

            // 更新缓存
            if (cacheService != null) {
                try {
                    this.deleteFromCache(ids);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

            ret.makeSuccess();
            ret.setData(tmpl);
        } catch (Exception e) {
            ret.setCode(NaviError.ERR_DBS);
            ret.setMsg(e.getMessage());
        }

        return ret;
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
                cacheService.set(this.buildKey(String.valueOf(id)), "{\"_null_\":1}");
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
                cacheService.set(keys, "{\"_null_\":1}");
            }

            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return false;
    }

    @Override
    public String buildKey(String... strings) {
        String attach = (null == strings || strings.length == 0) ? "" : strings[0];
        return CacheComponent.getRedisKey(this.classNm, attach);
    }

    @Override
    public int getExpire() {
        return CacheComponent.getExpire(this.classNm);
    }

}

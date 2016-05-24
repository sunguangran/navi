package com.youku.java.navi.dao;

import com.youku.java.navi.common.CacheComponent;
import com.youku.java.navi.common.NaviError;
import com.youku.java.navi.dto.db.AutoIncrDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.spi.ErrorCode;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@Setter
@Getter
@Slf4j
public class AutoIncrDao extends ABaseDao<AutoIncrDTO, String> {

    private int CACHE_STEP = 10000;

    public AutoIncrDao() {
        super(AutoIncrDTO.class);
    }

    protected AutoIncrDao(Class<AutoIncrDTO> classNm) {
        super(classNm);
    }

    public long getSid(String id) {
        return getSid(id, 1000000);
    }

    public long getSid(String id, long startValue) {
        try {
            String cacheKey = CacheComponent.getCacheKey(classNm, id);
            if (cacheService != null) {
                Long num = cacheService.get(cacheKey, Long.class);
                if (num != null && num != 0) {
                    Long res = cacheService.incr(cacheKey, 1);

                    if (res % CACHE_STEP == 0) {
                        try {
                            Query query = new Query();
                            query.addCriteria(Criteria.where("_id").is(id));

                            Update update = new Update().set("idv", res + CACHE_STEP + 1);

                            AutoIncrDTO tmp = dbService.findAndModify(query, update, AutoIncrDTO.class);
                            if (tmp == null) {
                                tmp = new AutoIncrDTO();
                                tmp.setId(id);
                                tmp.setIdv(res);

                                dbService.insert(tmp);
                            }

                        } catch (Exception e) {
                            log.error("{}", e.toString());
                        }
                    }

                    return res;
                }
            }

            // 没有命中缓存
            Query query = new Query();
            query.addCriteria(Criteria.where("_id").is(id));
            query.fields().include("_id").include("idv");
            Update update = new Update().inc("idv", 1);

            AutoIncrDTO tmp = dbService.findAndModify(query, update, AutoIncrDTO.class);
            if (tmp == null) {
                tmp = new AutoIncrDTO();
                tmp.setId(id);
                tmp.setIdv(startValue);

                dbService.insert(tmp);
            }

            if (cacheService != null) {
                try {
                    cacheService.set(cacheKey, tmp.getIdv());
                } catch (Exception ignored) {
                }
            }

            return tmp.getIdv();
        } catch (Exception e) {
            log.error("get autoincr id error " + id);
            return NaviError.ERR_DBS;
        }
    }

}

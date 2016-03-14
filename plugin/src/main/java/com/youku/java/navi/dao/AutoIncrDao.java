package com.youku.java.navi.dao;

import com.youku.java.navi.common.NaviError;
import com.youku.java.navi.dto.db.AutoIncrDTO;
import com.youku.java.navi.server.serviceobj.AbstractNaviNewDao;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@Setter
@Getter
@Slf4j
public class AutoIncrDao extends AbstractNaviNewDao<AutoIncrDTO> {

    public AutoIncrDao() {
        super(AutoIncrDTO.class);
    }

    protected AutoIncrDao(Class<AutoIncrDTO> classNm) {
        super(classNm);
    }

    @Override
    public String buildKey(String... keyComponents) {
        return null;
    }

    public int getExpire() {
        return 0;
    }

    public long getSid(String id) {

        return getSid(id, 1000000);
    }

    public long getSid(String id, long start_value) {
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("_id").is(id));
            query.fields().include("_id").include("idv");
            Update update = new Update().inc("idv", 1);
            AutoIncrDTO tmp = dbService.findAndModify(query, update, AutoIncrDTO.class);

            if (tmp == null) {
                // 无改key记录，直接插入
                AutoIncrDTO dto = new AutoIncrDTO();
                dto.set_id(id);
                dto.setIdv(start_value);

                dbService.insert(dto);
                return start_value;
            }
            return tmp.getIdv();
        } catch (Exception e) {
            log.error("get autoincr id error " + id);
            return NaviError.ERR_DBS;
        }
    }

}

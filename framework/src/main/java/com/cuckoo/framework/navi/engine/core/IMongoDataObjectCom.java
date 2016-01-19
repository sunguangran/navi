package com.cuckoo.framework.navi.engine.core;

import com.cuckoo.framework.navi.serviceobj.AbstractNaviBean;
import org.springframework.data.mongodb.core.query.Query;

public interface IMongoDataObjectCom {

    <T extends AbstractNaviBean> IDataObjectCom<T> getDataObjectCom(Query query, String cacheKey, Class<T> dtoClass);

    <T extends AbstractNaviBean> IListDataObjectCom<T> getListDataObjectCom(Query query, Class<T> dtoClass, String cacheKey);

}

package com.youku.java.navi.engine.core;

import com.youku.java.navi.server.serviceobj.AbstractNaviDto;
import org.springframework.data.mongodb.core.query.Query;

public interface IMongoDataObjectCom {

    <T extends AbstractNaviDto> IDataObjectCom<T> getDataObjectCom(Query query, String cacheKey, Class<T> dtoClass);

    <T extends AbstractNaviDto> IListDataObjectCom<T> getListDataObjectCom(Query query, Class<T> dtoClass, String cacheKey);

}

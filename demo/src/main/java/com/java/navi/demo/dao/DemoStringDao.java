package com.java.navi.demo.dao;

import com.java.navi.demo.dto.db.TDemoString;
import com.youku.java.navi.common.exception.NaviSystemException;
import com.youku.java.navi.dao.ABaseDao;
import com.youku.java.navi.dao.AutoIncrDao;
import com.youku.java.navi.engine.core.INaviCache;
import com.youku.java.navi.engine.core.INaviDB;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * demo dao,表主键为String类型
 *
 * @author sgran<sunguangran@youku.com>
 * @since 16/5/23
 */
@Slf4j
@Repository
public class DemoStringDao extends ABaseDao<TDemoString, String> {

    /**
     * 通过构造方法注入使用的db,缓存服务及自增id使用的dao类
     *
     * @param demoDbService
     *     当前dao使用的db服务实例
     * @param redisReadCacheService
     *     当前dao使用的缓存服务实例
     * @param autoIncrDao
     *     当前dao自增id使用的dao实例
     */
    @Autowired
    public DemoStringDao(INaviDB demoDbService, INaviCache redisReadCacheService, AutoIncrDao autoIncrDao) {
        super(TDemoString.class, demoDbService, redisReadCacheService, autoIncrDao);
    }

    public TDemoString createDemoString(String ccid, String name) throws NaviSystemException {
        TDemoString dto = new TDemoString();
        dto.setNa(name);
        dto.setCcid(ccid);

        return super.create(dto);
    }

}

package com.java.navi.demo.dao;

import com.java.navi.demo.dto.db.TDemo;
import com.youku.java.navi.common.exception.NaviSystemException;
import com.youku.java.navi.dao.AutoIncrDao;
import com.youku.java.navi.dao.BaseDao;
import com.youku.java.navi.engine.core.INaviCache;
import com.youku.java.navi.engine.core.INaviDB;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 * demo dao,表主键为Long类型
 *
 * @author sgran<sunguangran@youku.com>
 * @since 16/5/23
 */
@Slf4j
@Repository
public class DemoDao extends BaseDao<TDemo> {

    @Value("${ZK_HOSTNAME_ROOT}")
    private String zkRoot;

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
    public DemoDao(INaviDB demoDbService, INaviCache redisReadCacheService, AutoIncrDao autoIncrDao) {
        super(TDemo.class, demoDbService, redisReadCacheService, autoIncrDao);
    }

    public TDemo createDemo(String ccid, String name) throws NaviSystemException {
        log.info("zk root: " + zkRoot);

        TDemo dto = new TDemo();
        dto.setNa(name);
        dto.setCcid(ccid);

        return super.create(dto);
    }

}

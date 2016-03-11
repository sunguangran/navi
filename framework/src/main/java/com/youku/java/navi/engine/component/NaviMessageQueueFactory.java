package com.youku.java.navi.engine.component;

import com.youku.java.navi.common.NAVIERROR;
import com.youku.java.navi.common.exception.NaviSystemException;
import com.youku.java.navi.engine.component.NaviMQContext.MessageQueueType;
import com.youku.java.navi.engine.core.IBaseDataService;
import com.youku.java.navi.engine.core.INaviCache;
import com.youku.java.navi.engine.core.INaviMessageQueue;

public class NaviMessageQueueFactory {

    public INaviMessageQueue createMQ(IBaseDataService service, String queueKey, MessageQueueType mqType) {
        switch (mqType) {
            case REDIS:
                if (service instanceof INaviCache) {
                    return new NaviRedisMessageQueue((INaviCache) service);
                } else {
                    throw new NaviSystemException("The service is invalid!!", NAVIERROR.SYSERROR.code());
                }
            case OLDMUTIREDIS:
                if (service instanceof INaviCache) {
                    return new NaviRedisMutiKeyMessageQueue(queueKey, (INaviCache) service);
                } else {
                    throw new NaviSystemException("The service is invalid!!", NAVIERROR.SYSERROR.code());
                }
            case MUTIREDIS:
                if (service instanceof INaviCache) {
                    return new NaviNewRedisMutiKeyMessageQueue(queueKey, (INaviCache) service);
                } else {
                    throw new NaviSystemException("The service is invalid!!", NAVIERROR.SYSERROR.code());
                }
            case ZOOKEEPER:
                throw new NaviSystemException("The service is invalid!!", NAVIERROR.SYSERROR.code());
            default:
                throw new NaviSystemException("The service is invalid!!", NAVIERROR.SYSERROR.code());
        }
    }
}

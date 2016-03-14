package com.youku.java.navi.engine.component;

import com.youku.java.navi.common.NaviError;
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
                    throw new NaviSystemException("The service is invalid!!", NaviError.SYSERROR);
                }
            case OLDMUTIREDIS:
                if (service instanceof INaviCache) {
                    return new NaviRedisMutiKeyMessageQueue(queueKey, (INaviCache) service);
                } else {
                    throw new NaviSystemException("The service is invalid!!", NaviError.SYSERROR);
                }
            case MUTIREDIS:
                if (service instanceof INaviCache) {
                    return new NaviNewRedisMutiKeyMessageQueue(queueKey, (INaviCache) service);
                } else {
                    throw new NaviSystemException("The service is invalid!!", NaviError.SYSERROR);
                }
            case ZOOKEEPER:
                throw new NaviSystemException("The service is invalid!!", NaviError.SYSERROR);
            default:
                throw new NaviSystemException("The service is invalid!!", NaviError.SYSERROR);
        }
    }
}

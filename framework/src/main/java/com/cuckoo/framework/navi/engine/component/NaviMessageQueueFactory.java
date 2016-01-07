package com.cuckoo.framework.navi.engine.component;

import com.cuckoo.framework.navi.common.NaviError;
import com.cuckoo.framework.navi.common.exception.NaviSystemException;
import com.cuckoo.framework.navi.engine.component.NaviMQContext.MessageQueueType;
import com.cuckoo.framework.navi.engine.core.IBaseDataService;
import com.cuckoo.framework.navi.engine.core.INaviCache;
import com.cuckoo.framework.navi.engine.core.INaviMessageQueue;

public class NaviMessageQueueFactory {

    public INaviMessageQueue createMQ(IBaseDataService service, String queueKey, MessageQueueType mqType) {
        switch (mqType) {
            case REDIS:
                if (service instanceof INaviCache) {
                    return new NaviRedisMessageQueue((INaviCache) service);
                } else {
                    throw new NaviSystemException("The service is invalid.", NaviError.SYSERROR.code());
                }
            case OLDMUTIREDIS:
                if (service instanceof INaviCache) {
                    return new NaviRedisMutiKeyMessageQueue(queueKey, (INaviCache) service);
                } else {
                    throw new NaviSystemException("The service is invalid.", NaviError.SYSERROR.code());
                }
            case MUTIREDIS:
                if (service instanceof INaviCache) {
                    return new NaviNewRedisMutiKeyMessageQueue(queueKey, (INaviCache) service);
                } else {
                    throw new NaviSystemException("The service is invalid.", NaviError.SYSERROR.code());
                }
            case ZOOKEEPER:
                throw new NaviSystemException("The service is invalid.", NaviError.SYSERROR.code());
            default:
                throw new NaviSystemException("The service is invalid.", NaviError.SYSERROR.code());
        }
    }
}

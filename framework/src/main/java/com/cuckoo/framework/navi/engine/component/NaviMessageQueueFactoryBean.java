package com.cuckoo.framework.navi.engine.component;

import com.cuckoo.framework.navi.engine.core.INaviMessageQueue;
import com.cuckoo.framework.navi.engine.core.IBaseDataService;
import com.cuckoo.framework.navi.engine.component.NaviMQContext.MessageQueueType;
import org.springframework.beans.factory.FactoryBean;

public class NaviMessageQueueFactoryBean implements FactoryBean<INaviMessageQueue> {

    private NaviMessageQueueFactory factory = new NaviMessageQueueFactory();
    private IBaseDataService service;
    private String queueKey;
    private int mqType = 0;

    public INaviMessageQueue getObject() throws Exception {
        return factory.createMQ(service, queueKey, MessageQueueType.values()[mqType]);
    }

    public Class<?> getObjectType() {
        return INaviMessageQueue.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public IBaseDataService getService() {
        return service;
    }

    public void setService(IBaseDataService service) {
        this.service = service;
    }

    public String getQueueKey() {
        return queueKey;
    }

    public void setQueueKey(String queueKey) {
        this.queueKey = queueKey;
    }

    public int getMqType() {
        return mqType;
    }

    public void setMqType(int mqType) {
        this.mqType = mqType;
    }
}

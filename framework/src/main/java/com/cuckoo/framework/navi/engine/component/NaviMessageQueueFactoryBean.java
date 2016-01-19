package com.cuckoo.framework.navi.engine.component;

import com.cuckoo.framework.navi.engine.component.NaviMQContext.MQType;
import com.cuckoo.framework.navi.engine.core.IBaseDataService;
import com.cuckoo.framework.navi.engine.core.INaviMessageQueue;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.FactoryBean;

@Setter
@Getter
public class NaviMessageQueueFactoryBean implements FactoryBean<INaviMessageQueue> {

    private NaviMessageQueueFactory factory = new NaviMessageQueueFactory();
    private IBaseDataService service;
    private String queueKey;
    private int mqType = 0;

    public INaviMessageQueue getObject() throws Exception {
        return factory.createMQ(service, queueKey, MQType.values()[mqType]);
    }

    public Class<?> getObjectType() {
        return INaviMessageQueue.class;
    }

    public boolean isSingleton() {
        return true;
    }

}

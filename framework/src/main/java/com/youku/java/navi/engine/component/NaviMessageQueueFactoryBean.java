package com.youku.java.navi.engine.component;

import com.youku.java.navi.engine.component.NaviMQContext.MessageQueueType;
import com.youku.java.navi.engine.core.IBaseDataService;
import com.youku.java.navi.engine.core.INaviMessageQueue;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.FactoryBean;

public class NaviMessageQueueFactoryBean implements FactoryBean<INaviMessageQueue> {

    private NaviMessageQueueFactory factory = new NaviMessageQueueFactory();

    @Setter @Getter
    private IBaseDataService service;

    @Setter @Getter
    private String queueKey;

    @Setter @Getter
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

}

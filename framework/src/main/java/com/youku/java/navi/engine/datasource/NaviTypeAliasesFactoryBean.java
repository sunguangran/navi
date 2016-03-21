package com.youku.java.navi.engine.datasource;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.List;

/**
 * @FileName: NaviTypeAliasesFactoryBean.java
 * @Description: TODO
 * @Copyright: Copyright(C) 2015年1月14日 by 1verge
 * @Company: 1verge.com  (http://www.youku.com)
 * @Version: V1.0
 * @Createdate: 2015年1月14日 下午4:09:58
 * <p/>
 * Modification  History:
 * Date         Author        Version        Discription
 * -----------------------------------------------------------------------------------
 * 2015年1月14日   hequnfei      1.0            1.0
 * Why & What is modified: newly added
 */
@Setter
@Getter
public class NaviTypeAliasesFactoryBean implements FactoryBean<List<Class<?>>>, ApplicationContextAware {

    private ApplicationContext context;

    private String[] types;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    public List<Class<?>> getObject() throws Exception {
        List<Class<?>> list = new ArrayList<>();
        for (String type : types) {
            list.add(context.getClassLoader().loadClass(type));
        }
        
        return list;
    }

    public Class<?> getObjectType() {
        return List.class;
    }

    public boolean isSingleton() {
        return true;
    }

}

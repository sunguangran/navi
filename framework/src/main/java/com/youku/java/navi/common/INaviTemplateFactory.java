package com.youku.java.navi.common;

import org.springframework.beans.factory.DisposableBean;

import java.util.List;

public interface INaviTemplateFactory extends DisposableBean {

    /**
     * 设置开发或者测试环境服务列表
     * @param servers
     */
    void setServers(List<ServerAddress> servers);
    
    /**
     * 设置部署环境服务列表
     * @param deployServers
     */
    void setDeployServers(List<ServerAddress> deployServers);

}

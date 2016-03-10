package com.cuckoo.framework.navi.engine.datasource;

import com.cuckoo.framework.navi.server.ServerConfigure;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.InitializingBean;

public class NaviBasicDataSource extends BasicDataSource implements InitializingBean {


    private String deployUrl;
    private String deployPassword;
    private String deployUsername;


    public String getDeployUrl() {
        return deployUrl;
    }

    public void setDeployUrl(String deployUrl) {
        this.deployUrl = deployUrl;
    }

    public String getDeployPassword() {
        return deployPassword;
    }

    public void setDeployPassword(String deployPassword) {
        this.deployPassword = deployPassword;
    }

    public String getDeployUsername() {
        return deployUsername;
    }

    public void setDeployUsername(String deployUsername) {
        this.deployUsername = deployUsername;
    }

    public void afterPropertiesSet() throws Exception {
        if (ServerConfigure.isDeployEnv()) {
            super.setUrl(deployUrl);
            super.setUsername(deployUsername);
            super.setPassword(deployPassword);
        }
    }


}

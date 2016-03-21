package com.youku.java.navi.engine.datasource;

import com.youku.java.navi.server.ServerConfigure;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.InitializingBean;

@Setter
@Getter
public class NaviBasicDataSource extends BasicDataSource implements InitializingBean {

    private String deployUrl;
    private String deployPassword;
    private String deployUsername;

    public void afterPropertiesSet() throws Exception {
        if (ServerConfigure.isDeployEnv()) {
            super.setUrl(deployUrl);
            super.setUsername(deployUsername);
            super.setPassword(deployPassword);
        }
    }
}

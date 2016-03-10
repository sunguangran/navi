package com.youku.java.navi.engine.datasource.driver;

import com.youku.java.navi.engine.datasource.pool.NaviPoolConfig;
import com.youku.java.navi.common.ServerUrlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Slf4j
public abstract class NaviJdbcDriver extends AbstractNaviDriver {

    private Connection conn;

    public NaviJdbcDriver(ServerUrlUtil.ServerUrl server, String auth, NaviPoolConfig poolConfig) {
        super(server, auth, poolConfig);
        initConnection(server.getUrl(), auth, poolConfig);
    }

    protected void initConnection(String url, String auth, NaviPoolConfig poolConfig) {
        try {
            loadDriver();
            if (StringUtils.isEmpty(auth)) {
                conn = DriverManager.getConnection(url);
            } else {
                String username = auth.substring(0, auth.indexOf(';'));
                String password = auth.substring(auth.indexOf(';') + 1);
                conn = DriverManager.getConnection(url, username, password);
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    abstract void loadDriver() throws ClassNotFoundException;

    public void destroy() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isAlive() {
        try {
            return !conn.isValid(getPoolConfig().getSocketTimeout());
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean open() {
        return false;
    }

    public Connection getConnection() {
        return conn;
    }


}

package com.cuckoo.framework.navi.engine.datasource.service;

import com.cuckoo.framework.navi.common.NAVIERROR;
import com.cuckoo.framework.navi.common.NaviSystemException;
import com.cuckoo.framework.navi.engine.core.INaviDriver;
import com.cuckoo.framework.navi.engine.core.INaviJdbc;
import com.cuckoo.framework.navi.engine.datasource.driver.NaviJdbcDriver;

import java.sql.*;

public class NaviJDBCService extends AbstractNaviDataService implements
    INaviJdbc {

    protected NaviJdbcDriver getDriver() {
        INaviDriver driver = dataSource.getHandle();
        if (driver instanceof NaviJdbcDriver) {
            return (NaviJdbcDriver) driver;
        }
        driver.close();
        throw new NaviSystemException("the driver is invalid!",
            NAVIERROR.SYSERROR.code());
    }

    public ResultSet doQuery(String sql) throws SQLException {
        NaviJdbcDriver driver = getDriver();
        try {
            Connection conn = driver.getConnection();
            Statement st = conn.createStatement();
            return st.executeQuery(sql);
        } finally {
            driver.close();
        }
    }

    public ResultSet doQuery(String sql, IPSCallback p) throws SQLException {
        NaviJdbcDriver driver = getDriver();
        try {
            Connection conn = driver.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql);
            p.doPs(pst);
            return pst.executeQuery(sql);
        } finally {
            driver.close();
        }
    }

    public int doUpdate(String sql) throws SQLException {
        NaviJdbcDriver driver = getDriver();
        try {
            Connection conn = driver.getConnection();
            Statement st = conn.createStatement();
            return st.executeUpdate(sql);
        } finally {
            driver.close();
        }
    }

    public int doUpdate(String sql, IPSCallback p) throws SQLException {
        NaviJdbcDriver driver = getDriver();
        try {
            Connection conn = driver.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql);
            p.doPs(pst);
            return pst.executeUpdate(sql);
        } finally {
            driver.close();
        }
    }
}

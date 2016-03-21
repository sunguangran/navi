package com.youku.java.navi.engine.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface INaviJdbc {

    interface IPSCallback {
        void doPs(PreparedStatement pst);
    }

    ResultSet doQuery(String sql) throws SQLException;

    ResultSet doQuery(String sql, IPSCallback p) throws SQLException;

    int doUpdate(String sql) throws SQLException;

    int doUpdate(String sql, IPSCallback p) throws SQLException;

}

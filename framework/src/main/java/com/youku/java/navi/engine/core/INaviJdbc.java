package com.youku.java.navi.engine.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface INaviJdbc {

    public interface IPSCallback {
        void doPs(PreparedStatement pst);
    }

    public ResultSet doQuery(String sql) throws SQLException;

    public ResultSet doQuery(String sql, IPSCallback p) throws SQLException;

    public int doUpdate(String sql) throws SQLException;

    public int doUpdate(String sql, IPSCallback p) throws SQLException;

}

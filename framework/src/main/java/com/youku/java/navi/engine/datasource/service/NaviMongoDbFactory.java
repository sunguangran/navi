package com.youku.java.navi.engine.datasource.service;

import com.youku.java.navi.engine.core.INaviDataSource;
import com.youku.java.navi.engine.datasource.DefaultNaviDataSource;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.WriteConcern;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.util.Assert;

public class NaviMongoDbFactory implements MongoDbFactory {

    private String databaseNm;
    private WriteConcern writeConcern;
    private INaviDataSource dataSource;

    public NaviMongoDbFactory(INaviDataSource dataSource,
                              String databaseNm) {
        this.databaseNm = databaseNm;
        this.dataSource = dataSource;
    }

    public DB getDb() throws DataAccessException {
        return getDb(databaseNm);
    }

    public DB getDb(String dbName) throws DataAccessException {

        Assert.hasText(dbName, "Database name must not be empty.");

        DefaultNaviDataSource defaultDataSource = (DefaultNaviDataSource) dataSource;

        //NaviMongoDriver driver = (NaviMongoDriver) defaultDataSource.getHandle();
        Mongo mongo = (Mongo) defaultDataSource.getHandle().getDriver();
        //DB db = MongoDbUtils.getDB(mongo, dbName, null, null);
        DB db = mongo.getDB(databaseNm);
        if (writeConcern != null) {
            db.setWriteConcern(writeConcern);
        }

        return db;
    }

    /**
     * Configures the {@link WriteConcern} to be used on the {@link DB} instance
     * being created.
     *
     * @param writeConcern
     *     the writeConcern to set
     */
    public void setWriteConcern(WriteConcern writeConcern) {
        this.writeConcern = writeConcern;
    }

    public WriteConcern getWriteConcern() {
        return writeConcern;
    }

}

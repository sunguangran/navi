package com.youku.java.navi.engine.core;

import java.util.Date;
import java.util.List;

public interface INaviLog extends IBaseDataService {

    void setLevel(int level);

    boolean debug(String log, boolean asynchronous);

    boolean debug(String log, Throwable t, boolean asynchronous);

    boolean info(String log, boolean asynchronous);

    boolean info(String log, Throwable t, boolean asynchronous);

    boolean warn(String log, boolean asynchronous);

    boolean warn(String log, Throwable t, boolean asynchronous);

    boolean error(String log, boolean asynchronous);

    boolean error(String log, Throwable t, boolean asynchronous);

    boolean batchAppend(List<BatchLogEntry> logs);

    enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    }

    class BatchLogEntry {
        private int level;
        private String log;
        private Throwable excpetion;
        private Date date;

        BatchLogEntry(int level, String log, Throwable excpetion) {
            this.level = level;
            this.log = log;
            this.excpetion = excpetion;
            this.date = new Date();
        }

        public int getLevel() {
            return this.level;
        }

        public String getLog() {
            return this.log;
        }

        public Date getDate() {
            return this.date;
        }

        public Throwable getException() {
            return excpetion;
        }
    }
}

package com.youku.java.navi.engine.core;

import java.util.Date;
import java.util.List;

public interface INaviLog extends IBaseDataService {

    public void setLevel(int level);

    public boolean debug(String log, boolean asynchronous);

    public boolean debug(String log, Throwable t, boolean asynchronous);

    public boolean info(String log, boolean asynchronous);

    public boolean info(String log, Throwable t, boolean asynchronous);

    public boolean warn(String log, boolean asynchronous);

    public boolean warn(String log, Throwable t, boolean asynchronous);

    public boolean error(String log, boolean asynchronous);

    public boolean error(String log, Throwable t, boolean asynchronous);

    public boolean batchAppend(List<BatchLogEntry> logs);

    public enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    }

    public class BatchLogEntry {
        private int level;
        private String log;
        private Throwable excpetion;
        private Date date;

        public BatchLogEntry(int level, String log, Throwable excpetion) {
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

package com.youku.java.navi.engine.datasource.service;

import com.youku.java.navi.common.NaviError;
import com.youku.java.navi.common.exception.NaviSystemException;
import com.youku.java.navi.engine.core.INaviDriver;
import com.youku.java.navi.engine.core.INaviLog;
import com.youku.java.navi.engine.datasource.driver.NaviScribeDriver;
import com.youku.java.navi.server.ServerConfigure;
import com.youku.java.navi.utils.AsynchExecUtil;
import org.apache.commons.lang.StringUtils;
import scribe.thrift.LogEntry;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NaviScribeService extends AbstractNaviDataService implements INaviLog {

    private int level = LogLevel.INFO.ordinal();
    private String localhost;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String parent;
    private String module;

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean batchAppend(List<BatchLogEntry> logs) {
        NaviScribeDriver driver = getScribeDriver();
        try {
            List<LogEntry> list = new ArrayList<>();
            for (BatchLogEntry entry : logs) {
                if (this.level > entry.getLevel()) {
                    continue;
                }
                String log = formatLog(getLocalhost(), entry.getDate(), entry.getLog(), null);
                list.add(new LogEntry(getCategory(entry.getLevel()), log));
            }
            return driver.sendLog(list);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(),
                NaviError.SYSERROR, e);
        } finally {
            driver.close();
        }

    }

    private boolean append(int level, String log, Throwable t,
                           boolean asynchronous) {
        if (this.level > level) {
            return false;
        }
        return send(level, log, t, asynchronous);
    }

    public boolean debug(String log, boolean asynchronous) {
        return append(LogLevel.DEBUG.ordinal(), log, null, asynchronous);
    }

    public boolean debug(String log, Throwable t, boolean asynchronous) {
        return append(LogLevel.DEBUG.ordinal(), log, t, asynchronous);
    }

    public boolean info(String log, boolean asynchronous) {
        return append(LogLevel.INFO.ordinal(), log, null, asynchronous);
    }

    public boolean info(String log, Throwable t, boolean asynchronous) {
        return append(LogLevel.INFO.ordinal(), log, t, asynchronous);
    }

    public boolean warn(String log, boolean asynchronous) {
        return append(LogLevel.WARN.ordinal(), log, null, asynchronous);
    }

    public boolean warn(String log, Throwable t, boolean asynchronous) {
        return append(LogLevel.WARN.ordinal(), log, t, asynchronous);
    }

    public boolean error(String log, boolean asynchronous) {
        return append(LogLevel.ERROR.ordinal(), log, null, asynchronous);
    }

    public boolean error(String log, Throwable t, boolean asynchronous) {
        return append(LogLevel.ERROR.ordinal(), log, t, asynchronous);
    }

    private boolean send(final int level, final String log, final Throwable t, boolean asynchronous) {
        if (asynchronous) {
            AsynchExecUtil.execute(new Runnable() {
                public void run() {
                    try {
                        send(level, log, t);
                    } catch (Exception e) {
                    }
                }
            });
            return true;
        }
        return send(level, log, t);
    }

    private boolean send(int level, String log, Throwable t) {
        NaviScribeDriver driver = getScribeDriver();
        try {
            List<LogEntry> list = new ArrayList<LogEntry>();
            log = formatLog(getLocalhost(), new Date(), log, t);
            list.add(new LogEntry(getCategory(level), log));
            return driver.sendLog(list);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(),
                NaviError.SYSERROR, e);
        } finally {
            driver.close();
        }
    }

    private String getCategory(int level) {
        return getParent() + "/" + LogLevel.values()[level].toString().toLowerCase();
    }

    private String getParent() {
        if (parent == null) {
            StringBuffer str = new StringBuffer();
            str.append(ServerConfigure.getServer());
            if (!StringUtils.isEmpty(module)) {
                str.append("/").append(module);
            }
        }
        return parent;
    }

    private String getLocalhost() throws UnknownHostException {
        if (localhost == null) {
            synchronized (this) {
                if (localhost == null) {
                    InetAddress addr = InetAddress.getLocalHost();
                    byte[] ipAddr = addr.getAddress();
                    StringBuilder ipAddrStr = new StringBuilder("");
                    for (int i = 0; i < ipAddr.length; i++) {
                        if (i > 0) {
                            ipAddrStr.append(".");
                        }
                        ipAddrStr.append(ipAddr[i] & 0xFF);
                    }
                    localhost = ipAddrStr.toString();
                }
            }
        }
        return localhost;
    }

    private NaviScribeDriver getScribeDriver() {
        INaviDriver driver = dataSource.getHandle();
        if (driver instanceof NaviScribeDriver) {
            return (NaviScribeDriver) driver;
        }
        throw new NaviSystemException("the log system driver is invalid!",
            NaviError.SYSERROR);
    }

    private String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        t.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }

    private String formatLog(String localhost, Date date, String log,
                             Throwable t) {
        return String.format("%s %s %s", localhost, formatter.format(date),
            t == null ? log : log + getStackTrace(t));
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getModule() {
        return module;
    }
}

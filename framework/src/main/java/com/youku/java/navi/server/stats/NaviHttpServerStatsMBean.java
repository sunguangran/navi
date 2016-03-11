package com.youku.java.navi.server.stats;

public interface NaviHttpServerStatsMBean {

    void incrConnCount();

    void decrConnCount();

    long getConnCount();

    void incrReadCount();

    int getReadCountPerSecd();

    void incrWriteCount();

    int getWriteCountPerSecd();

    /**
     * 返回成功响应率
     *
     * @return %(百分比)
     */
    double getResponseRate();

    /**
     * 返回上行流量
     *
     * @return MB
     */
    int getReadThroughput();

    /**
     * 返回下行流量
     *
     * @return MB
     */
    int getWriteThroughput();

    void setWriteByte(long writeByte);

    void setReadByte(long readByte);

    long getWriteByte();

    long getReadByte();
}

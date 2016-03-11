package com.youku.java.navi.engine.core;

public interface IDelimiterBasedUDPService extends INaviUDPClientService {
    /**
     * 所有的分隔符必须为1个byte大小，否则为无效分隔符。
     *
     * @param service
     * @param module
     * @param api
     * @param msg
     * @param headerDelimiter
     * @param msgDelimiter
     * @param packetDemlimiter
     * @return
     */
    public String parseUDPPacket(String service, String module, String api, String extra, String msg, String headerDelimiter, String msgDelimiter, String packetDemlimiter);
}

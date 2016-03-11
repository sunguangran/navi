package com.youku.java.navi.engine.datasource.service;

import com.youku.java.navi.engine.core.IDelimiterBasedUDPService;

public class DelimiterBasedUDPClient extends NaviUDPClientService implements IDelimiterBasedUDPService {
    /**
     * 所有的分隔符必须为1个byte大小，否则为无效分隔符。
     *
     * @param service
     * @param module
     * @param api
     * @param msg
     * @param extra
     *     额外信息
     * @param headerDelimiter
     *     头部信息分隔符
     * @param msgDelimiter
     *     头部与内容分隔符
     * @param packetDemlimiter
     *     报文结尾分隔符
     * @return
     */
    public String parseUDPPacket(String service, String module, String api, String extra,
                                 String msg, String headerDelimiter, String msgDelimiter,
                                 String packetDemlimiter) {
        StringBuilder result = new StringBuilder();
        if (null != service) {
            result.append(service);
        }
        result.append(headerDelimiter);
        if (null != module) {
            result.append(module);
        }
        result.append(headerDelimiter);
        if (null != api) {
            result.append(api);
        }
        result.append(headerDelimiter);
        if (null != extra) {
            result.append(extra);
        }
        result.append(msgDelimiter);
        if (null != msg) {
            result.append(msg);
        }
        result.append(packetDemlimiter);
        return result.toString();
    }
}

package com.cuckoo.framework.navi.api;

import lombok.Getter;
import lombok.Setter;

import java.net.SocketAddress;

@Setter
@Getter
public class NaviRequestPacket {

    private String requestService;
    private String requestModule;
    private String requestApi;
    private String extraMsg;
    private byte[] packetContent;
    private SocketAddress remoteAddress;

    public NaviRequestPacket(String requestService, String requestModule, String requestApi, String extraMsg, byte[] packetContent) {
        this.requestService = requestService;
        this.requestModule = requestModule;
        this.requestApi = requestApi;
        this.extraMsg = extraMsg;
        this.packetContent = packetContent;
    }

    public int size() {
        return (null != requestService && !"".equals(requestService) ? requestService.length() : 0)
            + (null != requestModule && !"".equals(requestModule) ? requestModule.length() : 0)
            + (null != requestApi && !"".equals(requestApi) ? requestApi.length() : 0)
            + (null != extraMsg && !"".equals(extraMsg) ? extraMsg.length() : 0)
            + (null != packetContent ? packetContent.length : 0);
    }

}

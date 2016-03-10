package com.cuckoo.framework.navi.engine.core;

public interface ILengthBasedUDPService extends INaviUDPClientService {
    public byte[] parseUDPPacket(String service, String module, String api, String extra,
                                 byte[] msg);
}

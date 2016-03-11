package com.youku.java.navi.engine.datasource.service;

import com.youku.java.navi.engine.core.ILengthBasedUDPService;

public class LengthBasedUDPService extends NaviUDPClientService implements ILengthBasedUDPService {

    public byte[] parseUDPPacket(String service, String module, String api, String extra,
                                 byte[] msg) {
        int headerLength = 0;
        if (null != service && !"".equals(service)) {
            headerLength = headerLength + service.getBytes().length;
        }
        if (null != module && !"".equals(module)) {
            headerLength = headerLength + module.getBytes().length;
        }
        if (null != api && !"".equals(api)) {
            headerLength = headerLength + api.getBytes().length;
        }
        if (null != extra && !"".equals(extra)) {
            headerLength = headerLength + extra.getBytes().length;
        }
        int contentLength = null == msg ? 0 : msg.length;
        int totalLength = headerLength + contentLength + 4 * 6;
        byte[] array = new byte[totalLength];
        int start = 0;
        byte[] lengthBytes = null;
        //total length
        lengthBytes = intToBytes(totalLength);
        System.arraycopy(lengthBytes, 0, array, start, lengthBytes.length);
        start = start + 4;
        //service length
        lengthBytes = intToBytes(null == service ? 0 : service.getBytes().length);
        System.arraycopy(lengthBytes, 0, array, start, lengthBytes.length);
        start = start + 4;
        if (null != service && !"".equals(service)) {
            System.arraycopy(service.getBytes(), 0, array, start, service.getBytes().length);
            start = start + service.getBytes().length;
        }
        lengthBytes = intToBytes(null == module ? 0 : module.getBytes().length);
        System.arraycopy(lengthBytes, 0, array, start, lengthBytes.length);
        start = start + 4;
        if (null != module && !"".equals(module)) {
            System.arraycopy(module.getBytes(), 0, array, start, module.getBytes().length);
            start = start + module.getBytes().length;
        }
        lengthBytes = intToBytes(null == api ? 0 : api.getBytes().length);
        System.arraycopy(lengthBytes, 0, array, start, lengthBytes.length);
        start = start + 4;
        if (null != api && !"".equals(api)) {
            System.arraycopy(api.getBytes(), 0, array, start, api.getBytes().length);
            start = start + api.getBytes().length;
        }
        lengthBytes = intToBytes(null == extra ? 0 : extra.getBytes().length);
        System.arraycopy(lengthBytes, 0, array, start, lengthBytes.length);
        start = start + 4;
        if (null != extra && !"".equals(extra)) {
            System.arraycopy(extra.getBytes(), 0, array, start, extra.getBytes().length);
            start = start + extra.getBytes().length;
        }
        lengthBytes = intToBytes(contentLength);
        System.arraycopy(lengthBytes, 0, array, start, lengthBytes.length);
        start = start + 4;
        if (contentLength > 0) {
            System.arraycopy(msg, 0, array, start, msg.length);
        }
        return array;
    }

    /**
     * 将int数值转换为占四个字节的byte数组，本方法适用于(高位在前，低位在后)的顺序。  和bytesToInt2（）配套使用
     */
    public byte[] intToBytes(int value) {
        byte[] src = new byte[4];
        src[0] = (byte) ((value >> 24) & 0xFF);
        src[1] = (byte) ((value >> 16) & 0xFF);
        src[2] = (byte) ((value >> 8) & 0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }

}

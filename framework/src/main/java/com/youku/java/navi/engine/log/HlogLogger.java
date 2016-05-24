package com.youku.java.navi.engine.log;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HlogLogger {

    public static void busiLog(String namespace, String category,
                               JSONArray data_infos) {
        if (data_infos == null || data_infos.size() == 0) {
            return;
        }
        JSONObject json = new JSONObject();
        pieceDataInfo(json, namespace, category, data_infos);
        log.info(buildUDPPacket(json.toString()));
    }

    public static void busiLog(JSONArray data_infos) {
        HlogLogger.busiLog(null, null, data_infos);
    }

    public static void busiLog(String namespace, String category,
                               JSONObject data_info) {
        if (data_info == null || data_info.size() == 0) {
            return;
        }
        JSONArray data_infos = new JSONArray();
        data_infos.add(data_info);
        HlogLogger.busiLog(namespace, category, data_infos);
    }

    public static void busiLog(JSONObject data_info) {
        HlogLogger.busiLog(null, null, data_info);
    }

    public static void busiLog(String data) {
        HlogLogger.busiLog(null, null, data);
    }

    public static void busiLog(String namespace, String category, String data) {
        JSONObject data_info = new JSONObject();
        data_info.put("data", data);
        HlogLogger.busiLog(namespace, category, data_info);
    }

    public static void pieceDataInfo(JSONObject json, String namespace,
                                     String category, JSONArray data_infos) {
        if (namespace == null || namespace.equals("")) {
            namespace = "test2014";
        }
        if (category == null || category.equals("")) {
            category = "log4j";
        }
        json.put("namespace", namespace);
        json.put("category", category);
        json.put("data_info", data_infos);
    }

    private static String buildUDPPacket(String data) {
        StringBuilder msg = new StringBuilder();
        // service
        msg.append("hlogsys" + "\2");
        // module
        msg.append("hlogsys" + "\2");
        // action
        msg.append("udp_push.json" + "\2");
        // extra
        msg.append("0001");
        msg.append("\3");
        msg.append(data).append("\4");
        return msg.toString();
    }

}

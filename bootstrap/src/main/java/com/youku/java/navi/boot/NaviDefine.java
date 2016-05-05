package com.youku.java.navi.boot;

public interface NaviDefine {

    String NAVI_HOME = System.getProperty("NAVI_HOME");
    String NAVI_MODULES = NAVI_HOME + "/NaviModules/";
    String NAVI_LIBS = NAVI_HOME + "/NaviLibs/";
    String NAVI_CONF_PATH = NAVI_HOME + "/conf/server.conf";
    String NAVI_LOGBACK_PATH = NAVI_HOME + "/conf/logback.xml";

    String PREFIX_NAVI_BATCH = "batch_";

    String HTTP = "http";
    String TCP = "tcp";
    String UDP = "udp";
    String HEADER_DELIMITER = "header_delimiter";
    String CONTENT_DELIMITER = "content_delimiter";
    String PACKET_DELIMITER = "packet_delimiter";
    String PACKET_MAX_SIZE = "packet_max_size";


    //服务基本信息
    String PROTOCOL = "protocol";
    String SERVER = "server.name";
    String PORT = "listen.port";
    String MODE = "mode";
    String MODULE_LOAD_INTERVAL = "module.load.interval";

    //netty channel配置
    String BACKLOG = "backlog";
    String REUSEADDRESS = "reuseAddress";

    //child设置
    String CHILD_CHANNEL_CLOSE = "child.channel.close";
    String CHILD_CHANNEL_IDLTIME = "child.channel.idltime";
    String CHILD_KEEPALIVE = "child.keepAlive";
    String CHUNK_AGGR_SIZE = "chunk.size";
    String CHILD_TCPNODELAY = "child.tcpNoDelay";
    String CHILD_SENDBUFFERSIZE = "child.sendBufferSize";
    String CHILD_RECEIVEBUFFERSIZE = "child.receiveBufferSize";

    String WRITEBUFFERHIGHWATERMARK = "writeBufferHighWaterMark";
    String WRITEBUFFERLOWWARTERMARK = "writeBufferLowWaterMark";

    //moudle enter class
    //http server enter class
    //运行后台模块必需
    String DAEMON_MODULE_NAME="daemon_module_name";
    String DAEMON_CLASS_ARGS="daemon_class_args";

    String SERVERCLASS = "com.youku.java.navi.server.NaviNettyServer";
    String SERVERCLASS_UDP = "com.youku.java.navi.server.NaviNettyUDPServer";
    String SERVERCLASS_TCP = "com.youku.java.navi.server.NaviNettyTCPServer";

    String DAEMON_SERVERCLASS = "com.youku.java.navi.server.NaviDaemonServer";
    String ASYNC_SERVERCLASS = "com.youku.java.navi.server.NaviAsyncServer";

    //default setting
    String DEFAULT_PORT = "8080";
    String DEFAULT_SERVER = "javanavi";
    String DEFAULT_CHUNK_SIZE = "307200";

    String DEFAULT_HEADER_DELIMITER = "\2";
    String DEFAULT_CONTENT_DELIMITER = "\3";
    String DEFAULT_PACKET_DELIMITER = "\4";
    int DEFAULT_MAX_TCP_PACKET_SIZE = 1048576;
    int DEFAULT_MAX_UDP_PACKET_SIZE = 2048;

    String REDIRECT_STR = "redirect_str";

    //其他
    String REQUEST_TIMEOUT = "request_timeout";
    String UDP_MAX_PACKET_SIZE = "udp_max_packet_size";
    String TCP_MAX_PACKET_SIZE = "tcp_max_packet_size";

    enum WORK_MODE {

        DEV, TEST, DEPLOY;

        public static WORK_MODE toEnum(String mode) {
            if (mode == null) {
                return DEV;
            }

            for (WORK_MODE env : values()) {
                if (env.toString().toLowerCase().equals(mode)) {
                    return env;
                }
            }

            return DEV;
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }

    }

    enum WORK_PROTOCOL {

        HTTP, TCP, UDP;

        public static WORK_PROTOCOL toProtocol(String protocol) {
            if (protocol == null) {
                return HTTP;
            }
            for (WORK_PROTOCOL p : values()) {
                if (p.toString().toLowerCase().equals(protocol)) {
                    return p;
                }
            }
            return HTTP;
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

}

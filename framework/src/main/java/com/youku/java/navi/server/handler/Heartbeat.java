package com.youku.java.navi.server.handler;

import java.io.Serializable;


public final class Heartbeat implements Serializable {

    private static final long serialVersionUID = -6807761754610304465L;

    public static final byte[] BYTES = new byte[0];
    private static Heartbeat instance = new Heartbeat();

    public static Heartbeat getSingleton() {
        return instance;
    }

    private Heartbeat() {
    }
}

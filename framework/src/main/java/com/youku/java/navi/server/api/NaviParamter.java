package com.youku.java.navi.server.api;

import lombok.Getter;
import lombok.Setter;

/**
 * API 参数封装
 *
 */
@Setter
@Getter
public class NaviParamter {

    private String name;
    private String type;
    private boolean requiered;

}

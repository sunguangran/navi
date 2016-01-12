package com.cuckoo.framework.navi.server.api;

import lombok.Getter;
import lombok.Setter;

/**
 * API 参数封装
 */
@Setter
@Getter
public class NaviParameter {

    private String name;
    private String type;
    private boolean required;

}

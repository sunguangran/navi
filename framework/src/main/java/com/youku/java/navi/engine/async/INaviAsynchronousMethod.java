package com.youku.java.navi.engine.async;

import java.util.List;


public interface INaviAsynchronousMethod {

    Object invoke(List<String[]> paramsList) throws Exception;

    boolean getBatch();

}

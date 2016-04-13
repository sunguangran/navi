package com.youku.java.navi.engine.async;

import java.util.List;


public interface INaviAsynchronousMethod {

    public Object invoke(List<String[]> paramsList) throws Exception;

    public boolean getBatch();

}

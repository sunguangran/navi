package com.youku.java.navi.engine.async;

import com.youku.java.navi.utils.AlibabaJsonSerializer;

import java.util.List;

public abstract class AbstractNaviAsynchronousMethod implements INaviAsynchronousMethod {

    private AlibabaJsonSerializer jsonSerializer = new AlibabaJsonSerializer();

    public Object invoke(List<String[]> paramsList) throws Exception {
        if (getBatch()) {
            return doBacthInvoke(paramsList);
        } else {
            return doInvoke(paramsList.get(0));
        }
    }

    public Object doInvoke(String[] params) throws Exception {
        return null;
    }

    public Object doBacthInvoke(List<String[]> paramsList) throws Exception {
        return null;
    }

    protected <K> K getParam(String str, Class<K> classNm) {
        if (classNm.equals(String.class)) {
            return classNm.cast(str);
        }

        return jsonSerializer.getObjectFromJsonStr(str, classNm);
    }

    public boolean getBatch() {
        return false;
    }

}

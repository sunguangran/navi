package com.youku.java.navi.server.api;

import org.jboss.netty.buffer.ChannelBuffer;

public interface INaviResponseData {

    ChannelBuffer getResponseData() throws Exception;

    String getResponseType();

    void setCost(long cost);

    void setData(Object data);

    void setPage(int page);

    void setPageLength(int pageLength);

}

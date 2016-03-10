package com.youku.java.navi.server.handler;

import com.youku.java.navi.server.api.INaviPacketAction;
import com.youku.java.navi.server.api.NaviRequestPacket;
import com.youku.java.navi.server.module.INaviModuleContext;
import com.youku.java.navi.server.api.NaviResponsePacket;
import com.youku.java.navi.common.NAVIERROR;
import com.youku.java.navi.common.exception.NaviSystemException;
import com.youku.java.navi.server.module.NaviModuleContextFactory;

public class DefaultNaviPacketDispatcher extends AbstractNaviPacketDispatcher {

    @Override
    public void callApi(NaviRequestPacket packet, NaviResponsePacket response)
        throws Exception {

        INaviModuleContext moduleCtx = NaviModuleContextFactory.getInstance()
            .getNaviModuleContext(packet.getRequestModule());
        if (moduleCtx == null) {
            throw new NaviSystemException("module " + packet.getRequestModule()
                + " not found!", NAVIERROR.SYSERROR.code());
        }
        INaviPacketAction bean = (INaviPacketAction) moduleCtx.getBean(packet.getRequestApi());
        if (bean == null) {
            throw new NaviSystemException("api " + packet.getRequestApi()
                + " not found!", NAVIERROR.SYSERROR.code());
        }
        bean.action(packet, response);
    }

}

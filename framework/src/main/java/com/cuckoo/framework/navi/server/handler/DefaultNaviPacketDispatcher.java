package com.cuckoo.framework.navi.server.handler;

import com.cuckoo.framework.navi.api.INaviPacketAction;
import com.cuckoo.framework.navi.api.NaviRequestPacket;
import com.cuckoo.framework.navi.module.INaviModuleContext;
import com.cuckoo.framework.navi.api.NaviResponsePacket;
import com.cuckoo.framework.navi.common.NAVIERROR;
import com.cuckoo.framework.navi.common.NaviSystemException;
import com.cuckoo.framework.navi.module.NaviModuleContextFactory;

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

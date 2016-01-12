package com.cuckoo.framework.navi.server.handler;

import com.cuckoo.framework.navi.common.NaviError;
import com.cuckoo.framework.navi.common.exception.NaviSystemException;
import com.cuckoo.framework.navi.server.api.INaviPacketAction;
import com.cuckoo.framework.navi.server.api.NaviRequestPacket;
import com.cuckoo.framework.navi.server.api.NaviResponsePacket;
import com.cuckoo.framework.navi.server.module.INaviModuleContext;
import com.cuckoo.framework.navi.server.module.NaviModuleContextFactory;

public class DefaultNaviPacketDispatcher extends AbstractNaviPacketDispatcher {

    @Override
    public void callApi(NaviRequestPacket packet, NaviResponsePacket response) throws Exception {

        INaviModuleContext moduleCtx = NaviModuleContextFactory.getInstance().getNaviModuleContext(packet.getRequestModule());
        if (moduleCtx == null) {
            throw new NaviSystemException("module " + packet.getRequestModule() + " not found!", NaviError.SYSERROR.code());
        }

        INaviPacketAction bean = (INaviPacketAction) moduleCtx.getBean(packet.getRequestApi());
        if (bean == null) {
            throw new NaviSystemException("api " + packet.getRequestApi() + " not found!", NaviError.SYSERROR.code());
        }

        bean.action(packet, response);
    }

}

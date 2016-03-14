package com.youku.java.navi.dto.request;

import com.youku.java.navi.common.NaviError;
import com.youku.java.navi.dto.response.BaseResponse;
import com.youku.java.navi.server.api.NaviHttpRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * @author sgran<sunguangran@youku.com>
 * @since 2015/6/10
 */
@Slf4j
public abstract class ABaseRequest {

    public BaseResponse check(NaviHttpRequest request) {
        BaseResponse resp = new BaseResponse();

        // common check

        resp.setCode(NaviError.ACTION_SUCCED);
        return resp;
    }

}

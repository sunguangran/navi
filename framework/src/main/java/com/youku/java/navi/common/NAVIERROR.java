package com.youku.java.navi.common;

public interface NaviError {

    // 业务公共错误码定义
    int ACTION_SUCCED           = 0;

    int ACTION_FAILED           = -1;
    int ERR_REDIS               = -100;
    int ERR_REDIS_KEY           = -101;
    int ERR_NO_DATA             = -102;
    int ERR_DBS                 = -103;
    int ERR_PAGE_PARAM          = -104;
    int ERR_CACHE_MAXITEMS      = -105;
    int ERR_PARAMS              = -106;
    int ERR_OVERMAXSIZE         = -200;
    int ERR_NOMOREDATA          = -201;
    int ERR_DUPLICATE_KEY       = -300;



    int ACTION_NOT_SUPPORTED    = -404;
    int SYSERROR                = -500;
    int INVALID_HOST            = -90;
    int BUSI_PARAM_ERROR        = -92;

}

package com.youku.java.navi.server.module;

public interface INaviModuleContext {

    /**
     * 从模块中获取Bean对象
     *
     * @return
     * @throws Exception
     */
    Object getBean(String name) throws Exception;

    String getBeanId(Class<?> clazz) throws Exception;

    /**
     * 初始化module配置,推荐调用一次
     *
     * @return
     * @throws Exception
     */
    INaviModuleContext initModule() throws Exception;

    /**
     * 更新module配置
     *
     * @return
     * @throws Exception
     */
    INaviModuleContext refresh() throws Exception;

    /**
     * 关闭模块bean资源
     *
     * @throws Exception
     */
    void close() throws Exception;

}

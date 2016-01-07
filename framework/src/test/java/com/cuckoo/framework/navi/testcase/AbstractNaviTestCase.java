package com.cuckoo.framework.navi.testcase;

import com.cuckoo.framework.navi.server.module.INaviModuleContext;
import com.cuckoo.framework.navi.server.module.NaviModuleContextFactory;
import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;

/**
 * 单元测试抽象类。
 * 初始化navi模块，直接在getNaviClass获取所要测试的类
 */
@Slf4j
public class AbstractNaviTestCase extends TestCase {

    INaviModuleContext cxt;
    String moduleName;

    public void setUp() {
        try {
            cxt = NaviModuleContextFactory.getInstance().getNaviModuleContext(
                moduleName);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            log.info("Navi模块匹配失败，检查模块名");
            // e.printStackTrace();
        }
    }

    /**
     * 获取项目模块中所要测试的类
     */
    public Object getNaviClass(String str) {
        try {
            return cxt.getBean(str);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            log.info("所测试的类匹配失败，检查类名");
            e.printStackTrace();
            return null;
        }
    }

    // 在执行每个test之后，都要执行tearDown
    public void tearDown() {
    }

    public String getModuleName() {
        return moduleName;
    }

    /**
     * 测试之前，需要设置测试的模块名
     */
    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

}

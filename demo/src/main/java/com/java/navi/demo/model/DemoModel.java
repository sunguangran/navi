package com.java.navi.demo.model;

import com.java.navi.demo.common.ErrorCode;
import com.java.navi.demo.dao.DemoDao;
import com.java.navi.demo.dao.DemoStringDao;
import com.java.navi.demo.dto.db.TDemo;
import com.java.navi.demo.dto.db.TDemoString;
import com.youku.java.navi.dto.BaseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.exceptions.JedisException;

/**
 * demo model
 *
 * @author sgran<sunguangran@youku.com>
 * @since 16/5/23
 */
@Slf4j
@Service
public class DemoModel {

    @Autowired
    private DemoDao demoDao;

    @Autowired
    private DemoStringDao demoStringDao;

    public BaseResult<TDemo> createDemoModel(String name, String ccid) {
        BaseResult<TDemo> result = new BaseResult<>();
        try {
            TDemo dto = demoDao.createDemo(ccid, name);
            if (null == dto) {
                result.setCode(ErrorCode.ACTION_FAILED);
                result.setMsg("create demo dto failed");
                return result;
            }

            result.makeSuccess();
            result.setData(dto);
        } catch (JedisException e) {
            result.setCode(ErrorCode.ACTION_FAILED);
            result.setMsg("create demo dto failed, " + e.getMessage());
        }

        return result;
    }

    public BaseResult<TDemoString> createDemoStringModel(String name, String ccid) {
        BaseResult<TDemoString> result = new BaseResult<>();
        try {
            TDemoString dto = demoStringDao.createDemoString(ccid, name);
            if (null == dto) {
                result.setCode(ErrorCode.ACTION_FAILED);
                result.setMsg("create demo string dto failed");
                return result;
            }

            result.makeSuccess();
            result.setData(dto);
        } catch (JedisException e) {
            result.setCode(ErrorCode.ACTION_FAILED);
            result.setMsg("create demo string dto failed, " + e.getMessage());
        }

        return result;
    }

}

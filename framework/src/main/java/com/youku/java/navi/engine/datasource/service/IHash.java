package com.youku.java.navi.engine.datasource.service;

import java.util.List;
import java.util.Map;

/**
 * 用于数据源出现多个分片时，对传入的key值进行自定义分组
 *
 * @author sgran<sunguangran@youku.com>
 * @since 2016/3/22
 */
public interface IHash {

    <F, T> Map<F, List<T>> groupKeys();

}

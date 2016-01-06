package com.cuckoo.framework.navi.engine.core;

import com.cuckoo.framework.navi.engine.redis.INaviMultiRedis;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface INaviCache extends IBaseDataService {

    /**
     * 设置key的生命周期
     *
     * @param <K>
     * @param key
     *     键
     * @param timeout
     *     超时时间
     * @return 成功返回true, 失败返回false
     */
    <K> boolean expire(K key, long timeout);

    /**
     * 设置key上绑定的val值，key存在设置key的val，不存在增加一对key-val数据
     *
     * @param <K>
     * @param <V>
     * @param key
     *     键
     * @param val
     *     键值
     * @return 成功返回true, 失败返回false
     */
    <K, V> boolean set(K key, V val);

    /**
     * 设置key上绑定的val值，key存在则不设置key的val，不存在增加一对key-val数据
     *
     * @param <K>
     * @param <V>
     * @param key
     *     键
     * @param val
     *     键值
     * @return 成功-1，失败-0, 异常-null
     */
    <K, V> Long setnx(K key, V val);

    /**
     * 设置key上绑定的val值，key存在设置key的val，不存在增加一对key-val数据，并设置数据存活时间$timeout
     *
     * @param <K>
     * @param <V>
     * @param key
     *     键
     * @param val
     *     键值
     * @param timeout
     *     超时时间
     * @return
     */
    <K, V> boolean setex(K key, V val, long timeout);

    /**
     * 获取key上绑定的val值,并在key上设置新值
     *
     * @param <K>
     * @param <V>
     * @param key
     *     键
     * @param val
     *     键值
     * @param classNm
     *     键值类型
     * @return key存在则返回对应val，不存在返回null
     */
    <K, V> V getSet(K key, V val, Class<V> classNm);

    /**
     * 获取key上绑定的val值
     *
     * @param <K>
     * @param <V>
     * @param key
     *     键
     * @param classNm
     * @return key存在则返回对应val，不存在返回null
     */
    <K, V> V get(K key, Class<V> classNm);

    /**
     * @param <K>
     * @param <V>
     * @param classNm
     * @param keys
     *     键
     * @return
     */
    <K, V> List<V> MGet(Class<V> classNm, K... keys);

    /**
     * 对key上绑定的intval值自动加 $delta 指定的值
     *
     * @param <K>
     * @param key
     *     键
     * @param delta
     *     增长量
     * @return 返回该键值上绑定值执行加法操作后的值，失败返回null
     */
    <K> Long incr(K key, long delta);

    /**
     * 对key上绑定的intval值自动减 $delta 指定的值
     *
     * @param <K>
     * @param key
     *     键
     * @param delta
     *     减少量
     * @return 返回该键值上绑定值执行减法操作后的值，失败返回null
     */
    <K> Long decr(K key, long delta);

    /**
     * 键值是否存在
     *
     * @param <K>
     * @param key
     *     键
     * @return 存在返回true，不存在返回false
     */
    <K> boolean exists(K key);

    /**
     * 删除键
     *
     * @param <K>
     * @param key
     *     键
     * @return 返回成功个数
     */
    <K> Long delete(K... keys);

    /**
     * 在sort_list批量插入一组节点
     *
     * @param <K>
     * @param key
     *     键
     * @param vals
     *     String类型的list(score0,member0,score1,member1,....score[n],member[n]);
     * @return 插入成功返回 新增节点个数,如果失败返回小于0,如果该节点存在于sort_list中返回 0
     */
    <K, V> Long zBatchAdd(K key, Map<V, Double> map);

    /**
     * 在sort_list插入一个节点
     *
     * @param <K>
     * @param <V>
     * @param key
     *     键
     * @param val
     *     节点值
     * @param score
     *     排序值
     * @return 插入成功返回 1,如果该节点存在于sort_list中返回 0
     */
    <K, V> Long zadd(K key, V val, double score);

    /**
     * 获取sort_list中节点的排序值
     *
     * @param <K>
     * @param <V>
     * @param key
     *     键
     * @param val
     *     节点值
     * @return 成功返回节点排序值
     */
    <K, V> Double zscore(K key, V val);

    /**
     * 获取sort_list上节点成员排序在一段数值区间中的节点(降序)
     *
     * @param <K>
     * @param <V>
     * @param key
     *     键
     * @param min
     *     最小排序值
     * @param max
     *     最大排序值
     * @param limit
     *     返回个数
     * @param skip
     *     跳过个数
     * @param classNm
     *     节点类型
     * @return 返回节点成员信息
     */
    <K, V> Set<V> zRevRangeByScore(K key, double min, double max, long limit, long skip, Class<V> classNm);

    /**
     * 获取sort_list上节点成员排序在一段数值区间中的节点(降序)
     *
     * @param <K>
     * @param key
     *     键
     * @param min
     *     最小排序值
     * @param max
     *     最大排序值
     * @param limit
     *     返回个数
     * @param skip
     *     跳过个数
     * @param classNm
     *     节点类型
     * @return 返回节点成员信息，包含排序值
     */
    <K, V> Set<TypedTuple<V>> zRevRangeByScoreWithScore(K key, double min, double max, long limit, long skip, Class<V> classNm);

    /**
     * 获取sort_list上节点成员排序在一段数值区间中的节点(升序)
     *
     * @param <K>
     * @param <V>
     * @param key
     *     键
     * @param min
     *     最小排序值
     * @param max
     *     最大排序值
     * @param limit
     *     返回个数
     * @param skip
     *     跳过个数
     * @param classNm
     *     节点类型
     * @return 返回节点成员信息
     */
    <K, V> Set<V> zRangeByScore(K key, double min, double max, long limit, long skip, Class<V> classNm);

    /**
     * 获取sort_list上节点成员排序在一段数值区间中的节点(升序)
     *
     * @param <K>
     * @param key
     *     键
     * @param min
     *     最小排序值
     * @param max
     *     最大排序值
     * @param limit
     *     返回个数
     * @param skip
     *     跳过个数
     * @return 返回节点成员信息，包含排序值
     */
    <K, V> Set<TypedTuple<V>> zRangeByScoreWithScore(K key, double min, double max, long limit, long skip, Class<V> classNm);

    /**
     * 获取sort_list上节点成员排序在一段数值区间中的节点个数
     *
     * @param <K>
     * @param key
     *     键
     * @param min
     *     最小排序值
     * @param max
     *     最大排序值
     * @return 返回该段数据的个数，失败返回null
     */
    <K> Long zCount(K key, double min, double max);

    /**
     * 获取sort_list节点个数
     *
     * @param <K>
     * @param key
     *     键
     * @return 返回节点个数
     */
    <K> Long zSize(K key);

    /**
     * 升序获取sort_list的一段节点数据
     *
     * @param <K>
     * @param <V>
     * @param key
     *     键
     * @param start
     *     开始位置
     * @param end
     *     结束位置，-1为队列尾部
     * @param classNm
     *     节点类型
     * @return sort_list上指定开始位置和结束位置的一段节点数据
     */
    <K, V> Set<V> zRange(K key, long start, long end, Class<V> classNm);

    /**
     * 升序获取sort_list的一段节点数据，结果包含打分
     *
     * @param <K>
     * @param key
     *     键
     * @param start
     *     开始位置
     * @param end
     *     结束位置，-1为队列尾部
     * @param classNm
     *     节点类型
     * @return sort_list上指定开始位置和结束位置的一段节点数据，包含排序值
     */
    <K, V> Set<TypedTuple<V>> zRangeWithScore(K key, long start, long end, Class<V> classNm);

    /**
     * 降序获取sort_list的一段节点数据
     *
     * @param <K>
     * @param <V>
     * @param key
     *     键
     * @param start
     *     开始位置
     * @param end
     *     结束位置，-1为队列尾部
     * @param classNm
     *     节点类型
     * @return sort_list上指定开始位置和结束位置的一段节点数据
     */
    <K, V> Set<V> zReverseRange(K key, long start, long end, Class<V> classNm);

    /**
     * 降序获取sort_list的一段节点数据
     *
     * @param <K>
     * @param key
     *     键
     * @param start
     *     开始位置
     * @param end
     *     结束位置，-1为队列尾部
     * @param classNm
     *     节点类型
     * @return sort_list上指定开始位置和结束位置的一段节点数据，包含排序值
     */
    <K, V> Set<TypedTuple<V>> zReverseRangeWithScore(K key, long start, long end, Class<V> classNm);

    /**
     * 删除sort_list的节点数据
     *
     * @param <K>
     * @param <V>
     * @param key
     *     键
     * @param val
     *     节点值
     * @return 成功返回true, 失败返回false
     */
    <K, V> boolean zDelete(K key, V val);

    /**
     * 删除sort_list索引区间[$start, $stop]的节点数据
     *
     * @param <K>
     * @param key
     *     键
     * @param start
     *     开始位置
     * @param end
     *     结束位置,-1为队列尾部
     * @return 成功返回实际删除个数
     */
    <K> Long zRemRangeByRank(K key, long start, long end);

    /**
     * 删除sort_list评分区间[$min_score, $max_score]的节点数据
     *
     * @param <K>
     * @param key
     *     键
     * @param min
     *     最小排序值
     * @param max
     *     最大排序值
     * @return 成功返回实际删除个数
     */
    <K> Long zRemRangeByScore(K key, double min, double max);

    /**
     * 从队列的头部插入多个节点
     *
     * @param <K>
     * @param <V>
     * @param key
     *     键
     * @param val
     *     节点值数组
     * @return 队列中节点个数
     */
    <K, V> Long lPush(K key, V... val);

    /**
     * 从队列头部弹出一个节点
     *
     * @param <K>
     * @param <V>
     * @param key
     *     键
     * @param classNm
     *     节点类型
     * @return 弹出的节点值，队列为空返回null
     */
    <K, V> V lPop(K key, Class<V> classNm);

    /**
     * 阻塞式从队列头部弹出一个节点
     *
     * @param <K>
     * @param <V>
     * @param key
     *     键
     * @param 超时时间
     * @param classNm
     *     节点类型
     * @return 弹出的节点值
     */
    <K, V> V blPop(K key, int timeout, Class<V> classNm);

    /**
     * 从队列尾部插入多个节点
     *
     * @param <K>
     * @param <V>
     * @param key
     *     键
     * @param val
     *     节点值数组
     * @return 队列中节点个数
     */
    <K, V> Long rPush(K key, V... val);

    /**
     * 从队列尾部弹出一个节点
     *
     * @param <K>
     * @param <V>
     * @param key
     *     键
     * @param classNm
     *     节点类型
     * @return 弹出的节点值，队列为空时返回null
     */
    <K, V> V rPop(K key, Class<V> classNm);

    /**
     * 阻塞式从队列尾部弹出一个节点
     *
     * @param <K>
     * @param <V>
     * @param key
     *     键
     * @param timeout
     *     超时时间
     * @param classNm
     *     节点类型
     * @return 弹出的节点值
     */
    <K, V> V brPop(K key, int timeout, Class<V> classNm);

    /**
     * 获取队列的节点个数
     *
     * @param <K>
     * @param key
     *     键
     * @return 队列中节点的个数
     */
    <K> Long lSize(K key);

    /**
     * 获取queue队列上的一段数据
     *
     * @param <K>
     * @param <V>
     * @param key
     *     键
     * @param start
     *     开始位置
     * @param end
     *     结束位置，-1为队尾
     * @param classNm
     *     节点类型
     * @return queue队列上指定开始位置和结束位置的一段节点数据
     */
    <K, V> List<V> lGetRange(K key, long start, long end, Class<V> classNm);

    /**
     * 保留queue队列上的一段数据，删除其他数据
     *
     * @param <K>
     * @param key
     *     键
     * @param start
     *     开始位置
     * @param end
     *     结束位置，-1为队尾
     * @return 成功返回true，失败返回false
     */
    <K> boolean lTrim(K key, long start, long end);

    /**
     * 获取queue队列上位置为 $index 的节点数据
     *
     * @param <K>
     * @param <V>
     * @param key
     *     键
     * @param index
     *     关联位置，-1为队尾
     * @return 节点数据，不存在时返回null
     */
    <K, V> V lIndex(K key, long index, Class<V> classNm);

    /**
     * 在set批量插入一组节点
     *
     * @param <K>
     * @param <V>
     * @param key
     *     键
     * @param val
     *     值
     * @return 成功个数
     */
    <K, V> Long sBatchAdd(K key, V... val);

    /**
     * 在set上插入一个节点
     *
     * @param <K>
     * @param <V>
     * @param key
     *     键
     * @param val值
     * @return 成功个数
     */
    <K, V> Long sAdd(K key, V val);

    /**
     * 删除set上一个节点
     *
     * @param <K>
     * @param <V>
     * @param key
     *     键
     * @param val
     * @return 成功返回1，不存在该节点返回0
     */
    <K, V> Long sRem(K key, V val);

    /**
     * 从set上弹出一个成员
     *
     * @param <K>
     * @param <V>
     * @param key
     *     键
     * @param classNm
     *     返回类型
     * @return set上的成员，set为空返回null
     */
    <K, V> V sPop(K key, Class<V> classNm);

    /**
     * 获取set中所有成员
     *
     * @param <K>
     * @param <V>
     * @param key
     *     键
     * @param classNm
     *     返回类型
     * @return set中的所有成员，set为空返回null
     */
    <K, V> Set<V> sMembers(K key, Class<V> classNm);

    /**
     * 随机获取一个set成员
     *
     * @param <K>
     * @param <V>
     * @param key
     *     键
     * @param classNm
     *     返回类型
     * @return set中的一个成员，set为空返回null
     */
    <K, V> V sRandMember(K key, Class<V> classNm);

    /**
     * 获取set中成员个数
     *
     * @param <K>
     * @param <V>
     * @param key
     *     键
     * @return set中成员个数
     */
    <K, V> Long sSize(K key);

    /**
     * 判断$val是否是set中的成员
     *
     * @param <K>
     * @param <V>
     * @param key
     *     键
     * @param val
     *     值
     * @return 是返回1，否返回0
     */
    <K, V> Boolean sismember(K key, V val);

    /**
     * 批量获取一个hash表中多个成员键对应的值
     *
     * @param <K>
     * @param <V>
     * @param <F>
     * @param key
     *     键
     * @param fields
     *     成员键
     * @param classNm
     *     成员值类型
     * @return 成员值列表
     */
    <K, V, F> List<V> hMget(K key, F[] fields, Class<V> classNm);

    /**
     * 批量向hash表设置成员
     *
     * @param <K>
     * @param <V>
     * @param <F>
     * @param key
     *     键
     * @param hash
     *     成员键值对
     * @return 成功返回1, 失败返回0
     */
    <K, V, F> boolean hMset(K key, Map<F, V> hash);

    /**
     * 向hash表中添加一对成员键值对，如果成员键已存在则覆盖
     *
     * @param <K>
     * @param <V>
     * @param <F>
     * @param key
     *     键
     * @param field
     *     成员键
     * @param val
     *     成员值
     * @return 成功1，失败返回0
     */
    <K, V, F> Long hSet(K key, F field, V val);

    /**
     * 向hash表中添加一对成员键值对，如果成员键已存在不覆盖
     *
     * @param <K>
     * @param <V>
     * @param <F>
     * @param key
     *     键
     * @param field
     *     成员键
     * @param val
     *     成员值
     * @return 成功返回1，失败返回0
     */
    <K, V, F> Long hSetNX(K key, F field, V val);

    /**
     * 获取hash表中成员键对应的成员值
     *
     * @param <K>
     * @param <V>
     * @param <F>
     * @param key
     *     键
     * @param field
     *     成员键
     * @param classNm
     *     成员值类型
     * @return 成员值，不存在返回null
     */
    <K, V, F> V hGet(K key, F field, Class<V> classNm);

    /**
     * 删除hash表中的成员键
     *
     * @param <K>
     * @param <F>
     * @param key
     *     键
     * @param fields
     *     成员键
     * @return 成功个数
     */
    <K, F> Long hDel(K key, F... fields);

    /**
     * 获取hash表中成员个数
     *
     * @param <K>
     * @param key
     *     键
     * @return 成员个数
     */
    <K> Long hLen(K key);

    /**
     * 获取hash表中所有成员键
     *
     * @param <K>
     * @param <F>
     * @param key
     *     键
     * @param classNm
     *     成员键类型
     * @return 成员键set
     */
    <K, F> Set<F> hKeys(K key, Class<F> classNm);

    /**
     * 获取hash表中所有成员值
     *
     * @param <K>
     * @param <V>
     * @param key
     *     键
     * @param classNm
     *     成员值类型
     * @return 成员值列表
     */
    <K, V> List<V> hVals(K key, Class<V> classNm);

    /**
     * 获取hash表中所有成员键值对
     *
     * @param <K>
     * @param <V>
     * @param <F>
     * @param key
     *     键
     * @param fieldClassNm
     *     成员键类型
     * @param valueClassNm
     *     成员值类型
     * @return
     */
    <K, V, F> Map<F, V> hGetAll(K key, Class<F> fieldClassNm, Class<V> valueClassNm);

    /**
     * 判断hash表中是否拥有指定成员键
     *
     * @param <K>
     * @param <F>
     * @param key
     *     键
     * @param field
     *     成员键
     * @return 存在返回1，不存在返回0
     */
    <K, F> boolean hExists(K key, F field);

    /**
     * 自增hash表中的指定成员键的值
     *
     * @param <K>
     * @param <F>
     * @param key
     *     键
     * @param field
     *     成员键
     * @param delta
     *     自增数
     * @return 自增后成员值
     */
    <K, F> Long hIncrBy(K key, F field, long delta);

    /**
     * 设置key上追加的val值，key存在追加key的val，不存在增加一对key-val数据
     *
     * @param <K>
     * @param <V>
     * @param key
     *     键
     * @param val
     *     追加的值
     * @return 成功返回1
     */
    <K, V> Long append(K key, V val);

    /**
     * 获取key剩下的过期时间
     *
     * @param <K>
     * @param key
     *     键
     * @return 键不存在或没有设置过期时间则返回-1，否则返回正整数或0
     */
    <K> Long ttl(K key);

    /**
     * 对key所存储的字符串值，设置或清除指定偏移量上的位(bit)
     *
     * @param <K>
     * @param key
     *     键
     * @param offset
     *     偏移量
     * @param val
     *     bit值
     * @return
     */
    <K> boolean setbit(K key, long offset, boolean val);

    /**
     * 对key所存储的字符串值，获取指定偏移量上的位(bit)
     *
     * @param <K>
     * @param key
     *     键
     * @param offset
     *     偏移量
     * @return bit值
     */
    <K> boolean getbit(K key, long offset);

    /**
     * 对传入的一组key进行分组
     *
     * @param keys
     * @return
     */
    <K> List<List<K>> groupKey(Class<K> classNm, K... keys);


    public <K> INaviMultiRedis multi(K key);

    public <K> INaviMultiRedis openPipeline(K key);

    public <K> Object eval(K key, String script, int keyCount, String... params);

    public <K> Object evalsha(K key, String sha, int keyCount, String... params);

    /**
     * Typed ZSet tuple.
     */
    public interface TypedTuple<V> extends Comparable<Double> {
        V getValue();

        Double getScore();
    }
}

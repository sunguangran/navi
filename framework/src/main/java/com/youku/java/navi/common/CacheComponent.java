package com.youku.java.navi.common;

import com.youku.java.navi.engine.core.INaviCache;
import com.youku.java.navi.server.serviceobj.AbstractNaviDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by user on 2015/6/15.
 */
@Slf4j
public class CacheComponent {

    public static <T extends AbstractNaviDto> String getRedisKey(Class<T> clazz, Object id) {
        String pre = null;
        if (clazz.isAnnotationPresent(CommentDocument.class)) {
            CommentDocument d = clazz.getAnnotation(CommentDocument.class);
            pre = d.key();
        }

        if (StringUtils.isEmpty(pre)) {
            pre = clazz.getName();
        }
        return pre + ":" + id;
    }

    public static <T extends AbstractNaviDto> String getRedisKey(Class<T> clazz, String appid, Object id) {
        String pre = null;
        if (clazz.isAnnotationPresent(CommentDocument.class)) {
            CommentDocument d = clazz.getAnnotation(CommentDocument.class);
            pre = d.key();
        }
        if (StringUtils.isEmpty(pre)) {
            pre = clazz.getName();
        }
        return pre + ":" + appid + ":" + id;
    }

    public static <T extends AbstractNaviDto> String getMqKey(T t) {
        String mq = null;
        int rate = 15 * 60000;
        if (t.getClass().isAnnotationPresent(CommentDocument.class)) {
            CommentDocument d = t.getClass().getAnnotation(CommentDocument.class);
            mq = d.mq();
            rate = d.rate();
        }
        int interval = (int) (new Date().getTime() / rate);
        return mq + ":" + interval;
    }

    public static int getExpire(Class<? extends AbstractNaviDto> clazz) {
        int expire = 60 * 60 * 24;
        if (clazz.isAnnotationPresent(CommentDocument.class)) {
            CommentDocument d = clazz.getAnnotation(CommentDocument.class);
            expire = d.expire();
        }
        return expire;
    }

    public static int getDefaultExpire() {
        return 60 * 60 * 24;
    }

    public static int getDefaultExpire_week() {
        return 604800; // 7 days
    }

    public static int getDefaultExpire_hour() {
        return 3600;
    }

    /**
     * uid->sortedset[(msg_id,msg_sid),...]
     *
     * @param uid
     * @return
     */
    public static String getUserMsgKey(String uid) {
        return "USER_MSG_LST:" + uid;
    }

    public static String getUserMsgKey_convertBack(String key) {
        return key.substring(key.indexOf(":") + 1);
    }

    public static void main(String[] args) {
        System.out.println(CacheComponent.getUserMsgKey_convertBack("xxxxxxxxx:xyyyyyyyy"));
        System.out.println(CacheComponent.convertKeyToId("xxxxxxxxx:1xyyyyyyyy"));
    }

    public static String convertKeyToId(String key) {
        return key.substring(key.indexOf(":") + 1);
    }

    /**
     * msg->sortedset[(comment_id,comment_sid),...]
     *
     * @param msg_id
     * @return
     */
    public static String getMsgCommentKey(String msg_id) {
        return "MSG_CMT_LST:" + msg_id;
    }

    public static String getMsgCommentKey_convertBack(String key) {
        return key.substring("MSG_CMT_LST:".length());
    }

    /**
     * msg->sortedset[(comment_id,comment_sid),...]
     *
     * @param msg_id
     * @return
     */
    public static String getMsgCommentTimelineKey(String msg_id) {
        return "MSG_CMT_T_LST:" + msg_id;
    }

    public static String getMsgCommentTimelineKey_convertBack(String key) {
        return key.substring("MSG_CMT_T_LST:".length());
    }

    /**
     * comment->sortedset[(reply_id,reply_sid),...]
     *
     * @param cmt_id
     * @return
     */
    public static String getCommentReplyKey(String cmt_id) {
        return "CMT_RPLY_LST:" + cmt_id;
    }

    public static String getCommentReplyKey_convertBack(String key) {
        return key.substring(key.indexOf(":") + 1);
    }

    /**
     * 订阅列表的key，订阅列表结构为(friend_uid，订阅时间)组成的sortedset user ->sortedset[(friend_uid,create_time),...]
     *
     * @param uid
     * @return
     */
    public static String getSubscribeKey(String uid) {
        return "SUBSCRIBE_LST:" + uid;
    }

    /**
     * 订阅列表的key，订阅列表结构为(friend_uid,level)组成的hash
     *
     * @param uid
     * @return
     */
    public static String getSubscribeHashKey(String uid) {
        return "SUBSCRIBE_HASH:" + uid;
    }

    /**
     * 粉丝列表的key，粉丝列表结构为(粉丝id，订阅时间)组成的sortedset user ->sortedset[(follower_uid,sid),...]
     *
     * @param target_uid
     * @return
     */
    public static String getFollowerKey(String target_uid) {
        return "FOLLOWER_LST:" + target_uid;
    }

    /**
     * 好友(互粉)列表的key，订阅列表结构为(好友id，订阅时间)组成的sortedset user ->sortedset[(friend_uid,create_time),...]
     *
     * @param uid
     * @return
     */
    // public static String getFriendsKey(String uid) {
    // return "FRIENDS_LST:" + uid;
    // }

    /**
     * “关系存在”的key，存储在redisdb中，value是关系的level prefix:uid|targetUid:level
     *
     * @param relationId
     *            uid|targetUid
     * @return
     */
    // public static String getFollowingExistenceKey(String relationId) {
    // return "RELATION_FOLLOW:" + relationId;
    // }
    // public static String getFollowingExistenceKey_convertBack(String relationId) {
    // return relationId.substring("RELATION_FOLLOW:".length());
    // }

    /**
     * @param uid
     *            关注关系发起者的uid
     * @param followedUid
     *            被关注的uid
     * @return
     */
    // public static String getFollowedExistenceKey(String uid, String followedUid) {
    // return "RELATION_FOLLOWED:" + followedUid + "|" + uid;
    // }

    /**
     * 获取appid下的所有推荐用户，存储类型是set
     *
     * @param appid
     * @return
     */
    public static String getRecommendedUserKey(String appid) {
        return "CLOUDCOMMUNITY_REC_USER:" + appid;
    }

    /**
     * 获取为每个用户推荐用户的key值，存储类型是set
     *
     * @param appid
     * @return
     */
    public static String getRecommendedUserKey_perUser(String appid, String uid) {
        return "CLOUDCOMMUNITY_REC_USER:" + appid + ":" + uid;
    }

    /**
     * @param uid 收藏者UID
     * @return
     */
    public static String getFavoriteKey(String uid) {
        return "FAVORITE:" + uid;
    }

    /**
     * 判断key是否存在于缓存中。之所以用ttl而不是exist，是因为exist会赶上过期临界时间点，造成错误
     *
     * @param cacheService
     * @param key
     * @return
     */
    public static boolean existInCache(INaviCache cacheService, String key) {
        long ttl = 0;
        try {
            ttl = cacheService.ttl(key);
        } catch (Exception e) {
            log.error("cache error: " + e.getMessage(), e);
        }
        if (ttl == -1 || ttl >= 5) {
            return true;
        }
        return false;
    }

    /**
     * 判断key的状态是否 “在缓存，但是马上就要过期”
     *
     * @param cacheService
     * @param key
     * @return
     */
    public static boolean existInCache_ButAlmostExpire(INaviCache cacheService, String key) {
        long ttl = 0;
        try {
            ttl = cacheService.ttl(key);
        } catch (Exception e) {
        }
        if (ttl >= 0 && ttl < 5) {
            return true;
        }
        return false;
    }

    /**
     * 关注防刷需要的redis key
     *
     * @param type 1:检测一分钟内的关注次数 2：检测一小时内的关注次数
     * @param uid  关注发起者的uid
     * @return
     */
    public static String getFrequenceKeyOnFollowing(int type, String uid) {
        if (type == 1) {
            return "RELATION_FOLLOW_FREQUENCEKEY_MINUTE_" + uid;
        } else if (type == 2) {
            return "RELATION_FOLLOW_FREQUENCEKEY_HOUR_" + uid;
        } else {
            return null;
        }
    }

    /**
     * 被关注防刷需要的redis key
     *
     * @param type 1:检测一分钟内的关注次数 2：检测一小时内的关注次数
     * @param uid  被关注者的uid
     * @return
     */
    public static String getFrequenceKeyOnFollowed(int type, String uid) {
        if (type == 1) {
            return "RELATION_FOLLOWED_FREQUENCEKEY_MINUTE_" + uid;
        } else if (type == 2) {
            return "RELATION_FOLLOWED_FREQUENCEKEY_HOUR_" + uid;
        } else {
            return null;
        }
    }

    /**
     * 会话包含的用户ID
     *
     * @param cid 会话ID 当会话为帖子会话是CID即msg_id
     * @return
     */
    public static String getConversationUids(long cid) {
        return "CONVERSATION_MEMBERS:" + cid;
    }

    /**
     * 用户包含的会话ID
     *
     * @param uid 用户ID
     * @return
     */
    public static String getUidConversations(String uid) {
        return "MEMBER_CONVERSATIONS:" + uid;
    }

    /**
     * 用户私聊的标记
     *
     * @param uids 用户ID
     * @return
     */
    public static String getUidPrivateConversation(String uids) {
        return "PRIVATE_CONVERSATION:" + uids;
    }

    /**
     * 会话ID下的消息
     *
     * @param cid 会话ID
     * @return
     */
    public static String getConversationMsgs(long cid) {
        return "CONVERSATION_MSGS:" + cid;
    }

    public static Long getConversationMsgs_convertBack(String key) {
        return Long.valueOf(key.substring("CONVERSATION_MSGS:".length()));
    }

    public static String getCommentTempConversationKey(String msgid) {
        return "CMT_CONV:" + msgid;
    }

    /**
     * 用户收藏的会话ID
     *
     * @param uid 用户ID
     * @return
     */
    public static String getConversationFavorites(String uid) {
        return "FAVORITE_CONVERSATIONS:" + uid;
    }

    /**
     * 用户已退出的会话ID 只存一周
     *
     * @param uid 用户ID
     * @return
     */
    public static String getUserDelConversations(String uid) {
        return "USER_DEL_CONVERSATIONS:" + uid;
    }

    /**
     * 用户通知列表
     *
     * @param uid 用户ID
     * @return
     */
    public static String getUserNotices(String uid) {
        return "USER_NOTICES:" + uid;
    }

    /**
     * 系统通知列表
     *
     * @param appid
     * @return
     */
    public static String getSystemNotices(String appid) {
        return "SYSTEM_NOTICES:" + appid;
    }

    /**
     * 私信消息key 每条私信生成两个key 对应同一个t_letter_message的id value和score都是t_letter_message的id
     *
     * @param uid1
     * @param uid2
     * @return
     */
    public static String getLetterMessageKey(String uid1, String uid2) {
        return "LETTER_MESSAGE:" + uid1 + ":" + uid2;
    }

    /**
     * 私信列表key
     *
     * @param uid
     * @return
     */
    public static String getUserLettersKey(String uid) {
        return "USER_LETTERS:" + uid;
    }

    /**
     * message列表key
     *
     * @param msgid
     * @return
     */
    public static String getMessageKey(String msgid) {
        return "MESSAGE:" + msgid;
    }

    /**
     * hash
     *
     * @param uid
     * @return
     */
    public static String getUserBlackKey(String uid) {
        return "USER_BLACK:" + uid;
    }

    public static String getPassportUserKey(String appid, String uid) {
        return "PASSPORT_USER_INFO:" + appid + ":" + uid;
    }

    /**
     * 用户不感兴趣列表
     * set
     *
     * @param appid uid
     * @return
     */
    public static String getUninterestingUserKey(String appid, String uid) {
        return "UNINTERESTING_USER:" + appid + ":" + uid;
    }

    /**
     * 会话当日消息数列表
     * set
     *
     * @param appid uid
     * @return
     */
    public static String getConversationNumKey(String appid, long cid) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String date = dateFormat.format(new Date());
        return "CONVERSATION_MESSAGE_NUM:" + date + ":" + appid + ":" + cid;
    }

    public static String getIntervalKey(String appid, String uid) {
        return "COMMENT_INTERVAL:" + appid + ":" + uid;
    }

    public static int getIntervalTime() {
        return 5;
    }

    public static String getDefaultSubjectGroupKey(String appid) {
        return "CLOUDCOMMUNITY_SUBJECTGROUP_DEFAULT:" + appid;
    }

    public static String getEqDisplayKey(String brnadAndModelCombine) {
        return "CLOUDCOMMUNITY_EQ_DISPLAY:" + brnadAndModelCombine;
    }

    public static String getPointsKey(String appid, String uid) {
        return "CLOUDCOMMUNITY_MEMBER_POINTS:" + appid + ":" + uid;
    }

    /**
     * topic id -> sortedset[(msg_id,msg_sid),...]
     *
     * @param topicId
     * @return
     */
    public static String getTopicMsgKey(String topicId) {
        return "USER_TOPIC_LST:" + topicId;
    }

    public static String getTopicMsgKey_convertBack(String key) {
        return key.substring(key.indexOf(":") + 1);
    }

    public static String getLikeKeyByOid(String appid, String oid) {
        return "LIKE:" + appid + ":" + oid;
    }

    public static String getLikeUidKeyByOid(String appid, String oid) {
        return "LIKE:UID:" + appid + ":" + oid;
    }

}

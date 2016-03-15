package com.youku.java.navi.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author sgran<sunguangran@youku.com>
 * @since 2016/03/14
 */
@Slf4j
public class StringUtils extends org.apache.commons.lang.StringUtils {

    public static String encode(long vid) {
        String videoid;
        vid = vid << 2;
        videoid = new String(org.apache.commons.codec.binary.Base64.encodeBase64((vid + "").getBytes()));
        videoid = "X" + videoid;
        return videoid;
    }

    public static long decode(String code) {
        if (StringUtils.isEmpty(code) || code.length() <= 1 || 'X' != code.charAt(0)) {
            return -1;
        }

        String decode;
        try {
            decode = new String(com.alibaba.fastjson.util.Base64.decodeFast(code.substring(1)));
            return Long.parseLong(decode) >> 2;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return -1;
    }

    public static int getCurrentTimeBySeconds() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    public static void sortAsInteger(List<String> list) {
        List<Integer> intList = new ArrayList<>();
        for (String str : list) {
            intList.add(Integer.valueOf(str));
        }
        list.clear();

        Collections.sort(intList);

        for (Integer anIntList : intList) {
            list.add(anIntList.toString());
        }
    }

    /**
     * UTF-8编码格式判断
     *
     * @param text
     *     需要分析的数据
     * @return 是否为UTF-8编码格式
     */
    public static boolean isUTF8(String text) {
        byte[] rawtext = text.getBytes();
        int score = 0;
        int i, rawtextlen = 0;
        int goodbytes = 0, asciibytes = 0;

        // Maybe also use UTF8 Byte Order Mark: EF BB BF
        // Check to see if characters fit into acceptable ranges
        rawtextlen = rawtext.length;
        for (i = 0; i < rawtextlen; i++) {
            if ((rawtext[i] & (byte) 0x7F) == rawtext[i]) {
                // 最高位是0的ASCII字符
                asciibytes++;
                // Ignore ASCII, can throw off count
            } else if (-64 <= rawtext[i] && rawtext[i] <= -33
                // -0x40~-0x21
                && // Two bytes
                i + 1 < rawtextlen && -128 <= rawtext[i + 1]
                && rawtext[i + 1] <= -65) {
                goodbytes += 2;
                i++;
            } else if (-32 <= rawtext[i]
                && rawtext[i] <= -17
                && // Three bytes
                i + 2 < rawtextlen && -128 <= rawtext[i + 1]
                && rawtext[i + 1] <= -65 && -128 <= rawtext[i + 2]
                && rawtext[i + 2] <= -65) {
                goodbytes += 3;
                i += 2;
            }
        }
        if (asciibytes == rawtextlen) {
            return false;
        }
        score = 100 * goodbytes / (rawtextlen - asciibytes);

        // If not above 98, reduce to zero to prevent coincidental matches
        // Allows for some (few) bad formed sequences
        if (score > 98) {
            return true;
        } else if (score > 95 && goodbytes > 30) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isMobileNO(String mobile) {
        if (StringUtils.isEmpty(mobile)) {
            return false;
        }

        Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0-9])|(17[7]))\\d{8}$");
        Matcher m = p.matcher(mobile);
        return m.matches();
    }

    public static boolean isIp(String ip) {
        if (StringUtils.isEmpty(ip)) {
            return false;
        }

        Pattern pattern = Pattern.compile("^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$");
        return pattern.matcher(ip).matches();
    }

    /**
     * 验证输入的邮箱格式是否符合
     *
     * @param email
     * @return 是否合法
     */
    public static boolean isEmailAddress(String email) {

        if (StringUtils.isEmpty(email)) {
            return false;
        }
        boolean tag = true;
        final String pattern1 = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        final Pattern pattern = Pattern.compile(pattern1);
        final Matcher mat = pattern.matcher(email);
        if (!mat.find()) {
            tag = false;
        }
        return tag;
    }

    // 将127.0.0.1形式的IP地址转换成十进制整数，这里没有进行任何错误处理
    public static long ipToLong(String strIp) {
        long[] ip = new long[4];
        // 先找到IP地址字符串中.的位置
        int position1 = strIp.indexOf(".");
        int position2 = strIp.indexOf(".", position1 + 1);
        int position3 = strIp.indexOf(".", position2 + 1);
        // 将每个.之间的字符串转换成整型
        ip[0] = Long.parseLong(strIp.substring(0, position1));
        ip[1] = Long.parseLong(strIp.substring(position1 + 1, position2));
        ip[2] = Long.parseLong(strIp.substring(position2 + 1, position3));
        ip[3] = Long.parseLong(strIp.substring(position3 + 1));
        return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
    }

    // 将十进制整数形式转换成127.0.0.1形式的ip地址
    public static String longToIP(long longIp) {
        StringBuffer sb = new StringBuffer("");
        // 直接右移24位
        sb.append(String.valueOf((longIp >>> 24)));
        sb.append(".");
        // 将高8位置0，然后右移16位
        sb.append(String.valueOf((longIp & 0x00FFFFFF) >>> 16));
        sb.append(".");
        // 将高16位置0，然后右移8位
        sb.append(String.valueOf((longIp & 0x0000FFFF) >>> 8));
        sb.append(".");
        // 将高24位置0
        sb.append(String.valueOf((longIp & 0x000000FF)));
        return sb.toString();
    }

    public static String implode(List<String> list, String con) {
        StringBuilder builder = new StringBuilder();
        for (String tmp : list) {
            builder.append(con).append(tmp);
        }

        return builder.toString().substring(con.length());
    }

    public static String implode(String[] list, String con) {
        StringBuilder builder = new StringBuilder();
        for (String tmp : list) {
            builder.append(con).append(tmp);
        }

        return builder.toString().substring(con.length());
    }

    public static List<String> explode(String inStr, String delimiter) {
        List<String> list = new ArrayList<>();
        String arrStr[] = inStr.split(delimiter);
        for (int i = 0; i < arrStr.length; i++) {
            list.add(arrStr[i]);
        }
        return list;
    }

    /**
     * 全球局域网ip地址范围 10.0.0.0～10.255.255.255 　　 * 172.16.0.0～172.31.255.255 　　 * 192.168.0.0～192.168.255.255
     **/
    private static boolean isLanIp(String strIp) {
        if (!isIp(strIp)) {
            return true;
        }
        long ip = ipToLong(strIp);
        if (ip >= 167772160L && ip <= 184549375L) {
            return true;
        } else if (ip >= 2886729728L && ip <= 2887778303L) {
            return true;
        } else return ip >= 3232235520L && ip <= 3232301055L;
    }
    
}

package com.hyf.redis;

import com.hyf.bean.UserReadNews;
import com.hyf.service.UserService;

import com.hyf.utils.DataMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author: zhangocean
 * @Date: 2019/5/12 21:29
 * Describe: redis业务逻辑
 */
@Service
public class RedisService {

    @Autowired
    StringRedisUtil stringRedisUtil;
    @Autowired
    HashRedisUtil hashRedisUtil;
    @Autowired
    UserService userService;

    /**
     * 获得redis中用户的未读消息
     */
    public DataMap getUserNews(String username) {
        Map<String, Object> dataMap = new HashMap<>(2);
        int userId = userService.findIdByUsername(username);
        LinkedHashMap map = (LinkedHashMap) hashRedisUtil.getAllFieldAndValue(String.valueOf(userId));
        if(map.size() == 0){
            dataMap.put("result", 0);
        } else {
            int allNewNum = (int) map.get("allNewsNum");
            int commentNum = (int) map.get("commentNum");
            int leaveMessageNum = (int) map.get("leaveMessageNum");
            UserReadNews news = new UserReadNews(allNewNum, commentNum, leaveMessageNum);
            dataMap.put("result", news);
        }
        return DataMap.success().setData(dataMap);
    }

    /**
     * 已读一条消息时修改redis中的未读消息
     */
    public void readOneMsgOnRedis(int userId, int msgType) {
        LinkedHashMap map = (LinkedHashMap) hashRedisUtil.getAllFieldAndValue(String.valueOf(userId));
        int allNewsNum = (int) map.get("allNewsNum");
        hashRedisUtil.hashIncrement(String.valueOf(userId), "allNewsNum", -1);
        //如果总留言评论数为0则删除该key
        if(--allNewsNum == 0){
            hashRedisUtil.hashDelete(String.valueOf(userId), UserReadNews.class);
        } else if (msgType == 1){
            hashRedisUtil.hashIncrement(String.valueOf(userId), "commentNum", -1);
        } else {
            hashRedisUtil.hashIncrement(String.valueOf(userId), "leaveMessageNum", -1);
        }
    }

    /**
     * 已读所有消息时修改redis中的未读消息
     */
    public void readAllMsgOnRedis(int userId, int msgType) {
        LinkedHashMap map = (LinkedHashMap) hashRedisUtil.getAllFieldAndValue(String.valueOf(userId));
        int commentNum = (int) map.get("commentNum");
        int leaveMessageNum = (int) map.get("leaveMessageNum");
        if(commentNum == 0 || leaveMessageNum == 0){
            hashRedisUtil.hashDelete(String.valueOf(userId), UserReadNews.class);
        } else if (msgType == 1){
            hashRedisUtil.hashIncrement(String.valueOf(userId), "allNewsNum", -commentNum);
            hashRedisUtil.hashIncrement(String.valueOf(userId), "commentNum", -commentNum);
        } else {
            hashRedisUtil.hashIncrement(String.valueOf(userId), "allNewsNum", -leaveMessageNum);
            hashRedisUtil.hashIncrement(String.valueOf(userId), "leaveMessageNum", -leaveMessageNum);
        }
    }

    /**
     * 修改redis中的点赞未读量
     */
    public void readThumbsUpRecordOnRedis(String key, int increment){
        boolean thumbsUpNotReadIsExist = stringRedisUtil.hasKey(key);
        if(!thumbsUpNotReadIsExist){
            stringRedisUtil.set(key, 1);
        } else {
            stringRedisUtil.stringIncrement(key, increment);
        }
    }

    /**
     * 增加redis中的访客量
     */
    public Long addVisitorNumOnRedis(String key, Object field, long increment){
        boolean fieldIsExist = hashRedisUtil.hasHashKey(key, field);
        if(fieldIsExist){
            return hashRedisUtil.hashIncrement(key, field, increment);
        }
        return null;
    }

    /**
     * 向redis中保存访客量
     */
    public Long putVisitorNumOnRedis(String key, Object field, Object value){
        hashRedisUtil.put(key, field, value);
        return Long.valueOf(hashRedisUtil.get(key, field).toString());
    }

    /**
     * 获得redis中的访客记录
     */
    public Long getVisitorNumOnRedis(String key, Object field){
        boolean fieldIsExist = hashRedisUtil.hasHashKey(key, field);
        if(fieldIsExist){
            return Long.valueOf(hashRedisUtil.get(key, field).toString());
        }
        return null;
    }
}

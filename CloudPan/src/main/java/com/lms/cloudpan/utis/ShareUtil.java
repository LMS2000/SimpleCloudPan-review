package com.lms.cloudpan.utis;

import cn.hutool.extra.spring.SpringUtil;


import com.lms.cloudpan.constants.ShareConstants;
import com.lms.cloudpan.exception.BusinessException;
import com.lms.contants.HttpCode;
import com.lms.redis.RedisCache;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ShareUtil {


    //判断是否是锁定的资源

    public static void isSharedResource(Integer type,Integer uid,Integer targetId){
        RedisCache redisCache = SpringUtil.getBean(RedisCache.class);
        //模糊查询锁定的文件或者文件夹

        boolean flag=false;
        if(type.equals(ShareConstants.FILE_TYPE)){
            Set<String> matchedIds =
                    new HashSet<>(redisCache.keys(ShareConstants.SHARED_FILEIDS + uid + "*"));

            for (String matchedId : matchedIds) {
                Set<Integer> files = redisCache.getCacheSet(matchedId);
                if(files.contains(targetId)){
                    flag=true;break;
                }
            }
        }else{
            Set<String> matchedIds =
                    new HashSet<>(redisCache.keys("*"+ShareConstants.SHARED_FOLDERIDS + uid + "*"));
            for (String matchedId : matchedIds) {
                Set<Integer> files = redisCache.getCacheSet(matchedId);
                if(files.contains(targetId)){
                    flag=true;break;
                }
            }
        }
        if(flag){
            throw new BusinessException(HttpCode.PARAMS_ERROR,"文件或者文件夹处于分享状态");
        }
    }

    public static void isSharedResource(Integer type,Integer uid,List<Integer> targetIds){
            targetIds.forEach(targetId->{
                isSharedResource(type,uid,targetId);
            });
    }


    //删除分享缓存
    public static void deleteSharedResourceCache(Integer type,Integer uid,String sharekey){
        RedisCache redisCache = SpringUtil.getBean(RedisCache.class);
        if(type.equals(ShareConstants.FILE_TYPE)){
            String mapKey=ShareConstants.SHARE_MAP+sharekey;
            String clockey=ShareConstants.SHARED_FILEIDS+uid+"_"+sharekey;
            redisCache.deleteObject(mapKey);
            redisCache.deleteObject(clockey);
        }else{
            String mapKey=ShareConstants.SHARE_MAP+sharekey;
            String clocFolderkey=ShareConstants.SHARED_FOLDERIDS+uid+"_"+sharekey;
            String clockFileKey=ShareConstants.SHARED_FILEIDS+uid+"_"+sharekey;
            redisCache.deleteObject(mapKey);
            redisCache.deleteObject(clocFolderkey);
            redisCache.deleteObject(clockFileKey);
        }
    }


}

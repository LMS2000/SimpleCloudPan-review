package com.lms.cloudpan.utis;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

public class MybatisUtils {

    //查找符合条件的记录是否存在
    public static <T> boolean existCheck(IService<T> service, Map<String,Object> map){
        QueryWrapper<T> wrapper=new QueryWrapper<>();
        map.forEach(wrapper::eq);
        return service.count(wrapper)>0;
    }
}

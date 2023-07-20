package com.lms.cloudpan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lms.cloudpan.entity.dao.Shares;
import com.lms.cloudpan.entity.dto.CancelShareDto;
import com.lms.cloudpan.entity.dto.ShareDto;


public interface IShareService extends IService<Shares> {


    //分享接口
    String shareResource(ShareDto shareDto, Integer uid);


    //导入分享资源
    Boolean setShareResource(String shareKey,String shareSecret,String curPath,Integer uid);

    //取消分享

    Boolean cancelShare(CancelShareDto cancelShareDto, Integer uid);





}

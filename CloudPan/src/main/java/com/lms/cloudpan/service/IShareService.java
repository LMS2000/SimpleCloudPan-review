package com.lms.cloudpan.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lms.cloudpan.entity.dao.Shares;
import com.lms.cloudpan.entity.dto.*;
import com.lms.cloudpan.entity.vo.FileVo;
import com.lms.cloudpan.entity.vo.GetWebShareLoginVo;
import com.lms.cloudpan.entity.vo.SharesVo;

import javax.servlet.http.HttpSession;
import java.util.List;


public interface IShareService extends IService<Shares> {


    //分享接口
    SharesVo shareResource(ShareDto shareDto, Integer uid);


    //导入分享资源


    //取消分享

    Boolean cancelShare(List<String> ids, Integer uid);



    //分页查询分享文件列表
    Page<SharesVo>  pageShares(Integer uid, PageSharesDto pageSharesDto);


    GetWebShareLoginVo getShareInfo(String shareId,Integer uid);

    Boolean checkCode(String shareId, String code, HttpSession session);

    Page<FileVo> pageFileList(PageShareFileDto pageShareFileDto);

    String createDownloadUrl(String shareId,String fileId);

    Boolean saveShareFiles(SaveWebShareDto shareDto,Integer uid);
}

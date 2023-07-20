package com.lms.cloudpan.controller;


import com.lms.cloudpan.entity.dto.CancelShareDto;
import com.lms.cloudpan.entity.dto.ShareDto;
import com.lms.cloudpan.entity.vo.AuthorityVo;
import com.lms.cloudpan.service.IShareService;
import com.lms.cloudpan.utis.SecurityUtils;
import com.lms.result.EnableResponseAdvice;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/share")
@Api(tags = "文件管理")
@EnableResponseAdvice
public class ShareController {

    @Resource
    private IShareService shareService;

    @ApiOperation("创建分享链接")
    @PostMapping("/createLink")
    public String shareResource(@RequestBody ShareDto shareDto){
        Integer userId = SecurityUtils.getLoginUser().getUserId();
        return shareService.shareResource(shareDto,userId);
    }

    @ApiOperation("设置分享")
    @PostMapping("/{shareKey}/{shareSecret}")
    public Boolean setSharedResource(@PathVariable String shareKey,@PathVariable String shareSecret,@RequestParam("curPath") String curPath){
        Integer userId = SecurityUtils.getLoginUser().getUserId();
        return shareService.setShareResource(shareKey,shareSecret,curPath,userId);
    }
    @ApiOperation("取消分享链接")
    @PostMapping("/cancel")
    public Boolean cancelSharedResource(@RequestBody CancelShareDto cancelShareDto){
        Integer userId = SecurityUtils.getLoginUser().getUserId();
          return shareService.cancelShare(cancelShareDto,userId);
    }
}

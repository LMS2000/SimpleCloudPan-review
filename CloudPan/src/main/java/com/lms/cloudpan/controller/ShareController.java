package com.lms.cloudpan.controller;

import com.lms.cloudpan.entity.vo.CancelShareVo;
import com.lms.cloudpan.entity.vo.ShareVo;
import com.lms.cloudpan.service.IShareService;
import com.lms.cloudpan.utis.SecurityUtils;
import com.lms.result.EnableResponseAdvice;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/share")
@Api(tags = "文件管理")
@EnableResponseAdvice
public class ShareController {

    @Resource
    private IShareService shareService;
    @PostMapping("/createLink")
    public String shareResource(@RequestBody ShareVo shareVo){
        Integer userId = SecurityUtils.getLoginUser().getUserId();
        return shareService.shareResource(shareVo,userId);
    }

    @PostMapping("/{shareKey}/{shareSecret}")
    public Boolean setSharedResource(@PathVariable String shareKey,@PathVariable String shareSecret,@RequestParam("curPath") String curPath){
        Integer userId = SecurityUtils.getLoginUser().getUserId();
        return shareService.setShareResource(shareKey,shareSecret,curPath,userId);
    }

    @PostMapping("/cancel")
    public Boolean cancelSharedResource(@RequestBody CancelShareVo cancelShareVo){
        Integer userId = SecurityUtils.getLoginUser().getUserId();
          return shareService.cancelShare(cancelShareVo,userId);
    }
}

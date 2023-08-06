package com.lms.cloudpan.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lms.cloudpan.entity.dto.CancelShareDto;
import com.lms.cloudpan.entity.dto.PageSharesDto;
import com.lms.cloudpan.entity.dto.ShareDto;
import com.lms.cloudpan.entity.vo.SharesVo;
import com.lms.cloudpan.service.IShareService;
import com.lms.cloudpan.utils.SecurityUtils;
import com.lms.result.EnableResponseAdvice;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/share")
@Api(tags = "分享管理")
@EnableResponseAdvice
public class ShareController {

    @Resource
    private IShareService shareService;

    @ApiOperation("创建分享链接")
    @PostMapping("/createLink")
    public SharesVo shareResource(@Validated @RequestBody ShareDto shareDto){
        Integer userId = SecurityUtils.getLoginUser().getUserId();
        return shareService.shareResource(shareDto,userId);
    }


    @ApiOperation("取消分享链接")
    @PostMapping("/cancel")
    public Boolean cancelSharedResource(@RequestParam("ids") List<String> ids){
        Integer userId = SecurityUtils.getLoginUser().getUserId();
          return shareService.cancelShare(ids,userId);
    }

    @PostMapping("/page")
    @ApiOperation("分页查询分享文件列表")
    public Page<SharesVo> pageShares( @RequestBody PageSharesDto pageSharesDto){
        Integer userId = SecurityUtils.getLoginUser().getUserId();
            return shareService.pageShares(userId, pageSharesDto);
    }



}

package com.lms.cloudpan.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.infrastructure.jwt.Jwt;
import com.infrastructure.jwt.LoginUser;
import com.lms.cloudpan.constants.ShareConstants;
import com.lms.cloudpan.entity.dto.PageShareFileDto;
import com.lms.cloudpan.entity.dto.SaveWebShareDto;
import com.lms.cloudpan.entity.vo.FileVo;
import com.lms.cloudpan.entity.vo.GetWebShareLoginVo;
import com.lms.cloudpan.entity.vo.SharesVo;
import com.lms.cloudpan.exception.BusinessException;
import com.lms.cloudpan.service.IFileService;
import com.lms.cloudpan.service.IShareService;
import com.lms.cloudpan.utils.SecurityUtils;
import com.lms.result.EnableResponseAdvice;
import com.sun.net.httpserver.HttpsServer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/webShare")
@EnableResponseAdvice
@Api(tags = "外部分享管理")
public class WebShareController {

    @Resource
    private IFileService fileService;
    @Resource
    private IShareService shareService;

    @Resource
    private Jwt jwt;

    /**
     * 获取分享文件信息,无需权限访问，
     *
     * @param shareId
     * @return
     */
    @GetMapping("/getShareInfo/{shareId}")
    @ApiOperation("获取分享文件信息,无需权限访问")
    public GetWebShareLoginVo getShareInfo(@RequestHeader("token") String token,@PathVariable("shareId") String shareId) {
        Integer userId = jwt.getLoginUser(token).getUserId();
        return shareService.getShareInfo(shareId, userId);
    }


    @GetMapping("/getShareLoginInfo/{shareId}")
    @ApiOperation("防止绕过分享码")
    public GetWebShareLoginVo getWebShareLoginVo(@RequestHeader("token") String token,HttpSession session,@PathVariable("shareId") String shareId){
        SharesVo sharesVo =(SharesVo) session.getAttribute(ShareConstants.SESSION_SHARE_KEY + shareId);
        if(sharesVo==null)return null;
        Integer userId = jwt.getLoginUser(token).getUserId();
        return shareService.getShareInfo(shareId, userId);
    }



    /**
     * 检查分享码
     * @param shareId
     * @param code
     * @return
     */
    @PostMapping("/check/{shareId}/{code}")
    @ApiOperation("检查分享码")
    public Boolean checkCode(HttpSession session, @PathVariable("shareId") String shareId, @PathVariable("code") String code) {
        return shareService.checkCode(shareId, code,session);
    }

    /**
     * 获取分享信息的分页文件列表
     * @param pageShareFileDto
     * @return
     */
    @PostMapping("/page")
    @ApiOperation("获取分享信息的分页文件列表")
    public Page<FileVo> shareFileVoPage(@Validated @RequestBody PageShareFileDto pageShareFileDto){
        return shareService.pageFileList(pageShareFileDto);
    }


    @GetMapping("/getSaveFolder/{shareId}/{pid}")
    public List<FileVo> getSaveFolders(@RequestHeader("token") String token,HttpSession session, @PathVariable("pid") String pid, @PathVariable("shareId") String shareId){

        SharesVo sharesVo =(SharesVo) session.getAttribute(ShareConstants.SESSION_SHARE_KEY + shareId);
        if(sharesVo==null)return null;

        if(StringUtils.isEmpty(token)){
            return null;
        }
        Integer uid = jwt.getLoginUser(token).getUserId();
        return fileService.getSaveFolder(pid,uid);
    }

    /**
     * 创建下载链接
     * @param shareId
     * @param fileId
     * @return
     */
    @GetMapping("/createDownloadUrl/{shareId}/{fileId}")
    @ApiOperation("创建下载链接")
    public String createDownloadUrl(@PathVariable("shareId")  String shareId,
                                    @PathVariable("fileId")  String fileId) {
        return shareService.createDownloadUrl(shareId,fileId);
    }

    /**
     * 下载
     *
     * @param request
     * @param response
     * @throws Exception
     */
    @GetMapping("/download/{code}")
    @ApiOperation("下载文件")
    public Boolean download(HttpServletRequest request, HttpServletResponse response,
                         @PathVariable("code")  String code)  {
        return fileService.download(request,response,code);
    }

    /**
     * 保存到网盘
     * @param shareDto
     * @return
     */
    @PostMapping("/saveShare")
    public Boolean saveShare(@RequestHeader("token") String token, @RequestBody SaveWebShareDto shareDto){
        if(StringUtils.isEmpty(token)){
            return false;
        }
        Integer uid = jwt.getLoginUser(token).getUserId();
        return shareService.saveShareFiles(shareDto,uid);
    }

}

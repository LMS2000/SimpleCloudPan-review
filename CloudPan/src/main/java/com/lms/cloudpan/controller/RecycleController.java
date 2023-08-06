package com.lms.cloudpan.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lms.cloudpan.entity.dto.RecyclePageDto;
import com.lms.cloudpan.entity.vo.FileVo;
import com.lms.cloudpan.service.IFileService;
import com.lms.cloudpan.utils.SecurityUtils;
import com.lms.result.EnableResponseAdvice;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/recycle")
@EnableResponseAdvice
public class RecycleController {

    @Resource
    private IFileService fileService;


    /**
     * 分页查询回收站文件列表
     * @param recyclePageDto
     * @return
     */

    @PostMapping("/page")
    public Page<FileVo>  getRecycleFiles(@RequestBody RecyclePageDto recyclePageDto){
        Integer userId = SecurityUtils.getLoginUser().getUser().getUserId();
       return fileService.pageRecycleFiles(recyclePageDto,userId);
    }


    /**
     * 将选中文件的以及子文件变成删除状态
     *
     * @param ids
     * @return
     */
    @PostMapping("/delete")
    public Boolean deleteFiles( @NotNull @RequestParam("ids") List<String> ids) {
        Integer userId = SecurityUtils.getLoginUser().getUser().getUserId();
        return fileService.deleteRecycleFiles(ids, userId);
    }


    /**
     * 恢复文件
     * @param ids
     * @return
     */
    @PostMapping("/recover")
    public Boolean recoverFiles(@NotNull @RequestParam("ids") List<String> ids){
        Integer userId = SecurityUtils.getLoginUser().getUser().getUserId();
        return fileService.recoverFileBatch(ids, userId);
    }



}

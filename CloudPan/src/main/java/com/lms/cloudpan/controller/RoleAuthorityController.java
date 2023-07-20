package com.lms.cloudpan.controller;



import com.lms.cloudpan.aop.OpLog;
import com.lms.cloudpan.convert.StrToListFormatter;
import com.lms.cloudpan.entity.vo.AuthorityVo;
import com.lms.cloudpan.entity.vo.GetRoleAuthorityVo;
import com.lms.cloudpan.service.IRoleAuthorityService;
import com.lms.result.EnableResponseAdvice;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.DataBinder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;
import java.util.List;


@Validated
@RestController
@RequestMapping("/roleAuthority")
@RequiredArgsConstructor
@EnableResponseAdvice
@Api(tags = "角色权限请求")
public class RoleAuthorityController {
    private final IRoleAuthorityService iRoleAuthorityService;

    @InitBinder
    public void initBinder(DataBinder dataBinder){
        dataBinder.addCustomFormatter(new StrToListFormatter());
    }

    @ApiOperation("获取角色权限")
    @PostMapping("/{roleId}")
    @PreAuthorize("@ss.hasPermi('pan:roleAuthority:get')")
    public List<AuthorityVo> getAuthorityOfRole(@PathVariable("roleId")@Positive(message = "id不合法")Integer roleId){
        return iRoleAuthorityService.getAuthorityOfRole(roleId);
    }

//    @OpLog(desc = "权限%s赋予ID为%s的角色")
//    @ApiOperation("赋予角色权限")
//    @PostMapping("/releaseAuthorityToRole/{roleId}")
//    @PreAuthorize("@ss.hasPermi('pan:roleAuthority:releaseAuthorityToRole')")
//    public Boolean releaseAuthorityToRole(@RequestParam("authorityList")@ApiParam("权限id列表,由'-'分割权限id,如: 1-2-3") List<Integer> authorityList,
//                                       @PathVariable("roleId")@Positive(message = "id不合法")Integer roleId) {
//      return   iRoleAuthorityService.releaseAuthorityToRole(roleId,authorityList);
//    }
//
//    @OpLog(desc = "权限%s从ID为%s的角色处回收")
//    @ApiOperation("收回角色权限")
//    @GetMapping("/revokeAuthorityFromRole/{roleId}")
//    @PreAuthorize("@ss.hasPermi('pan:roleAuthority:revokeAuthorityFromRole')")
//    public Boolean revokeAuthorityFromRole(@RequestParam("authorityList")@ApiParam("权限id列表,由'-'分割权限id,如: 1-2-3") List<Integer> authorityList, @PathVariable("roleId")@Positive(message = "id不合法")Integer roleId) {
//     return    iRoleAuthorityService.revokeAuthorityFromRole(roleId,authorityList);
//    }


    /**
     * 获取权限树和角色的权限
     * @param rid
     * @return
     */
    @ApiOperation("获取权限树和角色的权限")
    @GetMapping("/roleTree/{rid}")
    public GetRoleAuthorityVo  getRoleAuthorityTree(@Positive(message = "id不合法") @PathVariable("rid") Integer rid){
        return iRoleAuthorityService.getAuthorityOfRoleTree(rid);
    }
}

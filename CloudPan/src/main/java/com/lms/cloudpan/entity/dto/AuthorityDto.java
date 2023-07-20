package com.lms.cloudpan.entity.dto;import io.swagger.annotations.ApiModelProperty;import lombok.AllArgsConstructor;import lombok.Builder;import lombok.Data;import lombok.NoArgsConstructor;import lombok.extern.slf4j.Slf4j;import javax.validation.constraints.NotBlank;import javax.validation.constraints.NotNull;import java.io.Serializable;@AllArgsConstructor@NoArgsConstructor@Data@Builder@Slf4jpublic class AuthorityDto implements Serializable {    private static final long serialVersionUID = 1L;    /**     * 启用     */    @ApiModelProperty("启用")    private Integer enable=1;    /**     * 资源名     */    @ApiModelProperty("权限名")    @NotBlank(message = "权限名不能为空")    @NotNull(message = "权限名不能为空")    private String name;    /**     * 资源描述     */    @ApiModelProperty("权限描述")    private String description;    @Override    public String toString() {        return "权限名: "+name+" ,权限描述: "+description+" 启用权限: "+((enable==0)?"是":"否");    }}
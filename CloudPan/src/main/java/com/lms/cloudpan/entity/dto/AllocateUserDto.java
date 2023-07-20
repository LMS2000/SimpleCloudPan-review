package com.lms.cloudpan.entity.dto;

import com.lms.page.CustomPage;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.io.Serializable;
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Slf4j
public class AllocateUserDto extends CustomPage implements Serializable {
    private static final long serialVersionUID = 1L;

    @Positive(message = "角色id不合法")
    @NotNull(message = "角色id不能为空")
    private Integer rid;

    private String username;

}

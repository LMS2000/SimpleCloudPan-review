package com.lms.cloudpan.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Slf4j
public class UserVo  implements Serializable {

    private static final long serialVersionUID = 1L;
    private Integer userId;

    private String username;

    private String email;
    private  Integer enable;
    private String avatar;
    private Long useQuota;
    private Long quota;
    private String remark;

    private List<Integer> rids;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;


}

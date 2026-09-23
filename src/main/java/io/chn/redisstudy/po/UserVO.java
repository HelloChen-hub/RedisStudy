package io.chn.redisstudy.po;

import lombok.Data;

@Data
public class UserVO {
    private Long id;

    private String username;

    private String phone;

    private String roleCode;
}

package io.chn.redisstudy.common;

import lombok.Data;

@Data
public class LoginUser {
    private Long id;
    private String phone;
    private String roleCode;
}

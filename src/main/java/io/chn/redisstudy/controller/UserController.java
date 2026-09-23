package io.chn.redisstudy.controller;

import io.chn.redisstudy.common.LoginUser;
import io.chn.redisstudy.po.UserVO;
import io.chn.redisstudy.service.UserService;
import io.chn.redisstudy.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {
    //依赖注入
    private final UserService userService;

    public UserController (UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Result<LoginUser> login(@RequestBody UserVO userVO) {
        LoginUser result = userService.login(userVO);
        return Result.success(result);
    }

    @GetMapping("/getUser")
    public Result<UserVO> getUser(@RequestParam("id") Long id) {
        UserVO userVO = userService.getUserById(id);
        return Result.success(userVO);
    }

}

package io.chn.redisstudy.service;

import com.baomidou.mybatisplus.spring.service.IService;
import io.chn.redisstudy.common.LoginUser;
import io.chn.redisstudy.entity.User;
import io.chn.redisstudy.po.UserVO;
import org.springframework.stereotype.Service;

@Service
public interface UserService extends IService<User> {
    LoginUser login(UserVO userVO);

    UserVO getUserById(Long id);
}

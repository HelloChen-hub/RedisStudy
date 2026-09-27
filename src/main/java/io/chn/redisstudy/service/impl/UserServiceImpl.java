package io.chn.redisstudy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import io.chn.redisstudy.common.LoginUser;
import io.chn.redisstudy.common.RedisPrefix;
import io.chn.redisstudy.entity.User;
import io.chn.redisstudy.mapper.UserMapper;
import io.chn.redisstudy.po.UserVO;
import io.chn.redisstudy.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final StringRedisTemplate stringRedisTemplate;

    public UserServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public LoginUser login(UserVO userVO) {
        //判断用户id是否为空
        if (userVO.getId() == null) {
            log.info("用户id为空");
            return null;
        }

        //根据id查询用户信息
        User user = getById(userVO.getId());

        //判断用户是否存在
        if(user == null) {
            log.info("用户不存在");
            return null;
        }

        //将用户信息存储到redis中
        // 1.将用户信息序列化
        String userId = String.valueOf(user.getId());
        LoginUser loginUser = BeanUtil.copyProperties(userVO, LoginUser.class);
        String userJsonStr = JSONUtil.toJsonStr(loginUser);
        stringRedisTemplate.opsForValue().set(RedisPrefix.LOGIN_USER_KEY + userId, userJsonStr, RedisPrefix.LOGIN_USER_TTL, TimeUnit.MINUTES);

        log.info("用户信息{}存储到redis中", userJsonStr);

        return BeanUtil.copyProperties(user, LoginUser.class);
    }

    @Override
    public UserVO getUserById(Long id) {
        User user = getById(id);
        if (BeanUtil.isEmpty(user)) {
            log.info("用户不存在");
            return null;
        }

        return BeanUtil.copyProperties(user, UserVO.class);
    }

    @Override
    public List<User> listAll() {
        return list();
    }
}

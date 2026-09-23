package io.chn.redisstudy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.chn.redisstudy.entity.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper extends BaseMapper<User> {
}

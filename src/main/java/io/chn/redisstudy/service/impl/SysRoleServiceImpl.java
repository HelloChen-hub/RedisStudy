package io.chn.redisstudy.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import io.chn.redisstudy.entity.SysRole;
import io.chn.redisstudy.mapper.SysRoleMapper;
import io.chn.redisstudy.service.SysRoleService;
import org.springframework.stereotype.Service;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {
}

package com.gym.mapper;

import com.gym.entity.Admin;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminMapper {

    /** 按登录账号查询（登录时用） */
    Admin selectByUsername(String username);
}

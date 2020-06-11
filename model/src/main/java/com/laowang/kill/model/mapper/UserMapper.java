package com.laowang.kill.model.mapper;

import com.laowang.kill.model.entity.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
   User selectById(@Param("id") Integer id);
}
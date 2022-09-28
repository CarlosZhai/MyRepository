package com.offcn.user.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.offcn.user.pojo.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface UserMapper extends BaseMapper<User> {
    /***
     * 增加用户积分
     * @param points
     */
    @Update("update tb_user set points=points+#{points} where username=#{username}")
    int addUserPoints(@Param("username") String username, @Param("points") Integer points);
}

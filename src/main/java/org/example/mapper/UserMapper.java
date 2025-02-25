package org.example.mapper;


import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import org.example.pojo.User;

@Mapper
public interface UserMapper {

    @Select("select * from user where username=#{username}")
    User findByUserName(String username) ;

    @Insert("insert into user(username,password,created_time) values(#{username},#{password},now())")
    void add(String username, String password);
}

package org.example.mapper;


import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import org.example.entity.User;

@Mapper
public interface UserMapper {

    //    @Select("select * from user where username=#{username}")
    User findByUserName(String username) ;

    @Insert("insert into user(username,password,created_time) values(#{username},#{password},now())")
    void add(String username, String password);

    @Update("update user set updated_time=#{updatedTime} where id=#{id}")
    void update(User user);
}

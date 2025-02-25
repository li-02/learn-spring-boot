package org.example.sevice.Impl;

import org.example.mapper.UserMapper;
import org.example.pojo.User;
import org.example.sevice.UserService;
import org.example.utils.Md5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User findUserByName(String username){
        return userMapper.findByUserName(username);
    }

    @Override
    public void register(String username, String password){
        String md5String= Md5Util.getMD5String(password);
        userMapper.add(username,md5String);
    }
}

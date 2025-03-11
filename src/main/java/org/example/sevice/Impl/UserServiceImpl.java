package org.example.sevice.Impl;

import org.example.entity.User;
import org.example.mapper.UserMapper;
import org.example.sevice.UserService;
import org.example.utils.Md5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

    @Override
    public void updateUserInfo(User user) {
        user.setUpdatedTime(LocalDateTime.now());
        userMapper.update(user);
    }
}

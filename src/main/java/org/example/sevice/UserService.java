package org.example.sevice;

import org.example.entity.User;

public interface UserService {

    User findUserByName(String username);

    void register(String username,String password);

    void updateUserInfo(User user);
}

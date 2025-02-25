package org.example.controller;

import jakarta.validation.constraints.Pattern;
import org.example.pojo.Result;
import org.example.pojo.User;
import org.example.sevice.UserService;
import org.example.utils.JwtUtil;
import org.example.utils.Md5Util;
import org.example.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Validated
public class UserController {

    @Autowired
    private UserService userService;


    @PostMapping("/register")
    public Result register(@Pattern(regexp ="^\\S{5,16}$") String username,@Pattern(regexp ="^\\S{5,16}$") String password) {
        // 查询用户
        User user=userService.findUserByName(username);
        if (user==null) {
            // 注册
            userService.register(username,password);
            return Result.success();
        }else{
            return Result.error("用户名被占用");
        }
    }

    @PostMapping("/login")
    public Result login(@Pattern(regexp ="^\\S{5,16}$") String username,@Pattern(regexp ="^\\S{5,16}$") String password) {
        // 根据username查询用户
        User user=userService.findUserByName(username);
        // 判断用户是否存在
        if(user==null) {
            return Result.error("用户名错误");
        }
        // 判断密码是否正确
        // 密码加密
        if(Md5Util.getMD5String(password).equals(user.getPassword())) {
            Map<String,Object> map=new HashMap<>();
            map.put("id",user.getId());
            map.put("username",user.getUsername());
            String token= JwtUtil.genToken(map);
            return Result.success(token);
        }else{
            return Result.error("密码错误");
        }

    }

    @GetMapping("/info")
    public Result<User> userInfo(){
        Map<String,Object> map= ThreadLocalUtil.get();
        String username=(String) map.get("username");
        User user=userService.findUserByName(username);
        return Result.success(user);
    }
}

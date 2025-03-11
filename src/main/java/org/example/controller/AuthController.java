package org.example.controller;

import jakarta.validation.constraints.Pattern;
import org.example.annotation.LogOperation;
import org.example.common.Result;
import org.example.dto.request.LoginForm;
import org.example.dto.response.LoginResponse;
import org.example.entity.User;
import org.example.exception.CustomerException;
import org.example.sevice.UserService;
import org.example.utils.JwtUtil;
import org.example.utils.Md5Util;
import org.example.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


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
    @LogOperation(value = "用户登陆", module = "用户登录")
    public Result login(@RequestBody LoginForm loginForm) {
        String username = loginForm.getUsername();
        String password = loginForm.getPassword();
        // 校验用户名和密码
        // 参数校验
        if (username == null || password == null ||
                !username.matches("^\\S{5,16}$") ||
                !password.matches("^\\S{5,16}$")) {
            throw new CustomerException("用户名或密码格式不正确");
        }
        // 根据username查询用户
        User user=userService.findUserByName(username);
        // 判断用户是否存在
        if(user==null) {
            throw new CustomerException("用户不存在");
        }
        // 判断密码是否正确
        // 密码加密
        if(Md5Util.getMD5String(password).equals(user.getPassword())) {
            Map<String,Object> map=new HashMap<>();
            map.put("id",user.getId());
            map.put("username",user.getUsername());
            String token = JwtUtil.genToken(map, 1000 * 60 * 60 * 24 * 3);
            // token存入redis, 过期时间和jwt的过期时间一致
            stringRedisTemplate.opsForValue().set(token, token, 3, TimeUnit.DAYS);
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setAccessToken(token);
            loginResponse.setExpireTime(null);
            loginResponse.setUsername(username);
            return Result.success(loginResponse);
        }else{
            throw new CustomerException("账号或密码错误");
        }

    }

    @GetMapping("/info")
    public Result<User> userInfo(){
        Map<String,Object> map= ThreadLocalUtil.get();
        String username=(String) map.get("username");
        User user=userService.findUserByName(username);
        return Result.success(user);
    }

    @PutMapping("/updateUserInfo")
    public Result updateUserInfo(@RequestBody User user) {
        userService.updateUserInfo(user);
        return Result.success();
    }

    // 修改密码接口，修改完后需要删除redis中的token
}

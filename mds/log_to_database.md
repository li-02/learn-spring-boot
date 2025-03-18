# 日志记录全流程

下面是一个基于我们已实现的日志框架的完整流程，从用户请求开始到日志存储完成：

## 1. 请求到达

当用户发送一个HTTP请求到你的Spring Boot应用时，首先被Tomcat或其他Web服务器接收。

## 2. 过滤器处理

请求经过Spring的过滤器链，此时可能会执行一些前置处理（如身份验证）。

## 3. 日志初始化

在请求到达控制器方法之前，通过AOP切面（GlobalLogAspect或EnhancedLogAspect）拦截：

1. 从RequestContextHolder获取当前请求信息
2. 调用LogUtil.getLogInfo()创建新的LogInfo对象
3. 设置基础信息：
    - requestId（UUID生成）
    - requestUrl（从HttpServletRequest获取）
    - requestMethod（GET/POST/PUT等）
    - ipAddress（客户端IP）
    - userAgent（浏览器信息）
    - username（当前登录用户）
    - startTime（当前时间戳）

## 4. 切面处理

根据不同情况，系统会选择不同的切面处理日志：

### 场景A：带有@LogOperation注解的方法

1. EnhancedLogAspect（@Order(1)）拦截带注解的方法
2. 从注解获取module和operation信息
3. 根据注解配置决定是否记录请求参数
4. 执行目标方法并捕获返回值
5. 根据注解配置决定是否记录响应结果
6. 调用logInfo.finish()计算执行时间
7. 如果不是控制器方法，直接记录日志

### 场景B：普通控制器方法

1. GlobalLogAspect（@Order(2)）拦截控制器方法
2. 检查方法是否已有@LogOperation注解（如有则跳过，避免重复记录）
3. 自动推断module（从控制器名称）和operation（从方法名）
4. 记录请求参数
5. 执行目标方法并捕获返回值
6. 记录响应结果
7. 调用logInfo.finish()计算执行时间

## 5. 异常处理

如果方法执行过程中发生异常：

1. 切面捕获异常
2. 将异常信息添加到LogInfo对象
3. 将LogType设置为ERROR
4. 调用LogUtil.logError()记录错误日志
5. 保存日志到数据库
6. 重新抛出异常，让全局异常处理器处理

## 6. 日志输出与存储

无论是正常执行还是异常情况，都会执行以下步骤：

### 日志文件记录

1. LogUtil.logInfo()或LogUtil.logError()被调用
2. LogInfo对象被转换为JSON格式
3. 使用SLF4J记录到日志文件
   ```
   INFO com.example.utils.LogUtil - 【操作日志】{"requestId":"abc123","url":"/api/users","method":"GET",...}
   ```

### 数据库存储

1. LogService.saveLog()被调用
2. 通过Mybatis的LogMapper.insert()方法将日志保存到数据库
3. 使用REQUIRES_NEW事务隔离级别，确保即使主业务回滚，日志也会保存

## 7. 日志清理

1. 操作完成后，LogUtil.clear()被调用
2. 从ThreadLocal中移除当前线程的LogInfo对象，防止内存泄漏

## 8. 请求完成

请求处理完成，响应返回给用户。

## 实际应用示例

### 带注解的业务方法

```java

@Service
public class UserService {
    @LogOperation(module = "用户管理", value = "创建用户", logParams = true, logResult = true)
    public UserDTO createUser(UserCreateRequest request) {
        // 业务逻辑
        return new UserDTO();
    }
}
```

### 控制器方法

```java

@RestController
@RequestMapping("/api/users")
public class UserController {
    @PostMapping
    public Result create(@RequestBody UserCreateRequest request) {
        // 调用服务层方法
        return Result.success();
    }
}
```

当用户调用创建用户API时，系统会记录从控制器到服务层的两条日志，提供完整的操作追踪。

这个完整的日志流程使你能够追踪每个请求的整个生命周期，包括执行时间、异常情况等，同时提供了灵活的配置选项来控制日志记录的细节。
# 日志记录的全流程调用情况

让我详细说明混合日志策略中各个组件的调用流程，以展示一个HTTP请求从进入系统到完成处理过程中的日志记录全流程。

## 1. 请求入口阶段（过滤器层）

当一个HTTP请求到达应用服务器时，首先进入过滤器链：

```
客户端请求 → Servlet容器 → Spring过滤器链 → RequestLoggingFilter
```

**RequestLoggingFilter执行流程**:
1. 判断是否为静态资源请求，如果是则跳过日志记录
2. 包装请求和响应对象(ContentCachingRequestWrapper/ResponseWrapper)
3. 初始化LogInfo对象并存入ThreadLocal
4. 记录基本信息：URL、HTTP方法、IP地址
5. 将请求传递给过滤器链中的下一个过滤器

## 2. 请求预处理阶段（拦截器层）

请求通过过滤器后，进入拦截器链：

```
RequestLoggingFilter → Spring拦截器链 → LoggingInterceptor → DispatcherServlet
```

**LoggingInterceptor执行流程**:
1. 在preHandle方法中，判断是否为Controller方法
2. 从ThreadLocal获取LogInfo对象
3. 补充信息：类名、方法名
4. 尝试获取当前登录用户信息
5. 允许请求继续处理

## 3. 请求处理阶段（AOP切面层）

请求到达Controller前，首先被AOP切面拦截：

```
DispatcherServlet → AOP代理 → EnhancedLogAspect → GlobalLogAspect → Controller方法
```

**EnhancedLogAspect执行流程**:
1. 检查方法是否有@LogOperation注解
2. 获取注解的value和module值
3. 从ThreadLocal获取LogInfo对象
4. 补充信息：操作描述、模块名称
5. 根据注解配置决定是否记录请求参数
6. 执行目标方法（proceed）

**GlobalLogAspect执行流程**:
1. 匹配所有Controller包下的方法
2. 从ThreadLocal获取LogInfo对象
3. 记录方法参数（如果未被EnhancedLogAspect处理）
4. 执行目标方法（proceed）

## 4. 业务处理阶段（Controller方法执行）

```
AOP切面 → Controller方法 → Service → Repository → 数据库
```

## 5. 响应处理阶段（AOP切面返回）

Controller方法执行完毕，响应按相反顺序返回：

```
Controller方法返回 → GlobalLogAspect → EnhancedLogAspect
```

**GlobalLogAspect后置处理**:
1. 记录响应结果
2. 计算方法执行时间
3. 如果是Controller方法且没有@LogOperation注解，记录基础日志

**EnhancedLogAspect后置处理**:
1. 根据注解配置决定是否记录响应结果
2. 计算方法执行时间
3. 如果有@LogOperation注解，记录详细操作日志

## 6. 请求完成阶段（拦截器返回）

```
AOP切面返回 → LoggingInterceptor → RequestLoggingFilter
```

**LoggingInterceptor后置处理**:
1. 在afterCompletion方法中检查是否有异常
2. 如有异常，记录异常信息

## 7. 请求结束阶段（过滤器返回）

```
拦截器链 → RequestLoggingFilter → 响应返回客户端
```

**RequestLoggingFilter后置处理**:
1. 如果没有更详细的日志记录（即类名为空），则记录基本的HTTP请求日志
2. 清理ThreadLocal中的LogInfo对象
3. 复制响应内容到原始响应对象

## 8. 日志输出阶段

在整个流程中，当调用LogUtil.logInfo()方法时：
1. 日志信息会被序列化为JSON格式
2. 通过SLF4J记录到配置的日志系统
3. 根据配置写入控制台、文件或发送到Logstash等日志收集系统

## 9. 异常处理流程

如果在任何阶段发生异常：
1. 最近的try/catch块会捕获异常
2. 调用LogUtil.logError()记录异常信息
3. 异常可能被重新抛出或被全局异常处理器处理

## 10. 特殊场景处理

**定时任务或非HTTP请求的日志记录**:
1. 可以在定时任务方法上使用@LogOperation注解
2. 由于没有HTTP请求上下文，部分信息（如IP、URL等）不可用
3. EnhancedLogAspect仍然会处理这些方法并记录日志

## 日志记录的调用序列图

```
客户端 ---> [Servlet容器]
              |
              v
        [RequestLoggingFilter] ---> 初始化LogInfo, 存入ThreadLocal
              |
              v
        [LoggingInterceptor] ---> 补充类名、方法名、用户名
              |
              v
        [EnhancedLogAspect] ---> 检查@LogOperation注解, 记录操作和模块
              |
              v
        [GlobalLogAspect] ---> 记录方法参数
              |
              v
        [Controller方法] ---> 执行业务逻辑
              |
              v
        [GlobalLogAspect] ---> 记录响应结果、执行时间
              |
              v
        [EnhancedLogAspect] ---> 记录详细操作日志
              |
              v
        [LoggingInterceptor] ---> 检查异常
              |
              v
        [RequestLoggingFilter] ---> 清理ThreadLocal, 复制响应内容
              |
              v
        [LogUtil.logInfo] ---> 格式化日志, 输出到SLF4J
              |
              v
        [日志系统] ---> 写入控制台/文件/Logstash
```

这个调用流程展示了混合日志策略的完整工作过程，从请求进入系统到响应返回客户端的每个阶段，都有相应的日志记录组件参与，确保了系统行为的可追踪性和可观测性。
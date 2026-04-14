# Aegis-Admin 深度学习导读

> 这是一份面向深度学习者的项目解构文档，从架构设计、业务流程、设计模式到实现细节，手把手带你理解整个系统。

**文档目标**：理解"为什么这样组织"而不仅仅是"是什么"。

---

## 目录

1. [项目背景与目标](#项目背景与目标)
2. [整体架构设计](#整体架构设计)
3. [模块划分哲学](#模块划分哲学)
4. [关键链路追踪：用户登录](#关键链路追踪用户登录)
5. [关键链路追踪：操作权限与日志](#关键链路追踪操作权限与日志)
6. [设计模式与最佳实践](#设计模式与最佳实践)
7. [配置详解](#配置详解)
8. [常见耦合点与性能瓶颈](#常见耦合点与性能瓶颈)
9. [扩展指南](#扩展指南)

---

## 项目背景与目标

### 项目定位

**Aegis-Admin** 是一个**多模块 Spring Boot 3 后端系统**，设计目标是：
- ✅ 提供**安全的用户认证**（JWT + Spring Security）
- ✅ 实现**基于角色的权限控制**（RBAC）
- ✅ 记录**操作审计日志**（AOP + RabbitMQ）
- ✅ 提供**可扩展的业务框架**（分层 + 依赖注入）

### 核心价值

| 方面 | 价值 |
|------|------|
| **安全性** | JWT Token + Spring Security + 权限校验 |
| **可追溯性** | AOP 切面自动记录每次操作日志 |
| **可维护性** | 清晰的分层结构 + 依赖注入 |
| **可扩展性** | 模块化设计，易于添加新功能 |
| **可伸缩性** | 消息队列异步处理日志，降低耦合 |

---

## 整体架构设计

### 1. 分层架构（3层 + 基础设施）

```
┌─────────────────────────────────────────────────────────────────┐
│                        表现层 (Controller)                       │
│  AuthController, SysUserController, SysRoleController 等        │
│  职责：接收 HTTP 请求、参数校验、返回 HTTP 响应                 │
└──────────────────────────┬──────────────────────────────────────┘
                           │ 依赖注入 (@Autowired)
┌──────────────────────────▼──────────────────────────────────────┐
│                      业务逻辑层 (Service)                        │
│  SysUserService, SysRoleService 等                              │
│  职责：核心业务逻辑、事务管理、业务规则验证                      │
└──────────────────────────┬──────────────────────────────────────┘
                           │ 使用 Mapper
┌──────────────────────────▼──────────────────────────────────────┐
│                    数据访问层 (Mapper)                           │
│  SysUserMapper extends BaseMapper<SysUser>                      │
│  职责：SQL 执行、ORM 映射、数据库操作                            │
└──────────────────────────┬──────────────────────────────────────┘
                           │ JDBC
                   ┌───────▼────────┐
                   │   MySQL 数据库  │
                   └────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│              基础设施层 (Filter, Config, Utils, Aspect)         │
├──────────────────────────────────────────────────────────────────┤
│  • JwtAuthFilter：JWT 令牌验证                                   │
│  • SecurityConfig：Spring Security 配置                         │
│  • RabbitMQConfig：消息队列配置                                 │
│  • SysLogAspect：AOP 切面日志记录                                │
│  • JwtUtils, JsonUtil：工具类                                   │
└─────────────────────────────────────────────────────────────────┘
```

### 2. 跨层通信约定

```
请求流入 ──┐
          ├─ 一进一出，遵循"输入验证→处理→输出包装"
响应流出 ──┘

输入端：Controller 参数校验 (@Valid, 自定义验证)
      ↓
处理端：Service 业务逻辑
      ↓
输出端：Result<T> 统一响应格式
      {
        "code": 200,           // 业务状态码
        "message": "success",  // 提示信息
        "data": {...},        // 真实数据
        "timestamp": 1234567  // 响应时间戳
      }
```

---

## 模块划分哲学

### 为什么要分成三个模块？

| 模块 | 职责 | 为什么要独立？ |
|------|------|---|
| **aegis-common** | 全局通用设施<br/>（注解、异常、结果、工具） | 👉 被所有模块依赖，独立方便复用；低耦合；便于版本管理 |
| **aegis-system** | 业务核心逻辑<br/>（Controller、Service、Entity、Mapper） | 👉 是真正的"业务域"；可独立测试；便于扩展新业务 |
| **aegis-app** | 应用启动程序<br/>（Application、配置、测试） | 👉 "粘合剂"角色；只依赖 system；便于切换配置环境 |

### 依赖关系流向

```
aegis-app
    ↓ 依赖
aegis-system ← 导入所有 Controller、Service、Config
    ↓ 依赖
aegis-common ← 导入工具、注解、响应格式

单向依赖流向：app → system → common
禁止逆向依赖：避免循环依赖
```

### 模块内部结构

**aegis-system 的标准包结构：**

```
aegis/system/
├── controller/        # 控制层
│   ├── AuthController.java        # 认证入口
│   ├── SysUserController.java      # 用户管理 CRUD
│   ├── SysRoleController.java      # 角色管理 CRUD
│   └── ...
├── service/           # 业务逻辑层
│   ├── SysUserService.java         # 接口定义
│   ├── SysRoleService.java         # 接口定义
│   └── impl/                       # 实现类
│       ├── SysUserServiceImpl.java
│       ├── SysRoleServiceImpl.java
│       └── UserDetailServiceImpl.java  # Spring Security 集成
├── entity/            # 数据实体 (ORM)
│   ├── SysUser.java               # 用户表映射
│   ├── SysRole.java               # 角色表映射
│   ├── SysMenu.java               # 菜单权限表映射
│   └── ...
├── mapper/            # 数据访问层 (DAO)
│   ├── SysUserMapper.java         # extends BaseMapper<SysUser>
│   ├── SysRoleMapper.java
│   └── ...
├── dto/               # 数据传输对象 (用于接收 HTTP 请求)
│   └── LoginDTO.java              # 登录请求 {username, password}
├── vo/                # 值对象 (用于 HTTP 响应)
│   ├── LoginVO.java               # 登录响应 {token, user info}
│   └── UserVO.java                # 用户视图 (敏感字段脱敏)
├── config/            # 配置类
│   ├── SecurityConfig.java        # Spring Security 配置
│   └── RabbitMQConfig.java        # RabbitMQ 配置
├── filter/            # 过滤器链
│   ├── JwtAuthFilter.java         # JWT 拦截
│   └── LogFilter.java             # 日志过滤
├── aspect/            # AOP 切面
│   └── SysLogAspect.java          # 操作日志记录
└── mq/                # 消息队列
    └── MQConsumer.java            # 异步消费日志
```

---

## 关键链路追踪：用户登录

> 这是项目中最复杂的一条业务链路，涉及认证、权限、加密、Token 生成。

### 场景描述

用户在前端输入 `username: admin` 和 `password: admin123`，点击登录按钮。后端如何处理？

### 完整调用链路

#### 🔵 第一步：HTTP 请求到达

```
客户端请求：
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
    "username": "admin",
    "password": "admin123"
}
```

#### 🔵 第二步：Spring Security 安全链路

**文件**：`SecurityConfig.java`

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/v1/auth/**").permitAll()  // ← 允许所有人访问登录接口
            .anyRequest().authenticated()  // ← 其他接口需要认证
        )
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        // ↑ 自定义过滤器：在标准认证前执行 JWT 验证
```

**这一步的意义**：
- ✅ `/api/v1/auth/login` 是公开接口，不需要 JWT
- ✅ 其他接口（如 `/api/v1/users`）需要有效的 JWT Token

#### 🔵 第三步：AuthController 收到请求

**文件**：`AuthController.java`

```java
@PostMapping("/login")
@OperationLog("登陆")  // ← AOP 切面会记录这次操作
public Result<?> login(@RequestBody LoginDTO loginDTO) {
    // ↑ Spring 自动反序列化 JSON → LoginDTO
```

**此时发生了什么**：
- ✅ HTTP 请求体 `{"username": "admin", "password": "admin123"}` 被反序列化成 `LoginDTO` 对象
- ✅ `@OperationLog` 注解被 AOP 拦截，准备记录日志

#### 🔵 第四步：查询用户信息

```java
SysUser sysUser = sysUserService.getUserByName(loginDTO.getUsername());
// 执行链路：
// AuthController.login() 
//   → SysUserService.getUserByName()
//     → SysUserServiceImpl.getUserByName()
//       → lambdaQuery().eq(SysUser::getUsername, username).one()
//         → MyBatis Plus 自动生成 SQL 查询
```

**对应 SQL 语句**：

```sql
SELECT * FROM sys_user WHERE username = 'admin' AND is_deleted = 0;
```

**为什么要用 Lambda 查询？**

```java
// ✅ 好：类型安全，编译时检查
lambdaQuery().eq(SysUser::getUsername, username).one()

// ❌ 不好：硬编码字符串，容易出错
query().eq("username", username).one()
```

#### 🔵 第五步：密码验证

```java
if (!passwordEncoder.matches(loginDTO.getPassword(), sysUser.getPassword())) {
    throw new UsernameNotFoundException("密码错误!");
}
// 执行链路：
// PasswordEncoder.matches(明文密码, 数据库密文密码)
//   → BCryptPasswordEncoder 算法验证
```

**为什么要用 BCrypt？**

- 🔐 **单向加密**：不能从密文反推明文
- 🔐 **盐值机制**：同一密码加密结果不同，防彩虹表攻击
- 🔐 **时间成本**：验证过程耗时，防暴力破解

#### 🔵 第六步：获取用户权限

```java
List<String> sysMenuRoleList = sysMenuService.getMenuIds(sysUser.getId());
// 执行链路：
// SysMenuService.getMenuIds(userId)
//   → 通过用户 ID 查询其拥有的所有菜单权限
//     → 返回权限字符串数组，如 ["sys:user:list", "sys:user:add", ...]
```

**权限表结构**：

```
用户 (sys_user)
  ↓ (一个用户可以有多个角色)
用户-角色表 (sys_user_role)
  ↓ (一个角色可以有多个菜单权限)
菜单表 (sys_menu)  或  角色-菜单表 (sys_role_menu)
```

#### 🔵 第七步：生成 JWT Token

```java
Map<String, Object> map = new HashMap<>();
map.put(JwtConstants.USERNAME, sysUser.getUsername());
map.put(JwtConstants.USER_ID, sysUser.getId());
map.put(JwtConstants.PERMISSION, sysMenuRoleList);

String accessToken = JwtUtils.generateAccessToken(map);
String refreshToken = JwtUtils.generateRefreshToken(map);
```

**生成的 Token 结构**：

```
JWT 格式：header.payload.signature

header:
{
  "alg": "HS256",
  "typ": "JWT"
}

payload:
{
  "username": "admin",
  "userId": 1,
  "permission": ["sys:user:list", "sys:user:add", ...],
  "iat": 1234567890,
  "exp": 1234567890 + 2小时
}

signature: HMAC-SHA256(header + payload + 密钥)
```

**Token 有效期**：

```java
private static final long ACCESS_TOKEN_EXPIRE = 2 * 60 * 60 * 1000L;    // 2小时
private static final long REFRESH_TOKEN_EXPIRE = 7 * 24 * 60 * 60 * 1000L; // 7天
```

#### 🔵 第八步：返回响应

```java
LoginVO loginVo = new LoginVO(
    accessToken,              // JWT 访问令牌
    refreshToken,             // 刷新令牌
    sysUser.getUsername(),    // 用户名
    sysUser.getNickname(),    // 昵称
    sysUser.getId(),          // 用户 ID
    sysMenuRoleList           // 权限列表
);

return Result.success(loginVo);
```

**HTTP 响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGc...",
    "refreshToken": "eyJhbGc...",
    "username": "admin",
    "nickname": "管理员",
    "userId": 1,
    "permissions": ["sys:user:list", "sys:user:add", ...]
  },
  "timestamp": 1712958234567
}
```

### 📊 登录流程图（文字版）

```
开始
 ↓
客户端 POST /api/v1/auth/login
 ↓
SecurityConfig 判断路径 → 允许通过（不需要 JWT）
 ↓
AuthController.login() 接收 LoginDTO
 ↓
AOP 切面 @OperationLog 拦截 → 准备记录日志
 ↓
SysUserService.getUserByName() → MyBatis 查询数据库
 ↓
【分支】用户不存在？→ 抛出异常 → GlobalExceptionHandler 处理 → 返回 401
 ↓（用户存在）
PasswordEncoder.matches() 验证密码
 ↓
【分支】密码错误？→ 抛出异常 → 返回 401
 ↓（密码正确）
SysMenuService.getMenuIds() → 查询用户权限
 ↓
JwtUtils.generateAccessToken() → 生成 Access Token
 ↓
JwtUtils.generateRefreshToken() → 生成 Refresh Token
 ↓
构造 LoginVO 对象
 ↓
Result.success(loginVo) → 返回 HTTP 200
 ↓
AOP 切面 SysLogAspect 记录日志 → 发送到 RabbitMQ
 ↓
客户端收到响应，保存 Token
```

---

## 关键链路追踪：操作权限与日志

### 场景描述

用户已登录，现在调用 `GET /api/v1/users` 获取用户列表。这个请求如何被验证权限？日志如何被记录？

### 完整调用链路

#### 🔴 第一步：请求带上 JWT Token

```
客户端请求：
GET http://localhost:8080/api/v1/users
Authorization: Bearer eyJhbGc.eyJpc3M.SflKxw...
```

#### 🔴 第二步：JwtAuthFilter 拦截验证

**文件**：`JwtAuthFilter.java`

```java
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String header = request.getHeader("Authorization");
        
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);  // ← 没有 Token，放行到下一个过滤器
            return;
        }
        
        String token = header.substring(7);  // ← 提取 "Bearer " 后面的 Token
        try {
            Claims claims = JwtUtils.parseToken(token);  // ← 解析和验证 Token
            
            String username = claims.get(JwtConstants.USERNAME).toString();
            List<String> permissions = (List<String>) claims.get(JwtConstants.PERMISSION);
            
            // ← 将权限信息存入 Spring Security 上下文
            List<SimpleGrantedAuthority> authorities = permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            // ↑ 关键：这一步让后续代码可以通过 SecurityContextHolder 获取当前用户信息
            
        } catch (Exception e) {
            filterChain.doFilter(request, response);  // ← Token 解析失败，放行
            return;
        }
        
        filterChain.doFilter(request, response);  // ← 放行请求继续
    }
}
```

**这一步的意义**：

- ✅ 验证 Token 是否有效（签名、过期时间）
- ✅ 从 Token 中提取用户名和权限
- ✅ 将信息存入 `SecurityContextHolder`，便于后续代码获取
- ⚠️ 如果 Token 无效，**不会在这里直接拒绝**，而是放行让 Spring Security 后续处理

#### 🔴 第三步：@PreAuthorize 权限检查

**文件**：`SysUserController.java`

```java
@PreAuthorize("hasAuthority('sys:user:list')")  // ← 检查权限
@GetMapping
@OperationLog("列出所有用户")
public Result<?> list() {
    // ...
}
```

**权限检查流程**：

```
进入方法前，Spring Security 拦截：
 ↓
从 SecurityContextHolder.getContext() 取出当前用户的 authorities
 ↓
检查是否包含 "sys:user:list"
 ↓
【分支】没有权限？→ 抛出 AccessDeniedException → GlobalExceptionHandler 处理 → 返回 403 Forbidden
 ↓（有权限）
允许执行方法体
```

#### 🔴 第四步：AOP 切面拦截记录日志

**文件**：`SysLogAspect.java`

```java
@Aspect
@Component
public class SysLogAspect {
    
    @Around("@annotation(operationLog)")  // ← 拦截所有被 @OperationLog 标注的方法
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog)
            throws Throwable {
        
        // ✅ 记录请求信息
        Long startTime = System.currentTimeMillis();
        LocalDateTime localDateTime = LocalDateTime.now();
        
        ServletRequestAttributes request = (ServletRequestAttributes) 
            RequestContextHolder.getRequestAttributes();
        
        String path = request.getRequest().getRequestURI();
        String ip = request.getRequest().getRemoteAddr();
        
        // ✅ 执行目标方法
        Object result;
        int status;
        try {
            result = joinPoint.proceed();  // ← 调用 list() 方法
            status = HttpStatus.OK.value();  // 200
        } catch (Exception e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR.value();  // 500
            throw new BusinessException(status, String.valueOf(e));
        }
        
        // ✅ 计算执行时间
        Long endTime = System.currentTimeMillis();
        Long costTime = endTime - startTime;
        
        // ✅ 构造日志对象
        SysLog sysLog = SysLog.builder()
                .operation(operationLog.value())         // "列出所有用户"
                .method(joinPoint.getSignature().getName())  // "list"
                .path(path)                              // "/api/v1/users"
                .params(objectMapper.writeValueAsString(filterParams))
                .result(String.valueOf(result))
                .ip(ip)                                  // "127.0.0.1"
                .operator("SysLogAspect")
                .timeCost(costTime)                      // 毫秒数
                .status(status)                          // 200
                .createTime(localDateTime)
                .updateTime(localDateTime)
                .build();
        
        // ✅ 发送日志到 RabbitMQ（异步处理，不阻塞请求）
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                "boot_SysLogAspect",
                objectMapper.writeValueAsString(sysLog)  // ← 序列化为 JSON
        );
        
        return result;
    }
}
```

**关键设计点**：

- 🎯 **AOP 切面自动拦截**：不需要在每个方法里写重复的日志代码
- 🎯 **异步消息队列**：日志发送到 RabbitMQ，不阻塞 HTTP 响应
- 🎯 **参数过滤**：敏感数据（如密码）不会被记录

#### 🔴 第五步：RabbitMQ 异步消费日志

**文件**：`RabbitMQConfig.java`

```java
@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE_NAME = "boot_topic_exchange";
    public static final String QUEUE_NAME = "boot_topic_queue";
    
    @Bean("bootExchange")
    public Exchange bootExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE_NAME).durable(true).build();
        // ↑ Topic Exchange：支持通配符路由
    }
    
    @Bean("bootQueue")
    public Queue bootQueue() {
        return QueueBuilder.durable(QUEUE_NAME).build();
        // ↑ 持久化队列：服务重启后消息不丢失
    }
    
    @Bean
    public Binding bootBinding(@Qualifier("bootQueue") Queue queue, 
                               @Qualifier("bootExchange") Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("boot.#").noargs();
        // ↑ 绑定关系：exchange 中 boot.* 的消息都被路由到 queue
    }
}
```

**消息流动**：

```
SysLogAspect 发送：
rabbitTemplate.convertAndSend(
    "boot_topic_exchange",      // Exchange
    "boot_SysLogAspect",        // Routing Key
    jsonLog                     // Message
)
 ↓
RabbitMQ Topic Exchange：
检查 Binding：是否存在 Exchange 与 Queue 的绑定？
绑定 Pattern：boot.#
Routing Key：boot_SysLogAspect
匹配？→ YES（boot.# 匹配 boot_SysLogAspect）
 ↓
消息进入 Queue
 ↓
MQConsumer 消费：
@RabbitListener(queues = "boot_topic_queue")
public void consume(String jsonLog) {
    SysLog sysLog = objectMapper.readValue(jsonLog, SysLog.class);
    sysLogService.save(sysLog);  // ← 保存到数据库
}
```

### 📊 权限检查 + 日志记录流程图

```
客户端请求
 ↓
GET /api/v1/users (Authorization: Bearer token)
 ↓
JwtAuthFilter 拦截
 ↓
解析 Token，提取 username、permissions
 ↓
SecurityContextHolder.setAuthentication() ← 存入 Spring Security 上下文
 ↓
路由到 SysUserController.list()
 ↓
Spring Security 检查 @PreAuthorize("hasAuthority('sys:user:list')")
 ↓
【分支】没有权限？→ AccessDeniedException → 返回 403
 ↓（有权限）
SysLogAspect 拦截（@Around @annotation(operationLog)）
 ↓
记录请求信息、时间戳、IP
 ↓
执行 SysUserController.list() 方法体
 ↓
【分支】方法抛异常？→ status = 500，重新抛异常
 ↓（成功）
计算执行耗时
 ↓
构造 SysLog 对象
 ↓
RabbitTemplate 发送到 RabbitMQ（异步，不阻塞响应）
 ↓
返回 Result<List<UserVO>> 给客户端
 ↓
后台 MQConsumer 异步消费日志 → 保存到数据库
```

---

## 设计模式与最佳实践

### 1. 依赖注入模式（Dependency Injection）

**在项目中的应用**：

```java
@RestController
public class SysUserController {
    @Autowired  // ← Spring 自动注入
    private SysUserService sysUserService;
    
    public Result<?> list() {
        sysUserService.list();  // ← 无需关心 Service 如何创建
    }
}
```

**为什么要用 DI？**

- ✅ **解耦**：Controller 不需要 new Service，只需声明依赖
- ✅ **易测试**：可以注入 Mock 对象进行单元测试
- ✅ **便于替换**：更换实现类时，只需改 Spring 配置

### 2. 工厂模式（Factory Pattern）

**在项目中的应用**：

```java
// MyBatis Plus 的 BaseMapper 就是工厂
@Repository
public interface SysUserMapper extends BaseMapper<SysUser> {
    // 不需要写任何代码，BaseMapper 自动提供 CRUD 方法
}

// 使用时：
SysUser user = sysUserMapper.selectById(1L);
sysUserMapper.insert(sysUser);
sysUserMapper.updateById(sysUser);
```

**BaseMapper 内部实现**（伪代码）：

```java
public interface BaseMapper<T> {
    T selectById(Serializable id);  // ← 工厂生成 SELECT 语句
    int insert(T entity);           // ← 工厂生成 INSERT 语句
    int updateById(T entity);       // ← 工厂生成 UPDATE 语句
    // ...
}
```

**好处**：

- ✅ **减少重复代码**：不用手写每个 Entity 的 CRUD SQL
- ✅ **自动化**：MyBatis Plus 根据 Entity 元数据自动生成 SQL

### 3. 单例模式（Singleton Pattern）

**在项目中的应用**：

```java
// JwtUtils 中的 ObjectMapper 是单例
public class JwtUtils {
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    // ↑ 静态初始化块，只会执行一次
    
    public static String generateAccessToken(Map<String, Object> claims) {
        // 直接用 SECRET_KEY，无需重复创建
    }
}

// PasswordEncoder 也是单例
@Configuration
public class SecurityConfig {
    @Bean  // ← Spring 将其注册为单例 Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

**为什么用单例？**

- ✅ **性能**：对象创建成本高（如 JWT 密钥初始化），单例避免重复创建
- ✅ **线程安全**：ObjectMapper、PasswordEncoder 都是线程安全的
- ✅ **内存节省**：全局只有一个实例

### 4. 策略模式（Strategy Pattern）

**在项目中的应用**：

```java
// SensitiveType 枚举定义了多种脱敏策略
public enum SensitiveType {
    PASSWORD,   // 密码脱敏：****
    PHONE,      // 电话脱敏：136****2588
    EMAIL,      // 邮箱脱敏：ad***@example.com
    DEFAULT;    // 默认脱敏
}

// 使用时：
@Data
public class SysUser {
    @Sensitive(type = PASSWORD)
    private String password;
    
    @Sensitive(type = PHONE)
    private String phone;
    
    @Sensitive(type = EMAIL)
    private String email;
}
```

**脱敏逻辑**（伪代码）：

```java
// LogDesensitizeUtil 中的策略实现
public static String desensitize(String value, SensitiveType type) {
    switch (type) {
        case PASSWORD:
            return "****";  // 完全隐藏
        case PHONE:
            return value.substring(0, 3) + "****" + value.substring(7);  // 只露出前3后4
        case EMAIL:
            return value.substring(0, 2) + "***@" + value.substring(value.indexOf("@"));
        default:
            return value;
    }
}
```

**优势**：

- ✅ **易扩展**：添加新的脱敏规则，只需添加新的枚举值和对应逻辑
- ✅ **易维护**：脱敏逻辑集中在一个地方

### 5. 模板方法模式（Template Method Pattern）

**在项目中的应用**：

```java
// MyBatis Plus 的 ServiceImpl 就是模板方法
public abstract class ServiceImpl<M extends BaseMapper<T>, T> implements IService<T> {
    protected M baseMapper;
    
    // 模板方法：定义了 Service 的通用流程
    public boolean save(T entity) {
        return baseMapper.insert(entity) > 0;  // ← 模板化操作
    }
    
    public boolean updateById(T entity) {
        return baseMapper.updateById(entity) > 0;  // ← 模板化操作
    }
    
    public T getById(Serializable id) {
        return baseMapper.selectById(id);  // ← 模板化操作
    }
}

// 具体实现
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> 
        implements SysUserService {
    
    @Override
    public SysUser getUserByName(String username) {
        // ← 自定义方法：需要特殊业务逻辑时才重写
        return lambdaQuery()
                .eq(SysUser::getUsername, username)
                .one();
    }
}
```

**好处**：

- ✅ **代码复用**：通用 CRUD 不用重写
- ✅ **一致性**：所有 Service 都遵循相同的流程

### 6. AOP 切面模式（Aspect-Oriented Programming）

**在项目中的应用**：

```java
// 日志记录：原本需要在每个方法里写 log.info()
// 现在用 @Aspect 统一处理

@Aspect
@Component
public class SysLogAspect {
    @Around("@annotation(operationLog)")  // ← 切点：被 @OperationLog 标注的方法
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) {
        // 前置逻辑：记录开始时间
        Long startTime = System.currentTimeMillis();
        
        // 目标方法执行
        Object result = joinPoint.proceed();
        
        // 后置逻辑：记录耗时，发送 MQ
        Long costTime = System.currentTimeMillis() - startTime;
        // ...
        
        return result;
    }
}
```

**好处**：

- ✅ **关注点分离**：业务逻辑和日志逻辑分开
- ✅ **减少重复代码**：不用在每个方法中写重复的日志代码
- ✅ **易于维护**：修改日志逻辑时，只需改一个地方

---

## 配置详解

### application.yaml 配置文件

**文件位置**：`aegis-app/src/main/resources/application.yaml`

```yaml
server:
  port: 8080  # Tomcat 服务器监听的端口
              # 项目启动后，访问 http://localhost:8080 即可
```

**说明**：
- `port: 8080` 是开发环境常用端口
- 生产环境通常改为 `8081` 或其他避免冲突

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/aegis_admin?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    # 数据库连接字符串：mysql://【主机】【端口】【数据库名】
    # useUnicode=true&characterEncoding=utf8 → 支持中文
    # serverTimezone=Asia/Shanghai → 时区设置（避免时间差）
    
    username: root
    # MySQL 用户名（默认 root）
    
    password: Root@123456
    # MySQL 密码
    # ⚠️ 生产环境不能硬编码密码，应该从环境变量读取
    
    driver-class-name: com.mysql.cj.jdbc.Driver
    # MySQL 8.0+ 驱动类：com.mysql.cj.jdbc.Driver
    # MySQL 5.7- 驱动类：com.mysql.jdbc.Driver
```

**为什么要这样配置？**

- ✅ **数据库连接参数**：告诉 Spring 如何连接数据库
- ✅ **编码配置**：确保中文数据正确存储和读取
- ✅ **时区配置**：避免时间戳的时区问题

```yaml
  application:
    name: aegis-app
    # Spring Boot 应用的名称
    # 用于日志、监控等场景
```

```yaml
  rabbitmq:
    host: localhost
    # RabbitMQ 服务器地址
    
    port: 5672
    # RabbitMQ 默认端口（AMQP 协议）
    # 管理界面端口是 15672（HTTP）
    
    username: guest
    password: guest
    # 默认登录凭证
    # 生产环境应该修改
```

**为什么要这样配置？**

- ✅ **消息队列连接参数**：告诉 Spring AMQP 如何连接 RabbitMQ
- ✅ **异步处理**：日志、邮件等操作可以异步处理，不阻塞 HTTP 响应

```yaml
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    # 将 SQL 语句打印到控制台，便于调试
    # 生产环境应该关闭（会降低性能）
    
    map-underscore-to-camel-case: true
    # 下划线 ↔ 驼峰法 自动映射
    # 数据库列名：user_name → Java 属性：userName
  
  global-config:
    db-config:
      logic-delete-field: isDeleted
      # 逻辑删除字段名
      
      logic-delete-value: 1
      # 删除时设置为 1
      
      logic-not-delete-value: 0
      # 未删除时设置为 0
```

**为什么要配置逻辑删除？**

- ✅ **软删除**：不真正删除数据，只标记为已删除
- ✅ **可恢复**：需要时可以恢复数据
- ✅ **便于审计**：保留完整的数据历史

**逻辑删除工作原理**：

```java
// 执行 delete 时，MyBatis Plus 自动转换

// 你写的代码：
sysUserService.removeById(1L);

// MyBatis Plus 自动转换为：
UPDATE sys_user SET is_deleted = 1 WHERE id = 1;  // ← 不是 DELETE

// 查询时自动过滤：
SELECT * FROM sys_user WHERE is_deleted = 0;  // ← 自动加上条件
```

### SecurityConfig 配置文件

**文件位置**：`aegis-system/src/main/java/com/aegis/system/config/SecurityConfig.java`

```java
@Configuration
@EnableWebSecurity
// ↑ 启用 Spring Security web 安全功能
@EnableMethodSecurity
// ↑ 启用方法级别的权限检查（@PreAuthorize、@PostAuthorize 等）
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            // 关闭 CSRF 保护
            // ⚠️ 仅用于 REST API（无状态认证）
            // 如果是传统网站（Form 表单），不能关闭
            
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                // 白名单：/api/v1/auth/** 接口所有人都可以访问（登录、刷新 Token）
                
                .requestMatchers("/test/pzy/**").permitAll()
                // 测试接口，允许所有人访问
                
                .anyRequest().authenticated()
                // 其他所有接口都需要认证（必须有有效的 JWT）
            )
            
            .formLogin(AbstractHttpConfigurer::disable)
            // 关闭表单登录
            // 因为这是 REST API，不使用表单提交
            
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            // 在标准的用户名密码认证过滤器前，添加 JWT 认证过滤器
            // 过滤器执行顺序很重要：JWT 验证必须先执行
            
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    // 认证失败处理：返回 401
                    response.setStatus(ResultCode.UNAUTHORIZED.getCode());
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":401,\"message\":\"未登录或token已过期\"}");
                })
                
                .accessDeniedHandler((request, response, authException) -> {
                    // 权限不足处理：返回 403
                    response.setStatus(ResultCode.FORBIDDEN.getCode());
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":403,\"message\":\"没有权限\"}");
                })
            )
        ;
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
        // 使用 BCrypt 算法加密密码
        // 每次加密同一个密码，结果都不同（因为有随机盐值）
    }
}
```

**配置的三个核心决策**：

| 决策 | 原因 |
|------|------|
| 关闭 CSRF | REST API 是无状态的，无需 CSRF 保护 |
| 关闭表单登录 | 使用 JSON API，不需要 HTML 表单 |
| 添加 JWT 过滤器 | 在标准认证前验证 Token |

### RabbitMQConfig 配置文件

**文件位置**：`aegis-system/src/main/java/com/aegis/system/config/RabbitMQConfig.java`

```java
@Configuration
public class RabbitMQConfig {
    
    public static final String EXCHANGE_NAME = "boot_topic_exchange";
    public static final String QUEUE_NAME = "boot_topic_queue";
    
    @Bean("bootExchange")
    public Exchange bootExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE_NAME).durable(true).build();
        // Exchange 类型：Topic
        // durable(true)：服务重启后 Exchange 不会被删除
    }
    
    @Bean("bootQueue")
    public Queue bootQueue() {
        return QueueBuilder.durable(QUEUE_NAME).build();
        // durable(true)：服务重启后 Queue 及其消息不会丢失
    }
    
    @Bean
    public Binding bootBinding(@Qualifier("bootQueue") Queue queue,
                               @Qualifier("bootExchange") Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("boot.#").noargs();
        // 绑定关系：Exchange 中 routing key 匹配 "boot.#" 的消息都进入 Queue
        // "boot.#" 通配符含义：boot. 开头的所有 routing key
        //   ├─ boot_SysLogAspect  ✓ 匹配
        //   ├─ boot_xxxx          ✓ 匹配
        //   └─ other_log          ✗ 不匹配
    }
}
```

**RabbitMQ 工作流程**：

```
生产者（SysLogAspect）
 ↓ 发送消息
rabbitTemplate.convertAndSend(
    "boot_topic_exchange",   // Exchange
    "boot_SysLogAspect",     // Routing Key
    jsonLog
)
 ↓
Exchange 查看绑定规则
 ↓
是否存在 Binding：Pattern="boot.#"，Queue="boot_topic_queue"？
 ↓
YES → 消息进入 boot_topic_queue
 ↓
消费者（MQConsumer）
 ↓
@RabbitListener(queues = "boot_topic_queue")
public void consume(String jsonLog) { ... }
```

---

## 常见耦合点与性能瓶颈

### 🚨 耦合点 1：获取当前用户信息困难

**问题**：

```java
// SysLogAspect 中需要获取当前用户 ID，但没有直接的方式
@Around("@annotation(operationLog)")
public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) {
    // .userId() TODO: 从 SecurityContextHolder 获取用户 ID
    // ↑ 注释表明这里还没有实现
}
```

**原因**：

- SecurityContextHolder 中存储的是 `UsernamePasswordAuthenticationToken`
- principal 只有 username，没有其他用户信息

**解决方案**：

```java
// 方法 1：创建自定义 UserDetailsImpl，存入更多信息
public class UserDetailsImpl extends User {
    private Long userId;
    private String nickname;
    
    public UserDetailsImpl(String username, String password, Long userId) {
        super(username, password, new ArrayList<>());
        this.userId = userId;
    }
}

// 方法 2：创建工具类从 Token 中提取用户 ID
public class SecurityUtils {
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof String) {
            // principal 是 username，需要再查一次数据库
            String username = (String) auth.getPrincipal();
            SysUser user = sysUserService.getUserByName(username);
            return user.getId();
        }
        return null;
    }
}

// 在 SysLogAspect 中使用
@Around("@annotation(operationLog)")
public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) {
    Long userId = SecurityUtils.getCurrentUserId();
    sysLog.setUserId(userId);
    // ...
}
```

### 🚨 耦合点 2：参数过滤逻辑分散

**问题**：

```java
// LogFilter.logFilter() 负责敏感数据过滤
// 但它的实现逻辑不清楚
List<Object> filteredParams = LogFilter.logFilter(joinPoint, result);
```

**原因**：

- 需要哪些参数被过滤？
- 过滤规则在哪里定义？
- 如果添加新的敏感字段，如何扩展？

**改进方案**：

```java
// 集中管理敏感字段
@Component
public class SensitiveFieldFilter {
    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "password", "phone", "email", "idCard", "bankCard"
    );
    
    public Object filterSensitiveData(Object obj) {
        if (obj instanceof String) return "***";
        if (obj instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) obj;
            for (String field : SENSITIVE_FIELDS) {
                if (map.containsKey(field)) {
                    map.put(field, "***");
                }
            }
        }
        return obj;
    }
}
```

### 🚨 耦合点 3：权限查询性能

**问题**：

```java
// 每次登录都要查一次权限表
List<String> sysMenuRoleList = sysMenuService.getMenuIds(sysUser.getId());
```

**性能风险**：

- 如果权限表数据庞大（100+ 权限）
- 每个用户登录都要 JOIN 多个表
- 高并发场景下，数据库查询会成为瓶颈

**改进方案**：

```java
// 方案 1：缓存权限（Redis）
@Cacheable(value = "userPermissions", key = "#userId")
public List<String> getMenuIds(Long userId) {
    // 第一次查询，结果存入 Redis
    // 后续查询直接从 Redis 读取
}

// 方案 2：登录时一次查询，Token 中包含权限
// （项目已经这样做了）
map.put(JwtConstants.PERMISSION, sysMenuRoleList);
String accessToken = JwtUtils.generateAccessToken(map);
```

### ⚡ 性能瓶颈 1：LogAspect 同步发送日志

**问题**：

```java
// 发送日志到 RabbitMQ 是同步操作
// 如果 RabbitMQ 故障，会阻塞 HTTP 响应
rabbitTemplate.convertAndSend(EXCHANGE_NAME, "boot_SysLogAspect", jsonLog);
```

**影响**：

- HTTP 响应时间 = 业务逻辑 + 日志发送
- RabbitMQ 故障 → 所有请求都会超时

**改进方案**：

```java
// 异步发送日志，不阻塞响应
@Async  // ← 使用 @Async 注解
public void sendLogAsync(String jsonLog) {
    try {
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, "boot_SysLogAspect", jsonLog);
    } catch (Exception e) {
        // 日志发送失败不影响业务响应
        log.error("发送日志失败", e);
    }
}

// 在 AOP 中调用
sendLogAsync(objectMapper.writeValueAsString(sysLog));

// 返回结果（无需等待日志发送完成）
return result;
```

### ⚡ 性能瓶颈 2：SQL 打印降低性能

**问题**：

```yaml
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    # ← 每次 SQL 都打印，I/O 开销大
```

**影响**：

- 开发环境：便于调试，可以保留
- 生产环境：打印日志是 I/O 操作，会降低吞吐量

**改进方案**：

```yaml
# 分环境配置
spring:
  profiles:
    active: dev
---
spring:
  config:
    activate:
      on-profile: dev
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # 开发环境打印
---
spring:
  config:
    activate:
      on-profile: prod
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.noop.NoLoggingImpl  # 生产环境关闭
```

### ⚡ 性能瓶颈 3：每次密码验证都用 BCrypt

**问题**：

```java
if (!passwordEncoder.matches(loginDTO.getPassword(), sysUser.getPassword())) {
    // BCrypt 算法为了安全性，密码验证较慢（耗时 100ms+）
}
```

**影响**：

- 登录请求本身较慢
- 如果有人恶意尝试密码（10 次错误），总耗时 1 秒+

**改进方案**：

```java
// 实现登录失败次数限制
@Service
public class LoginAttemptService {
    private Map<String, LoginAttempt> cache = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION = 15 * 60 * 1000;  // 15 分钟锁定
    
    public void recordFailure(String username) {
        LoginAttempt attempt = cache.computeIfAbsent(username, 
            k -> new LoginAttempt());
        attempt.increment();
        
        if (attempt.getCount() >= MAX_ATTEMPTS) {
            attempt.setLockedUntil(System.currentTimeMillis() + LOCK_DURATION);
        }
    }
    
    public boolean isLocked(String username) {
        LoginAttempt attempt = cache.get(username);
        if (attempt != null && attempt.getLockedUntil() > System.currentTimeMillis()) {
            return true;
        }
        return false;
    }
}

// 在登录时使用
@PostMapping("/login")
public Result<?> login(@RequestBody LoginDTO loginDTO) {
    if (loginAttemptService.isLocked(loginDTO.getUsername())) {
        throw new UsernameNotFoundException("账号已锁定，请 15 分钟后重试");
    }
    
    try {
        // 验证逻辑...
        loginAttemptService.reset(loginDTO.getUsername());  // 成功则重置
    } catch (Exception e) {
        loginAttemptService.recordFailure(loginDTO.getUsername());  // 失败则记录
        throw e;
    }
}
```

---

## 扩展指南

### 场景 1：新增一个业务模块（以"菜单管理"为例）

#### Step 1：创建 Entity

**文件**：`aegis-system/src/main/java/com/aegis/system/entity/SysMenu.java`

模仿 `SysUser.java` 的结构：

```java
package com.aegis.system.entity;

import com.aegis.common.annotation.Sensitive;
import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_menu")  // ← 表名
public class SysMenu {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;        // 菜单名称
    private String path;        // 菜单路径
    private String icon;        // 菜单图标
    private Integer sort;       // 排序
    private Integer visible;    // 是否可见
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer isDeleted;
}
```

#### Step 2：创建 Mapper

**文件**：`aegis-system/src/main/java/com/aegis/system/mapper/SysMenuMapper.java`

```java
package com.aegis.system.mapper;

import com.aegis.system.entity.SysMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {
    // 继承 BaseMapper 自动获得 CRUD 方法
    // 如果需要自定义 SQL，可以在这里添加
}
```

#### Step 3：创建 Service 接口

**文件**：`aegis-system/src/main/java/com/aegis/system/service/SysMenuService.java`

```java
package com.aegis.system.service;

import com.aegis.system.entity.SysMenu;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SysMenuService extends IService<SysMenu> {
    // 继承 IService 自动获得 CRUD 方法
    // 定义菜单特有的业务方法
    
    // 获取用户的权限列表
    List<String> getMenuIds(Long userId);
}
```

#### Step 4：创建 Service 实现

**文件**：`aegis-system/src/main/java/com/aegis/system/service/impl/SysMenuServiceImpl.java`

```java
package com.aegis.system.service.impl;

import com.aegis.system.entity.SysMenu;
import com.aegis.system.mapper.SysMenuMapper;
import com.aegis.system.service.SysMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu>
        implements SysMenuService {
    
    @Override
    public List<String> getMenuIds(Long userId) {
        // 查询用户的权限列表
        // 需要 JOIN: sys_user → sys_user_role → sys_role_menu → sys_menu
        // 可以使用 MyBatis 的 @Select 注解编写 SQL
        
        return lambdaQuery()
                .select(SysMenu::getName)
                .eq(SysMenu::getVisible, 1)
                .list()
                .stream()
                .map(SysMenu::getName)
                .collect(Collectors.toList());
    }
}
```

#### Step 5：创建 Controller

**文件**：`aegis-system/src/main/java/com/aegis/system/controller/SysMenuController.java`

模仿 `SysUserController.java`：

```java
package com.aegis.system.controller;

import com.aegis.common.annotation.OperationLog;
import com.aegis.common.result.Result;
import com.aegis.system.entity.SysMenu;
import com.aegis.system.service.SysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/menus")
public class SysMenuController {
    
    @Autowired
    private SysMenuService sysMenuService;
    
    @PreAuthorize("hasAuthority('sys:menu:list')")
    @GetMapping
    @OperationLog("列出所有菜单")
    public Result<?> list() {
        return Result.success(sysMenuService.list());
    }
    
    @PreAuthorize("hasAuthority('sys:menu:add')")
    @PostMapping
    @OperationLog("新增菜单")
    public Result<?> add(@RequestBody SysMenu sysMenu) {
        sysMenuService.save(sysMenu);
        return Result.success(sysMenu);
    }
    
    @PreAuthorize("hasAuthority('sys:menu:edit')")
    @PutMapping("/{id}")
    @OperationLog("编辑菜单")
    public Result<?> update(@PathVariable Long id, @RequestBody SysMenu sysMenu) {
        sysMenu.setId(id);
        sysMenuService.updateById(sysMenu);
        return Result.success();
    }
    
    @PreAuthorize("hasAuthority('sys:menu:delete')")
    @DeleteMapping("/{id}")
    @OperationLog("删除菜单")
    public Result<?> delete(@PathVariable Long id) {
        sysMenuService.removeById(id);
        return Result.success();
    }
}
```

#### Step 6：启动应用，测试 API

```bash
# 启动应用
mvn spring-boot:run

# 先登录获取 Token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 返回 Token
{
  "data": {
    "accessToken": "eyJhbGc...",
    "refreshToken": "eyJhbGc..."
  }
}

# 用 Token 调用菜单接口
curl -X GET http://localhost:8080/api/v1/menus \
  -H "Authorization: Bearer eyJhbGc..."
```

### 场景 2：新增一个自定义注解

#### Step 1：定义注解

**文件**：`aegis-common/src/main/java/com/aegis/common/annotation/RequireAdmin.java`

```java
package com.aegis.common.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAdmin {
    String message() default "仅管理员可以执行此操作";
}
```

#### Step 2：创建切面处理

**文件**：`aegis-system/src/main/java/com/aegis/system/aspect/AdminCheckAspect.java`

```java
package com.aegis.system.aspect;

import com.aegis.common.annotation.RequireAdmin;
import com.aegis.common.exception.BusinessException;
import com.aegis.common.result.ResultCode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AdminCheckAspect {
    
    @Around("@annotation(requireAdmin)")
    public Object around(ProceedingJoinPoint joinPoint, RequireAdmin requireAdmin) 
            throws Throwable {
        
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        
        // 简单判断（实际应该查询数据库）
        if (!"admin".equals(username)) {
            throw new BusinessException(
                    ResultCode.FORBIDDEN.getCode(),
                    requireAdmin.message()
            );
        }
        
        return joinPoint.proceed();
    }
}
```

#### Step 3：使用注解

```java
@RestController
@RequestMapping("/api/v1/system")
public class SystemController {
    
    @RequireAdmin(message = "仅管理员可以访问系统设置")
    @PostMapping("/config")
    public Result<?> updateSystemConfig(@RequestBody SystemConfig config) {
        // 这个方法只有 admin 用户可以调用
        // 其他用户调用会返回 403 Forbidden
    }
}
```

### 场景 3：添加数据库迁移脚本

> 为什么需要？生产环境中，数据库结构变更需要可追踪和可回滚。

**使用 Flyway 进行数据库版本管理**：

#### Step 1：添加依赖

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

#### Step 2：创建 SQL 脚本

**文件位置**：`aegis-app/src/main/resources/db/migration/`

**命名规则**：`V版本号__描述.sql`

例如 `V1.0.0__init_schema.sql`：

```sql
-- V1.0.0__init_schema.sql

CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    nickname VARCHAR(50),
    avatar VARCHAR(255),
    status INT DEFAULT 1,
    create_by VARCHAR(50),
    update_by VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    path VARCHAR(255),
    icon VARCHAR(100),
    sort INT DEFAULT 0,
    visible INT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**V1.0.1__add_role_table.sql**（后续版本）：

```sql
-- V1.0.1__add_role_table.sql

CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### Step 3：配置 Flyway

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true  # 如果表已存在，自动创建 Flyway 表
```

#### Step 4：启动应用时自动执行

```
应用启动
 ↓
Flyway 检查已执行的 SQL（flyway_schema_history 表）
 ↓
找到新的 SQL 脚本（V1.0.1__xxx.sql）
 ↓
顺序执行新脚本
 ↓
记录执行历史
 ↓
应用启动完成
```

### 场景 4：集成 Swagger 自动生成 API 文档

#### Step 1：添加依赖

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.0.0</version>
</dependency>
```

#### Step 2：配置 Swagger

```yaml
springdoc:
  swagger-ui:
    path: /swagger-ui.html
    operations-sorter: method  # 按 HTTP 方法排序
```

#### Step 3：在 Controller 上添加注解

```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "用户管理", description = "用户增删改查接口")
public class SysUserController {
    
    @Operation(summary = "获取用户列表", description = "分页获取所有用户")
    @GetMapping
    public Result<?> list() { ... }
    
    @Operation(summary = "新增用户", description = "创建新用户")
    @PostMapping
    public Result<?> save(@RequestBody SysUser sysUser) { ... }
}
```

#### Step 4：访问文档

```
启动应用后访问：http://localhost:8080/swagger-ui.html
```

---

## 总结

### 项目架构核心要点

| 要点 | 说明 |
|------|------|
| **分层架构** | Controller → Service → Mapper → Database |
| **模块化** | aegis-app 依赖 aegis-system 依赖 aegis-common |
| **认证方式** | JWT Token + Spring Security |
| **权限控制** | @PreAuthorize + 注解 |
| **日志记录** | AOP 切面自动拦截，异步发送 MQ |
| **数据安全** | BCrypt 密码加密、@Sensitive 字段脱敏、逻辑删除 |
| **可扩展性** | 按照模板模式添加新模块（Entity → Mapper → Service → Controller） |

### 学习路径建议

```
第一天：理解整体架构
  ├─ 阅读 README 和项目结构
  ├─ 运行项目，测试登录 API
  └─ 单步调试（Debug）登录流程

第二天：深入认证与权限
  ├─ 理解 JWT Token 原理
  ├─ 追踪 SecurityConfig 和 JwtAuthFilter
  ├─ 修改权限规则，观察效果
  └─ 编写单元测试

第三天：学习设计模式和最佳实践
  ├─ 分析 AOP 切面的实现
  ├─ 理解 MyBatis Plus 的工厂模式
  ├─ 优化日志记录的性能
  └─ 添加新的注解切面

第四天：扩展新功能
  ├─ 按照指南添加一个新模块
  ├─ 集成 Swagger 文档
  ├─ 添加数据库迁移脚本
  └─ 编写集成测试

第五天：性能优化与问题排查
  ├─ 识别并优化性能瓶颈
  ├─ 实现缓存策略
  ├─ 监控和日志分析
  └─ 生产环境部署清单
```

### 常见问题 FAQ

**Q1：为什么要分成三个模块？**
A：遵循单一职责原则。aegis-common 是基础设施，aegis-system 是业务核心，aegis-app 是启动程序。模块化便于复用、测试和维护。

**Q2：JWT Token 如何防止篡改？**
A：Token 中包含签名（Signature），签名是用密钥对 header + payload 加密生成的。任何篡改 payload 都会导致签名不匹配，Token 验证失败。

**Q3：为什么要用 RabbitMQ 发送日志？**
A：异步处理，不阻塞 HTTP 响应。即使日志发送失败，也不影响业务逻辑。这样可以处理日志系统故障的情况。

**Q4：逻辑删除和物理删除有什么区别？**
A：逻辑删除标记 `is_deleted = 1`，数据仍在数据库；物理删除直接 `DELETE`，数据彻底删除。逻辑删除便于数据恢复和审计。

**Q5：为什么 PasswordEncoder 要用 BCrypt？**
A：BCrypt 加入了盐值和时间成本，即使两个用户密码相同，加密结果也不同。每次验证都要花 100ms+ 计算，防止暴力破解。

---

## 相关资源

- Spring Boot 官方文档：https://spring.io/projects/spring-boot
- Spring Security 官方文档：https://spring.io/projects/spring-security
- MyBatis Plus 官方文档：https://baomidou.com
- JWT 标准：https://jwt.io
- RabbitMQ 官方文档：https://www.rabbitmq.com/documentation.html

---

**文档版本**：1.0  
**最后更新**：2026-04-12  
**适配项目版本**：Aegis-Admin 1.0.0



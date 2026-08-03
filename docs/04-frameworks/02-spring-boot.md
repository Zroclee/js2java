# Spring Boot：Java 后端的"操作系统"

> 状态：✅ 已完成

## 简介
本章学习内容：Spring 到 Spring Boot 的演进、IoC/DI 原理与 Bean、AOP、Spring MVC Web 层、自动配置、与 Express/NestJS 的对比、最小应用实战
前置知识：完成「14-反射注解泛型」（迷你 IoC）与「01-maven」；有 Express 或 NestJS 经验
阅读时长：约 70 分钟（全教程最核心一章）
难度：🌟🌟🌟🌟
重要程度：🌟🌟🌟🌟🌟
当前进度：95%

---

## 为什么需要 Spring：先看没有框架的日子

没有框架时，一个业务类的依赖全靠自己手工组装：

```java
public class UserService {
    private UserDao userDao = new UserDao();          // 写死在代码里 ⚠️
    private MailSender mailSender = new MailSender(); // 每个依赖都自己 new
    // 换实现？改代码。要单例？自己保证。依赖的依赖？一层层 new 下去……
}
```

问题成堆：**创建逻辑和业务逻辑耦合**、换实现要改源码、对象生命周期没人管（要单例的 new 了一堆，共享状态的乱套）、每个项目重复造这套轮子。

Spring 的答案只有一句话：**对象的创建和组装，交给容器来做——你的代码只声明"我需要什么"**。这就是 IoC。

## 从 Spring 到 Spring Boot：二十年演进史

```
2003  Spring Framework：IoC 容器 + AOP 横空出世（替代笨重的 EJB）
      └─ 痛点：XML 配置地狱——每个 Bean 都要写 <bean> 标签，几百行起步
2006+ 注解时代：@Component/@Autowired 取代 XML，配置回到代码里
2014  Spring Boot：约定优于配置 🆕
      ├─ 自动配置：classpath 里有什么，就自动配好什么
      ├─ 内嵌服务器：不用装 Tomcat，main 方法直接启动 Web 服务
      └─ Starter 依赖：一个坐标拉来一整套（≈ 套餐 vs 单点）
```

前端类比 😌：这演进史你熟——从手写 webpack.config 几百行，到 **create-vue / Vite 开箱即用**的"约定优于配置"。**Spring Boot 不是新框架，是 Spring 的"自动驾驶版"**：引擎（IoC/AOP/MVC）全是 Spring 的，Boot 把钥匙、导航、座椅全调好了。

## IoC 与 DI：把第 14 章的迷你容器换成工业版

### 概念对齐（你在迷你 IoC 里已经做过一遍 😌）

| 概念 | 你的迷你 IoC | Spring |
|------|-------------|--------|
| 标记要被管理 | `@Component` 自定义注解 | `@Component` 官方注解（一模一样） |
| 容器 | `MiniContainer`（一个 Map） | `ApplicationContext`（超级大 Map + 生命周期管理） |
| 创建实例 | 反射 `newInstance` | 反射 + 代理增强 |
| 取用 | `getBean(Class)` | `@Autowired` 自动注入到字段/构造器 |
| 依赖注入 | 练习 3 的 `@AutoInject` | `@Autowired`（一模一样） |

**IoC（控制反转）**：对象的创建权从你的代码"反转"给容器。**DI（依赖注入）**：IoC 的实现方式——你声明依赖，容器装配时注入进来。两个词日常混用。

### Bean：被容器管理的对象 🆕

**Bean 就是"Spring 容器创建并管理的对象"**——本质是个普通 Java 对象，特殊在生杀大权归容器。把你的类交给容器的方式是贴注解：

```java
// 四个常用注解，功能相同（都注册为 Bean），语义分层 🔥
@Component    // 通用组件
@Service      // 业务逻辑层
@Repository   // 数据访问层（DAO/Mapper）
@RestController // Web 控制器（= @Controller + 返回 JSON）

@Service
public class UserService {
    private final UserRepository userRepository;

    // 构造器注入（推荐写法 🔥）：声明依赖，容器启动时自动注入
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

⚠️ **字段注入 vs 构造器注入**：老代码常见 `@Autowired private UserRepository repo;`（字段注入）——能跑，但隐藏依赖、不利于测试、无法 final。**规范写法是构造器注入**（只有一个构造器时 @Autowired 可省）💼。

### Bean 默认是单例 ⚠️（和并发章联动）

容器里每个 Bean 默认**全局只有一个实例**（singleton 作用域）——所有请求共享同一个 UserService 对象。推论：

```java
@Service
public class UserService {
    private int counter;   // ⚠️ 危险！多线程共享的可变状态 → 竞态条件（并发章的坑在这重现）
    // Bean 应该设计为【无状态】：依赖是 final 的，数据走参数和返回值
}
```

需要每次新实例时声明 `@Scope("prototype")`，但 99% 的业务 Bean 都是无状态单例 💼。

## AOP：不改业务代码，统一加"横切"行为 🆕

### 是什么

日志、事务、权限、耗时统计——这些需求和每个业务方法都有关，但又**不是业务本身**。写进方法里是污染，复制一百处是灾难。**AOP（面向切面编程）：把这些"横切关注点"抽出来，由框架在方法执行前后统一织入**。

### 前端类比：Express 中间件，但范围大得多

Express 中间件也是一种切面——在请求-响应的通道上统一插行为（cors、日志）。但注意差异 ⚠️：**Express 中间件只能切 HTTP 请求；AOP 能切任意 Bean 的任意方法**——Service 层、Mapper 层、任何自定义方法，HTTP 只是其中一个入口。

### 核心概念三件套 💼

```java
@Aspect          // 切面 = 切点 + 通知 的合体
@Component
public class LogAspect {

    // 切点（Pointcut）：切哪些方法（这里：service 包下所有类的所有方法）
    @Around("execution(* com.example.service.*.*(..))")
    public Object logTime(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = pjp.proceed();              // 执行原方法
        System.out.println(pjp.getSignature() + " 耗时 " + (System.currentTimeMillis() - start) + "ms");
        return result;
    }
}
```

- **切点（Pointcut）**：选哪些方法（表达式匹配）
- **通知（Advice）**：什么时候动手——`@Before` 前 / `@After` 后 / **`@Around` 环绕**（前后都管，最强）
- **切面（Aspect）**：两者打包

### AOP 的实现：代理模式 💡

容器给你的 Bean 常常**不是原对象，而是代理对象**：调用方以为自己调的是 UserService，实际调的是"包了壳的 UserService"——壳里先跑切面逻辑，再调真身。这解释了两大天问：

1. **`@Transactional` 为什么贴个注解就有事务**：Spring 给你的 Service 是代理，方法进入时开事务、正常返回提交、异常回滚——**上一章 MySQL 的事务，在这里变成了一行注解** 🔥
2. **为什么自调用（this.method()）会失效**：this 绕过代理直接调原对象，切面没机会介入 ⚠️💼

顺带回扣接口章：JDK 动态代理要求目标有接口，没接口走 CGLIB（生成子类代理）——当年学的接口在这又升值了 💡。

## Spring MVC：Web 层，对标 Express 路由

### 请求处理流程

```
HTTP 请求 → DispatcherServlet（前置控制器，≈ Express 的 app 总入口）
         → 按 URL 映射到某个 @RestController 的方法
         → 参数自动绑定（JSON → Java 对象！）
         → 方法返回 Java 对象 → Jackson 自动序列化为 JSON 响应 🆕
```

🆕 最爽的一点：**你全程只和 Java 对象打交道**——请求 JSON 自动变对象，返回对象自动变 JSON（靠 Jackson 库，Boot 内置）。Express 里手动 `JSON.parse / res.json()` 的心智负担，这里归零。

### 注解对照 Express 🔥

```java
@RestController
@RequestMapping("/users")              // 路径前缀 ≈ app.use('/users', router)
public class UserController {

    private final UserService userService;
    public UserController(UserService userService) {   // 注入 Service
        this.userService = userService;
    }

    @GetMapping("/{id}")               // ≈ router.get('/users/:id')
    public User getById(@PathVariable Long id) {       // ≈ req.params.id
        return userService.getById(id); // 返回对象 → 自动变 JSON
    }

    @GetMapping                         // ≈ router.get('/users?name=x')
    public List<User> list(@RequestParam String name) { // ≈ req.query.name
        return userService.findByName(name);
    }

    @PostMapping                        // ≈ router.post('/users')
    public User create(@RequestBody @Valid CreateUserReq req) {  // ≈ req.body（JSON → 对象）
        return userService.create(req);
    }

    @PutMapping("/{id}")                // @PutMapping / @DeleteMapping 同理
    public User update(@PathVariable Long id, @RequestBody UpdateUserReq req) { ... }
}
```

| Spring MVC | Express |
|-----------|---------|
| `@RestController` + `@RequestMapping` | `express.Router()` |
| `@GetMapping("/{id}")` | `router.get('/:id')` |
| `@PathVariable` | `req.params` |
| `@RequestParam` | `req.query` |
| `@RequestBody` | `req.body`（且自动反序列化为对象 🆕） |
| 返回对象自动 JSON | `res.json(obj)` 手动 |
| `@Valid` 参数校验 | 手写或 zod/joi 中间件 |

### 全局异常处理：第 12 章的预告兑现 🔥

业务代码放心 `throw BizException`，统一在一个地方转成响应：

```java
@RestControllerAdvice            // ≈ Express 的错误处理中间件 (err, req, res, next)
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ApiError handleBiz(BizException e) {
        return new ApiError(e.getCode(), e.getMessage());   // 统一错误格式
    }
}
```

## 与 Express / NestJS 的区别（你点名的问题）💼

### vs Express：极简工具箱 vs 全套工业体系

| 维度 | Express | Spring Boot |
|------|---------|-------------|
| 哲学 | 极简：路由 + 中间件，结构自己定 | 全家桶：分层架构（Controller→Service→Mapper）制度化 🔥 |
| 依赖管理 | 手动传参/模块单例，约定俗成 | **IoC 容器**：注入是框架级能力 🆕 |
| 横切逻辑 | 中间件（仅限 HTTP 通道） | **AOP**：可切任意 Bean 方法 |
| 请求处理 | 手动解析、手动 res.json | 对象自动绑定/序列化 |
| 配置 | dotenv + 自己组织 | application.yml + 自动配置 |
| 适合 | 小项目、微服务、快速原型 | 中大型业务系统、团队协作 |

一句话：**Express 给你自由，Spring 给你体系**。自由意味着每个项目结构都不一样；体系意味着任何 Java 后端打开任何 Spring 项目都能秒懂结构 🔥。

### vs NestJS：它就是"TypeScript 版 Spring" 😲

如果你有 NestJS 经验，这章你其实学完了大半——**NestJS 是照着 Spring 抄的**（作者公开承认）：

| NestJS | Spring |
|--------|--------|
| `@Module()` | 包扫描 + `@Configuration` |
| `@Injectable()` | `@Service` / `@Component` |
| `@Controller()` + `@Get()` | `@RestController` + `@GetMapping` |
| 构造器注入 | 构造器注入（一模一样） |
| 装饰器驱动 | 注解驱动 |
| `main.ts` bootstrap | `main` + `SpringApplication.run` |

IoC 容器、DI、装饰器/注解、分层、单例 Provider——心智完全同构。学 NestJS 的经历，就是你学 Spring 的预科 😌。

## 自动配置：Boot 的魔法，两分钟解密

为什么 pom 加个 starter、main 方法一跑，Web 服务器、JSON 转换全好了？

```xml
<!-- Starter ≈ 套餐：一个坐标拉来 spring-web + 内嵌 Tomcat + Jackson 一整套 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

```
启动流程（简化）：
@SpringBootApplication
  ├─ 包扫描：把所有 @Component/@Service/... 注册成 Bean（迷你 IoC 同款）
  └─ 自动配置：扫描 classpath——
       发现 Tomcat 在？→ 自动配置内嵌服务器
       发现 Jackson 在？→ 自动配置 JSON 转换器
       （@ConditionalOnClass 条件注解：有什么配什么 🆕）
```

配置文件 `application.yml`（≈ .env + config 合体 🔥）：

```yaml
server:
  port: 8081          # 改端口
spring:
  application:
    name: demo
```

## 实战：15 分钟跑起第一个 Spring Boot 应用

1. **生成项目**：打开 [start.spring.io](https://start.spring.io/)（官方脚手架 ≈ create-vue）→ 选 Maven / Java 21 / 依赖勾 `Spring Web` → 下载解压，IDEA 打开（Ultimate 也可直接 New Project → Spring Initializr）
2. **看启动类**（已生成）：

```java
@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);   // 启动整个容器+Web服务
    }
}
```

3. **写一个接口**：

```java
@RestController
public class HelloController {
    @GetMapping("/hello")
    public Map<String, Object> hello(@RequestParam(defaultValue = "Java") String name) {
        return Map.of("msg", "Hello, " + name + "!");
    }
}
```

4. **运行 main**，访问 `http://localhost:8080/hello?name=js2java` → `{"msg":"Hello, js2java!"}` ✅

没有装 Tomcat、没有 web.xml、没有 XML 配置——这就是 Boot 的"自动驾驶"。

## 练习

1. 在实战项目基础上新增 `UserService`（@Service，内含一个返回模拟用户的方法），用**构造器注入**到 `UserController`，实现 `GET /users/{id}` 返回 JSON 用户对象；再用 Debug 在 Service 方法里打断点，体会调用链。
2. 写一个 `@Aspect` 切面：统计所有 Controller 方法的执行耗时并打印；观察它"不改任何 Controller 代码就生效"的事实。
3. 思考题：在 `UserService` 的方法 A 里用 `this.methodB()` 调用同类方法 B，B 上的切面（或 @Transactional）会生效吗？为什么？（提示：想想代理）

## 本章总结

- Spring 解决的问题：依赖手工组装的耦合 → **IoC：创建权交给容器**；演进史 = XML 地狱 → 注解 → Boot 约定优于配置（≈ 手写 webpack → create-vue）
- **Bean** = 容器管理的对象，默认**单例无状态**；构造器注入是规范写法；四大注解语义分层
- **AOP** = 不改代码统一织入横切行为；切点+通知+切面；**代理模式**是底牌（解释 @Transactional 魔法与 this 自调用失效）
- **Spring MVC**：DispatcherServlet 统一入口，注解对标 Express 路由，**对象 ⇄ JSON 全自动**；@RestControllerAdvice 全局异常
- vs Express：自由 vs 体系；vs NestJS：**NestJS 就是 TS 版 Spring**，心智同构 😌
- 自动配置 = 条件注解"classpath 有什么配什么"；Starter ≈ 依赖套餐

下一章：[MyBatis](./03-mybatis.md)——给这套体系接上数据库

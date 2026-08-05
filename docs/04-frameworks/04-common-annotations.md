# Java 常用注解速查：Spring Boot / Validation / Lombok / JPA / MyBatis

> 状态：✅ 已完成

## 简介
本章学习内容：按「框架 / 依赖库」维度系统梳理 Java 后端高频注解——Spring & Spring Boot（Bean、Web、事务）、JSR-380 Validation 参数校验、Lombok 代码生成、JPA 对象关系映射、MyBatis / MyBatis-Plus 数据访问，以及测试注解
前置知识：完成「14-反射注解泛型」（注解语法与原理）、「02-spring-boot」「03-mybatis」
阅读时长：约 45 分钟
难度：🌟🌟🌟
重要程度：🌟🌟🌟🌟🌟
当前进度：97%

---

## 为什么单开一章讲注解

你在前面章节已经见过满屏的 `@` 了。这章做一件事：**把散落的注解按"它们属于哪个库"重新归堆，形成一张可以长期查阅的地图**。

先回扣第 14 章的底层认知（这是理解一切注解的钥匙）🔥：

> **注解本身只是一张贴纸，不会做任何事。真正干活的是"读取贴纸的人"**——可能是 IoC 容器（扫描 `@Component` 注册 Bean）、AOP 代理（看到 `@Transactional` 包上事务）、编译器插件（Lombok 看到 `@Data` 生成代码）、校验框架（看到 `@NotNull` 执行检查）。

所以遇到任何陌生注解，只需要问三个问题：**它是哪个库的？谁来读它？读到之后做什么？** 本章的每一节就按这三个问题展开。

前端类比 😌：这套玩法你其实早会了——**TS 装饰器**。NestJS 的 `@Injectable()`、class-validator 的 `@IsEmail()`、TypeORM 的 `@Entity()`，和 Java 注解是同一个心智模型（装饰器本身就是从 Java/C# 注解借鉴来的）。如果你有 NestJS + TypeORM 经验，本章一半是"迁移"，直接加速。

### 全景地图：注解按依赖归堆

| 依赖（Starter） | 干什么 | 代表注解 |
|----------------|--------|----------|
| `spring-boot-starter` | IoC / 配置 / 行为增强 | `@Component`、`@Autowired`、`@Configuration`、`@Transactional` |
| `spring-boot-starter-web` | Web 层（MVC） | `@RestController`、`@GetMapping`、`@RequestBody` |
| `spring-boot-starter-validation` | 参数校验（JSR-380） | `@NotBlank`、`@Email`、`@Valid` |
| `lombok`（编译期，非运行时框架） | 消除样板代码 | `@Data`、`@Builder`、`@Slf4j` |
| `spring-boot-starter-data-jpa` | ORM 对象关系映射 | `@Entity`、`@Id`、`@ManyToOne` |
| `mybatis-spring-boot-starter` | SQL 映射 | `@Mapper`、`@Select`、`@Param` |
| `mybatis-plus-boot-starter` | MyBatis 增强 | `@TableName`、`@TableId`、`@TableLogic` |
| `spring-boot-starter-test` | 测试 | `@SpringBootTest`、`@Test` |

⚠️ 一个经典误区：注解**不是 Spring 一家的**。`@Entity` 属于 JPA 规范、`@NotNull` 属于 Jakarta Validation 规范、`@Data` 属于 Lombok——它们只是恰好都能在 Spring 项目里协作。分清"注解的娘家"，查文档时才不会迷路 💼。

---

## 一、Spring / Spring Boot 注解 🔥

这一组数量最多，按用途再分四小堆。

### 1.1 启动与配置

```java
@SpringBootApplication   // 三合一：@Configuration + @EnableAutoConfiguration + @ComponentScan
public class DemoApplication { ... }
```

| 注解 | 贴在哪 | 含义与作用 |
|------|--------|-----------|
| `@SpringBootApplication` | 启动类 | 开启包扫描 + 自动配置，整个应用的入口开关 |
| `@Configuration` | 类 | 声明"我是配置类"，类里的 `@Bean` 方法返回值会注册进容器 |
| `@Bean` | 方法 | 手动注册一个 Bean（用于注册**第三方类**——你不能去别人源码上贴 `@Component`） |
| `@Value("${server.port}")` | 字段 | 从 application.yml 注入单个配置值 |
| `@ConfigurationProperties(prefix = "app")` | 类 | 把一组配置绑定成对象（比一堆 `@Value` 优雅 🔥） |
| `@Profile("dev")` | 类/方法 | 该 Bean 只在指定环境（profile）生效 ≈ 前端的 `if (process.env.NODE_ENV)` |

```java
// @Configuration + @Bean：注册第三方对象的标配姿势
@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();   // 返回值被容器收编为 Bean，别人可以注入
    }
}

// @ConfigurationProperties：一组配置 → 一个对象
@ConfigurationProperties(prefix = "upload")
@Component
public class UploadProps {
    private String dir;      // 自动绑定 upload.dir
    private long maxSize;    // 自动绑定 upload.max-size（kebab-case 自动转驼峰）
}
```

### 1.2 Bean 注册与注入

| 注解 | 含义 | 备注 |
|------|------|------|
| `@Component` / `@Service` / `@Repository` / `@RestController` | 注册为 Bean，语义分层 | 功能相同，见 Spring Boot 章 |
| `@Autowired` | 按类型注入 | 构造器注入时可省略 |
| `@Qualifier("x")` | 同类型多 Bean 时按名字指定 | 与 `@Autowired` 搭配 |
| `@Primary` | 同类型多 Bean 时设为默认 | 贴在某个实现上 |
| `@Resource(name = "x")` | 按名字注入（JSR-250 规范） | 非 Spring 亲儿子，用法类似 💼 |
| `@Scope("prototype")` | 每次注入新建实例 | 默认 singleton |
| `@Lazy` | 延迟初始化，首次使用时才创建 | 解决循环依赖/启动慢的偏方 |

⚠️ `@Autowired` vs `@Resource`（面试高频 💼）：前者**按类型**（Spring 的），多个候选时配合 `@Qualifier`；后者**先按名字**（Java EE 规范）。日常构造器注入为主，这两个出现得越来越少。

### 1.3 Web 层（Spring MVC）

这组在 Spring Boot 章已逐个对标 Express，这里速查：

| 注解 | 贴在哪 | 作用 |
|------|--------|------|
| `@RestController` | 类 | = `@Controller` + 返回值自动转 JSON |
| `@RequestMapping("/users")` | 类/方法 | 路径前缀/通用映射 |
| `@GetMapping` `@PostMapping` `@PutMapping` `@DeleteMapping` `@PatchMapping` | 方法 | 按 HTTP 方法的映射 |
| `@PathVariable` | 参数 | 取路径变量 `/users/{id}` |
| `@RequestParam` | 参数 | 取 query 参数，可设 `required`/`defaultValue` |
| `@RequestBody` | 参数 | 请求体 JSON → Java 对象 |
| `@RequestHeader` / `@CookieValue` | 参数 | 取请求头 / Cookie |
| `@RestControllerAdvice` + `@ExceptionHandler` | 类 + 方法 | 全局异常处理（≈ Express 错误中间件） |
| `@CrossOrigin` | 类/方法 | 放开 CORS（≈ cors 中间件） |

### 1.4 行为增强（AOP 驱动的"魔法注解"）🆕

**这类注解是注解威力的极致体现：贴上去，方法就多出一种能力**。底牌全是 AOP 代理（见 Spring Boot 章）。

| 注解 | 作用 | 要点 |
|------|------|------|
| `@Transactional` | 方法套上数据库事务，异常自动回滚 | 🔥💼 注意 `rollbackFor`、自调用失效、只读事务 |
| `@Async` | 方法丢进线程池异步执行，立即返回 | 需在配置类加 `@EnableAsync` |
| `@Scheduled(cron = "0 0 2 * * ?")` | 定时任务 | 需 `@EnableScheduling`；≈ node-cron |
| `@Cacheable("users")` / `@CacheEvict` | 方法结果缓存（常配 Redis） | 读缓存/清缓存成对出现 |
| `@EventListener` | 监听 Spring 事件，做发布订阅 | ≈ Node 的 EventEmitter |
| `@Retryable` | 失败后自动重试 | 需 spring-retry 依赖 |

```java
@Service
public class OrderService {

    @Transactional(rollbackFor = Exception.class)   // ⚠️ 默认只对 RuntimeException 回滚
    public void createOrder(OrderReq req) {
        orderMapper.insert(req);      // 任何一步抛异常 → 整体回滚
        stockMapper.deduct(req);      // 不加 rollbackFor，受检异常不回滚！💼
    }

    @Async                              // 调用方立即返回，方法在线程池里跑
    public void sendNotify(Long orderId) { ... }

    @Scheduled(fixedDelay = 60_000)     // 每 60s 跑一次
    public void closeTimeoutOrders() { ... }
}
```

⚠️ **所有 AOP 注解的共同天坑：自调用失效**。同类中 `this.methodA()` 调方法 B，B 上的 `@Transactional/@Async/@Cacheable` 全部不生效——因为 this 绕过了代理对象（原理见 Spring Boot 章「AOP 的实现」）。

---

## 二、Validation 参数校验（JSR-380 / Hibernate Validator）🔥

**直接迁移 😌**：这就是 Java 版的 **class-validator**（NestJS 生态那个），连用法都几乎一样——在 DTO 字段上贴约束注解，入口处加一个 `@Valid` 触发。

依赖：`spring-boot-starter-validation`。触发方式：Controller 参数前加 `@Valid`（或类上加 `@Validated`，支持分组校验）：

```java
@PostMapping
public User create(@RequestBody @Valid CreateUserReq req) { ... }
//                            ^^^^^^ 没有它，下面贴的注解全是摆设 ⚠️
```

### 常用约束注解速查

| 注解 | 含义 | 适用类型 |
|------|------|----------|
| `@NotNull` | 不能为 null（空字符串/空集合**能过**） | 任意 |
| `@NotEmpty` | 不能为 null，且长度/大小 > 0 | String、集合、数组 |
| `@NotBlank` | 不能为 null，且去空格后非空 | 仅 String |
| `@Size(min=, max=)` | 长度/大小范围 | String、集合 |
| `@Min(n)` / `@Max(n)` | 数值上下限 | 数字 |
| `@Positive` / `@Negative` / `@PositiveOrZero` | 正负约束 | 数字 |
| `@Email` | 邮箱格式 | String |
| `@Pattern(regexp = "...")` | 自定义正则 | String |
| `@Length(min=, max=)` | 字符串长度（Hibernate 扩展） | String |
| `@Range(min=, max=)` | 数值范围（Hibernate 扩展） | 数字 |
| `@Past` / `@Future` | 过去/未来的时间 | 时间类型 |
| `@Valid`（贴在字段上） | 嵌套对象级联校验 🔥 | 对象字段 |

⚠️💼 **`@NotNull` / `@NotEmpty` / `@NotBlank` 三连区别是面试必考题**：`null` 用 NotNull 挡；`""` 要 NotEmpty；`"   "`（纯空格）只有 NotBlank 能挡。字符串字段**首选 `@NotBlank`**。

```java
public class CreateUserReq {

    @NotBlank(message = "用户名不能为空")          // message：校验失败的提示语
    @Size(min = 2, max = 20, message = "用户名长度 2-20 位")
    private String username;

    @NotBlank
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotNull
    @Min(value = 0, message = "年龄不能为负")
    @Max(150)
    private Integer age;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Valid                                       // 级联校验嵌套对象 🔥
    @NotNull
    private AddressReq address;
}
```

校验失败会抛 `MethodArgumentNotValidException`，交给全局异常处理（`@RestControllerAdvice`）统一转成错误响应——和 class-validator 的 `ValidationPipe` + exception filter 一个套路。

**分组校验** 💼：同一个 DTO，"新增"和"修改"校验规则不同（如修改时 id 必填）——用 `groups` 属性：

```java
public class UpdateUserReq {
    @NotNull(groups = Update.class)      // 只在 Update 组生效
    private Long id;

    public interface Update {}            // 分组标记：就是个空接口
}
// Controller 里指定组：@Validated(Update.class) @RequestBody UpdateUserReq req
```

**自定义校验注解**：需求特殊时（如"枚举值合法性"），可以仿照第 14 章自定义注解 + 实现 `ConstraintValidator` 接口造一个——这在实战项目里很常见。

---

## 三、Lombok：编译期代码生成

⚠️ **先重塑一个认知：Lombok 不是"注解驱动框架"，和 Spring 那套完全不是一个机制**。它的注解在**编译期**由编译器插件读取，直接把 getter/setter/构造器等代码**织进字节码**——运行期注解早已消失，反射都看不到。所以它更像一个"Babel 插件/语法糖"，而不是 NestJS 装饰器。

### 常用注解速查

| 注解 | 生成的内容 |
|------|-----------|
| `@Getter` / `@Setter` | 全字段（或单字段）的 get/set 方法 |
| `@ToString` | toString() |
| `@EqualsAndHashCode` | equals() + hashCode() |
| `@Data` | 上面四个 + `@RequiredArgsConstructor` 打包 🔥 |
| `@NoArgsConstructor` | 无参构造（MyBatis/JPA 反射建对象需要 ⚠️） |
| `@AllArgsConstructor` | 全参构造 |
| `@RequiredArgsConstructor` | 只为 `final` 字段生成构造（**配合构造器注入绝配** 🔥） |
| `@Builder` | 建造者模式链式调用 ≈ TS 的可选参数对象字面量 |
| `@Slf4j` | 注入日志对象 `log`（`log.info(...)` 直接用） 🔥 |
| `@Cleanup` | try-with-resources 的简写，自动关流 |

```java
@Data                        // getter/setter/toString/equals 全有了
@Builder                     // User.builder().name("x").age(18).build()
@NoArgsConstructor           // 框架反射用
@AllArgsConstructor          // @Builder 需要
public class User {
    private Long id;
    private String name;
    private Integer age;
}

@Slf4j
@Service
@RequiredArgsConstructor     // 只为 final 字段生成构造器 → 天然构造器注入 🔥
public class UserService {
    private final UserMapper userMapper;   // 不用写构造器，也不用 @Autowired
    public void demo() { log.info("hello"); }
}
```

💼 实战建议：实体类 `@Data` + `@Builder` 四件套；Service 类 `@RequiredArgsConstructor` + `@Slf4j`。RuoYi 等国内项目大量使用，不认识的注解先猜"Lombok 的吧"。

---

## 四、JPA / Spring Data JPA（ORM 注解）

**直接迁移 😌**：如果你用过 **TypeORM**，这组注解就是逐字翻译——`@Entity`、`@Column`、`@ManyToOne` 连名字都一样（TypeORM 本来就是抄 JPA 的）。

JPA 注解解决一件事：**Java 对象 ⇄ 数据库表 的映射规则**。

| 注解 | 贴在哪 | 作用 |
|------|--------|------|
| `@Entity` | 类 | 声明"我是实体，请管理我"（必贴） |
| `@Table(name = "t_user")` | 类 | 指定表名（默认类名） |
| `@Id` | 字段 | 主键（必贴） |
| `@GeneratedValue(strategy = IDENTITY)` | 主键字段 | 主键生成策略：IDENTITY 自增 / AUTO / SEQUENCE |
| `@Column(name=, nullable=, length=)` | 字段 | 列映射细节（默认驼峰转下划线） |
| `@Transient` | 字段 | 该字段**不映射**数据库列 |
| `@Enumerated(EnumType.STRING)` | 枚举字段 | 枚举按名称存库（默认存序号，危险 ⚠️） |

```java
@Entity
@Table(name = "t_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // 自增主键
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)    // ⚠️ 默认 ORDINAL 存 0/1/2，加枚举就错位，务必 STRING
    private Status status;

    @Transient                      // 不建列，纯 Java 侧使用
    private String displayName;
}
```

### 关系映射（JPA 独有，MyBatis 没有的概念）🆕

```java
@Entity
public class Order {

    @ManyToOne(fetch = FetchType.LAZY)       // 多单对一用户；LAZY 懒加载（避免 N+1 💼）
    @JoinColumn(name = "user_id")            // 本表的外键列
    private User user;
}

@Entity
public class User {

    @OneToMany(mappedBy = "user")            // 一对多，关系由对方 Order.user 维护
    private List<Order> orders;

    @ManyToMany
    @JoinTable(name = "user_role",           // 多对多：中间表
               joinColumns = @JoinColumn(name = "user_id"),
               inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> roles;
}
```

⚠️ 关系映射是 JPA 的重灾区：N+1 查询、`mappedBy` 方向搞反、级联删除误伤。国内团队更常用 MyBatis（SQL 自己写，无此心智负担），所以这一组**看懂即可，选型看团队**。

---

## 五、MyBatis / MyBatis-Plus 注解

### 5.1 MyBatis 核心注解

| 注解 | 贴在哪 | 作用 |
|------|--------|------|
| `@Mapper` | 接口 | 声明 Mapper，生成代理实现（≈ NestJS 里"只写接口，框架实现"） |
| `@MapperScan("com.x.mapper")` | 启动/配置类 | 批量扫描，省去每个接口贴 `@Mapper` 🔥 |
| `@Select` / `@Insert` / `@Update` / `@Delete` | 方法 | 注解直写 SQL（替代 XML，适合简单语句） |
| `@Param("name")` | 参数 | 给参数命名，SQL 里 `#{name}` 引用；**多参数必贴** ⚠️ |
| `@Results` / `@Result` | 方法 | 字段与列的映射关系（驼峰映射不一致时用） |
| `@Options(useGeneratedKeys = true, keyProperty = "id")` | 插入方法 | 回填自增主键 |

```java
@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user WHERE id = #{id}")
    User selectById(Long id);                       // 单参数可不贴 @Param

    @Select("SELECT * FROM user WHERE name = #{name} AND status = #{status}")
    List<User> findBy(@Param("name") String name,   // ⚠️ 多参数必贴，否则 #{name} 找不到
                      @Param("status") Integer status);

    @Insert("INSERT INTO user(name, age) VALUES(#{name}, #{age})")
    @Options(useGeneratedKeys = true, keyProperty = "id")   // 插入后 user.id 被回填
    int insert(User user);
}
```

⚠️💼 再次强调 MyBatis 章的铁律：`#{}` 是预编译占位符（防注入），`${}` 是字符串裸拼接（仅用于表名/排序字段等场景）。注解 SQL 和 XML SQL 同样适用。

💼 实战选择：简单单表 SQL 用注解；**复杂 SQL（多表、动态条件）一律回 XML**——注解里拼 `<if>` 是灾难。

### 5.2 MyBatis-Plus 增强注解

MyBatis-Plus 白送单表 CRUD，注解负责"实体 ⇄ 表"的映射校准：

| 注解 | 作用 |
|------|------|
| `@TableName("t_user")` | 类名与表名不一致时指定 |
| `@TableId(type = IdType.AUTO)` | 标记主键及生成策略（AUTO 自增 / ASSIGN_ID 雪花算法） |
| `@TableField("user_name")` | 字段与列名不一致时指定 |
| `@TableField(exist = false)` | 该字段在表中**不存在**（≈ JPA 的 `@Transient`） |
| `@TableLogic` | 逻辑删除：delete 变 update `deleted=1` 🔥 |
| `@Version` | 乐观锁：更新时带版本号比对 |

```java
@TableName("t_user")
public class User {

    @TableId(type = IdType.ASSIGN_ID)      // 雪花 ID
    private Long id;

    @TableField("user_name")               // 字段名 ≠ 列名
    private String username;

    @TableLogic                            // 调 removeById 实际是 UPDATE deleted=1
    private Integer deleted;

    @TableField(exist = false)             // 表中无此列
    private String token;
}
```

---

## 六、测试注解

| 注解 | 作用 |
|------|------|
| `@SpringBootTest` | 启动完整 Spring 容器做集成测试 🔥 |
| `@Test` | 标记测试方法（JUnit 5） |
| `@BeforeEach` / `@AfterEach` | 每个测试方法前/后执行 ≈ Jest 的 `beforeEach` |
| `@MockBean`（新版 `@MockitoBean`） | 把容器里某 Bean 换成 Mock 替身 ≈ `jest.mock()` |
| `@WebMvcTest(XxxController.class)` | 只加载 Web 层切片，轻量测 Controller |

```java
@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean                       // 不连真库：把 Mapper 换成 Mock
    private UserMapper userMapper;

    @Test
    void shouldReturnUser() {
        Mockito.when(userMapper.selectById(1L))
               .thenReturn(new User(1L, "张三"));   // 打桩 ≈ jest.fn().mockReturnValue
        assertEquals("张三", userService.getById(1L).getName());
    }
}
```

---

## 七、总对照表：Java 注解 ↔ 你熟悉的 TS 装饰器 😌

| Java 生态 | NestJS / TS 生态 | 用途 |
|-----------|------------------|------|
| `@Service` / `@Component` | `@Injectable()` | 注册进容器 |
| `@RestController` + `@GetMapping` | `@Controller()` + `@Get()` | 路由 |
| `@RequestBody` / `@PathVariable` / `@RequestParam` | `@Body()` / `@Param()` / `@Query()` | 参数绑定 |
| `@Transactional` | TypeORM `@Transaction()` / queryRunner | 事务 |
| `@NotBlank` / `@Email`（+ `@Valid`） | class-validator `@IsNotEmpty()` / `@IsEmail()`（+ ValidationPipe） | 参数校验 |
| `@Entity` / `@Column` / `@ManyToOne` | TypeORM `@Entity()` / `@Column()` / `@ManyToOne()` | ORM 映射 |
| `@ConfigurationProperties` | `@InjectConfig()` / ConfigModule | 配置绑定 |
| `@Scheduled` | `@Cron()`（@nestjs/schedule） | 定时任务 |
| `@MockBean` | `jest.mock()` / TestingModule.overrideProvider | 测试替身 |

心智同构，剩下的只是记名字 😌。

---

## 练习

1. 在 Spring Boot 实战项目中写一个 `CreateBookReq`（图书馆场景）：要求书名 `@NotBlank` 且长度 2-50、作者 `@NotBlank`、ISBN 用 `@Pattern` 校验 13 位数字、价格 `@Positive`；在 Controller 用 `@Valid` 触发，并用 `@RestControllerAdvice` 捕获校验异常返回统一格式 `{code, message}`。
2. 思考题：你在 `UserService.methodA()` 上贴了 `@Transactional`，同类中 `methodB()` 里用 `this.methodA()` 调用它，然后 methodA 抛了异常——事务会回滚吗？为什么？如果用的是 Lombok 的 `@RequiredArgsConstructor`，有没有办法优雅地解决？（提示：想想代理 + 自己注入自己）

## 本章总结

- 注解三问定乾坤：**哪个库的？谁读它？读它做什么？**——运行时框架（Spring/AOP）、编译期插件（Lombok）、校验器（Validation）三种机制泾渭分明 ⚠️
- **Spring 系**四大堆：启动配置（`@SpringBootApplication`/`@Bean`/`@ConfigurationProperties`）、Bean 注入（`@Autowired`/`@Qualifier`）、Web 层（`@RestController` 家族）、AOP 魔法（`@Transactional`/`@Async`/`@Scheduled`，自调用失效是天坑 💼）
- **Validation** ≈ class-validator：`@Valid` 触发 + 字段贴约束；`@NotNull`/`@NotEmpty`/`@NotBlank` 三连区别必考 💼
- **Lombok** 是编译期织入不是框架魔法：`@Data`、`@RequiredArgsConstructor` + `@Slf4j` 是实战标配
- **JPA** ≈ TypeORM（关系映射是重灾区，看懂即可）；**MyBatis** 记住 `@Mapper`/`@Param`/注解 SQL 只适合简单语句，Plus 的 `@TableLogic`/`@Version` 解决逻辑删除与乐观锁
- 整张地图与 TS 装饰器逐行同构——你在 NestJS 的经验再次兑现 😌

框架篇真正收官 🎉 下一站：[projects/library-system 图书馆管理系统](../../projects/library-system)——到实战里去见这些注解的"活物"。

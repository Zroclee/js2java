# MyBatis 与 Java 数据访问全景

> 状态：✅ 已完成

## 简介
本章学习内容：DAO 模式、JDBC 与 JdbcTemplate、MyBatis 完整实战（Mapper/XML/动态 SQL）、JPA 与 MyBatis-Plus、四种方案对比选型
前置知识：完成「02-spring-boot」与「03-database/02-mysql」（mysql8 容器还在跑）
阅读时长：约 55 分钟
难度：🌟🌟🌟
重要程度：🌟🌟🌟🌟🌟
当前进度：100%

---

## 全景：Java 访问数据库的四条路

```
原始 JDBC        →  JdbcTemplate     →  MyBatis          →  JPA (Hibernate)
手工作坊           Spring 简化封装      半自动 ORM           全自动 ORM
样板代码爆炸       轻量直接            SQL 自己写           对象即表
（了解原理即可）    （小项目可用）      （国内主流 🔥）      （国外/快速开发主流）
                                        └── MyBatis-Plus：MyBatis 的国产增强 🔥
```

本章任务：先搞懂分层思想（DAO 模式），然后四条路各看一眼，**重点吃透国内事实标准 MyBatis**。

## DAO 模式：数据访问层的封装思想 🆕

**是什么**：DAO（Data Access Object）——把"怎么查数据库"封装成独立一层，业务层只调方法、不碰 SQL：

```
Controller（接收请求）→ Service（业务逻辑）→ DAO/Mapper（SQL 操作）→ 数据库
```

前端类比 😌：就是你熟悉的 **API 层封装**——组件不直接 `fetch`，而是调 `api/user.ts` 里的 `getUser(id)`；后端换成 Service 不直接写 SQL，而是调 `UserMapper.selectById(id)`。好处同款：**数据库细节集中一处，换实现/加缓存/写测试都好下手**。

💡 名词说明：MyBatis 里 DAO 叫 **Mapper**，同一个东西两种叫法，看老代码别懵。

## 第一条路：JDBC 与 JdbcTemplate（Spring 自带）

### 原始 JDBC：明白为什么要封装（了解即可）

```java
// 感受一下没有框架的日子：查一个用户要写多少样板
Connection conn = DriverManager.getConnection(url, user, pwd);
PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE id = ?");
ps.setLong(1, 1L);
ResultSet rs = ps.executeQuery();
while (rs.next()) { /* 逐列取出塞进对象 */ }
rs.close(); ps.close(); conn.close();   // 漏关一个就泄漏 ⚠️
```

### JdbcTemplate：Spring 的简化封装

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
```

```yaml
# application.yml：数据源配置（四种方案共用这一段 🔥）
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/demo?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
```

```java
@Repository
public class UserDao {
    private final JdbcTemplate jdbcTemplate;
    public UserDao(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    public String findNameById(long id) {
        return jdbcTemplate.queryForObject(
            "SELECT name FROM users WHERE id = ?", String.class, id);   // 一行搞定
    }

    public List<User> findAll() {
        return jdbcTemplate.query("SELECT * FROM users",
            (rs, rowNum) -> new User(rs.getLong("id"), rs.getString("name")));
    }
}
```

**定位**：比 JDBC 清爽十倍，但 SQL 和对象映射仍手写。**小工具、脚本、极简单表场景可用**；正式业务系统上 MyBatis/JPA。

## 第二条路：MyBatis（主菜 🔥）

### 是什么：SQL 自己写、映射它来管的"半自动 ORM"

前端对照：Prisma/TypeORM 帮你生成 SQL；**MyBatis 反过来——SQL 全部自己写（掌控力 💯），它负责把结果集自动映射成 Java 对象**。国内互联网公司的绝对主流。

### 接入

```xml
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>3.0.3</version>
</dependency>
```

```yaml
# application.yml 追加
mybatis:
  mapper-locations: classpath:mapper/*.xml     # XML 放哪
  configuration:
    map-underscore-to-camel-case: true         # 🔥 下划线列名 → 驼峰字段自动映射（user_name → userName）
```

### 实战三件套：实体 + Mapper 接口 + XML

```java
// 1️⃣ 实体类（≈ TS 的 interface/type，对应 users 表）
public class User {
    private Long id;
    private String name;
    private Integer age;
    private BigDecimal balance;
    // getter/setter 省略（IDEA Cmd+N 生成）
}
```

```java
// 2️⃣ Mapper 接口（只写方法签名，不用写实现！🆕）
@Mapper
public interface UserMapper {
    User selectById(Long id);
    List<User> selectAll();
    List<User> selectByCondition(@Param("name") String name, @Param("minAge") Integer minAge);
    int insert(User user);
    int update(User user);
    int deleteById(Long id);
}
```

```xml
<!-- 3️⃣ resources/mapper/UserMapper.xml：SQL 的家 -->
<mapper namespace="com.example.demo.mapper.UserMapper">   <!-- 绑定接口全限定名 -->

    <select id="selectById" resultType="com.example.demo.entity.User">
        SELECT * FROM users WHERE id = #{id}
    </select>

    <select id="selectByCondition" resultType="com.example.demo.entity.User">
        SELECT * FROM users
        <where>                                     <!-- 动态 SQL 🔥：自动处理 AND 拼接 -->
            <if test="name != null">AND name LIKE CONCAT('%', #{name}, '%')</if>
            <if test="minAge != null">AND age &gt;= #{minAge}</if>
        </where>
    </select>

    <insert id="insert" useGeneratedKeys="true" keyProperty="id">  <!-- 自增 id 回填到对象 🔥 -->
        INSERT INTO users (name, age, balance) VALUES (#{name}, #{age}, #{balance})
    </insert>

    <update id="update">
        UPDATE users SET name = #{name}, age = #{age} WHERE id = #{id}
    </update>

    <delete id="deleteById">
        DELETE FROM users WHERE id = #{id}
    </delete>
</mapper>
```

```java
// Service 中注入使用——接口没有实现类，MyBatis 用动态代理自动生成（代理模式又赢了 💡）
@Service
public class UserService {
    private final UserMapper userMapper;
    public UserService(UserMapper userMapper) { this.userMapper = userMapper; }

    public User getUser(Long id) { return userMapper.selectById(id); }
}
```

### ⚠️ `#{ }` vs `${ }`：SQL 注入的生死线 💼

```xml
WHERE name = #{name}     <!-- ✅ 预编译占位符（?），参数被安全转义——永远用它 -->
WHERE name = '${name}'   <!-- ⚠️ 字符串拼接！传入 ' OR '1'='1 就是 SQL 注入 -->
ORDER BY ${column}       <!-- 唯一合法用途：列名/排序字段等无法预编译的场景（且必须白名单校验） -->
```

类比前端：模板字符串拼接 SQL vs 参数化查询——**`#{}` 是预编译防注入，`${}` 是裸拼接**，面试必考。

### 动态 SQL 常用标签（一提即可）

`<if>` 条件、`<where>` 智能 WHERE、`<foreach>` 遍历（IN 查询）、`<set>` 智能 SET（更新时去逗号）、`<choose>` 多分支——查文档即会 🔥。

## 第三条路：JPA / Hibernate（全自动 ORM）

**哲学反转**：MyBatis 是"SQL 为中心"，JPA 是"**对象为中心**"——你几乎不写 SQL，表结构由实体类生成，查询靠**方法名推导**：

```java
// 1️⃣ 实体即表
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Integer age;
    // ...
}

// 2️⃣ Repository 接口：方法名即查询 🆕（Spring Data JPA 自动实现）
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByNameContaining(String name);        // 自动翻译成 LIKE %name%
    List<User> findByAgeGreaterThanEqual(Integer age);   // 自动翻译成 age >= ?
    // save/findById/findAll/deleteById 全由父接口自带，一行不用写
}

// 3️⃣ 使用
userRepository.findByNameContaining("张");   // 完事，没有 SQL、没有 XML
```

爽点：CRUD 零 SQL、改字段自动同步表。**痛点**：复杂查询（多表 JOIN、报表统计）时自动生成的 SQL 又臭又长且难调优，得退回手写 `@Query`——此时 MyBatis 的"SQL 全掌控"就香了 ⚠️。

## 第四条路：MyBatis-Plus（国产增强 🔥）

MyBatis 的"插件加强版"：**保留 MyBatis 一切能力，同时把单表 CRUD 做成自带**：

```java
// Mapper 继承 BaseMapper——单表增删改查全白送，XML 都不用写
@Mapper
public interface UserMapper extends BaseMapper<User> { }

// 直接用
userMapper.selectById(1L);
userMapper.selectList(new QueryWrapper<User>().like("name", "张").ge("age", 18));  // 条件构造器
userMapper.insert(user);
// 分页插件、逻辑删除、自动填充……开箱即用
```

国内实战地位：**新项目用 MyBatis 时大概率直接用 MyBatis-Plus**（RuoYi 各分支都在用）💼——复杂 SQL 照样写 XML，单表 CRUD 白嫖，两不耽误。

## 四方对比与选型 💼

| 维度 | JdbcTemplate | MyBatis | JPA | MyBatis-Plus |
|------|-------------|---------|-----|--------------|
| SQL 控制 | 全手写 | **全手写（XML 集中管理）** | 基本自动生成 | 单表自动 + 复杂手写 |
| 样板代码 | 中 | 少 | **最少** | 最少 |
| 复杂查询/调优 | 直接但繁琐 | **最强** 🔥 | 弱（得退回 @Query） | 同 MyBatis |
| 学习成本 | 低 | 中 | 中高（黑盒多） | 中 |
| 国内岗位生态 | 辅助 | **主流** 🔥 | 少数派 | **主流** 🔥 |
| 适合 | 小工具/脚本 | 复杂业务系统（国内标配） | 快速原型/国外团队 | 国内新项目首选 |

**给你的选型路径**：学习顺序 JdbcTemplate（懂原理）→ MyBatis（主修）→ MyBatis-Plus（实战就用它）→ JPA（看国外项目源码时不懵）。

## 练习

1. 用上一章的 Spring Boot 项目接入 MyBatis：配好数据源（连 docker 的 mysql8/demo 库），按三件套实现 users 表的完整 CRUD，并用 `selectByCondition` 体验动态 SQL 的 `<if>` 组合。
2. 故意把 XML 里一个 `#{name}` 改成 `${name}`，用 `'; DROP TABLE users; --` 之类的输入测试（在学习库上！），观察两种占位符的本质差异，然后改回来。
3. 在同一个项目里加 spring-boot-starter-data-jpa，用 `findByAgeGreaterThanEqual` 实现同样的查询——对比"写 XML"和"写方法名"两种心智，说说你目前的偏好。

## 本章总结

- **DAO 模式** ≈ 前端 API 层封装：数据库细节集中一层；MyBatis 里叫 Mapper
- 四条路：**JdbcTemplate**（Spring 自带，轻量）→ **MyBatis**（SQL 全掌控，国内主流 🔥）→ **JPA**（对象中心，方法名即查询）→ **MyBatis-Plus**（单表 CRUD 白送，实战首选）
- MyBatis 三件套：实体 + Mapper 接口（无实现，动态代理生成 💡）+ XML；**`#{}` 预编译防注入，`${}` 裸拼接** ⚠️💼；动态 SQL 标签处理条件拼接
- 选型：国内复杂业务 → MyBatis/MyBatis-Plus；快速原型/国外 → JPA

框架篇收官 🎉 所有理论装备已集齐——下一站进入实战：[projects/library-system 图书馆管理系统](../../projects/library-system)

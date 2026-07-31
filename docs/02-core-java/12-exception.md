# 异常处理：受检异常这个 Java 独有的"紧箍咒"

> 状态：✅ 已完成

## 简介
本章学习内容：异常类体系与继承关系、受检 vs 非受检异常、try-catch-finally、throw 与 throws、自定义异常、用异常写健壮代码
前置知识：完成「11-io」（见过 IOException）；JS try-catch 经验
阅读时长：约 30 分钟
难度：🌟🌟
重要程度：🌟🌟🌟🌟
当前进度：48%

---

## 先对齐：try-catch 语法你会，JS 没有的是"类型体系"

```java
try {
    int r = 10 / 0;
} catch (ArithmeticException e) {
    System.out.println("算错了: " + e.getMessage());
} finally {
    System.out.println("一定执行");      // 和 JS finally 一样 😌
}
```

语法层面和 JS 几乎一样。但 JS 的异常世界是一片草原——`throw` 可以抛任何东西（字符串、对象都行），所有错误都是运行时才冒出来的；Java 的异常世界是一棵**严格的类继承树**，并且把异常分成两类，其中一类**编译器会强制你处理** 🆕。这棵树的形状，决定了你怎么写 Java 代码。

## 异常家族：一棵继承树 💼

```
Throwable（一切可抛出物的祖先）
├── Error                  系统级灾难：JVM 自己都出问题了
│    ├── OutOfMemoryError      内存耗尽
│    └── StackOverflowError    栈溢出（无限递归）
│
└── Exception              程序级问题，你要面对的就是这一支
     ├── RuntimeException  【非受检异常】运行时常见错误
     │    ├── NullPointerException         空指针（老熟人）
     │    ├── IllegalArgumentException     参数非法
     │    ├── IndexOutOfBoundsException    下标越界（10 章踩过）
     │    ├── ClassCastException           强转失败（继承章踩过）
     │    ├── NumberFormatException        数字解析失败（08 章踩过）
     │    ├── ArithmeticException          算术错误（除零）
     │    └── ConcurrentModificationException  遍历改集合（10 章踩过）
     │
     └── 其他 Exception   【受检异常】外部不可控因素
          ├── IOException     IO 失败（11 章见过）
          ├── SQLException    数据库操作失败
          └── ParseException  解析失败
```

三句话读懂这棵树：

1. **Error 别碰**：JVM 级别的灾难，程序救不回来，不捕获也不处理
2. **非受检异常（RuntimeException 一族）**：基本是**程序 bug**——空指针、越界、类型转错。编译器不强制你 catch，因为正确做法是**把代码写对**，而不是到处包 try-catch
3. **受检异常（checked）** 🆕：是**外部世界的不可控因素**——文件不存在、网络断了、数据库挂了。这些代码写得再好也可能发生，所以 Java 的设计哲学是：**编译器逼你提前想好对策** ⚠️💼

## 受检异常：JS 完全没有的机制 🆕⚠️

**规则**：方法里可能抛出受检异常时，只有两条路，编译器盯着你二选一：

```java
// 路线 1：自己 catch 处理掉
public String read() {
    try {
        return Files.readString(Path.of("a.txt"));
    } catch (IOException e) {          // 就地解决
        return "默认内容";
    }
}

// 路线 2：throws 声明"我不处理，调用我的人处理"
public String read() throws IOException {   // 把责任抛给调用方
    return Files.readString(Path.of("a.txt"));
}
```

不写 catch 也不写 throws？**编译直接失败**——这就是"受检"的含义：编译器检查过才放行。JS 里函数会抛什么错全靠文档和运气；Java 把"我可能失败"写进了**方法签名**，成为类型系统的一部分：

```java
String read() throws IOException
//                ↑ 这个方法的"能力清单"里明确包含"可能读失败"
//                  调用方编译时就被提醒：你得有所准备
```

这是争议了三十年的设计（很多新语言放弃了它），但它是 Java 代码"强迫健壮"气质的来源 💼。

## 抛出异常：throw 与 throws

```java
// throw：主动抛出（≈ JS throw，但只能抛 Throwable 的子类 ⚠️）
if (age < 0) {
    throw new IllegalArgumentException("年龄不能为负: " + age);
}

// throws：方法签名上声明受检异常（上面刚讲）
public void save(String path) throws IOException { ... }
```

⚠️ 区分俩兄弟：**throw 是动作（在方法体内抛），throws 是声明（在方法签名上标记）**。JS 只有 throw；JS 用户常问"为什么 throws 拼写多个 s"，因为那是两个完全不同的东西。

多 catch 的规则 ⚠️：

```java
try {
    // ...
} catch (NullPointerException e) {     // 子类在前
    // ...
} catch (RuntimeException e) {         // 父类在后
    // ...
}
// 顺序颠倒会编译错误：父类先把所有异常接住了，子类永远摸不到

catch (IOException | ParseException e) { ... }   // 多个异常并列处理用 |
```

## 自定义异常：给业务错误一个身份 🔥

系统异常描述的是"技术故障"（空指针、IO 失败）；业务系统还需要描述"业务故障"——余额不足、库存不够、无权限。自定义异常两行起步：

```java
// 推荐继承 RuntimeException（非受检，不污染方法签名）🔥
public class BizException extends RuntimeException {

    private final int code;            // 业务错误码（给前端/调用方看的）

    public BizException(int code, String message) {
        super(message);                // message 交给父类存着
        this.code = code;
    }

    public int getCode() { return code; }
}
```

```java
// 使用
if (balance < amount) {
    throw new BizException(1001, "余额不足");
}
```

💡 为什么推荐继承 `RuntimeException` 而不是 `Exception`：继承后者会让它变成受检异常——`throws BizException` 像病毒一样传染整条调用链的方法签名，代码很快满目疮痍。业界共识：**业务异常走非受检** 🔥。

## 用异常写健壮代码：五条军规 🔥

**1. 快速失败（fail-fast）：在方法入口就把非法参数扔出去**

```java
public void setAge(int age) {
    if (age < 0 || age > 150) {
        throw new IllegalArgumentException("年龄非法: " + age);  // 问题在源头爆炸
    }
    this.age = age;
}
```

JS 前端常静默失败（返回 undefined 糊弄过去），错误飘到很远的地方才发作，排查如破案；Java 文化是让错误**在离源头最近的地方响**——这正是"健壮"的含义。

**2. 绝不吞异常** ⚠️

```java
try {
    risky();
} catch (Exception e) {
    // 什么都不写 —— 代码界的谋杀：错误被悄悄埋了 💀
}

// 底线：至少打印/记日志
} catch (Exception e) {
    e.printStackTrace();   // 学习阶段可以；生产用日志框架
}
```

**3. 抛具体异常，不抛笼统的 `Exception`**——调用方才能精准 catch。

**4. 底层异常翻译成业务异常（保留 cause 链）** 🔥

```java
try {
    userDao.insert(user);
} catch (SQLException e) {
    throw new BizException(2001, "用户创建失败", e);  // e 作为 cause 传进去
    // 上层只看到干净的业务错误，但排查时完整的技术堆栈还在 ✅
}
```

**5. 别拿异常当流程控制** ⚠️

```java
// ❌ 反例：用异常做正常逻辑判断
try {
    return Integer.parseInt(input);
} catch (NumberFormatException e) {
    return 0;    // 每次非法输入都靠抛异常走流程——异常是"例外"，很贵
}
// ✅ 先校验，再解析
```

💡 预告：真实项目里不会到处 try-catch——Spring 有**全局异常处理**（`@ControllerAdvice`），业务代码放心 `throw`，统一在一个地方转成给前端的响应。框架章见。

## 对比理解

| 维度 | JS | Java |
|------|-----|------|
| 可抛出的东西 | 任何值（字符串都行） | 只能是 `Throwable` 子类 ⚠️ |
| 异常分类 | 无（全是运行时） | **受检 / 非受检 / Error** 三分 🆕 |
| 编译器态度 | 不管 | 受检异常必须 catch 或 throws 🆕💼 |
| 方法声明失败 | 靠文档/注释 | `throws` 写进签名 |
| 自定义错误 | `class MyError extends Error` | 继承 `RuntimeException`（推荐） |
| try-catch-finally | 相同 😌 | 相同 + 多 catch、catch 合并 |

## 练习

1. 写一个 `divide(int a, int b)` 方法：`b` 为 0 时抛出 `IllegalArgumentException`（带提示信息）。再写两个调用方：一个用 try-catch 打印友好提示，一个用 throws 把异常继续上交，体会两条路线的差异。
2. 自定义一个 `InsufficientBalanceException`（继承 RuntimeException，带错误码 1001），写 `withdraw(double balance, double amount)` 方法在余额不足时抛出它；然后在调用处 catch 并打印错误码和消息。
3. 观察与思考：给 `Files.readString(Path.of("不存在的文件"))` 分别加上 catch 打印 `e.getClass().getName()`——运行后说出它属于受检还是非受检，并解释为什么 `Integer.parseInt("abc")` 却不要求你强制 catch。

## 本章总结

- 异常树：`Throwable → Error（别碰）/ Exception → 受检（外部不可控，编译器强制处理）+ RuntimeException（程序 bug，不强制）` 💼
- **受检异常**是 Java 独有设计：catch 或 throws 二选一，"可能失败"写进方法签名 🆕
- `throw` 是抛的动作，`throws` 是签名声明；多 catch 子类在前父类在后
- 自定义异常继承 `RuntimeException`，带错误码，描述业务故障
- 健壮五军规：fail-fast、不吞异常、抛具体类型、保留 cause 链、异常不当流程控制；全局处理留给 Spring 🔥

下一章：[线程与并发](./13-concurrency.md)——从前端的单线程世界，进入多线程共享内存的深水区

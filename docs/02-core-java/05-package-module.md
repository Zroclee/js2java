# 包与模块：Java 的代码组织术

> 状态：✅ 已完成

## 简介
本章学习内容：package 声明与目录绑定、包级作用域、import 导入；模块化的动机与 module-info 实现
前置知识：完成「01-class-field-method」（访问修饰符）；ES Module 使用经验
阅读时长：约 15 分钟
难度：🌟
重要程度：🌟🌟🌟
当前进度：23%

---

## 包（package）：类的命名空间

### 是什么 & 为什么需要

类一多就会出两个问题：**撞名**（你和同事都写了 `User` 类）和**混乱**（几百个类平铺一坨）。JS 用"文件即模块"天然解决了组织问题；Java 的答案是包——**一个包就是一组相关类的命名空间，同时对应一个物理目录**。它有双重身份：

```
逻辑上：com.example.order 是一个命名空间
物理上：对应目录  src/main/java/com/example/order/
```

### 声明与目录绑定 ⚠️

每个 `.java` 文件**第一行**必须声明自己属于哪个包：

```java
package com.example.order;        // 第一行，分号结尾

public class OrderService { ... }
```

铁律：**包名必须和文件所在的目录路径完全一致**——声明了 `com.example.order`，文件就必须放在 `com/example/order/` 目录下，不一致直接编译错误。这点和 JS 的 import 路径异曲同工，但 JS 是"引用时对路径"，Java 是"文件声明+物理位置"双重一致。

### 命名规范：反写域名 🆕

Java 社区用**反写域名**保证全球唯一：`com.company.project.module`。

为什么 🆕：JS 包冲突由 npm registry 集中管理（重名就注册不上）；Java 没有中心注册局，类库来自五湖四海，于是约定"用你的域名反写"——Apache 的库叫 `org.apache.commons`，你公司域名的项目就叫 `com.yourcompany.xxx`。学习项目用 `com.example` 或自己的名字即可。

### 包级作用域：访问修饰符的第四档

第一章留了伏笔——**不写任何修饰符**（称为 default / package-private）的含义现在揭晓：

```java
package com.example.order;

class OrderHelper {          // 不写修饰符 → 只有同包的类能用 🔥
    void calculate() { ... } // 方法同理
}
```

| 修饰符 | 本类 | 同包 | 子类 | 所有人 |
|--------|------|------|------|--------|
| `private` | ✅ | ❌ | ❌ | ❌ |
| **不写（默认）** | ✅ | ✅ | ❌ | ❌ |
| `protected` | ✅ | ✅ | ✅ | ❌ |
| `public` | ✅ | ✅ | ✅ | ✅ |

🔥 实务用途：**包内协作的实现细节类，用默认访问藏起来**——这是 Java 的"模块内部私有"，比 ES Module 里"不 export 就外部不可见"多了一层（Java 是类粒度，JS 是文件粒度）。

### import：导入其他包的类

```java
package com.example.order;

import com.example.user.User;        // 导入单个类 ≈ import { User } from '...'
import com.example.util.*;           // 导入整个包（⚠️ 不推荐，污染命名空间）

public class OrderService {
    private User user;               // 现在可以直接写短名了
}
```

对比 ES Module：

| 维度 | ES Module | Java 包 |
|------|-----------|---------|
| 模块单位 | 文件 | 包（目录） |
| 引入什么 | 按文件路径：`from './user.js'` | 按全限定类名：`import com.example.user.User` ⚠️ |
| 导出控制 | 不 export 即私有 | 访问修饰符（private/默认/protected/public） |
| 同目录引用 | 也要写相对路径 | **同包免 import** 🆕 |
| 内置对象 | 全局直接可用 | `java.lang` 包自动导入（String、Math 等）🆕 |

两个实用细节：

```java
import static java.lang.Math.max;    // 静态导入：之后直接写 max(a, b)（≈ JS 解构导入）

// 两个包都有 User 类撞名时：用全限定名救场
com.example.order.User u1 = new com.example.order.User();
com.example.user.User u2 = new com.example.user.User();
```

## 模块（module）：给包加上硬边界

### 为什么要模块化

包解决了命名和组织，但留下两个窟窿 ⚠️：

1. **边界是软的**：任何 `public` 类对整个 classpath 可见——你无法表达"这个 public 类只给我的库内部用，不对外"
2. **依赖是隐式的**：代码用了哪个库的类，编译配置里没有显式声明，缺了到运行时才炸

JDK 自己就是最大受害者：`rt.jar`（运行时核心库）是个几千类的巨型单体，想裁掉用不到的部分都做不到。

前端对比一下就很清楚：npm 包有 `package.json` **显式声明依赖**（dependencies）和**显式控制导出**（exports 字段）——Java 9（2017）的模块系统（JPMS）就是给 Java 补上这层 🆕：

| 能力 | npm/package.json | Java 模块 |
|------|------------------|-----------|
| 声明依赖 | `dependencies` | `requires` |
| 控制导出 | `exports` 字段 | `exports` 指令 |
| 运行环境 | Node + node_modules | 模块路径（module path） |

### 怎么实现：module-info.java

在源码根目录放一个**模块描述文件** `module-info.java`：

```
src/
├── module-info.java          # 模块的"package.json"
└── com/example/order/
    ├── OrderService.java     # 想对外暴露
    └── internal/
        └── OrderHelper.java  # 想藏起来
```

```java
module com.example.order {
    requires com.example.user;      // 声明：我依赖 user 模块（≈ dependencies）
    requires java.sql;              // 依赖 JDK 自带模块也要声明

    exports com.example.order;      // 声明：只有这个包对外可见（≈ exports 字段）
    // com.example.internal 没 exports → 里面的 public 类外部也访问不到 ⚠️ 强封装！
}
```

关键转变：**public 不再等于"所有人可用"**——public 只是"本模块内可用"，对外还要包被 `exports`。类上没 public、包没 exports，双重闸门。

### 现实地位：了解即可

诚实地告诉你 🔥：**业务开发几乎不写 module-info.java**。现实中依赖管理是 Maven 的天下（靠 pom.xml 而不是 requires），Spring Boot 应用也不强制模块化。模块系统最大的成果是 **JDK 自己被拆成了几十个模块**（`java.base`、`java.sql` 等），让运行时可以裁剪（第一章提的 `jlink` 就靠它）。

所以对模块的要求是：**知道它为什么存在、`requires`/`exports` 什么意思、看到 module-info.java 不发懵**——就够了。

## 练习

1. 创建两个包 `com.example.order` 与 `com.example.user`，在 user 包写 `User` 类（public）和 `UserValidator` 类（不写修饰符），在 order 包的 `OrderService` 里 import 并使用 `User`；然后试试在 order 包里 new 一个 `UserValidator`——观察编译错误并解释原因。
2. 给上面的练习项目加上 `module-info.java`：user 模块 exports `com.example.user` 包，order 模块 requires user 模块。然后把 user 模块里另一个包 `com.example.user.internal` 留着不 exports，在 order 模块里访问它，验证"public 也访问不到"。

## 本章总结

- 包 = 命名空间 + 物理目录双重身份；**声明与目录必须一致**；反写域名保证全球唯一 🆕
- 不写修饰符 = 同包可见，这是藏实现细节的常规手段；`java.lang` 自动导入、同包免 import
- import 按全限定类名而非文件路径；撞名用全限定名救场
- 模块化的动机：包的边界太软（public 全可见、依赖隐式）——类比 npm 的 dependencies/exports
- `module-info.java` 用 `requires` 声明依赖、`exports` 控制导出；**public + 未 exports = 外部不可见** ⚠️
- 现实：业务开发以 Maven 为准，模块了解即可；JDK 自身模块化是其最大成果

OOP 板块收官 🎉 下一章进入数据处理：[数据类型与字符串](./06-data-types.md)

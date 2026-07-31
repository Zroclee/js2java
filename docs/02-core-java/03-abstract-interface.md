# 抽象类与接口：Java 的契约设计

> 状态：✅ 已完成

## 简介
本章学习内容：抽象类（abstract class）、接口（interface）的声明/实现/继承、两者选型、面向接口编程
前置知识：完成「02-inheritance-polymorphism」；有 TS interface / abstract 使用经验
阅读时长：约 30 分钟
难度：🌟🌟🌟
重要程度：🌟🌟🌟🌟🌟
当前进度：16%

---

## 本章的主角是接口

抽象类你其实在 TS 里已经见过（`abstract class`），概念几乎照搬。但**接口是 Java 世界的重量级角色** 🆕——后端代码里接口无处不在：Service 是接口、Mapper 是接口、Spring 的依赖注入默认面向接口。可以说不会接口就没法读后端代码，所以本章 70% 篇幅给它。

## 抽象类：半成品图纸

**是什么**：用 `abstract` 修饰的类，**不能被 `new` 实例化**，可以包含"只有声明没有实现"的抽象方法。

**为什么需要**：上一章的继承里，父类 `Animal.speak()` 写一句默认实现其实很勉强——每种动物叫法都不同，父类根本给不出有意义的默认行为。抽象类解决的就是这种"**我能定骨架，但填不了肉**"的场景：

```java
public abstract class Animal {
    protected String name;

    public Animal(String name) {   // 抽象类可以有构造器（子类用 super 调）
        this.name = name;
    }

    // 普通方法：骨架，所有子类共用
    public void sleep() {
        System.out.println(name + " 在睡觉");
    }

    // 抽象方法：只有声明没有方法体，强制子类实现
    public abstract void speak();
}
```

```java
public class Dog extends Animal {
    public Dog(String name) { super(name); }

    @Override
    public void speak() {          // 抽象方法必须被实现（除非子类也是抽象类）
        System.out.println(name + "：汪汪");
    }
}
```

```java
new Animal("x");   // ❌ 编译错误：抽象类不能实例化
new Dog("旺财");    // ✅
```

规则速记：**有抽象方法的类必须声明为 abstract；抽象类可以一个抽象方法都没有；子类要么实现所有抽象方法，要么自己也声明为 abstract**。

前端对照：和 TS 的 `abstract class` 语义完全一致，无缝迁移 😌。

## 接口：纯契约 🆕

### 是什么

接口（interface）是一种**只定义"能做什么"、不关心"怎么做"**的类型。它是比抽象类更纯粹的抽象：抽象类还能存状态（字段）、给实现，接口（传统上）什么都不能带，只有方法签名。

**为什么需要它**：回到多态。上一章我们用父类 `Animal` 做统一抽象，但现实中很多"能力"不在同一棵继承树上——鸟和飞机都会"飞"，但它们没有共同父类。接口就是**跨继承树的契约**：不管什么类，只要声明自己会飞，就能被当作"会飞的东西"使用。

### 声明

```java
public interface Flyable {
    // 方法默认就是 public abstract，修饰符可省 ⚠️
    void fly();

    // 字段默认是 public static final（全局常量），基本不用
    int MAX_HEIGHT = 10000;
}
```

⚠️ 前端注意：接口里写 `void fly();` 等价于 `public abstract void fly();`——**它天生就是公开且抽象的**，写上 `private` 直接编译错误。

### 实现：implements

```java
public class Bird extends Animal implements Flyable {
    public Bird(String name) { super(name); }

    @Override
    public void speak() { System.out.println(name + "：叽叽"); }

    @Override
    public void fly() {                     // 实现接口的契约
        System.out.println(name + " 扇动翅膀飞");
    }
}
```

重点来了——**一个类可以实现多个接口** 🆕🔥：

```java
public class SuperBird extends Animal implements Flyable, Swimmable {
    // 两个接口的抽象方法都得实现
}
```

这就是 Java 解决"单继承限制"的方案：**类只能 extends 一个父类，但可以 implements 任意多个接口**。继承管"是什么"，接口管"能做什么"，一只鸟是一种动物（单继承），同时会飞也会游泳（多接口）。

### 接口的继承

接口之间也可以继承，而且**可以一次继承多个**：

```java
public interface SwimAndFly extends Flyable, Swimmable {
    void glide();   // 自己再加一个抽象方法
}
```

实现 `SwimAndFly` 的类要实现全部三个方法（fly、swim、glide）。

### Java 8+：接口也能有默认实现（default 方法）

```java
public interface Flyable {
    void fly();

    // default 方法：带实现的接口方法，实现类可继承也可重写 🆕
    default void land() {
        System.out.println("缓缓降落");
    }
}
```

**为什么需要** 💼：接口最大的软肋是"一改全炸"——给接口加个新方法，几百个实现类全部编译报错。default 方法让接口可以"带着默认行为进化"，老实现类无感升级。JDK 自己就是这么干的（比如 `List.sort()`）。

### ⚠️ TS 用户最关键的认知差异

| 维度 | TS interface | Java interface |
|------|--------------|----------------|
| 判定方式 | **结构类型**：对象形状符合就算实现 | **名义类型**：必须显式 `implements` 声明 ⚠️ |
| 运行时 | 编译后彻底消失（类型擦除） | **运行时真实存在**（Spring 的动态代理就靠它）💼 |
| 能装什么 | 任意形状（函数、对象、数组） | 方法签名 + 常量 + default/static 方法 |

第一行是精髓：TS 里 `{ fly() {} }` 天然就是 `Flyable`（鸭子类型的类型化）；Java 里一个类方法再全，**没写 `implements Flyable` 就不是 Flyable**。契约必须签字画押，这是 Java 的仪式感，也是它能做严格架构约束的原因。

## 抽象类 vs 接口：怎么选 💼

| 维度 | 抽象类 | 接口 |
|------|--------|------|
| 能 new 吗 | 都不能 | 都不能 |
| 字段 | 任意（可有实例状态） | 只有 `public static final` 常量 |
| 方法 | 抽象 + 普通方法都行 | 抽象方法 + default + static |
| 构造器 | 有 | 无 |
| 继承/实现 | 单继承 | 多实现；接口间多继承 |
| 设计语义 | **"是什么"**：同一族类的共同底座 | **"能做什么"**：跨族类的能力契约 |

经验法则：**共享状态和代码骨架 → 抽象类；定义能力和契约 → 接口**。实际后端开发中接口出场率远高于抽象类 🔥。

## 面向接口编程：后端的空气

上一章的支付例子，正规写法是面向接口：

```java
// 契约
public interface Payment {
    void pay(int amount);
}

// 实现
public class Alipay implements Payment {
    @Override
    public void pay(int amount) { System.out.println("支付宝支付 " + amount + " 元"); }
}

public class WechatPay implements Payment {
    @Override
    public void pay(int amount) { System.out.println("微信支付 " + amount + " 元"); }
}

// 使用方：只依赖接口，不认识任何具体实现
public class PayService {
    public void checkout(Payment payment, int amount) {
        payment.pay(amount);
    }
}
```

`PayService` 只认识 `Payment` 接口——新增支付方式零修改，这就是**依赖倒置**：依赖抽象而非具体 🔥。

为什么这件事在后端如此重要：Spring 的 IoC 容器装配 Bean 时，注入的都是接口类型（`@Autowired private UserService service;`，实际注入哪个实现由容器决定）；单元测试时可以把接口换成 mock 实现。前端类比：TS 里给组件声明 props 接口约束输入——Java 把这个思路从组件级别放大到了**整个应用架构**级别。

## 双语言对照

```typescript
// TypeScript
interface Payment {
  pay(amount: number): void;
}

class Alipay implements Payment {   // TS 也有 implements
  pay(amount: number) { console.log(`支付宝支付 ${amount} 元`); }
}

// 但 TS 是结构类型：这个没写 implements 的对象也合法 ⚠️
const cash = { pay: (amount: number) => console.log(`现金 ${amount} 元`) };
const p: Payment = cash;   // ✅ 形状符合就行
```

```java
// Java：必须显式 implements，且接口运行时不消失
Payment p = new Alipay();     // 接口类型指向实现类对象 = 多态
p.pay(100);                   // 运行期执行 Alipay 的实现

// Payment 接口里加个 default 方法，Alipay/WechatPay 都不用改
```

## 练习

1. 定义接口 `Playable`（抽象方法 `play()`，default 方法 `stop()` 打印"停止播放"），再定义接口 `Recordable`（抽象方法 `record()`）；写一个 `MusicPlayer` 类同时实现两个接口，并在 `main` 中演示：用 `Playable` 类型变量指向 `MusicPlayer` 对象，分别调用 `play()` 和继承来的 `stop()`。
2. 思考：接口 `A` 和接口 `B` 都有 `default void hello()` 且实现不同，类 `C implements A, B` 会编译通过吗？先给出你的判断，再用代码验证，并说明 Java 的处理规则。

## 本章总结

- 抽象类 = 半成品图纸：不能 new，定骨架留空给子类填；与 TS `abstract class` 语义一致
- 接口 = 纯契约 🆕：方法默认 `public abstract`，类用 `implements` 实现、**可实现多个**，接口间可**多继承**
- default 方法（Java 8+）让接口可以带默认实现安全进化 💼
- ⚠️ 核心差异：TS 接口是结构类型（形状符合即算数、编译后消失）；Java 接口是名义类型（必须显式声明、运行时存在）
- 选型：共享状态用抽象类，定义能力用接口；后端以接口为主——**面向接口编程是 Spring 的地基**

下一章：[静态成员与枚举](./04-static-enum.md)

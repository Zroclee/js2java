# 类、字段与方法：Java OOP 的第一块积木

> 状态：✅ 已完成

## 简介
本章学习内容：类与对象、字段、方法、访问修饰符、构造方法、方法重载
前置知识：JS class 语法；完成「准备工作」两章
阅读时长：约 30 分钟
难度：🌟🌟
重要程度：🌟🌟🌟🌟🌟
当前进度：10%

---

## 先吃颗定心丸：外壳你已经会了

ES6 之后 JS 也有 `class`，Java 的类语法外壳和它高度相似——都有构造器、都有 `this`、都有 `new`。所以这一章真正要学的不是"类是什么"，而是**两者不一样的地方**。差异集中在四点：

| 差异点 | JS | Java |
|--------|-----|------|
| 类型 | 字段/参数不用声明类型 | 字段、参数、返回值**全部要声明类型** |
| 访问控制 | 约定俗成（`_private`）或较新的 `#field` | `private`/`public` 是语言强制，编译器把关 ⚠️ |
| 方法归属 | 函数可以游离在类外 | 方法**必须**写在某个类里 🆕 |
| `this` | 指向随调用方式变（臭名昭著） | 永远指向当前实例，稳定可靠 😌 |

## 核心概念对照

### 类（Class）与对象（Object）

两边心智完全一致：**类是图纸，对象是图纸造出来的实例**。

```java
// Java
public class User {
    // 字段（field）：类的数据
    private String name;
    private int age;

    // 方法（method）：类的行为
    public String introduce() {
        return "我叫 " + name + "，今年 " + age + " 岁";
    }
}
```

```javascript
// JavaScript 对照
class User {
  // JS 的字段直接写在构造器里赋值即可（新语法也支持字段声明）
  constructor(name, age) {
    this.name = name;
    this.age = age;
  }

  introduce() {
    return `我叫 ${this.name}，今年 ${this.age} 岁`;
  }
}
```

术语对应：Java 说的**字段（field）**就是 JS 的"实例属性"，**方法（method）**两边一致。实例化都是 `new`：

```java
User user = new User();   // Java：左边要声明类型 ⚠️
```
```javascript
const user = new User();  // JS
```

### 字段必须声明类型与位置

Java 的字段声明在类体里、所有方法之外，且**必须写类型**：

```java
public class User {
    private String name;   // 类型 + 字段名，分号结尾
    private int age;       // 不赋值也有默认值：int 是 0，对象是 null ⚠️
}
```

⚠️ **前端易踩的坑**：Java 字段不初始化也有默认值（数值 `0`、布尔 `false`、引用类型 `null`）。`null` 是 Java 世界万恶之源——著名的 `NullPointerException`（空指针异常，≈ JS 的 `Cannot read property of undefined`，但出现频率高得多）💼。但在**方法内部的局部变量**没有默认值，不初始化直接用会编译报错——规则刚好相反，注意区分。

### 访问修饰符：真正的"私有"

JS 的"私有"长期靠命名约定 `_name`，ES2022 才有 `#name`。Java 从第一天起就是语言级强制：

| 修饰符 | 谁能访问 | 前端理解 |
|--------|----------|----------|
| `private` | 仅本类 | ≈ JS 的 `#field` |
| 不写（默认） | 同包（package）可访问 | 🆕 "包"的概念第五章讲 |
| `protected` | 同包 + 子类 | 继承章节展开 |
| `public` | 所有人 | 默认开放的 JS 属性 |

🔥 **行业铁律：字段一律 `private`，通过方法暴露访问**。这就是 Java Bean 规范——为每个字段提供 getter/setter：

```java
public class User {
    private String name;

    public String getName() {        // getter
        return name;
    }
    public void setName(String name) { // setter：可以在里面做校验 🆕
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("名字不能为空");
        }
        this.name = name;
    }
}
```

JS 里你直接 `user.name = ""` 就改掉了，拦不住；Java 的 `private` + setter 把"赋值"变成一道关卡，校验逻辑有了安放之处。这不是繁文缛节，是后端代码可维护性的基石 🔥。

> 💡 不用手写 getter/setter——IDEA 里 `Cmd + N`（第二章说过）一键生成。

## 构造方法：比你熟悉的 constructor 多一点

### 基本形态

```java
public class User {
    private String name;
    private int age;

    // 构造方法：方法名 = 类名，没有返回值类型（连 void 都不写）⚠️
    public User(String name, int age) {
        this.name = name;   // this.name 是字段，name 是参数
        this.age = age;
    }
}
```

和 JS 的 `constructor(name, age) { this.name = name; }` 作用相同。差异：

1. **写法**：不是 `constructor` 关键字，而是"与类同名的方法"，且**不声明返回值类型**——写上 `void` 它就变成一个普通方法了，巨坑 ⚠️
2. **默认构造器**：一个构造器都不写时，编译器送一个无参构造器。但**只要你写了任何一个构造器，默认的就收回了**——此时 `new User()` 会编译报错。JS 的 class 也有类似规则，但前端很少感知 💼
3. **`this(...)` 调用兄弟构造器** 🆕：

```java
public User() {
    this("匿名", 0);   // 调用另一个构造器，必须写在第一行
}

public User(String name, int age) {
    this.name = name;
    this.age = age;
}
```

## 方法重载（Overload）：JS 没有的能力 🆕

**是什么**：同一个类里，允许存在多个**同名但参数列表不同**的方法。

```java
public class Printer {
    public void print(String msg) {
        System.out.println("文本: " + msg);
    }

    public void print(int num) {              // 参数类型不同 ✅ 合法重载
        System.out.println("数字: " + num);
    }

    public void print(String msg, int times) { // 参数个数不同 ✅ 合法重载
        for (int i = 0; i < times; i++) System.out.println(msg);
    }
}
```

调用时编译器根据**实参的类型和个数**自动选择版本：

```java
printer.print("hi");        // → 文本: hi
printer.print(42);          // → 数字: 42
printer.print("hi", 3);     // → hi 打印 3 次
```

**为什么需要**：JS 里实现同样效果只能写在一个函数里手动判断：

```javascript
function print(a, b) {
  if (typeof a === 'number') { /* ... */ }
  else if (typeof b === 'number') { /* ... */ }
  // 参数组合越多越混乱
}
```

静态类型让"按参数分派"可以发生在编译期，每个重载版本干净利落。

⚠️ **重载的两个考点** 💼：
1. **只看参数列表（类型/个数/顺序），与返回值无关**——仅返回值不同的两个同名方法是编译错误，不是重载
2. 重载是**编译期**决定的（看声明类型），和后面的"重写（Override，运行期多态）"别搞混——继承章再掰扯

### 可变参数：Java 的 rest 参数

```java
public int sum(int... nums) {   // ≈ JS 的 function sum(...nums)
    int total = 0;
    for (int n : nums) total += n;  // nums 是个 int 数组
    return total;
}

sum(1, 2); sum(1, 2, 3); sum();  // 都能调
```

规则也和 rest 参数神似：**必须是最后一个参数，且最多一个**。

## 完整示例：双语言对照

```java
public class Counter {
    private int count;

    public Counter() {           // 无参构造
        this(0);
    }

    public Counter(int initial) { // 有参构造
        this.count = initial;
    }

    public void add() {          // 重载 1：加 1
        add(1);
    }

    public void add(int n) {     // 重载 2：加 n
        this.count += n;
    }

    public int getCount() {
        return count;
    }

    public static void main(String[] args) {
        Counter c = new Counter(10);
        c.add();
        c.add(5);
        System.out.println(c.getCount());  // 16
    }
}
```

```javascript
// JS 对照：没有重载，用默认参数或判断模拟
class Counter {
  constructor(initial = 0) {
    this.count = initial;
  }

  add(n = 1) {        // JS 一个方法 + 默认参数搞定
    this.count += n;
  }

  getCount() {
    return this.count;
  }
}
```

看完这个对照你应该有感觉了：**JS 用"默认参数/参数判断"灵活解决的问题，Java 倾向用"重载"写得泾渭分明**。两种风格，没有高下，但读 Java 代码时要习惯它的仪式感十足。

## 练习

1. 设计一个 `Book` 类：私有字段 `title`、`price`；提供无参构造（默认 title="未命名"）和全参构造；为字段写 getter/setter，`setPrice` 里校验价格不能为负；写两个重载方法 `describe()` 和 `describe(String prefix)`。
2. 思考并验证：给 `Book` 再加一个方法 `public String describe()` 会编译通过吗？为什么？（不写答案，自己用 IDEA 试一下红线提示）

## 本章总结

- 类与对象的心智和 JS 一致，四大差异：**强制类型、强制访问控制、方法必须在类内、this 稳定**
- 字段有默认值（对象类型是 `null`——空指针的源头），局部变量没有默认值
- 构造方法 = 与类同名、无返回类型；写了任意构造器，默认无参构造就没了；`this(...)` 可串联构造器
- 重载 🆕：同名不同参，编译期分派，只看参数列表不看返回值；可变参数 `int... nums` ≈ JS rest

下一章：[继承与多态](./02-inheritance-polymorphism.md)

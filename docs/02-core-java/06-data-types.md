# 数据类型：静态类型世界的第一课

> 状态：✅ 已完成

## 简介
本章学习内容：基本类型（8 种）、引用类型、包装类与自动装箱、类型定义与转换
前置知识：完成「05-package-module」；熟悉 JS 的 typeof 与原始值/对象之分
阅读时长：约 30 分钟
难度：🌟🌟
重要程度：🌟🌟🌟🌟🌟
当前进度：26%

---

## 先重塑最根本的认知：变量有没有类型

JS 里**变量没有类型，值才有类型**——`let x = 1` 之后随手 `x = "abc"`，没人拦你，类型是运行时的事。

Java 反过来：**变量必须先声明类型，且终身锁定**，编译器在编译期就检查每一次赋值：

```java
int age = 18;
age = "abc";   // ❌ 编译错误，IDEA 里直接红线——这就是静态类型 🆕
```

你在 TS 里其实体验过这套（`let age: number`），但 TS 的类型在编译后消失（类型擦除，运行时不拦）；Java 的类型是**语言级强制**，从编译到运行全程有效。这是重塑层的第一课：类型不再是"建议"，是"法律"。

## 两大类：基本类型 vs 引用类型

Java 的数据类型分两大阵营，这是本章的主线：

```
Java 数据类型
├── 基本类型（primitive）── 8 种，变量里存的就是"值本身"
│    ├── 整数：byte / short / int / long
│    ├── 浮点：float / double
│    ├── 字符：char
│    └── 布尔：boolean
└── 引用类型（reference）── 变量里存的是"对象的地址"
     └── 类（String 等）、接口、数组、枚举……
```

好消息：这个二分法 JS 里就有——**原始值按值传递，对象按引用传递**，行为一模一样，属于迁移层 😌：

```java
int a = 10;
int b = a;
b = 99;
System.out.println(a);   // 10 —— 基本类型赋值是"复制值"，互不影响

int[] arr1 = {1, 2, 3};
int[] arr2 = arr1;       // 数组是引用类型
arr2[0] = 99;
System.out.println(arr1[0]);  // 99 —— 复制的是地址，指向同一个数组
```

但有两个关键差异要注意 ⚠️：

1. **基本类型不能是 `null`**——`int age = null;` 编译错误。只有引用类型可以为 null（所以空指针 NPE 只会发生在引用类型身上）
2. **String 是引用类型**——它不是基本类型！JS 用户很容易想当然 ⚠️（字符串的坑很多，下一章专门讲）

## 基本类型逐个看：怎么定义

### 八种基本类型速查表

| 类型 | 字节 | 范围/说明 | 对应 JS |
|------|------|-----------|---------|
| `byte` | 1 | -128 ~ 127 | 🆕 无 |
| `short` | 2 | ±3 万 | 🆕 无 |
| `int` | 4 | ±21 亿，**默认选择** 🔥 | ≈ number（整数部分） |
| `long` | 8 | 巨大整数 | ≈ bigint |
| `float` | 4 | 单精度小数（少用） | ≈ number |
| `double` | 8 | 双精度小数，**默认选择** 🔥 | ≈ number |
| `char` | 2 | 单个 Unicode 字符 | 🆕 无（JS 只有字符串） |
| `boolean` | - | 只有 true / false | boolean |

JS 一个 `number` 打天下，Java 给你 6 种数值类型——不是刁难你，是因为**内存和精度都是钱**：后端服务百万级对象时，每个字段省 4 字节都可观 💼。

### 定义语法与字面量规则

```java
int age = 18;              // 整数默认就是 int
long big = 100L;           // ⚠️ long 字面量要加 L（小写 l 像 1，别用）
double price = 3.14;       // 小数默认是 double
float f = 3.14F;           // ⚠️ float 字面量必须加 F，否则把 double 塞进 float 会报错
char c = 'A';              // ⚠️ 单引号！双引号 "A" 是 String（引用类型）
boolean flag = true;       // 只有 true/false

int million = 1_000_000;   // 下划线分隔符（JS 也有，纯好看）
```

### ⚠️ JS 用户最大的坑：没有 truthy / falsy

```javascript
// JS：0、""、null、undefined 都是 falsy
if (count) { ... }        // count 为 0 时不执行
```

```java
// Java：if 里必须是 boolean 表达式，别的类型直接编译错误 🆕
if (count) { ... }        // ❌ 编译错误！int 不是 boolean
if (count > 0) { ... }    // ✅ 老老实实写比较
if (name != null && !name.isEmpty()) { ... }  // 判断字符串的标准姿势
```

刚转过来的人会非常别扭，但它**消灭了一整类 bug**（`""` 和 `0` 被意外当 false）——这是静态类型送你的第一份礼物。

### 类型转换

```java
int i = 100;
long l = i;              // 小转大：自动（隐式）转换 ✅

long big = 100L;
int n = (int) big;       // 大转小：必须强转（显式），自己承担后果 ⚠️

int overflow = (int) 3_000_000_000L;
System.out.println(overflow);  // 输出一个莫名其妙的负数——溢出！编译不拦 💥

int x = 5, y = 2;
System.out.println(x / y);           // 2 ⚠️ 整数除法，小数直接砍掉
System.out.println(x / (double) y);  // 2.5 ✅ 先转成 double
```

JS 里 `5 / 2 = 2.5` 天经地义；Java 里**两个 int 相除结果还是 int**——新手第一大坑，记牢 🔥。

## 包装类：给基本类型穿上对象的外衣

### 为什么需要包装类

基本类型有三个"做不到"：

1. **集合装不了**：`ArrayList<int>` ❌ 编译错误——Java 集合只能装对象，必须写 `ArrayList<Integer>` 🔥（下一章集合天天见）
2. **表示不了"无"**：数据库里年龄字段可以是 NULL，`int` 却无法为 null——`Integer` 可以
3. **没有方法**：`Integer.parseInt("123")` 这类转换方法挂在包装类上

### 对照表与定义

| 基本类型 | 包装类 | 备注 |
|----------|--------|------|
| byte | Byte | |
| short | Short | |
| int | **Integer** | ⚠️ 不是 Int！特殊两个之一 |
| long | Long | |
| float | Float | |
| double | Double | |
| char | **Character** | ⚠️ 不是 Char！特殊两个之二 |
| boolean | Boolean | |

```java
Integer score = Integer.valueOf(100);   // 正式写法
int n = score.intValue();               // 取回基本值
Integer parsed = Integer.parseInt("123"); // 字符串 → int 🔥 后端解析参数天天用
```

### 自动装箱/拆箱：糖衣与炮弹 🆕

Java 5 之后编译器帮你自动转换：

```java
Integer score = 100;    // 自动装箱：int → Integer（编译器偷偷 valueOf）
int n = score;          // 自动拆箱：Integer → int（编译器偷偷 intValue）
```

写起来丝滑，但糖衣里有炮弹 ⚠️💼（面试必考）：

```java
Integer a = null;
int b = a;              // 💥 NullPointerException！拆箱瞬间 null 被调用 intValue()

Integer x = 127, y = 127;
System.out.println(x == y);   // true（JVM 缓存了 -128~127 的 Integer）

Integer m = 128, n2 = 128;
System.out.println(m == n2);  // false！超出缓存范围，是两个对象——== 比的是地址

System.out.println(m.equals(n2));  // true ✅ 包装类比较永远用 equals
```

规则一句话：**包装类是对象，`==` 比的是地址不是值——比较用 `equals`，拆箱前防 `null`** ⚠️。（`==` 和 `equals` 的完整恩怨，下一章字符串主场再战。）

## 全面对比：Java vs JS 类型世界

| 维度 | JS | Java |
|------|-----|------|
| 类型归属 | 值有类型，变量没有 | **变量有类型，终身锁定** 🆕 |
| 数字 | 一种 number 通吃 | 6 种数值类型按场景选 |
| 字符 | 无，只有字符串 | `char` 单引号 🆕 |
| 布尔 | truthy/falsy 万物可判 | **只有 true/false**，if 必须布尔表达式 ⚠️ |
| 空值 | null + undefined 两个 | 只有 null，且**基本类型不能为 null** |
| 类型检查 | 运行时 typeof | 编译期全面检查 |
| 原始/对象之分 | 有（原始值 vs 对象） | 有（基本类型 vs 引用类型），行为一致 😌 |
| 包装 | 自动装箱但几乎无感 | 装箱/拆箱有坑：缓存、NPE、== 💼 |

## 练习

1. 定义八个基本类型的变量并打印：注意 `long` 加 `L`、`float` 加 `F`、`char` 用单引号。然后故意写 `int x = 3.14;` 和 `if (1) { }` 看 IDEA 红线，体会编译期类型检查。
2. 写一个方法接收 `Integer` 参数并返回 `int`：分别传入 `100` 和 `null` 调用，观察第二次抛出的异常类型；再用 `equals` 改写两个 `Integer(128)` 的比较，对照 `==` 的结果。

## 本章总结

- Java 变量**先声明类型、终身锁定**，编译期检查——类型是法律不是建议 🆕
- 两大类：**基本类型**（8 种，存值本身，不能为 null）vs **引用类型**（存地址，可为 null）；赋值行为与 JS 原始值/对象一致
- 定义要点：`long` 加 L、`float` 加 F、`char` 单引号；**没有 truthy/falsy**；整数除法砍小数 ⚠️
- 包装类 = 基本类型的对象外衣（集合/可空/有方法）；自动装箱拆箱丝滑但有坑：**比较用 equals，拆箱防 null，127 是分界线** 💼

下一章：[数值运算](./07-operators.md)——位运算、溢出与 BigDecimal 的世界

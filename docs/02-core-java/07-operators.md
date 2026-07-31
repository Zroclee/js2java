# 数值运算：类型提升、溢出与大数的世界

> 状态：✅ 已完成

## 简介
本章学习内容：算术/位/移位/布尔运算符、类型提升、溢出、短路求值、三元表达式、BigInteger 与 BigDecimal
前置知识：完成「06-data-types」（基本类型与整数除法基础）
阅读时长：约 30 分钟
难度：🌟
重要程度：🌟🌟🌟
当前进度：29%

---

## 先过一遍"和 JS 一样"的部分（迁移层，30 秒）

```java
int a = 10, b = 3;
a + b;   // 13      a - b;   // 7
a * b;   // 30      a / b;   // 3 ⚠️ 整数除法（06 章讲过）
a % b;   // 1       a++;     // 自增，++a / a++ 区别同 JS
```

`+ - * / % ++ --`、比较运算符 `> < >= <= == !=`、赋值 `= += -=`——全部和 JS 一致，快速扫过。

真正的分水岭在于：**JS 的 number 是"一种类型打天下"，Java 是六种数值类型混在一起算**——由此产生了 JS 世界里不存在的三个概念：类型提升、溢出、大数类。本章的主战场是它们，外加一个常被忽视的差异：`||` 的返回值。

## 类型提升：混合运算的隐形规则 🆕

**是什么**：不同类型的数值一起运算时，Java 会先把"小"类型自动提升为"大"类型再计算。

```java
int i = 10;
long l = 20L;
long r = i + l;        // i 先提升为 long，结果是 long

double d = i + 0.5;    // int + double → double
```

提升方向（小 → 大）：`byte → short → int → long → float → double`

### 最坑的一条：byte/short/char 运算先提升为 int ⚠️💼

```java
byte b = 1;
// b = b + 1;          // ❌ 编译错误！b + 1 的结果已经是 int，塞不回 byte
b = (byte) (b + 1);    // ✅ 必须强转

b += 1;                // ✅ 但这又可以了！⚠️
// += 隐含了强制转换：b += 1 等价于 b = (byte)(b + 1)
```

`b + 1` 报错、`b += 1` 却通过——这对矛盾是面试经典题。原因：**byte、short、char 参与运算时，Java 一律先把它们提升为 int**（JVM 没有针对小类型的算术指令）。JS 没有这个概念——JS 的 number 不分大小，自然无所谓提升。

## 溢出：JS 不会环绕，Java 会 🆕⚠️

JS 的 number 是双精度浮点数，整数大到 2⁵³ 才开始失真，而且失真是"变不精确"。

Java 的 `int` 是**固定 32 位、有明确边界**（约 ±21 亿），越界时不是报错，也不是失真——而是**静默环绕**：

```java
int max = Integer.MAX_VALUE;          // 2147483647
System.out.println(max + 1);          // -2147483648 💥 加一变成了最小的负数！

int price = 1_000_000_000;
System.out.println(price * 3);        // -1294967296 💥 静默溢出，编译运行都不警告
```

数值绕了一圈回到负数——程序不崩、不报错，结果却是错的，这比异常可怕得多 💼。防御姿势：

```java
long r = (long) price * 3;            // 1️⃣ 提前提升为 long（注意强转要在运算前）
Math.addExact(max, 1);                // 2️⃣ 溢出时抛 ArithmeticException，让错误显形 🔥
```

## 位运算与移位：前端的老熟人，后端的常客

位运算符 JS 里也有（`& | ^ ~ << >> >>>`），语义一致，属于迁移层——但前端一年用不上几次，后端却常在这些地方出现 🔥：权限位设计、哈希计算、网络协议、源码阅读（HashMap 里全是位运算）。

```java
int a = 0b1100;   // 12（0b 前缀是二进制字面量）
int b = 0b1010;   // 10

a & b;   // 0b1000 = 8   按位与
a | b;   // 0b1110 = 14  按位或
a ^ b;   // 0b0110 = 6   按位异或（相同为 0，不同为 1）
~a;      // 按位取反

a << 1;  // 24   左移 1 位 ≈ ×2（比乘法快的性能 trick）
a >> 1;  // 6    右移 1 位 ≈ ÷2
```

**`>>` vs `>>>` 的区别** 🆕💼：

```java
int neg = -8;
neg >> 1;    // -4  算术右移：高位补"符号位"（负数补 1），保持正负
neg >>> 1;   // 2147483644  逻辑右移：高位一律补 0，负数秒变超大正数
```

**典型应用：用位掩码做权限**（Linux 的 rwx 权限就是这么设计的）：

```java
class Perm {
    static final int READ = 0b100;    // 4
    static final int WRITE = 0b010;   // 2
    static final int EXEC = 0b001;    // 1
}

int userPerm = Perm.READ | Perm.WRITE;        // 授予：读+写 = 6
boolean canWrite = (userPerm & Perm.WRITE) != 0;  // 校验：按位与提取
```

一个 int 就能装 32 种开关——Spring Security、Netty 源码里随处可见。

## 布尔运算与"短路"：相似的外壳，不同的返回值 ⚠️

### 短路（short-circuit）概念本身：与 JS 一致 😌

```java
boolean r = (user != null) && (user.getName().length() > 0);
// user 为 null 时，右半部分不会执行——&& 左边 false 就"短路"，
// 这避免了空指针，和 JS 的 && 行为完全相同
```

`&&`：左边 false 就不算右边；`||`：左边 true 就不算右边。短路是防御 null 的基本功 🔥。

### 但返回值完全不同！⚠️

这是 JS 用户必须重塑的点：

```javascript
// JS：&& 和 || 返回的是"决定结果的那个值"
const name = user.name || '匿名';    // 空值兜底，前端天天写
const port = config.port || 8080;
```

```java
// Java：&& 和 || 只返回 boolean！🆕
String name = user.name || "匿名";   // ❌ 编译错误，右边不是 boolean
```

JS 的 `||` 是"取值工具"，Java 的 `||` 是"纯逻辑判断"——**Java 里空值兜底要用三元表达式**（下一节）：

```java
String name = (user.name != null) ? user.name : "匿名";   // ✅
// 或 Java 8+ 的 Optional 风格（集合/Stream 章节再深入）：
String name2 = java.util.Optional.ofNullable(user.name).orElse("匿名");
```

顺带一个冷知识 💼：`&` 和 `|` 也可以连接两个 boolean，效果是"**不短路**的与/或"（两边都算完）。日常别用，但看到源码别懵。

## 三元表达式：Java 里的"取值工具"

```java
// 语法与 JS 完全相同
int max = (a > b) ? a : b;
String tag = (score >= 60) ? "及格" : "不及格";
```

角色定位：JS 里 `a || b`、`a ?? b`、`cond ? x : y` 三种习惯，在 Java 里只剩三元这一种（Java 没有 `||` 取值和 `??`——Java 8 之前连 Optional 都没有）。嵌套三元可读性差，别写 💡。

## 大数：BigInteger 与 BigDecimal 🆕🔥

### 为什么需要它们

两个问题 JS 用户都熟，但解法完全不同：

```java
// 问题 1：连 long 都不够大
long f = 1;
for (int i = 2; i <= 21; i++) f *= i;
System.out.println(f);   // 21! 就溢出成负数了 💥

// 问题 2：double 精度丢失（JS 同坑 😌 0.1 + 0.2 ≠ 0.3，IEEE 754 的锅）
System.out.println(0.1 + 0.2);   // 0.30000000000000004
```

JS 对大数的答案是 ES2020 的 `BigInt`；Java 的答案是两个**类**：`BigInteger`（任意大整数）和 `BigDecimal`（任意精确小数）。

### BigInteger：任意大的整数

```java
import java.math.BigInteger;

BigInteger big = new BigInteger("99999999999999999999999999");
BigInteger r = big.add(BigInteger.ONE);        // 加减乘除全是方法调用 ⚠️
BigInteger p = big.multiply(new BigInteger("2"));
```

⚠️ 两个注意：它们是**对象**，`+ - * /` 用不了，必须调方法（`add/subtract/multiply/divide`）；且**不可变**——`big.add(ONE)` 返回新对象，不改变 big 本身（和 String 一个脾气）。

### BigDecimal：金额计算的唯一选择 🔥💼

**后端铁律：涉及钱（金额、费率、汇率）一律用 BigDecimal，禁用 double/float。** 面试必考，工作中必用。

```java
import java.math.BigDecimal;

// ⚠️ 创建必须用字符串构造器！
BigDecimal a = new BigDecimal("0.1");
BigDecimal b = new BigDecimal("0.2");
System.out.println(a.add(b));        // 0.3 ✅ 精确

BigDecimal wrong = new BigDecimal(0.1);   // ⚠️ 用 double 构造，误差直接带进来
System.out.println(wrong);            // 0.1000000000000000055511... 💥

// 运算：方法调用
BigDecimal price = new BigDecimal("19.99");
BigDecimal qty = new BigDecimal("3");
BigDecimal total = price.multiply(qty);          // 59.97

// 除法 ⚠️：除不尽时必须指定舍入模式，否则抛 ArithmeticException
BigDecimal avg = price.divide(new BigDecimal("3"), 2, BigDecimal.ROUND_HALF_UP);
//                                                     保留2位  四舍五入

// 比较 ⚠️💼：用 compareTo，不用 equals！
BigDecimal x = new BigDecimal("2.0");
BigDecimal y = new BigDecimal("2.00");
x.equals(y);          // false！equals 连精度位数一起比
x.compareTo(y) == 0;  // true ✅ compareTo 只比数值
```

## 对比总表

| 主题 | JS | Java |
|------|-----|------|
| 混合类型运算 | 只有 number，无概念 | **类型提升**，byte/short/char 先变 int ⚠️ |
| 数值越界 | 2⁵³ 后缓慢失真 | **静默环绕**成负数，Math.addExact 可显形 🆕 |
| 位运算 | 有（32 位转换）但极少用 | 同语法，权限/哈希/源码常用 🔥 |
| 右移 | `>>` `>>>` 同义 | `>>` 带符号 vs `>>>` 补零 💼 |
| `&&`/`||` 返回值 | 返回决定结果的值（`\|\|` 可兜底取值） | **只返回 boolean**，兜底用三元 ⚠️ |
| 大整数 | BigInt（ES2020） | BigInteger 类，方法运算 🆕 |
| 精确小数 | 无原生方案（decimal.js 库） | **BigDecimal**，金额必用 🔥💼 |

## 练习

1. 浮点陷阱复现与修复：先用 double 计算 `0.1 + 0.2` 打印结果，再用 BigDecimal 重做，最后试试 `new BigDecimal(0.1)` 与 `new BigDecimal("0.1")` 打印值的差异。
2. 写一个方法 `safeAdd(int a, int b)`：用 `Math.addExact` 实现并在溢出时返回提示信息；再写一段代码验证 `Integer.MAX_VALUE + 1` 的环绕行为。

## 本章总结

- 算术运算符与 JS 重合，差异三剑客：**类型提升**（byte/short/char 先变 int，`b+1` 报错 `b+=1` 通过）、**溢出环绕**（静默变负数，比异常更可怕）、**大数类**
- 位运算/移位语法同 JS，但后端真用：权限掩码、哈希、源码；`>>` 带符号、`>>>` 补零
- 短路行为同 JS，但 `&&`/`||` **只返回 boolean**——JS 的 `||` 兜底取值写法在 Java 要换成三元 ⚠️
- **金额必用 BigDecimal**：字符串构造器创建、`compareTo` 比较、除法指定舍入模式；double 算钱是后端事故高发区 🔥💼

下一章：[字符串处理](./08-string.md)——`==` 与 `equals` 的主战场

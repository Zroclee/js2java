# 字符串处理：String 的不可变世界

> 状态：✅ 已完成

## 简介
本章学习内容：字符串声明、不可变性、`==` vs `equals` 与常量池、常用操作方法、类型转换、StringBuilder 与 StringJoiner
前置知识：完成「07-operators」；JS 字符串使用经验
阅读时长：约 30 分钟
难度：🌟🌟
重要程度：🌟🌟🌟🌟🌟
当前进度：32%

---

## 声明与不可变性

### 两种声明方式

```java
String a = "hello";                  // 方式 1：字面量（99% 的场景用这个）🔥
String b = new String("hello");      // 方式 2：new 对象（几乎不用，但它俩的区别是面试题 💼）
```

### 不可变：和 JS 一样 😌

好消息——**Java 的 String 和 JS 的字符串一样，都是不可变的**：

```java
String s = "hello";
s.toUpperCase();          // 返回新字符串 "HELLO"，s 本身纹丝不动
System.out.println(s);    // 还是 "hello"
String upper = s.toUpperCase();   // 想生效必须接住返回值 ⚠️
```

迁移层，零成本。但记住这个脾气，后面 StringBuilder 的存在意义就系在它身上。

## 比较：`==` vs `equals`（本章主战场 💼）

### 第一刀：`==` 比的是"是不是同一个对象"

JS 里 `'a' === 'a'` 永远 true，因为 JS 字符串是原始值，`===` 比的就是值。

Java 里 String 是**对象**（引用类型，06 章埋的伏笔），`==` 比的是**两个引用指向的地址**——值一样但对象不同，`==` 就是 false：

```java
String a = "hello";
String b = "hello";
String c = new String("hello");

System.out.println(a == b);        // true  ⚠️ 别急，这是常量池的功劳，下面讲
System.out.println(a == c);        // false 💥 值一样，但不是同一个对象！
System.out.println(a.equals(c));   // true  ✅ equals 才是比值
```

**铁律：字符串（以及一切对象）比较内容，永远用 `equals`，不用 `==`** 🔥。实际开发中 90% 的"字符串比较失效" bug 都是手滑写了 `==`。

### 为什么 `a == b` 又是 true？字符串常量池 🆕💼

JVM 为了省内存，维护了一个**字符串常量池**：

```
"hello" 字面量 ──▶ 进常量池（只存一份）
String a = "hello" ──▶ 指向池里这份
String b = "hello" ──▶ 池里已有，复用！指向同一份 ✅ 所以 a == b 为 true
String c = new String("hello") ──▶ new 强制在堆里开新对象，地址不同 ❌
```

规则一句话：**字面量进池复用，`new` 必开新对象**。所以 `==` 的结果取决于字符串怎么来的——这种"有时对有时错"的特性正是它危险的地方，统一用 `equals` 就永远不用操心。

### 比较方法全家桶

```java
// 1. equals：比值（注意调用姿势，防 NPE ⚠️）
String input = null;
// input.equals("admin");          // 💥 NPE！对 null 调方法
"admin".equals(input);             // ✅ 安全：常量在前（Yoda 风格）
java.util.Objects.equals(input, "admin");  // ✅ 更安全：null 友好，返回 false

// 2. equalsIgnoreCase：忽略大小写
"ABC".equalsIgnoreCase("abc");     // true

// 3. compareTo：按字典序比大小（≈ JS 的 localeCompare）
"a".compareTo("b");                // 负数（a 在前）
```

## 常用操作方法：JS API 对照表 🔥

| 操作 | Java | JS 对照 | 备注 |
|------|------|---------|------|
| 长度 | `s.length()` | `s.length` | ⚠️ Java 是**方法调用**（数组才是 `.length` 属性） |
| 取字符 | `s.charAt(0)` | `s[0]` / `s.charAt(0)` | 返回 char |
| 截取 | `s.substring(1, 3)` | `s.slice(1, 3)` | 前闭后开，同 JS |
| 查找 | `s.indexOf("x")` / `s.contains("x")` | 同名 | 找不到返回 -1 |
| 替换 | `s.replace("a", "b")` | `s.replaceAll("a", "b")` | ⚠️ 下详 |
| 正则替换 | `s.replaceAll("\\d", "#")` | `s.replace(/\d/g, "#")` | ⚠️ 下详 |
| 切分 | `s.split(",")` | `s.split(",")` | ⚠️ 参数是**正则**，`"a.b".split("\\.")` 才能按点切 |
| 去空白 | `s.trim()` / `s.strip()` | `s.trim()` | `strip()` 是 Java 11+，支持 Unicode 空白 |
| 大小写 | `s.toUpperCase()` / `s.toLowerCase()` | 同名 | |
| 首尾判断 | `s.startsWith("x")` / `endsWith` | 同名 | |
| 空判断 | `s.isEmpty()` / `s.isBlank()` | 无（手写） | `isBlank()` Java 11+：空白字符也算空 🔥 |
| 拼接 | `s.concat(t)` 或 `+` | `+` 或模板字符串 | |
| 格式化 | `String.format("%s-%d", name, age)` | `` `${name}-${age}` `` | Java 没有模板字符串 ⚠️ |

⚠️ **replace 与 replaceAll 的命名陷阱**（和 JS 反着来）：

```java
"a-b-c".replace("-", "+");     // "a+b+c" —— replace 就换全部！（普通字符串匹配）
"a-b-c".replaceAll("-", "+");  // 也能用，但参数是正则 ⚠️
"a.b.c".replaceAll(".", "/");  // "//////" 💥 点号在正则里是"任意字符"
"a.b.c".replace(".", "/");     // "a/b/c" ✅ 想换全部用 replace 就对了
```

JS 是 `replace` 换首个、`replaceAll` 换全部；Java 是 `replace` 换全部（字面匹配）、`replaceAll` 上正则——刚好拧着，必踩一次 ⚠️。

💡 **Java 15+ 文本块**缓解没有模板字符串的痛：

```java
String html = """
    <div>
        <p>Hello %s</p>
    </div>
    """.formatted(name);   // Java 15 文本块 + formatted ≈ 多行模板字符串
```

## 类型转换 🔥（后端高频：前端传参全是字符串）

```java
// 字符串 → 数字（HTTP 参数、配置文件读出来都是 String）
int age = Integer.parseInt("18");
double price = Double.parseDouble("19.99");
// ⚠️ "18a" 会抛 NumberFormatException，生产代码要捕获

// 数字 → 字符串
String s1 = String.valueOf(18);     // 推荐
String s2 = Integer.toString(18);
String s3 = "" + 18;                // 也能用，不优雅

// 字符串 ↔ 字符数组（算法题常用）
char[] chars = "abc".toCharArray();
String back = new String(chars);
```

## 为什么需要 StringBuilder：不可变的代价 ⚠️🔥

不可变是安全的，但**拼接会付出性能代价**——每次 `+` 都创建一个全新的 String 对象：

```java
// ❌ 循环里的字符串拼接：每次循环都 new 一个中间对象
String result = "";
for (int i = 0; i < 10000; i++) {
    result += i;   // 一万个临时 String 对象被创建又丢弃 💥
}
```

JS 里你也听过"循环拼字符串慢"，但 V8 的优化（rope 结构）让日常无感；JVM 对简单拼接也有优化，**唯独循环里救不了** ⚠️。所以 Java 给出了官方答案——**StringBuilder：可变的字符序列**：

```java
// ✅ 循环拼接的正确姿势
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 10000; i++) {
    sb.append(i);              // 在同一个对象里改，不产生中间垃圾
}
String result = sb.toString(); // 最后一次性落成 String
```

常用方法：`append()` 追加、`insert()` 插入、`delete()` 删除、`reverse()` 反转——全是"原地修改"，最后 `toString()`。

💼 顺带认识 **StringBuffer**：StringBuilder 的线程安全版（方法加了锁），单线程下用 StringBuilder 更快。什么时候要线程安全？并发章节见。

## StringJoiner：专治"分隔符拼接" 🆕

经典痛点：把 `[a, b, c]` 拼成 `"a,b,c"`——手写循环要处理"最后一个元素后面不能多逗号"：

```java
// JS 的解法：['a','b','c'].join(',') 一行搞定
// Java 的官方答案 1：String.join 🔥
String s1 = String.join(",", "a", "b", "c");           // "a,b,c"
String s2 = String.join(",", List.of("a", "b", "c"));  // 集合也行

// Java 的答案 2：StringJoiner（需要前后缀时）
StringJoiner sj = new StringJoiner(",", "[", "]");     // 分隔符、前缀、后缀
sj.add("a").add("b").add("c");
System.out.println(sj);   // [a,b,c]
```

## 练习

1. 验证常量池：分别用字面量、`new String()`、`intern()` 三种方式得到 `"java"`，两两用 `==` 和 `equals` 比较并打印，解释每组结果。（`intern()` 的作用：把对象拉进常量池，查资料了解）
2. 实现方法 `csvJoin(List<String> items)`：分别用「朴素 `+` 循环」「StringBuilder」「StringJoiner」三种方式把集合拼成逗号分隔字符串，循环 10 万次对比耗时，感受差距。

## 本章总结

- String **不可变**（同 JS），所有"修改"返回新对象；声明用字面量即可
- **比较永远用 `equals`**：`==` 比地址；常量池让字面量复用（`==` 时真时假，最坑）💼；`"常量".equals(变量)` 或 `Objects.equals` 防 NPE
- API 与 JS 大体对应，三大坑：`length()` 是方法、**`replace` 换全部而 `replaceAll` 吃正则**（和 JS 反着来）、`split` 参数是正则
- 类型转换：`Integer.parseInt` / `String.valueOf`，后端接参日常 🔥
- **循环拼接用 StringBuilder**（不可变的性能代价）；分隔符拼接用 `String.join` 或 `StringJoiner`

下一章：[流程控制](./09-control-flow.md)

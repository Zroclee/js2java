# 流程控制：判断与循环

> 状态：✅ 已完成

## 简介
本章学习内容：if / switch 判断、for / while / do-while 循环、增强 for、break / continue 与标签
前置知识：完成「08-string」；JS 流程控制经验
阅读时长：约 15 分钟
难度：🌟
重要程度：🌟🌟🌟🌟🌟
当前进度：35%

---

## 实话实说：这章 90% 是迁移层 😌

if、for、while、do-while、break、continue——语法和 JS **逐字相同**，快速扫一遍就当复习：

```java
if (score >= 60) {
    System.out.println("及格");
} else if (score >= 40) {
    System.out.println("补考");
} else {
    System.out.println("重修");
}

for (int i = 0; i < 5; i++) { ... }   // 经典三段式，同 JS
while (running) { ... }               // 同 JS
do { ... } while (running);           // 同 JS：先执行一次再判断
```

真正值得停留的只有三个点。

## 差异点 1：条件必须是 boolean（老规则，新场景）⚠️

第 06 章说过的"没有 truthy/falsy"在流程控制里天天见面：

```java
if (list.size()) { ... }          // ❌ 编译错误，int 不是 boolean
if (list.size() > 0) { ... }      // ✅
if (!list.isEmpty()) { ... }      // ✅ 更地道

while (line != null) { ... }      // ✅ null 判断要显式写
// while (line) { ... }           // ❌ JS 的习惯在这全得改
```

JS 里 `if (obj)`、`if (str)` 的习惯全部作废，判空、判空串、判长度都要写成布尔表达式。别扭一周就习惯了。

## 差异点 2：增强 for ≈ for...of 😌

遍历数组/集合的专用语法，和 JS 的 `for...of` 完全对应：

```java
int[] nums = {1, 2, 3};
for (int n : nums) {              // 读作"对 nums 里的每个 int n"
    System.out.println(n);
}

List<String> names = List.of("a", "b", "c");
for (String name : names) {       // 集合也能直接遍历 🔥 后端日常
    System.out.println(name);
}
```

```javascript
for (const n of [1, 2, 3]) { ... }  // JS 对照，一个意思
```

⚠️ 和 JS 一样，增强 for 拿不到**下标**——需要下标就退回经典三段式 `for (int i = 0; ...)`。

## 差异点 3：switch——新旧两代语法 🆕（本章唯一重头）

### 老式 switch：穿透陷阱 ⚠️

和 JS 的 switch 一样臭名昭著：**忘了 `break` 就会穿透**（fall-through），继续执行下一个 case：

```java
switch (day) {
    case 1:
        System.out.println("周一");
        break;              // ⚠️ 漏掉 break，会接着执行 case 2 的代码！
    case 2:
        System.out.println("周二");
        break;
    default:
        System.out.println("其他");
}
```

比 JS 强的一点：Java 的 switch 早就支持**字符串**（JS 本来就支持，Java 7 才补上）和**枚举** 🔥。

### 新式 switch 表达式（Java 12+/14 正式）：现代化改造 🆕

Java 12 起 switch 被重做了一遍，改用箭头语法，**不穿透、还能当表达式返回值**：

```java
// 1. 箭头语法：自带 break，不再穿透 ✅
switch (day) {
    case 1 -> System.out.println("周一");
    case 2 -> System.out.println("周二");
    default -> System.out.println("其他");
}

// 2. 可以返回值，直接赋给变量（≈ JS 里你用对象映射干的事）🔥
String label = switch (day) {
    case 1 -> "周一";
    case 2 -> "周二";
    case 3, 4, 5 -> "工作日中段";    // 多个值逗号合并
    case 6, 7 -> "周末";
    default -> "非法";
};

// 3. case 里要写多行：用大括号 + yield 返回
String desc = switch (day) {
    case 1 -> {
        System.out.println("新的一周开始了");
        yield "周一";               // ⚠️ 块内返回值用 yield，不是 return
    }
    default -> "其他";
};
```

JS 对照：新式 switch 表达式约等于你习惯的 `const map = { 1: '周一' }[day] ?? '其他'` 对象映射——但 Java 这个有编译器检查兜底（枚举做 switch 时，漏掉一个值编译器会警告 💼）。

## 冷知识：标签 break / continue（了解即可）

嵌套循环想跳出**外层**循环时，JS 和 Java 都支持标签，语法几乎一样：

```java
outer:
for (int i = 0; i < 3; i++) {
    for (int j = 0; j < 3; j++) {
        if (i * j == 2) break outer;   // 直接跳出外层循环
    }
}
```

JS：`outer: for (...) { break outer; }`——一样。两边都属于"知道有这东西，但优先重构代码避免用它" 💡。

## 练习

1. 用**新式 switch 表达式**写一个方法 `String seasonOf(int month)`：根据月份返回"春/夏/秋/冬"，多月份合并 case，非法月份走 default。
2. 用增强 for 遍历一个 `List<String>`，找出其中第一个长度大于 5 的元素并打印下标——注意：增强 for 拿不到下标，想想怎么改造（至少两种方案）。

## 本章总结

- if/for/while/do-while 与 JS 逐字相同，直接迁移 😌
- 唯一硬性差异：**条件必须是 boolean 表达式**，JS 的 truthy 习惯全部作废 ⚠️
- 增强 for ≈ `for...of`，遍历集合的日常选择，但拿不到下标
- switch 有两代：老式会穿透（`break` 不能忘）；**新式箭头语法 + 可返回值 + `yield`**，优先用新的 🔥

下一章：[数组与集合](./10-collections.md)——Java 数据处理的主菜

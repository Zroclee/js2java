# 数组与集合框架：Java 数据处理的主菜

> 状态：✅ 已完成

## 简介
本章学习内容：数组与 Arrays、集合框架体系、ArrayList / HashSet / HashMap 概念与常用方法、遍历删除的坑、Queue / Stack / Iterator / Collections 拓展
前置知识：完成「09-control-flow」（增强 for）；JS Array / Set / Map 经验
阅读时长：约 45 分钟
难度：🌟🌟🌟
重要程度：🌟🌟🌟🌟🌟
当前进度：39%

---

## 数组：定长的"老实人"

先花五分钟解决数组——它和 JS 数组**只差一个字：定**。

```java
// 声明与初始化
int[] arr = new int[5];          // 定长 5，元素默认 0
int[] nums = {1, 2, 3};          // 字面量初始化
String[] names = new String[3];  // 引用类型数组，元素默认 null

nums[0] = 10;
System.out.println(nums.length); // ⚠️ 数组的 length 是【属性】，不加括号！
```

| 维度 | JS 数组 | Java 数组 |
|------|---------|-----------|
| 长度 | 动态，随便 push | **创建时定死，终生不变** ⚠️ |
| 类型 | 混装（`[1, 'a', {}]`） | **元素类型统一** |
| 长度获取 | `.length` | `.length`（属性，无括号）⚠️ |
| 越界 | 返回 undefined | **抛 ArrayIndexOutOfBoundsException** 💥 |

🤯 三个"长度"别搞混（💼 经典混乱点）：**数组 `.length`（属性）**、**字符串 `.length()`（方法）**、**集合 `.size()`（方法）**。

`Arrays` 工具类补几个常用操作（没有 push/pop，定长数组不配有）：

```java
import java.util.Arrays;

Arrays.toString(nums);        // "[1, 2, 3]" —— 打印数组必须用它 ⚠️ 直接打印是地址
Arrays.sort(nums);            // 排序
int[] copy = Arrays.copyOf(nums, 5);  // 扩容只能靠"复制出一个新数组"
```

数组定长是硬伤，所以日常 90% 的场景用的是下面的集合——**想要 JS 数组那种动态体验，请用 ArrayList**。

## 集合框架总览：一张家谱

```
        Collection（接口）                 Map（接口）⚠️ 独立体系
        ├── List（有序、可重复、有下标）      ├── HashMap 🔥
        │    ├── ArrayList 🔥               ├── LinkedHashMap（保留插入序）
        │    └── LinkedList                 └── TreeMap（按 key 排序）
        ├── Set（不重复、无下标）
        │    ├── HashSet 🔥
        │    └── TreeSet（排序）
        └── Queue（队列）
             └── Deque（双端队列）
```

⚠️💼 **Map 不是 Collection 的子接口**——它是独立家族（键值对 vs 单元素），面试常拿来挖坑。

声明时必须写**泛型**（元素类型），且**基本类型要用包装类**（06 章的伏笔回收）：

```java
List<Integer> list = new ArrayList<>();    // ❌ ArrayList<int> 不存在
Map<String, Integer> map = new HashMap<>(); // 右边 <> 可省略类型（菱形推断）
```

## List：有序可重复，≈ JS 数组

### ArrayList vs LinkedList

| | ArrayList 🔥 | LinkedList |
|---|---|---|
| 底层 | 动态数组 | 双向链表 |
| 随机访问 get(i) | **极快** O(1) | 慢 O(n) |
| 中间插入/删除 | 慢（要搬数据） | 快 O(1) |
| 选型 | **默认用它，99% 场景** 💼 | 频繁头尾增删时考虑 |

### 常用方法对照表 🔥

| 操作 | JS 数组 | ArrayList |
|------|---------|-----------|
| 尾部加 | `arr.push(x)` | `list.add(x)` |
| 按下标读 | `arr[i]` | `list.get(i)` ⚠️ 没有 `[]` 语法 |
| 按下标改 | `arr[i] = x` | `list.set(i, x)` |
| 按下标删 | `arr.splice(i, 1)` | `list.remove(i)` |
| 长度 | `arr.length` | `list.size()` |
| 包含 | `arr.includes(x)` | `list.contains(x)` |
| 找下标 | `arr.indexOf(x)` | `list.indexOf(x)` |
| 判空 | `arr.length === 0` | `list.isEmpty()` |
| 切片 | `arr.slice(1, 3)` | `list.subList(1, 3)`（⚠️ 是视图不是拷贝） |

```java
List<String> list = new ArrayList<>();
list.add("Java");
list.add("JS");
list.get(0);                    // "Java"
list.set(1, "JavaScript");      // 改掉 "JS"
list.remove(0);
list.size();                    // 1

for (String s : list) { ... }   // 增强 for 遍历（上章刚学）
```

⚠️ 一个经典坑：`list.remove(1)` 对 `List<Integer>` 是**按下标删**而不是删元素 1——删元素要写 `list.remove(Integer.valueOf(1))`。

## Set：去重，≈ JS Set

```java
Set<String> set = new HashSet<>();
set.add("a");
set.add("a");          // 加不进去，重复元素静默忽略
set.add("b");
set.size();            // 2
set.contains("a");     // true（≈ JS set.has）

for (String s : set) { ... }   // 可遍历，但没有下标、不保证顺序 ⚠️
```

| 实现类 | 特点 | JS 对照 |
|--------|------|----------|
| `HashSet` 🔥 | 去重、**无序** | `new Set()` |
| `LinkedHashSet` | 去重 + 保留插入序 | JS Set（JS Set 天然保序，所以更贴近这个） |
| `TreeSet` | 去重 + **自动排序** | 无对应 |

去重的原理：`hashCode()` + `equals()` 联合判断——**自定义对象想正确去重，必须重写这两个方法** 💼（面试高频，先在脑子里挂个号）。

## Map：键值对（本章重头戏）

### HashMap 日常使用 ≈ JS 的 Map/Object 😌

```java
Map<String, Integer> scores = new HashMap<>();

scores.put("小明", 90);              // ≈ map.set / obj.key =
scores.put("小红", 85);
scores.get("小明");                  // 90 ≈ map.get / obj[key]
scores.get("不存在");                // ⚠️ 返回 null，不是 undefined
scores.getOrDefault("不存在", 0);    // 0 🔥 兜底神器（≈ JS 的 ?? 0）
scores.containsKey("小明");          // true
scores.remove("小红");
scores.size();                       // 1
```

⚠️ JS 里 `obj[key]` 的方括号便捷读写，Java **没有**——一切走 `put/get`。

### 三种遍历方式 🔥

```java
// 1. 遍历键值对（推荐，一次拿俩）
for (Map.Entry<String, Integer> entry : scores.entrySet()) {
    System.out.println(entry.getKey() + " = " + entry.getValue());
}

// 2. 只遍历键
for (String key : scores.keySet()) { ... }

// 3. lambda 风格（Java 8+，先把 (k, v) -> 当匿名函数语法糖，细节后面章节讲）
scores.forEach((k, v) -> System.out.println(k + " = " + v));
```

### HashMap 底层原理（面试必考 💼，两分钟版）

```
put("小明", 90)
  1. 对 key 算 hashCode → 定位到数组的某个"桶"（bucket）
  2. 桶是空的 → 直接放
  3. 桶里已有元素（哈希冲突）→ 挂成链表
  4. 链表太长（≥8）→ 升级成红黑树，查询从 O(n) 变 O(log n)
```

所以 HashMap = **数组 + 链表 + 红黑树** 的混血结构。记结论即可：**为什么快？哈希定位桶，O(1)；为什么自定义对象当 key 要重写 hashCode/equals？因为定位和比较都靠它们** 💼。

### 兄弟们（一提即可）

- `LinkedHashMap`：保留插入顺序（≈ JS Map 的保序特性）
- `TreeMap`：按 key 自动排序
- `Hashtable`：上古线程安全版，**别用**，知道它存在即可 ⚠️

## ⚠️ 遍历删除的坑：ConcurrentModificationException 💼

```java
List<String> list = new ArrayList<>(List.of("a", "b", "c"));
for (String s : list) {
    if (s.equals("b")) {
        list.remove(s);   // 💥 ConcurrentModificationException！
    }
}
```

增强 for 遍历时集合被结构性修改，迭代器会立刻报警。正确姿势：

```java
// 方案 1：Iterator 的 remove（官方推荐）
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    if (it.next().equals("b")) {
        it.remove();      // ✅ 通过迭代器自己删
    }
}

// 方案 2：Java 8+ 一行流 🔥
list.removeIf(s -> s.equals("b"));
```

## 拓展速览（提一笔即可）

**Queue / Deque**：队列（FIFO）。JS 里你用数组的 `push + shift` 模拟队列，Java 用专用接口：

```java
Deque<String> queue = new ArrayDeque<>();   // 🔥 队列和栈都用它
queue.offer("a");          // 入队（≈ push）
queue.poll();              // 出队取头（≈ shift）
queue.peek();              // 偷看头元素不出队

// 同一个 ArrayDeque 当栈用（LIFO）：
queue.push("x");           // 压栈
queue.pop();               // 弹栈
```

⚠️ 有个叫 `Stack` 的类是历史遗留（基于 Vector，性能差），**别用**，栈也请用 `ArrayDeque` 💡。

**Iterator**：迭代器 ≈ JS 的迭代器协议（`Symbol.iterator` + `next()`）。增强 for 底层就是它，日常直接用增强 for 即可，只在"边遍历边删"时需要显式出场（上面已演示）。

**Collections 工具类**（集合的"Math 类"，全静态方法）🔥：

```java
Collections.sort(list);            // 排序
Collections.reverse(list);         // 反转
Collections.shuffle(list);         // 洗牌
Collections.max(list);             // 最大（还有 min）
Collections.frequency(list, "a");  // 统计出现次数
Collections.emptyList();           // 空集合（比返回 null 优雅 ⚠️ 防 NPE）
```

**不可变集合**（Java 9+）🔥：

```java
List<String> fixed = List.of("a", "b", "c");       // 不可增删改
Map<String, Integer> m = Map.of("a", 1, "b", 2);   // ≈ JS 的 Object.freeze
fixed.add("d");   // 💥 UnsupportedOperationException
```

## 对比总表

| 需求 | JS | Java |
|------|-----|------|
| 动态数组 | `Array` | `ArrayList`（定长用原生数组） |
| 去重集合 | `Set` | `HashSet`（保序用 LinkedHashSet） |
| 键值对 | `Object` / `Map` | `HashMap` 🔥（保序 LinkedHashMap，排序 TreeMap） |
| 队列/栈 | 数组模拟 | `ArrayDeque`（别用 Stack 类） |
| 遍历 | `for...of` | 增强 for |
| 工具方法 | 数组原型方法 | `Arrays` / `Collections` 工具类 |
| 不可变 | `Object.freeze` | `List.of` / `Map.of` |

## 练习

1. 用 `HashMap<String, Integer>` 统计字符串 `"hello world hello java world"` 中每个单词出现的次数并打印（提示：`split` 切分 + `getOrDefault`）。
2. 有一个 `List<Integer>` 装着 1~10，用两种方式删除其中所有偶数：(a) Iterator；(b) `removeIf`。再试试在增强 for 里直接 `remove`，观察抛出的异常名称。

## 本章总结

- 数组**定长**、类型统一；`Arrays` 工具类辅助；想要动态用 `ArrayList`
- 三个长度别混：数组 `.length`、字符串 `.length()`、集合 `.size()` 💼
- `ArrayList` ≈ JS 数组（默认选择）；`HashSet` 去重；`HashMap` ≈ JS Map/Object，但读写必须 `put/get`
- HashMap 原理一句话：**哈希定位桶 + 链表 + 红黑树**；自定义对象当 key/set 元素要重写 `hashCode` + `equals` 💼
- **遍历中删除用 `Iterator.remove()` 或 `removeIf`**，直接删会炸 ⚠️
- 队列/栈用 `ArrayDeque`（Stack 类是古董）；`Collections` 工具类 + `List.of` 不可变集合是日常好帮手

下一章：[IO 与时间日期](./11-io.md)

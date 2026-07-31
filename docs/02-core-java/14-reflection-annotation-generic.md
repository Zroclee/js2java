# 反射、注解与泛型：框架魔法的三大基石

> 状态：✅ 已完成

## 简介
本章学习内容：泛型类/方法/通配符/类型擦除、注解与元注解、反射操作类结构、三者合体手写迷你 IoC
前置知识：完成「13-concurrency」；TS 泛型与装饰器经验
阅读时长：约 50 分钟
难度：🌟🌟🌟🌟
重要程度：🌟🌟🌟🌟
当前进度：61%

---

## 为什么这三个概念放在一章

因为它们就是 **Spring 这台机器的三块核心零件**：

```
注解（@Component @Autowired）  → 标记意图："我要被管理"、"我要被注入"
反射（运行时读注解、造对象）    → 执行意图：框架在运行时发现标记并行动
泛型（List<T> Optional<T>）    → 让框架代码类型安全地通用于所有类
```

学完这章你会恍然大悟：Spring 不是魔法，是这三者的组装。本章结尾我们就用 30 行代码手写一个迷你版 IoC 验证这一点。

## 一、泛型：给容器贴上类型标签

### 为什么需要它

没有泛型的年代（Java 5 之前），集合什么都能装，取出来全是 `Object`，靠强转续命：

```java
// 远古写法：编译不拦，运行时炸 ⚠️
List list = new ArrayList();
list.add("文本");
list.add(123);                  // 混装成功
String s = (String) list.get(1); // 💥 ClassCastException，运行时才发现
```

JS 数组混装是常态，但 Java 是静态类型世界——**泛型让"这个集合装什么"成为编译期契约**：

```java
List<String> list = new ArrayList<>();
list.add("文本");
// list.add(123);        // ❌ 编译错误，错误在写代码时就被按住 ✅
String s = list.get(0); // 不用强转
```

TS 用户完全熟悉这套（`Array<string>`），迁移层 😌。

### 泛型类与泛型方法

```java
// 泛型类：T 是"类型参数"，使用时才确定
public class Box<T> {
    private T content;
    public void put(T item) { this.content = item; }
    public T get() { return content; }
}

Box<String> stringBox = new Box<>();   // T = String
Box<Integer> intBox = new Box<>();     // T = Integer

// 泛型方法：类型参数写在返回值前面 ⚠️ 语法特殊
public static <T> T first(List<T> list) {
    return list.get(0);
}
String s = first(List.of("a", "b"));   // 编译器自动推断 T = String
```

### 通配符：extends 与 super 💼

```java
List<Integer> ints = List.of(1, 2, 3);
// List<Number> numbers = ints;      // ❌ 编译错误！泛型不继承（List<Integer> 不是 List<Number> 的子类）⚠️

List<? extends Number> numbers = ints;   // ✅ "Number 及其子类" → 能读不能写
// numbers.add(1);                      // ❌ 编译错误（不知道具体是哪个子类）
Number n = numbers.get(0);               // ✅ 读出来一定是 Number

List<? super Integer> nums2 = new ArrayList<Number>();
nums2.add(1);                            // ✅ "Integer 及其父类" → 能写不能精确读
```

记忆口诀 **PECS**：**P**roducer **E**xtends **C**onsumer **S**uper——从集合里**读**用 extends，往集合里**写**用 super。看到 `List<? extends X>` 发懵时念一遍。

### 类型擦除：泛型只活在编译期 ⚠️💼

```java
List<String> a = new ArrayList<>();
List<Integer> b = new ArrayList<>();
System.out.println(a.getClass() == b.getClass());   // true！运行时都是 ArrayList
```

编译完成后，`<String>` `<Integer>` 被**擦除**——运行时它们就是同一个 ArrayList。TS 泛型也擦除，但 Java 的擦除有更实际的后遗症：

- 运行时拿不到泛型参数（`new T()` ❌、`T.class` ❌）
- 框架要拿泛型信息得走特殊手段（Spring 的 `ResolvableType`，了解即可）

**面试必考**：泛型是编译期检查工具，不是运行时特性。

## 二、注解：代码上的"标签"

### 是什么

注解（Annotation）是**贴在代码元素上的元数据标签**——本身不改变任何逻辑，等着被编译器或框架**读取后产生行为**。你已经见过三个内置注解：

```java
@Override            // 编译器读：检查是否真重写（继承章）
@Deprecated          // 编译器/IDE 读：标记过时，画删除线
@SuppressWarnings("unchecked")  // 编译器读：别警告我
```

### 自定义注解与元注解

```java
import java.lang.annotation.*;

@Target(ElementType.METHOD)              // 元注解 1：能贴在哪（方法/类/字段...）
@Retention(RetentionPolicy.RUNTIME)      // 元注解 2：活多久 ⚠️ 关键！
public @interface MyLog {
    String value() default "";           // 注解可以带参数
}
```

`@Retention` 三档决定注解的寿命 💼：

| 级别 | 活到什么时候 | 用途 |
|------|-------------|------|
| `SOURCE` | 编译后丢弃 | 仅给编译器看（@Override） |
| `CLASS` | 进字节码，运行时不加载（默认） | 字节码工具 |
| `RUNTIME` | **运行时还在，可被反射读取** | **框架注解全在这档** 🔥 |

### 和 TS 装饰器的关键区别 ⚠️

```typescript
// TS 装饰器：本质是【函数调用】，能直接修改被装饰的东西
function Log(target, key, descriptor) {
  const original = descriptor.value;
  descriptor.value = (...args) => { console.log('调用前'); original(...args); };
}
```

```java
// Java 注解：纯标签，自己【什么都不做】
@MyLog("查询用户")    // 贴在这里毫无效果——直到有代码用反射读它并行动
public List<User> findUsers() { ... }
```

**TS 装饰器自带行为，Java 注解的行为由"读它的人"赋予**——这个"读的人"就是反射。下一节合体。

## 三、反射：运行期解剖类的能力 🆕

### 为什么 Java 需要"反射"这个重型 API

JS 里运行时操作对象结构是家常便饭：`obj[key]`、`Object.keys(obj)`、动态调方法 `obj[methodName]()`。

Java 的类是**封闭的静态结构**——编译后字段方法都固定了，想用"字符串名"去访问一个方法，必须走反射（Reflection）API：**在运行期检查类结构、创建对象、调用方法、读写字段**。

### 获取 Class 对象（一切的入口）

```java
// 三种方式拿到类的"解剖图"
Class<User> c1 = User.class;                          // 类名.class
Class<?> c2 = user.getClass();                        // 对象.getClass()
Class<?> c3 = Class.forName("com.example.User");      // 全限定名字符串 🔥 框架最爱
```

### 四大常用操作

```java
Class<?> clazz = Class.forName("com.example.User");

// 1. 创建对象（≈ new，但用字符串驱动的类名）
Object user = clazz.getDeclaredConstructor().newInstance();

// 2. 拿方法并调用（≈ obj[methodName]()）
Method method = clazz.getMethod("setName", String.class);
method.invoke(user, "小明");                 // 等价于 user.setName("小明")

// 3. 读写字段（private 也能强攻 ⚠️）
Field field = clazz.getDeclaredField("name");
field.setAccessible(true);                   // 打开 private 的访问权限 💥 框架日常
field.set(user, "强制改名");

// 4. 读注解（框架魔法的引爆点 🔥）
if (method.isAnnotationPresent(MyLog.class)) {
    MyLog log = method.getAnnotation(MyLog.class);
    System.out.println("发现注解: " + log.value());
}
```

⚠️ 反射的代价：绕过编译期检查（错了运行时才炸）、性能比直接调用慢、破坏封装。**业务代码少用，框架代码狂用**——理解它的最佳视角就是"这是写给框架看的 API"。

## 四、合体：30 行手写迷你 IoC 🆕🔥

把三块零件装起来，模拟 Spring 的核心动作——**扫描带注解的类 → 反射创建实例 → 放进容器管理**：

```java
// 第 1 步：自定义注解（标记"我要被容器管理"）
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)        // 必须 RUNTIME，否则反射读不到
@interface Component { }

// 第 2 步：一个被标记的类
@Component
class UserService {
    public String hello() { return "Hello from UserService"; }
}

// 第 3 步：迷你容器（泛型 + 反射）
class MiniContainer {
    private final Map<String, Object> beans = new HashMap<>();   // 装实例的容器

    public void register(Class<?> clazz) throws Exception {
        if (clazz.isAnnotationPresent(Component.class)) {        // 反射读注解
            Object instance = clazz.getDeclaredConstructor().newInstance();  // 反射造对象
            beans.put(clazz.getSimpleName(), instance);          // 放进 Map 管理
            System.out.println("已装配: " + clazz.getSimpleName());
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type) {                        // 泛型方法：返回精确类型
        return (T) beans.get(type.getSimpleName());              // 调用方不用强转
    }
}

// 第 4 步：运行
public static void main(String[] args) throws Exception {
    MiniContainer container = new MiniContainer();
    container.register(UserService.class);          // 已装配: UserService

    UserService service = container.getBean(UserService.class);  // 不用强转 ✅
    System.out.println(service.hello());            // Hello from UserService
}
```

运行它，你就亲手复刻了 Spring 的核心循环：

```
@Component 注解标记   →   反射扫描并 newInstance   →   Map 容器统一管理   →   getBean 按类型取用
（Spring 的 @Component）（Spring 的 BeanFactory）  （Spring 的 IoC 容器）   （Spring 的依赖注入）
```

**Spring 的真相**：在你启动类上做全包扫描，把所有带 `@Component/@Service/@RestController` 的类用反射实例化，放进一个叫 ApplicationContext 的大 Map，谁需要就注入给谁。剩下的 AOP、自动配置都是这套机制的扩展——框架章我们会亲眼看到它。

## 对比理解

| 概念 | TS / JS | Java |
|------|---------|------|
| 泛型 | TS 泛型，编译后消失 😌 | 语法类似，同样擦除，但影响反射行为 ⚠️ |
| 装饰器/注解 | TS decorator = **函数调用，自带行为** | 注解 = **纯标签，行为靠反射赋予** ⚠️ |
| 动态操作结构 | `obj[key]` 日常 | 必须走反射 API（Class/Method/Field）🆕 |
| 运行时创建对象 | `new cls()` 随时 | `Class.forName().newInstance()` 🔥 |

## 练习

1. 写一个泛型方法 `<T> List<T> filter(List<T> list, T exclude)`：返回去掉指定元素的新列表；分别用 `List<Integer>` 和 `List<String>` 调用验证类型推断。
2. 定义注解 `@RequiresAuth(role = "admin")`（RUNTIME 保留、贴在方法上），写两个方法分别贴与不贴；用反射扫描这个类的所有方法，打印出每个方法"是否需要鉴权、角色是什么"。
3. 扩展本章的迷你容器：增加 `@AutoInject` 注解——注册时如果某字段贴了它，就用反射把对应类型的 bean 注入该字段（`field.setAccessible(true)` + `field.set`）。做完这题，你已经理解了 `@Autowired` 的本质。

## 本章总结

- 泛型 = 编译期类型契约：`Box<T>` 泛型类、`<T> T` 泛型方法、`? extends/super` 通配符（PECS 口诀）；**类型擦除**让它只活在编译期 💼
- 注解 = 纯标签：`@Target` 贴哪、`@Retention(RUNTIME)` 才能被框架读到；与 TS 装饰器的本质区别是"不自带行为" ⚠️
- 反射 = 运行期解剖类：`Class.forName` → `newInstance` → `method.invoke` → `field.setAccessible` 四连招，业务少用框架狂用
- **三者合体 = Spring 的核心机制**：注解标记 + 反射装配 + 泛型容器——迷你 IoC 你已亲手写过 🔥

Java 核心板块收官 🎉 下一站进入实战装备篇：[Docker](../03-database/01-docker.md)

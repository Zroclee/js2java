# 继承与多态：原型链用户需要重建的认知

> 状态：✅ 已完成

## 简介
本章学习内容：extends 继承、原型链 vs 继承链、方法重写（Override）、向上转型、多态
前置知识：完成「01-class-field-method」；熟悉 JS class extends 与原型链
阅读时长：约 30 分钟
难度：🌟🌟🌟
重要程度：🌟🌟🌟🌟🌟
当前进度：13%

---

## 语法外壳：几乎一模一样

```java
// Java
public class Dog extends Animal {
    public Dog(String name) {
        super(name);           // 调用父类构造器，必须在第一行
    }

    @Override                  // 重写父类方法（注解详解见下文）
    public void speak() {
        System.out.println("汪汪");
    }
}
```

```javascript
// JavaScript 对照
class Dog extends Animal {
  constructor(name) {
    super(name);               // 一样：必须先 super 才能用 this
  }

  speak() {                    // 一样：同名方法覆盖父类
    console.log("汪汪");
  }
}
```

`extends`、`super()`、先调 super 才能用 `this`——这些规则两边完全一致。如果只看语法，你会觉得已经学完了。**但外壳之下的机制，是两种完全不同的世界。**

## 核心差异：原型链 vs 继承链

### JS：对象与对象的链条（原型链）

JS 的 class 继承是**语法糖**，底层是原型链。它的本质是**对象与对象之间的链接**：

```
dog 实例 ──__proto__──▶ Dog.prototype ──__proto__──▶ Animal.prototype ──__proto__──▶ Object.prototype ──▶ null
```

`dog.speak()` 时，引擎沿这条链**逐层找对象身上的方法**，找到即停。这条链是**运行时的活结构**——`Object.setPrototypeOf()` 甚至能在运行时改链。

### Java：类与类的静态层级（继承链）

Java 没有原型，继承是**类与类之间的静态关系**，编译期就已固定：

```
Dog 类 ──extends──▶ Animal 类 ──extends──▶ Object 类（所有类的祖先 🆕）
```

关键差异：

| 维度 | JS 原型链 | Java 继承链 |
|------|-----------|-------------|
| 链的参与者 | **对象**链接对象 | **类**链接类 |
| 建立时机 | 运行时（动态） | 编译期（静态）⚠️ |
| 链的顶端 | `Object.prototype` | `Object` 类——所有类隐式继承它 🆕 |
| 可否运行时改链 | 可以（setPrototypeOf） | 不可以 |
| 类型检查 | 无（鸭子类型） | 有——不是父子关系直接编译失败 |

⚠️ 思维转换的关键：在 JS 里"方法在哪"要沿链找；在 Java 里"能调什么方法"首先由**变量的声明类型**决定——编译器只认声明类型上定义的方法。这个差异正是后面理解多态的地基。

### Object 类：Java 的万物之父 🆕

Java 中每个类都隐式 `extends Object`，所以任何对象都有这些方法：

- `toString()`——≈ JS 的 `obj.toString()`，打印对象时自动调用
- `equals()`——判断相等，**下一章数据类型会重点讲它**（先记住：别用 `==` 比对象 ⚠️）
- `hashCode()`——配合哈希表用，集合章节见

## 方法重写（Override）：规则与坑

**是什么**：子类重新定义父类中**签名相同**的方法，覆盖其行为。

### 重写规则（必须同时满足）

```java
public class Animal {
    public void speak() {
        System.out.println("动物叫");
    }
}

public class Dog extends Animal {
    @Override               // ← 建议永远加上
    public void speak() {   // 方法名、参数列表必须与父类完全相同
        System.out.println("汪汪");
    }
}
```

1. **方法签名相同**：方法名 + 参数列表一致（返回值可以相同或是其子类，这叫"协变返回"）
2. **访问权限不能更严格**：父类是 `public`，子类不能改成 `private`（可以不变或放宽）
3. **不能重写的方法**：`final` 方法（禁止重写）、`static` 方法（属于类，不参与）、`private` 方法（子类根本看不见）

### @Override：防手滑神器 ⚠️💼

JS 里你覆盖父类方法，写错了没人管。Java 新手最常犯的事故是：**想重写，结果参数写岔了，变成"重载"**——多态悄悄失效，编译还不报错：

```java
public class Dog extends Animal {
    // 父类是 speak()，这里写成了 speak(String mood)
    public void speak(String mood) {   // 这不是重写，是重载！多态失效 ⚠️
        System.out.println("汪汪 " + mood);
    }
}
```

`@Override` 注解的作用：**告诉编译器"我声明这是重写"，如果不是真重写就编译报错**。它把静默事故变成编译错误——代价一个单词，收益巨大，后端规范要求必加 🔥。

### 重写 vs 重载 💼（面试必考对照）

上一章埋的伏笔，现在兑现：

| 维度 | 重载 Overload | 重写 Override |
|------|---------------|---------------|
| 发生位置 | 同一个类内 | 父子类之间 |
| 方法签名 | 参数列表**必须不同** | 签名**必须相同** |
| 返回值 | 随意 | 相同或子类 |
| 分派时机 | **编译期**（看声明类型） | **运行期**（看实际对象）⚠️ |
| 注解 | 无 | `@Override` |

最后一行是多态的核心，马上展开。

## 多态：父类引用指向子类对象

### 概念与三要素

**多态（Polymorphism）**：用父类类型的变量指向子类对象，调用方法时执行的是**子类的实现**。

```java
Animal a = new Dog();   // 向上转型：声明类型是父类，实际对象是子类
a.speak();              // 输出"汪汪" ← 运行期看 new 的是谁，不是看声明类型
```

多态成立的三要素 💼：**继承 + 重写 + 父类引用指向子类对象**，缺一不可。

### JS 用户怎么看这件事

JS 里你天天享受多态，只是从没叫过这个名字——**鸭子类型**：

```javascript
function makeItSpeak(animal) {
  animal.speak();   // 只要有 speak 方法就能用，管它是什么
}
makeItSpeak(dog); makeItSpeak(cat); makeItSpeak({ speak: () => {} });  // 全都可以
```

JS 的哲学："走起来像鸭子、叫起来像鸭子，它就是鸭子"——**运行时不检查类型，有方法就行**。

Java 是静态类型，想获得同样的灵活性必须走"正规军"路线：**用父类（或接口）类型作为统一抽象**，让不同的子类实现可替换。`Animal a = new Dog()` 就是 Java 版的鸭子类型——只是"鸭子谱系"提前用类型系统登记好了。

### 为什么需要多态

后端高频场景：**同一套流程，不同的实现可插拔**。比如支付：

```java
public class PayService {
    // 声明参数为父类类型：任何支付方式的子类都能传进来 🔥
    public void checkout(Payment payment, int amount) {
        payment.pay(amount);   // 运行期自动执行实际支付方式的实现
    }
}

class Alipay extends Payment { @Override public void pay(int amount) { /* 支付宝逻辑 */ } }
class WechatPay extends Payment { @Override public void pay(int amount) { /* 微信逻辑 */ } }
```

新增一种支付方式 = 新写一个子类，`checkout` 一行不改。前端类比：Vue 的插槽、策略模式、`switch` 换成映射表——思想一致，Java 用类型系统把它制度化了。

### 向下转型与 instanceof ⚠️

```java
Animal a = new Dog();

// a.bark();  ❌ 编译错误！声明类型 Animal 上没有 bark 方法
Dog d = (Dog) a;   // 向下转型：强制转回子类
d.bark();          // ✅

// 但如果 a 实际不是 Dog——运行时 ClassCastException 💥
// 安全姿势（Java 16+ 模式匹配写法）：
if (a instanceof Dog dog) {   // 判断 + 转型一步到位
    dog.bark();
}
```

前端类比：`as` 断言骗过 TS 编译器，运行时可能炸——Java 的强转同样"编译放行，运行爆炸"，所以转之前先 `instanceof`。

### 经典坑：字段不参与多态 ⚠️💼

```java
class Animal { public String name = "动物"; }
class Dog extends Animal { public String name = "狗"; }

Animal a = new Dog();
System.out.println(a.name);    // 输出"动物"！不是"狗"
a.speak();                     // 但方法调用是"汪汪"
```

**多态只对方法有效，字段永远看声明类型**。原因：方法调用是运行期动态分派，字段访问是编译期就定死的。面试常考，实务上直接用 private 字段 + 方法访问就能绕开这个坑。

## 双语言完整对照

```java
// Java
public class Main {
    public static void main(String[] args) {
        Animal[] animals = { new Dog("旺财"), new Cat("咪咪") };
        for (Animal a : animals) {
            a.speak();            // 多态：各自执行自己的实现
        }
    }
}

class Animal {
    protected String name;
    public Animal(String name) { this.name = name; }
    public void speak() { System.out.println(name + " 叫了一声"); }
}

class Dog extends Animal {
    public Dog(String name) { super(name); }
    @Override
    public void speak() { System.out.println(name + "：汪汪"); }
}

class Cat extends Animal {
    public Cat(String name) { super(name); }
    @Override
    public void speak() { System.out.println(name + "：喵喵"); }
}
```

```javascript
// JS 对照：行为一样，但没有类型约束——数组里混什么都可以
class Animal {
  constructor(name) { this.name = name; }
  speak() { console.log(`${this.name} 叫了一声`); }
}
class Dog extends Animal {
  speak() { console.log(`${this.name}：汪汪`); }   // 覆盖父类，无需注解
}
class Cat extends Animal {
  speak() { console.log(`${this.name}：喵喵`); }
}

const animals = [new Dog("旺财"), new Cat("咪咪")];
animals.forEach(a => a.speak());
```

行为一致，差别在**约束**：JS 数组里混进一个 `{ speak() {} }` 的普通对象照样能跑（鸭子类型）；Java 数组声明了 `Animal[]`，编译器保证里面全是 Animal 子孙——灵活换安全，这就是静态类型的交易。

## 练习

1. 设计 `Shape` 父类（含 `area()` 方法）与 `Circle`、`Rectangle` 子类，各自实现面积计算；写一个 `printArea(Shape shape)` 方法体验多态，并给所有重写方法加上 `@Override`。
2. 故意制造一个 bug：把 `Circle` 的 `area()` 方法参数改成 `area(int scale)`（去掉 `@Override`），在 `main` 里用 `Shape s = new Circle(); s.area();` 观察输出——解释为什么没有报错、但结果不是你想要的。

## 本章总结

- `extends`/`super` 语法与 JS 一致；底层完全不同——**JS 原型链是对象间运行时链接，Java 继承链是类间编译期静态关系**
- 所有类隐式继承 `Object`，`toString/equals/hashCode` 人人都有
- 重写规则：签名相同、权限不缩、final/static/private 不能重写；`@Override` 必加，防止"想重写却写成重载"
- 多态三要素：继承 + 重写 + 父类引用指向子类对象；**方法运行期分派，字段看声明类型** ⚠️
- 向下转型先 `instanceof`，强转是"编译放行、运行爆炸"

下一章：[抽象类与接口](./03-abstract-interface.md)

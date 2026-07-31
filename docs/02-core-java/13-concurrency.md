# 线程与并发：从单线程世界跳进共享内存的深水区

> 状态：✅ 已完成

## 简介
本章学习内容：进程与线程、线程创建/状态/中断/守护、线程同步与锁（synchronized / volatile / Lock）、线程池、虚拟线程
前置知识：完成「12-exception」；理解 JS 事件循环与单线程模型
阅读时长：约 60 分钟（本教程最难一章，请预留完整时间）
难度：🌟🌟🌟🌟🌟
重要程度：🌟🌟🌟🌟🌟
当前进度：52%

---

## 世界观重建：这章没有迁移，只有拓荒 🆕

先诚实地接受一件事：**你在 JS 里建立的并发直觉，这里几乎全部作废**。

```
JS 的世界：单线程 + 事件循环
  - 任何时刻只有一行代码在执行
  - "异步"不是并行，是"稍后执行"——setTimeout 的回调也是排队等主线程空闲
  - 你永远不用担心"另一个线程正在改这个变量"——根本没有另一个线程

Java 的世界：多线程 + 共享内存
  - 多个线程在多核 CPU 上【真正同时】执行
  - 它们读写同一块堆内存——同一个对象、同一个变量
  - 力量：真并行，吃满多核      危险：互相踩脚（本章主战场）
```

Web Worker 看似接近，但它**内存隔离**（靠 postMessage 传消息），本质还是"各自为战"；Java 线程是**同一片内存里抢东西**——这是质的区别。JS 里仅有的两个可迁移的小锚点：`Promise ≈ Future`（未来结果的凭证）、`AbortController ≈ interrupt`（协作式取消）。其余全是新大陆。

## 进程与线程：两家餐馆 vs 多个厨师

- **进程（Process）**：操作系统分配资源的最小单位，**各自独立内存**。≈ 启动两个 Node 程序，互不知道对方的变量
- **线程（Thread）**：进程内的执行流，**同进程内所有线程共享内存**。≈ 一家餐馆里的多个厨师，共用同一个厨房（灶台、食材）

类比落地：Chrome 每个标签页是一个进程（一个崩了不影响其他）；Java 程序是一个进程，里面的线程是共用厨房的厨师——**"共享厨房"正是后面一切同步问题的根源**。

## 创建线程：三种方式

```java
// 方式 1：继承 Thread（了解即可，不推荐——任务和线程耦合）
class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println("跑在新线程里");
    }
}
new MyThread().start();

// 方式 2：实现 Runnable（推荐 🔥 任务与线程分离）
Runnable task = () -> System.out.println("任务描述：做什么");
new Thread(task).start();

// 方式 3：实现 Callable（任务有返回值，≈ Promise 的味道 🆕）
Callable<Integer> task2 = () -> {
    Thread.sleep(1000);
    return 42;                       // 能返回结果！
};
FutureTask<Integer> future = new FutureTask<>(task2);
new Thread(future).start();
Integer result = future.get();       // get() 会阻塞等结果（≈ await promise）
```

⚠️💼 **第一个大坑：`start()` 和 `run()` 的区别**

```java
Thread t = new Thread(task);
t.start();   // ✅ 真正开启新线程，task 在新线程里跑
t.run();     // ❌ 只是普通方法调用，在当前线程同步执行——线程根本没开！
```

面试必考。记住：**线程的一切从 `start()` 开始**。

## 线程的状态与常用方法

```
        start()                          结束/异常
 NEW ────────▶ RUNNABLE（就绪/运行中） ────────▶ TERMINATED
                    │
                    ├── sleep(ms) / join() ──▶ TIMED_WAITING（计时等待）
                    ├── wait() ──────────────▶ WAITING（无限等待，了解即可）
                    └── 等锁 ────────────────▶ BLOCKED（阻塞，抢锁失败排队）
```

```java
Thread.sleep(1000);       // 当前线程睡 1 秒（TIMED_WAITING），≈ JS 的 await sleep(1000)
t.join();                 // 当前线程等 t 跑完再继续（主线程等子线程的常用法 🔥）
Thread.currentThread();   // 拿到当前线程对象
t.getState();             // 查看线程状态（Debug 排查用）
```

## 中断 interrupt：Java 的 AbortController

老式 `stop()` 方法被废弃了——它暴力杀线程，锁不释放、数据写到一半，留下一地烂摊子 ⚠️。Java 的正确姿势是**协作式中断**：发信号，线程自己找安全的地方停。

```java
Thread t = new Thread(() -> {
    while (!Thread.currentThread().isInterrupted()) {   // 循环里检查中断标志 🔥
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            break;        // sleep 时被中断会抛异常，借此退出循环
        }
    }
    System.out.println("线程安全地停下来了");
});
t.start();
Thread.sleep(2000);
t.interrupt();   // 发送中断信号 ≈ AbortController.abort()
```

## 守护线程（Daemon）：仆人线程

```java
Thread t = new Thread(task);
t.setDaemon(true);   // 必须在 start() 之前设置 ⚠️
t.start();
```

规则一句话：**JVM 会等所有"用户线程"结束才退出，但不会等守护线程**。主线程跑完时，守护线程被直接掐死——适合后台服务型工作（JVM 的 GC 线程就是守护线程）。类比：用户线程是餐厅顾客（走完才能打烊），守护线程是背景音乐（顾客走光就关）。

## 线程同步：本章的心脏 💼

### 先看事故现场：丢失更新

```java
public class RaceDemo {
    private static int count = 0;

    public static void main(String[] args) throws InterruptedException {
        Runnable add = () -> {
            for (int i = 0; i < 10000; i++) {
                count++;                 // ⚠️ 危险动作
            }
        };

        Thread t1 = new Thread(add);
        Thread t2 = new Thread(add);
        t1.start(); t2.start();
        t1.join();  t2.join();           // 等两个线程都跑完

        System.out.println(count);       // 期望 20000，实际可能是 17342、18991……每次都不一样 💥
    }
}
```

**为什么会错**：`count++` 看起来一步，实际是三步——

```
读 count 的值 → 加 1 → 写回 count
```

两个线程交错执行：都读到 100，各自加 1，都写回 101——**一次加法丢失了**。这叫**竞态条件（race condition）** 🆕：结果取决于线程调度的运气。JS 单线程永远不会有这个问题；Java 里它无处不在：库存超卖、余额错算、计数丢失——后端事故的常客 🔥。

### 并发三特性（记住名词即可 💼）

| 特性 | 含义 | 上面的例子 |
|------|------|-----------|
| **原子性** | 操作不可分割，要么全做要么不做 | count++ 不是原子的 💥 |
| **可见性** | 一个线程改了，其他线程立刻看得到 | CPU 缓存可能让别的线程看旧值 |
| **有序性** | 指令不被重排 | JVM 优化可能打乱顺序（进阶，先挂号） |

### synchronized：互斥锁（最重要的一节）

**思路**：给危险代码配一把锁——同一时刻只允许一个线程进入，其他排队。

```java
public class SafeCounter {
    private int count = 0;
    private final Object lock = new Object();   // 锁对象（任何对象都能当锁）

    public void add() {
        synchronized (lock) {      // 进入括号 = 抢到锁；离开括号 = 释放锁
            count++;               // 同一时刻只有一个线程能执行这里 ✅
        }
    }
}
```

`add()` 方法改成这样后，上面的演示稳定输出 20000。

锁的三种用法（知道前两种就够）：

```java
// 1. 同步代码块（上面）：锁任意指定对象，粒度细 🔥
// 2. 同步实例方法：锁的是 this
public synchronized void add() { count++; }

// 3. 同步静态方法：锁的是"类"（呼应 static 章：类也有独立空间）
public static synchronized void reset() { count = 0; }
```

两个关键认知 💼：

- **锁的是对象不是代码**：两个线程抢的是"同一个锁对象"。你用 `lock1` 我用 `lock2`，各玩各的锁，等于没锁 ⚠️
- **synchronized 可重入**：同一线程拿到锁后再进另一个 synchronized 方法不会把自己锁死（JS 用户可忽略，提一声防源码发懵）

### volatile：轻量的可见性保证 ⚠️💼

```java
private volatile boolean running = true;   // 一个线程改，其他线程立刻可见
```

它只解决**可见性**（禁用 CPU 缓存优化），**不解决原子性**——`volatile int count` 做 `count++` 照样丢更新 ⚠️。适用场景：状态开关标志。别拿它当锁用。

### Lock：手动挡的锁 🔥

```java
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

private final Lock lock = new ReentrantLock();

public void add() {
    lock.lock();                    // 手动加锁
    try {
        count++;
    } finally {
        lock.unlock();              // ⚠️ 必须在 finally 里解锁，否则异常时锁永远不放——死锁温床
    }
}
```

`synchronized` vs `Lock` 💼：

| | synchronized | ReentrantLock |
|---|---|---|
| 加解锁 | 自动（进/出代码块） | 手动（必须 finally unlock）⚠️ |
| 尝试加锁 | 不行，抢不到就死等 | `tryLock()` 抢不到可以放弃 🔥 |
| 公平性 | 非公平 | 可选公平锁 |
| 选型 | **默认首选**（简单不易错） | 需要 tryLock/定时等待时 |

### 死锁：互相等待的僵局 ⚠️

```java
// 线程 A：锁 1 → 想要锁 2
// 线程 B：锁 2 → 想要锁 1
// 结果：都拿着对方想要的东西，永远等下去 💀
```

防死锁经验法则：**多个锁按固定顺序获取**、用 `tryLock` 带超时。面试必考概念，工作中遇到就是排查一下午 🔥。

### 开箱即用的线程安全工具 🔥

不用每次都自己加锁，JUC（java.util.concurrent）包备好了：

```java
AtomicInteger count = new AtomicInteger(0);
count.incrementAndGet();            // 原子自增，无锁（底层 CAS）——比 synchronized 轻量 🔥

ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
// 分段锁的 HashMap，多线程放心 put/get 🔥（普通 HashMap 多线程下会出灵异事件 ⚠️）
```

`wait()` / `notify()`（线程间"等通知"协作）：了解概念即可，初级实战用得少，面试再问。

## 线程池：复用线程的池子 🔥

**为什么需要**：创建线程很贵（要向操作系统申请资源），高并发下"来一个请求 new 一个线程"会瞬间耗尽内存。线程池 = **预先创建一批线程反复复用**（≈ 数据库连接池、Node 的 worker 池）。

```java
import java.util.concurrent.*;

// 创建（演示用工厂方法，生产见下方提醒）
ExecutorService pool = Executors.newFixedThreadPool(4);   // 固定 4 个线程

// 提交任务
pool.execute(() -> System.out.println("无返回值的 Runnable"));

Future<Integer> future = pool.submit(() -> {              // submit 可拿 Future
    Thread.sleep(1000);
    return 42;
});
Integer r = future.get();          // 阻塞等结果 ≈ await

pool.shutdown();                   // 优雅关闭：不再接新任务，跑完存量
```

### ThreadPoolExecutor 七大参数 💼（面试必考）

```java
new ThreadPoolExecutor(
    4,                    // 核心线程数：常驻线程
    8,                    // 最大线程数：忙不过来时最多扩到
    60L, TimeUnit.SECONDS,// 临时工空闲多久辞退
    new LinkedBlockingQueue<>(100),  // 任务排队队列
    Executors.defaultThreadFactory(),// 线程工厂（起名用）
    new ThreadPoolExecutor.AbortPolicy()  // 拒绝策略：队列也满了怎么办
);
```

执行流程：**核心线程干活 → 满了进队列排队 → 队列也满了加临时工 → 临时工也满了执行拒绝策略**。拒绝策略四种，默认 `AbortPolicy`（抛异常），常用 `CallerRunsPolicy`（让提交者自己跑，天然限流）💡。

⚠️ **生产环境提醒**：`Executors.newFixedThreadPool` 这类工厂方法用的队列**无界**（任务可无限堆积→内存溢出），阿里开发规范要求手动 `new ThreadPoolExecutor` 显式配置。学习阶段用工厂方法无妨 💡。

## 虚拟线程：Java 21 的游戏规则改变者 🆕🔥

传统线程（平台线程）是**操作系统线程的 1:1 包装**——一个就要 1MB 栈内存，几千个就是极限。高并发场景（十万连接）扛不住，这是 Java 多年心病（Node 单线程异步反而在这点上很优雅）。

Java 21 的**虚拟线程（Virtual Thread）**：由 **JVM 自己调度**的轻量线程，一个平台线程可以驮着成千上万个虚拟线程跑——≈ Go 语言的 goroutine 🆕。

```java
// 创建一个虚拟线程，API 和普通线程几乎一样 😌
Thread vThread = Thread.ofVirtual().start(() -> {
    System.out.println("跑在虚拟线程里");
});

// 或者：每个任务一个虚拟线程的执行器（推荐姿势 🔥）
try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 100_000; i++) {
        executor.submit(() -> {
            Thread.sleep(1000);      // 十万个"睡一秒"的任务也能轻松跑完
            return 1;
        });
    }
}
```

**百万线程成为可能**，写法还是朴素的同步代码——不用回调、不用响应式编程。Spring Boot 3.2+ 一行配置开启（框架章见）。

选型一句话：**IO 密集型（等数据库、等网络）用虚拟线程爽翻；CPU 密集型（算圆周率）老线程池照旧**——虚拟线程的优势在"等待的时候让出平台线程"，纯计算没有等待可让 💼。

## 对比理解：JS vs Java 并发观

| 维度 | JS | Java |
|------|-----|------|
| 并发模型 | 单线程 + 事件循环（假并行） | 多线程共享内存（真并行）🆕 |
| 内存 | 各上下文隔离 | 线程共享堆内存——危险与力量同源 |
| 异步凭证 | Promise | Future（`get()` 会阻塞 ⚠️） |
| 取消 | AbortController | interrupt（协作式） |
| 同时改一个变量 | 不可能发生 | 竞态条件，必须同步 💥 |
| 互斥 | 不需要 | synchronized / Lock 🆕 |
| 轻量并发 | 事件循环天然轻量 | 虚拟线程（Java 21 追上来了） |

## 练习

1. **亲手制造事故**：运行本章的 `RaceDemo` 三次，记录 count 结果；然后用 `synchronized` 修复它，再运行三次验证恒为 20000。最后换成 `AtomicInteger` 再实现一遍。
2. **中断与守护**：写一个线程每秒打印一次心跳；主线程 5 秒后 `interrupt()` 它——观察它是如何"体面地"停下的。再把它设为守护线程，观察主线程结束后它的命运。
3. **线程池实操**：用 `ThreadPoolExecutor`（核心 2、最大 4、队列容量 2、拒绝策略 CallerRunsPolicy）提交 10 个耗时 1 秒的任务，打印每个任务被哪个线程执行——观察"核心→队列→临时工→拒绝"的完整流程。

## 本章总结

- 进程 = 独立内存的两个餐馆；线程 = 共享厨房的多个厨师——**共享内存是 Java 并发一切问题的根源**
- 创建线程首选 `Runnable`/`Callable`；`start()` 才开线程，`run()` 只是普通调用 💼；`interrupt` 协作式停止 ≈ AbortController
- **竞态条件**来自非原子操作交错执行；`synchronized` 互斥（锁对象、可重入）、`volatile` 只保可见性不保原子性 ⚠️、`ReentrantLock` 手动挡必须 finally 解锁
- 死锁 = 互相持锁等待，固定顺序 + tryLock 预防
- 线程池复用线程，七大参数与"核心→队列→临时工→拒绝"流程 💼；生产环境手动配置队列上限
- 虚拟线程（Java 21）= JVM 调度的轻量线程，IO 高并发的银弹，Spring Boot 3.2+ 可用 🆕🔥

下一章：[反射、注解与泛型](./14-reflection-annotation-generic.md)——Java 核心板块的收官篇

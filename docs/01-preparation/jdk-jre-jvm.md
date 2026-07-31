# JDK、JRE 与 JVM：Java 的运行世界

> 状态：✅ 已完成

## 简介
本章学习内容：JDK / JRE / JVM 三者关系，Java 版本选择，macOS 与 Windows 下的安装和环境变量配置
前置知识：会基本命令行操作（日常用 npm 的经验足够）
阅读时长：约 20 分钟
难度：🌟
重要程度：🌟🌟🌟🌟
当前进度：3%

---

## 从一个你熟悉的场景说起

前端执行一段 JS：

```bash
node hello.js
```

`node` 命令背后是 V8 引擎：读入源码 → 解析 → 直接执行。源码就是运行的全部。

Java 不一样，它有**两道工序**：

```bash
javac HelloWorld.java   # 第一步：编译，源码 .java → 字节码 .class
java HelloWorld         # 第二步：运行，JVM 加载 .class 执行
```

多出来的这道"编译成字节码"的工序，就是理解 JVM / JRE / JDK 的钥匙。

## JVM：Java 虚拟机

**是什么**：JVM（Java Virtual Machine）是运行 Java 字节码（`.class` 文件）的虚拟计算机。它不认识 `.java` 源码，只认识字节码。

**为什么需要它**：为了那句著名的口号——**Write Once, Run Anywhere（一次编译，到处运行）**。

前端做跨平台靠"浏览器都实现了同一套 Web 标准"；Java 的思路相反：**每个操作系统各自实现一个 JVM，字节码是统一格式**，同一份 `.class` 扔给 Windows、macOS、Linux 的 JVM 都能跑。平台差异被 JVM 这一层吃掉了。

除了跨平台，JVM 还包办了两件大事：

- **内存管理（GC 垃圾回收）**：JS 里你从没手动释放过内存，V8 有 GC——JVM 也一样，而且 GC 调优是后端的高频课题 🔥
- **JIT 即时编译**：热点代码在运行时被编译成机器码，越跑越快。V8 也有 JIT，思想同源

**类比**：JVM ≈ V8 引擎。但注意一个区别——V8 藏在 Node 里面你几乎感知不到；JVM 在 Java 世界里是显性的存在，启动参数、内存配置、性能调优都绕不开它 💼

## JRE：Java 运行时环境

**是什么**：JRE（Java Runtime Environment）= **JVM + 运行所需的核心类库**（`java.lang`、`java.util` 这些标准库）。

**类比**：约等于你装的 Node.js 本体——能跑程序，但没有开发工具。

**解决什么问题**：如果一台机器只是**运行** Java 程序（比如服务器上跑打包好的应用），装 JRE 就够了，不需要编译器。

> ⚠️ **注意**：从 Java 9 起，Oracle 不再提供独立的 JRE 下载，JDK 内嵌了运行时（可用 `jlink` 裁剪出最小运行时）。所以你现在去官网找"JRE 下载"是找不到的——**直接装 JDK 就行**，它包含一切。

## JDK：Java 开发工具包

**是什么**：JDK（Java Development Kit）= **JRE + 开发工具**。开发工具里最核心的就是编译器 `javac`，此外还有：

| 工具 | 作用 | 前端类比 |
|------|------|----------|
| `javac` | 编译器：`.java` → `.class` | tsc / esbuild |
| `java` | 启动 JVM 运行字节码 | node |
| `jar` | 打包字节码为 `.jar` 压缩包 | 打 dist 包 |
| `javadoc` | 从注释生成文档 | JSDoc |
| `jdb` | 调试器 | node --inspect |

**一句话总结三者关系**（💼 面试必考）：

```
JDK ⊃ JRE ⊃ JVM

JDK = JRE + 编译器等开发工具
JRE = JVM + 核心类库
JVM = 执行字节码的虚拟机
```

## 对比理解：Java 世界 vs Node.js 世界

| 概念 | Node.js 世界 | Java 世界 |
|------|-------------|-----------|
| 源码 | `.js` | `.java` |
| 运行单位 | 源码本身（V8 直接解释） | 字节码 `.class`（🆕 JS 没有这一层） |
| 引擎 | V8 | JVM |
| 运行时 | Node.js（V8 + 内置模块） | JRE（JVM + 核心类库） |
| 开发工具包 | Node + npm + tsc 等散件 | JDK（官方全家桶） |
| 编译 | 可选（TS → JS，工程化行为） | **必经**（javac 是语言流程的一部分）⚠️ |
| 跨平台 | 各平台有对应的 Node 二进制 | 各平台有对应的 JVM，字节码通用 |

#### 前端开发者迁移理解

你可以把整条链路这样映射：`javac` 就是 `tsc`，`.class` 就是编译产物，`java` 就是 `node`，JVM 就是 V8。最大的认知差异是：**TS 编译是工程选择，Java 编译是语言强制流程**——没有"直接运行 .java"这回事（Java 11 起 `java HelloWorld.java` 支持单文件源码直接跑，但底层仍然是先编译）。

## 选哪个版本？

Java 版本迭代很快（半年一个版本），但**只关注 LTS（长期支持）版**：8、11、17、21。

- **学习推荐 JDK 17 或 21**：语法现代，企业主流正在从 8 向 17 迁移
- 💼 面试和工作中你会大量见到 JDK 8——老项目存量巨大
- ⚠️ 版本号陷阱：老资料里的 "Java 1.8" 就是 "Java 8"，9 之后取消了 `1.x` 命名

发行版选择：**Oracle JDK 商用收费**，日常使用选免费开源的 **OpenJDK 发行版**即可，推荐 [Eclipse Temurin（原 AdoptOpenJDK）](https://adoptium.net/)——社区主流，稳。

---

## macOS 安装与配置

### 方式一：Homebrew（推荐）

```bash
# 安装 Temurin JDK 21（LTS）
brew install --cask temurin@21

# 如需 JDK 17
brew install --cask temurin@17
```

安装后系统可能提示需要创建一个软链让 `java_home` 检测到，按 brew 输出的提示执行即可（通常是一条 `sudo ln -sfn ...` 命令）。

### 方式二：官网安装包

到 [adoptium.net](https://adoptium.net/) 下载 macOS 版 `.pkg`（注意区分 Apple Silicon / Intel），双击安装。

### 配置 JAVA_HOME

macOS 自带一个专用工具 `/usr/libexec/java_home`，用来定位 JDK 安装路径：

```bash
# 列出所有已安装的 JDK 及路径
/usr/libexec/java_home -V

# 输出示例：
# Matching Java Virtual Machines (2):
#     21.0.5 (arm64) "Eclipse Temurin" - "OpenJDK 21.0.5" /Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
#     17.0.13 (arm64) "Eclipse Temurin" - "OpenJDK 17.0.13" /Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
```

编辑 `~/.zshrc`（macOS 默认 shell 是 zsh）：

```bash
# JAVA_HOME：让 java_home 工具动态返回路径，多版本共存时可指定版本
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

# 把 JDK 的 bin 目录加入 PATH
export PATH="$JAVA_HOME/bin:$PATH"
```

生效并验证：

```bash
source ~/.zshrc

java -version    # 显示 openjdk version "21.0.x" ...
javac -version   # 显示 javac 21.0.x
echo $JAVA_HOME  # 显示 /Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
```

`javac -version` 也要试——只有 `java` 能跑不代表装了 JDK（可能是纯 JRE 环境）⚠️

### 为什么需要 JAVA_HOME 这个变量？

🔥 这不是 Java 本身的要求，而是**生态约定**：Maven、Tomcat、Spring Boot 启动脚本等大量 Java 工具都会读 `JAVA_HOME` 来定位 JDK，而不是依赖 PATH。类比前端某些工具读 `NODE_PATH`。不配它，后面 Maven 章节会直接报错。

## Windows 安装与配置

### 下载安装

1. 到 [adoptium.net](https://adoptium.net/) 下载 Windows 版 `.msi` 安装包（选 LTS 21 或 17）
2. 双击安装，**安装向导中勾选这两个选项**（省事的关键）：
   - ✅ `Set JAVA_HOME variable`——自动写入 JAVA_HOME 环境变量
   - ✅ `Add to PATH`——自动把 bin 目录加入 Path
3. 默认安装路径类似：`C:\Program Files\Eclipse Adoptium\jdk-21.0.x-hotspot\`

### 手动配置环境变量（安装时没勾选的话）

1. `Win + S` 搜索「编辑系统环境变量」→ 打开 → 点「环境变量」
2. 在「系统变量」中**新建**：
   - 变量名：`JAVA_HOME`
   - 变量值：`C:\Program Files\Eclipse Adoptium\jdk-21.0.x-hotspot`（你的实际安装路径，注意是 JDK 根目录，**不是 bin 目录** ⚠️）
3. 找到系统变量中的 `Path` → 编辑 → **新建**一条：
   - `%JAVA_HOME%\bin`
4. 一路确定保存

> 🆕 老教程还会让你配 `CLASSPATH`——那是 Java 1.x 时代的遗留，现代 Java **不需要配**，配错反而引发诡异的类找不到问题。

### 验证

**新开一个**终端（PowerShell 或 CMD，必须新开，旧窗口不加载新环境变量 ⚠️）：

```powershell
java -version    # 显示 openjdk version "21.0.x" ...
javac -version   # 显示 javac 21.0.x
echo %JAVA_HOME% # CMD；PowerShell 用 $env:JAVA_HOME
```

---

## 第一个程序：亲手跑一遍两道工序

概念讲完了，用 5 分钟验证整条链路。任意目录新建 `HelloWorld.java`：

```java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, JVM!");
    }
}
```

```bash
javac HelloWorld.java   # 编译，目录下会多出一个 HelloWorld.class（字节码）
java HelloWorld         # 运行（注意：不带 .class 后缀 ⚠️）
# 输出：Hello, JVM!
```

和 `node hello.js` 对比一下体验：node 一步到位，Java 先出 `.class` 再运行——你现在亲眼看到了 JVM 真正消费的东西是什么。

> 💡 也可以试试 Java 11+ 的快捷方式：`java HelloWorld.java`（直接给源码文件），一步跑完。适合学习阶段跑小文件，底层仍是先编译到内存。

## 常见问题

| 问题 | 原因与解决 |
|------|-----------|
| `'java' 不是内部或外部命令` / `command not found: java` | PATH 没配上。Windows 检查是否新开了终端；macOS 检查 `~/.zshrc` 是否 source |
| `java` 能用但 `javac` 找不到 | 装的是 JRE 而非 JDK，重新安装 JDK |
| 装了多个 JDK，默认版本不对 | macOS：修改 `-v` 参数指定版本；Windows：把目标版本的 Path 条目**上移**（Windows 按顺序匹配）⚠️ |
| Maven / IDE 报 JDK 相关错误但命令行正常 | 它们读的是 `JAVA_HOME`，检查该变量 |

## 练习

1. 用 `/usr/libexec/java_home -V`（macOS）或查看环境变量（Windows）确认你机器上所有 JDK 版本，并口述 JDK / JRE / JVM 的包含关系——能讲清楚才算过。
2. 写一个 `PrintArgs.java`，在 `main` 方法中遍历打印 `args` 数组，用 `javac` 编译后分别以「不带参数」和「带 3 个参数」两种方式运行，观察结果差异。（提示：JS 里对应物是 `process.argv`）

## 本章总结

- **JVM** 是执行字节码的虚拟机（≈ V8），负责跨平台、GC、JIT
- **JRE** = JVM + 核心类库，只够"跑"；**JDK** = JRE + 编译器等工具，开发必装
- Java 运行是**两道工序**：`javac` 编译出 `.class` 字节码，`java` 交给 JVM 执行——这是与 `node xx.js` 最本质的区别
- 版本选 LTS（17/21），发行版选 Temurin；环境变量关键是 `JAVA_HOME` + `PATH`，`CLASSPATH` 不需要配

下一章：[IDEA：Java 开发的主战场](./idea-debug-maven.md)

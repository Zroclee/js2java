# IDEA：Java 开发的主战场

> 状态：✅ 已完成

## 简介
本章学习内容：IDEA 安装与版本选择、从 WebStorm/VS Code 迁移经验、常用快捷键、Debug、创建 Maven 项目与管理依赖
前置知识：完成上一章 JDK 安装配置；有 WebStorm 或 VS Code 使用经验
阅读时长：约 25 分钟
难度：🌟
重要程度：🌟🌟🌟🌟🌟
当前进度：6%

---

## 为什么是 IDEA，而不是继续用 VS Code？

先说结论：**Java 开发不是"编辑器 + 插件"能愉快胜任的场景**。

VS Code 的哲学是"编辑器内核 + 插件拼装"，前端用它很舒服，因为 JS 是动态语言，编辑器能帮的有限。Java 是强类型语言，IDE 可以精确分析每个变量的类型、每个方法的调用链——重构、跳转、代码生成、智能提示的威力完全不是一个量级。**IDEA 把这套能力全部内置**，开箱即用，不用装一堆插件再祈祷它们配合良好。

还有一个更直接的理由：后端 Java 面试和工作中，IDEA 是绝对主流 🔥，团队协作时别人分享的配置、截图、排查路径全是 IDEA。

## 好消息：你的经验可以平移

**IDEA 和 WebStorm 是亲兄弟**——同为 JetBrains 出品，共用同一个 IDE 平台。事实上 WebStorm 就是"IDEA + 前端插件集"的裁剪版。如果你用过 WebStorm：界面布局、设置入口、快捷键、插件市场**几乎完全一致**，这一章可以快速扫过。

VS Code 用户需要迁移的就几个关键心智：

| 你的习惯（VS Code） | IDEA 对应 | 备注 |
|---------------------|-----------|------|
| `Cmd/Ctrl + Shift + P` 命令面板 | `Cmd/Ctrl + Shift + A`（Find Action） | 找不到的功能都从这搜 ⚠️ 最该记住的一个 |
| `Cmd + P` 快速打开文件 | 双击 `Shift`（Search Everywhere） | 功能更强：文件/类/符号/动作一起搜 |
| 装插件获得能力 | 大部分能力内置 | 代码分析、重构、Git、数据库工具开箱即有 |
| 打开即用 | **首次打开项目要索引（Indexing）** | ⚠️ 索引时提示"不可用"是正常的，等进度条走完 |
| 文件夹即项目 | Project / Module 两级结构 | 见下文 |

## 安装与版本选择

IDEA 分两个版本：

| 版本 | 费用 | 够不够学 |
|------|------|----------|
| **Community（社区版）** | 免费 | Java 核心、Maven、Git、Debug 全都有——学到框架前完全够用 |
| **Ultimate（旗舰版）** | 付费（30 天试用，学生免费） | 多了 Spring Boot 支持、数据库工具等——**学到 Spring Boot 章节时建议上** 🔥 |

安装方式（macOS）：

```bash
# 社区版
brew install --cask intellij-idea-ce

# 旗舰版
brew install --cask intellij-idea
```

Windows 或想统一管理多版本：官网下载安装包，或用官方的 **JetBrains Toolbox** App 管理（类比前端用 nvm 管理 Node）。

## 第一个项目：认识 Project、Module 与标准目录

`File → New → Project`，左侧选 **Java**（或 Maven），填写：

- **Name**：项目名，如 `hello-java`
- **Build system**：选 **Maven**（后面详说）
- **JDK**：选第一章装好的 JDK 21（如果没有，点 Add JDK 指到安装路径）

⚠️ 需要重建的前端心智：**IDEA 里 Project ≠ 单个代码包**。

| IDEA | 前端类比 |
|------|----------|
| Project | 一个工作区（workspace） |
| Module | 工作区里的一个包（package） | 

学习阶段一个 Project 一个 Module 即可，知道有这层概念，后面看 RuoYi 多模块项目时不懵。

创建后的目录结构是 Maven 标准布局（后端世界几十年的约定 🔥）：

```
hello-java/
├── pom.xml                  # ≈ package.json（核心！）
└── src/
    ├── main/
    │   ├── java/            # 源代码（.java 放这里）
    │   └── resources/       # 配置文件、静态资源
    └── test/
        └── java/            # 测试代码
```

## 常用快捷键（每天真正会按的那些）

WebStorm 用户基本不用重学；VS Code 用户重点记右列无对应或逻辑不同的。macOS / Windows 双列：

| 功能 | macOS | Windows | 说明 |
|------|-------|---------|------|
| 万能搜索 | `Shift Shift` | `Shift Shift` | 找文件/类/符号/动作 🔥 使用频率最高 |
| 查找动作 | `Cmd + Shift + A` | `Ctrl + Shift + A` | ≈ VS Code 命令面板 |
| 全局文本搜索 | `Cmd + Shift + F` | `Ctrl + Shift + F` | 同 VS Code |
| 跳转定义 | `Cmd + B` | `Ctrl + B` | ≈ F12；Java 里跳得极准 |
| 返回上次位置 | `Cmd + [` | `Ctrl + Alt + ←` | 跳完能回来 |
| 最近文件 | `Cmd + E` | `Ctrl + E` | |
| 重命名重构 | `Shift + F6` | `Shift + F6` | 🔥 全项目安全改名，远超查找替换 |
| 生成代码 | `Cmd + N` | `Alt + Insert` | 🔥 自动生成构造器/getter/setter，Java 必备 |
| 重写父类方法 | `Ctrl + O` | `Ctrl + O` | 学 OOP 章节天天用 |
| 智能补全/修复 | `Alt + Enter` | `Alt + Enter` | 🔥 报错处按它，自动导包/建方法/修类型 |
| 格式化代码 | `Cmd + Alt + L` | `Ctrl + Alt + L` | ≈ Prettier |
| 注释/取消注释 | `Cmd + /` | `Ctrl + /` | 同 VS Code |
| 查看参数提示 | `Cmd + P` | `Ctrl + P` | 方法有哪些重载时好用 |
| 运行 | `Ctrl + R` | `Shift + F10` | |
| Debug | `Ctrl + D` | `Shift + F9` | |

不需要全背。**先记三个：`Shift Shift`、`Alt + Enter`、`Cmd + N`**，剩下的在用中自然记住。

## Debug：比 VS Code 更强的调试体验

Debug 的心智模型和 VS Code 完全一致：打断点 → Debug 模式启动 → 程序停在断点 → 单步/查看变量。差异在于 Java 调试器能力更强。

**基本操作**：

| 操作 | macOS | Windows | 说明 |
|------|-------|---------|------|
| 打/取消断点 | 点行号左侧，或 `Cmd + F8` | `Ctrl + F8` | 红点即断点 |
| 启动 Debug | `Ctrl + D` 或点 🐞 图标 | `Shift + F9` | 别点成 ▶️ 运行 ⚠️ |
| Step Over（单步跳过） | `F8` | `F8` | 执行当前行，不进方法 |
| Step Into（单步进入） | `F7` | `F7` | 进入方法内部 |
| Step Out（跳出） | `Shift + F8` | `Shift + F8` | 从方法里跳出来 |
| 继续执行 | `Cmd + Alt + R` | `F9` | 放行到下一个断点 |
| 表达式求值 | `Alt + F8` | `Alt + F8` | 🔥 ≈ VS Code 的 Debug Console |

**三个前端不常用的实用技能**：

1. **Evaluate Expression（`Alt + F8`）**：停在断点时，随手写一段代码立即执行并看结果——改数据、调方法都行，验证想法不用重启 🔥
2. **条件断点**：右键断点 → 填条件（如 `i == 100`），只在条件满足时暂停。循环里抓特定场景的神器
3. **断点不暂停只打印**：右键断点 → 取消勾选 Suspend → 勾选 Log evaluated expression——等于不中断程序的 `console.log` 💡

## Maven 项目与依赖管理

### 先建立类比（30 秒）

| 概念 | npm 世界 | Maven 世界 |
|------|----------|------------|
| 项目描述文件 | `package.json` | `pom.xml` |
| 依赖坐标 | `@scope/name@version` | `groupId : artifactId : version` |
| 包仓库 | npmjs.com | Maven Central（搜索用 mvnrepository.com 🔥） |
| 安装依赖 | `npm install` | 改完 pom.xml 点 **Reload**（或 IDEA 自动） |
| 本地下载位置 | `node_modules`（项目内） | `~/.m2/repository`（全局共享缓存）⚠️ |
| 脚本命令 | npm scripts | Maven 生命周期（clean/package/install） |

⚠️ 一个重要差异：npm 把依赖装进每个项目的 `node_modules`；Maven 把依赖下载到**用户目录下的 `~/.m2`**，所有项目共享同一份缓存。所以 Java 项目里看不到装依赖的文件夹，别慌。

### pom.xml 长什么样

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project ...>
    <modelVersion>4.0.0</modelVersion>

    <!-- 项目自己的坐标：组织.项目:版本 -->
    <groupId>com.example</groupId>       <!-- ≈ npm 的 scope，通常是反写的域名 -->
    <artifactId>hello-java</artifactId>  <!-- ≈ 包名 -->
    <version>1.0.0</version>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
    </properties>

    <dependencies>
        <!-- 每个 dependency ≈ dependencies 里的一条 -->
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
            <version>3.14.0</version>
        </dependency>
    </dependencies>
</project>
```

### 实操：添加一个依赖

以 Apache Commons Lang（工具类库，≈ 前端的 lodash）为例：

1. 打开 [mvnrepository.com](https://mvnrepository.com/)（≈ npmjs.com），搜 `commons-lang3`
2. 选版本，复制 Maven 坐标那段 XML
3. 粘贴进 `pom.xml` 的 `<dependencies>` 里
4. **点右上角浮动的小图标（或 `Cmd + Shift + I`）Reload Maven 项目**——这一步 ≈ `npm install` ⚠️ 忘了 Reload 会报"找不到类"
5. 代码里直接 `import org.apache.commons.lang3.StringUtils;` 使用

### Maven 面板

IDEA 右侧边栏的 **Maven** 按钮展开面板：

- **Lifecycle（生命周期）**：`clean`（清理）、`compile`（编译）、`package`（打包成 jar）、`install`（装进本地仓库）——双击即执行，≈ npm scripts 🔥
- **Dependencies**：依赖树，排查"这个类到底谁带进来的"很有用

Maven 的生命周期、多模块、依赖冲突等深入内容，本教程在 `docs/04-frameworks/01-maven.md` 有专章——现在你只需要会**建项目、加依赖、点 Reload**。

## 常见问题

| 问题 | 解决 |
|------|------|
| 新建项目选不到 JDK | Project SDK 处 Add JDK，指向第一章的 `JAVA_HOME` 路径 |
| 代码满屏红线但命令行能编译 | 大概率在索引中（看右下角进度条），等它完成 ⚠️ |
| 加了依赖代码还是报红 | 没 Reload Maven 项目；或去 Maven 面板点刷新图标 |
| 依赖下载极慢 | Maven 默认走国外仓库，需配置国内镜像——专章会讲，学习初期可先用热点 |
| 快捷键和文章不一致 | 你可能选了非默认 Keymap：`Settings → Keymap` 确认是 macOS / Default |

## 练习

1. 用 IDEA 创建一个 Maven 项目，添加 `commons-lang3` 依赖，在 `main` 方法里用 `StringUtils.reverse("hello")` 打印反转字符串。要求全程只用快捷键：万能搜索找文件、`Cmd + N` 生成 main 方法、`Ctrl + D` 启动 Debug。
2. 在第 1 题代码中打一个条件断点，让程序只在某个字符串长度为 5 时暂停，并用 `Alt + F8` 在断点处把字符串改写成别的值后继续运行，观察输出变化。

## 本章总结

- IDEA 与 WebStorm 同平台，VS Code 用户重点迁移三个心智：`Shift Shift` 万能搜索、`Cmd/Ctrl+Shift+A` 动作面板、**首次打开要等索引**
- 快捷键先记三个就够：`Shift Shift`、`Alt + Enter`、`Cmd + N`（生成代码）
- Debug 心智与 VS Code 一致，`Alt + F8` 表达式求值和条件断点是进阶神器
- Maven ≈ npm：`pom.xml` 加坐标 → **Reload**（≈ npm install）→ 使用；依赖存在全局 `~/.m2` 而非项目内

准备工作到此完成，下一章正式进入 Java 语言：[类、字段与方法](../02-core-java/01-class-field-method.md)

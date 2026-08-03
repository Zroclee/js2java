# Maven：Java 世界的 npm，以及更多

> 状态：✅ 已完成

## 简介
本章学习内容：Maven 依赖管理机制（传递/范围/冲突）、与 npm/pnpm 的深度对比、构建生命周期、多模块项目、发布依赖
前置知识：完成「准备工作 · IDEA」（pom.xml 基础）；熟练使用 npm/pnpm
阅读时长：约 40 分钟
难度：🌟🌟
重要程度：🌟🌟🌟🌟🌟
当前进度：81%

---

## Maven 到底是什么：npm + 构建脚本 + 项目规范

第二章你已经会"加坐标 → Reload → 用"的日常操作。本章回答更深的问题：**这套机制是怎么运作的，和你熟悉的 npm/pnpm 有什么本质不同？**

先定位：npm 是包管理器，构建靠 scripts 里手写的命令；**Maven 是三合一**——依赖管理 + 构建生命周期（编译/测试/打包）+ 项目结构规范（约定优于配置：`src/main/java` 放哪它说了算）。

> 💡 **"那 Maven ≈ npm + webpack + eslint 的集合体？"——方向对，两个边界要修正：**
>
> - **构建 ≠ bundling**：webpack/vite 是 *bundler*——把几百个 JS 模块合并压缩成少量 bundle（解决浏览器无模块系统、请求昂贵）。Maven 的构建是 *compile + archive*：javac 把 `.java` 一对一编译成 `.class`，jar 只是把这些 class **原样打成 zip**——不合并、不压缩、不 tree-shake。JVM 靠 classpath 按需加载类，根本不需要 bundling。所以 Java 世界没有"打包器"这个角色，Maven 更像 `tsc + zip + 任务流水线`。
> - **"项目结构规范"不是代码风格**：Maven 管的是**物理目录布局**（`src/main/java` 必须在哪）——"文件放哪"的强制约定，前端没有对应物（目录全靠团队自觉）。eslint/prettier 管"代码怎么写"，Java 的对应物是 **Checkstyle**（≈eslint）和 **Spotless**（≈prettier）——以 **Maven 插件**身份挂载的可选配件，不是 Maven 本体。
>
> 精确等式：`Maven ≈ npm + tsc&流水线 + 强制目录布局`。顺带一提：Maven 本体其实是个插件框架，compile/test/package 全是官方插件干的——这点倒和 webpack 的插件体系神似。

## 依赖管理机制 🔥

### 传递依赖（Transitive Dependencies）

```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-web</artifactId>
    <version>6.1.0</version>
</dependency>
<!-- 你只声明了 spring-web，但它会自动带来 spring-core、spring-beans…… -->
```

你只声明 A，A 依赖的 B、B 依赖的 C 全部自动引入——和 npm 一样 😌。查看完整依赖树：

```bash
mvn dependency:tree          # 🔥 排查"这个类到底谁带进来的"的神器
```

### 依赖范围（scope）🆕 —— 比 npm 精细

| scope | 何时可用 | npm/pnpm 对照 |
|-------|----------|---------------|
| `compile`（默认） | 编译+运行全程 | `dependencies` |
| `test` | 仅测试代码（JUnit） | `devDependencies` |
| `provided` | 编译时需要，运行时由环境提供（如 Servlet API 由 Tomcat 提供） | ≈ `peerDependencies`（宿主提供）💡 |
| `runtime` | 编译不需要，运行才要（如 MySQL 驱动） | 无对应 |
| `system` / `import` | 本地路径 / 导入 BOM | 少用/见下文 |

```xml
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>4.13.2</version>
    <scope>test</scope>      <!-- 打包时不会进生产 jar -->
</dependency>
```

### 依赖冲突与调解规则 ⚠️💼（与 npm 的本质分歧点）

A 依赖 fastjson 1.2.83，B 依赖 fastjson 2.0.0——**同一个 jar，Maven 一个项目里只允许存在一个版本**，必须调解：

1. **路径最近优先**：谁的依赖路径短，用谁的
2. **同深度，声明优先**：pom 里先声明的赢

```bash
mvn dependency:tree          # 看冲突现场（会标出 omitted for conflict）
```

手动干预三招：

```xml
<!-- 1. exclusions：排除掉传递进来的某个依赖 -->
<dependency>
    <groupId>com.example</groupId>
    <artifactId>some-lib</artifactId>
    <exclusions>
        <exclusion>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
        </exclusion>
    </exclusions>
</dependency>

<!-- 2. dependencyManagement：锁定版本（不管谁传递进来，统一用这个）≈ pnpm overrides 🔥 -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
            <version>2.0.43</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

⚠️ **这正是与 npm/pnpm 最本质的区别**：npm 允许同一包多个版本嵌套共存（各依赖各的 node_modules），pnpm 用全局 store + 硬链接隔离；**Maven 的世界里一个类只能有一个版本**——选错了就是 `NoSuchMethodError` 灵异事件现场。

## 与 npm / pnpm 的正面对比 💼

| 维度 | npm / pnpm | Maven |
|------|-----------|-------|
| 依赖存储 | `node_modules` 项目内（pnpm 全局 store + 硬链接） | `~/.m2/repository` **全局共享** |
| 版本冲突 | 多版本嵌套共存（pnpm 隔离出"幽灵依赖"问题） | **单版本调解**：就近+声明优先 ⚠️ |
| 依赖分级 | dependencies / dev / peer | scope 五档（compile/test/provided/runtime/…） |
| 版本统一 | `overrides`（pnpm）/ `resolutions`（yarn） | `dependencyManagement` |
| 锁文件 | `package-lock.json` / `pnpm-lock.yaml` **标配** | **官方没有锁文件** ⚠️（靠 dependencyManagement 人工钉版本） |
| 镜像源 | npm registry / 淘宝源 | Maven Central / 阿里云镜像（settings.xml） |
| 构建职责 | scripts 只是命令别名 | **生命周期**：编译→测试→打包→安装 内置 |
| 项目规范 | 目录随意 | 约定优于配置（src/main/java 等强制布局） |

⚠️ 锁文件那行值得停留：npm 的 lockfile 保证"团队每个人装出一样的树"；Maven 没有官方等价物——版本漂移风险靠 `dependencyManagement` 钉死版本来防，这也是大厂 pom 里版本号全集中在 properties/dependencyManagement 的原因 🔥。

## 构建生命周期：mvn 命令在干什么

```bash
mvn clean          # 清理 target/（≈ rm -rf dist）
mvn compile        # 编译 src/main/java → target/classes
mvn test           # 跑单元测试
mvn package        # 打成 jar（target/xxx.jar ≈ npm run build 出 dist）
mvn install        # 装进本地仓库 ~/.m2（≈ 发布给自己用，见下文）
mvn deploy         # 发布到远程仓库
```

关键认知 🆕：生命周期是**单向流水线**——执行 `package` 会自动先跑 `compile` 和 `test`。IDEA 右侧 Maven 面板双击节点 = 执行到该阶段为止的全流程。

💡 国内加速（构建慢必做）：`~/.m2/settings.xml` 配置阿里云镜像 ≈ 切换淘宝 npm 源：

```xml
<mirrors>
    <mirror>
        <id>aliyun</id>
        <mirrorOf>central</mirrorOf>
        <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
</mirrors>
```

## 多模块项目：Java 的 monorepo 🔥

### 为什么需要

项目一大就要拆：RuoYi 就拆成 `ruoyi-common / ruoyi-system / ruoyi-admin` 多个模块——公共代码复用、职责分层、独立编译。

**前端类比直接上 pnpm workspace monorepo** 😌：

```
my-project/                        # ≈ monorepo 根目录
├── pom.xml                        # 父 POM（packaging=pom）≈ 根 package.json + pnpm-workspace.yaml
├── common/                        # 子模块 ≈ packages/common
│   └── pom.xml
├── service/                       # 子模块 ≈ packages/service
│   └── pom.xml
└── admin/                         # 子模块（依赖 common 和 service）
    └── pom.xml
```

### 三个关键机制

```xml
<!-- 1. 父 POM：声明聚合 + 统一管理（packaging 是 pom，不出 jar） -->
<groupId>com.example</groupId>
<artifactId>my-project</artifactId>
<packaging>pom</packaging>
<modules>
    <module>common</module>
    <module>service</module>
    <module>admin</module>
</modules>
<dependencyManagement>
    <!-- 全模块版本统一在此钉死 ≈ pnpm catalog 🔥 -->
</dependencyManagement>
```

```xml
<!-- 2. 子模块：继承父 POM，获得统一管理的依赖版本 -->
<parent>
    <groupId>com.example</groupId>
    <artifactId>my-project</artifactId>
    <version>1.0.0</version>
</parent>
<artifactId>admin</artifactId>
<dependencies>
    <!-- 3. 模块间依赖：admin 依赖 common，像依赖普通 jar 一样声明 -->
    <dependency>
        <groupId>com.example</groupId>
        <artifactId>common</artifactId>
        <version>1.0.0</version>      <!-- 版本可由父 POM 统一省略 -->
    </dependency>
</dependencies>
```

效果：**根目录 `mvn clean package` 一次构建全部模块**（自动按依赖顺序），子模块通过父 POM 共享版本管理——和 pnpm workspace 的心智几乎一比一。

## 发布一个依赖 🆕（扩展）

Java 的"npm publish"分三级，按使用范围理解：

### 1. 发布到本地仓库：`mvn install`

```bash
cd common && mvn clean install
# common-1.0.0.jar 进了 ~/.m2/repository/com/example/common/1.0.0/
# 本机任何项目现在都能用坐标引用它 ≈ pnpm link 的正式版
```

多模块项目的日常：改了 common → install → admin 里生效 🔥。

### 2. 发布到公司私服：`mvn deploy`

团队共享的 jar 发布到私有仓库（Nexus / 阿里云效），pom 里配 `<distributionManagement>` 声明仓库地址，settings.xml 配账号密码 ≈ 发布到公司 npm 私服（verdaccio 等）。

### 3. 发布到 Maven Central（中央仓库）

想让自己的库被全世界 `dependency` 引用（≈ npm publish 到官方 registry）：注册 Sonatype 账号 → GPG 签名 → staging 审核 → 同步中央仓库。流程比 npm publish 重得多，知道有这条路即可。

### SNAPSHOT：Java 特有的开发版概念 🆕

```xml
<version>1.0.0-SNAPSHOT</version>
```

带 `-SNAPSHOT` 的版本号是"**还在开发的快照版**"：Maven 每次都去拉最新的（同一版本号内容可变）；去掉 SNAPSHOT 的 `1.0.0` 是"**不可变的正式版**"。npm 里没有完全对应物（最接近的是 beta/next 标签，但 npm 版本本身不可变）——团队协作开发多模块时 SNAPSHOT 是常态 💼。

## 练习

1. 在你第二章创建的 Maven 项目里执行 `mvn dependency:tree`，找一个传递依赖，用 `exclusions` 排除它，再跑一次对比树的变化。
2. 动手搭一个两模块项目：父 POM + `common`（写一个 StringUtil 工具类）+ `app`（依赖 common 并调用工具方法）；在根目录 `mvn clean package` 一次构建，再在 app 里运行 main 验证调用成功。
3. 思考题：为什么 pnpm 能做到"同一包多版本共存"而 Maven 必须二选一？提示：从"JS 模块是文件，Java 类全摊在同一个 classpath 命名空间"的角度想。

## 本章总结

- Maven = 依赖管理 + 构建生命周期 + 项目规范三合一；坐标定位、传递依赖自动引入
- **scope 五档**比 npm 精细（provided ≈ peerDependencies）；**冲突单版本调解**（就近+声明优先）是与 npm 多版本共存的本质分歧 ⚠️💼
- 无官方锁文件，版本钉死靠 `dependencyManagement`（≈ pnpm overrides）
- 生命周期流水线：`clean → compile → test → package → install → deploy`
- 多模块 ≈ pnpm workspace：父 POM 聚合 + 继承 + dependencyManagement 统一版本
- 发布三级：install 本地 → deploy 私服 → 中央仓库；`SNAPSHOT` 是可变开发版 🆕

下一章：[Spring Boot](./02-spring-boot.md)——框架篇的重头戏，迷你 IoC 的知识马上兑现

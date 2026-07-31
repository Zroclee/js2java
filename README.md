# js2java

> 前端工程师的 Java 速通手册：用你熟悉的 JS，对标着把 Java 拿下。

这是一套为前端开发者准备的 Java 学习仓库，目标只有一个——**最快路径，从 JS 到全栈**。

不搞学院派的循序渐进，全部内容以「JS ↔ Java 概念对比」为主线：
类型系统、OOP、集合、异常、并发……每个知识点都用你已经会的 JS 来锚定 Java，少走弯路，直击差异。

## 这不是单纯的学习，而是三件事

这套教程对你的 JS 经验做的，不是从零教学，而是三种操作——**知识的迁移、概念的重塑与拓展**：

| 动作 | 含义 | 例子 |
|------|------|------|
| **迁移** | 外壳与心智直接平移，用已知加速未知 | npm → Maven、V8 → JVM、TS abstract → Java abstract |
| **重塑** | 外壳相同但内核迥异，必须打破旧模型重建 | 原型链 ≠ 继承链、TS 结构类型 ≠ Java 名义类型、JS `==` 比值 ≠ Java `==` 比引用 |
| **拓展** | 认知版图里不存在的全新大陆，从零建立概念 | 接口契约、受检异常、多线程共享内存、IoC 容器 |

阅读时请留意每个知识点属于哪一层：**迁移层可以快速扫过；重塑层务必放慢——这里最容易带着 JS 直觉踩坑；拓展层没有锚点可用，需要耐心建立新概念**。

## 学习路线

完整大纲见 **[ROADMAP.md](./ROADMAP.md)**，包含每个知识点的难度、重要度、JS 对比锚点与完成标准。

```
准备工作 → Java 核心（OOP / 数据处理 / 异常 / 并发 / 反射注解泛型）
        → Docker → 数据库 → 框架（Maven / Redis / Spring Boot / MyBatis）
        → 项目实战 → 部署
```

## 仓库结构

```
docs/        # 📘 学习文档（核心：JS vs Java 对比）
exercises/   # 🧩 练习代码（JS / Java 双语言对照）
projects/    # 🏗️ 完整项目（后台管理系统，含部署脚本）
ROADMAP.md   # 学习路线与完成标准
AGENTS.md    # 内容生成规范（AI 协作约束）
```

## 学习进度

| 章节 | 文档 | 难度 | 重要度 | 状态 |
|------|------|------|--------|------|
| 一、准备工作 | [JDK / JRE / JVM](./docs/01-preparation/jdk-jre-jvm.md) | 🌟 | 🌟🌟🌟🌟 | ✅ 已完成 |
| | [IDEA 与 Maven 项目](./docs/01-preparation/idea-debug-maven.md) | 🌟 | 🌟🌟🌟🌟🌟 | ✅ 已完成 |
| 二、Java 核心 | [类、字段与方法](./docs/02-core-java/01-class-field-method.md) | 🌟🌟 | 🌟🌟🌟🌟🌟 | ✅ 已完成 |
| | [继承与多态](./docs/02-core-java/02-inheritance-polymorphism.md) | 🌟🌟🌟 | 🌟🌟🌟🌟🌟 | ✅ 已完成 |
| | [抽象类与接口](./docs/02-core-java/03-abstract-interface.md) | 🌟🌟🌟 | 🌟🌟🌟🌟🌟 | ✅ 已完成 |
| | [静态成员与枚举](./docs/02-core-java/04-static-enum.md) | 🌟🌟 | 🌟🌟🌟🌟 | ✅ 已完成 |
| | [包与模块](./docs/02-core-java/05-package-module.md) | 🌟 | 🌟🌟🌟 | ✅ 已完成 |
| | [数据类型](./docs/02-core-java/06-data-types.md) | 🌟🌟 | 🌟🌟🌟🌟🌟 | ✅ 已完成 |
| | [数值运算](./docs/02-core-java/07-operators.md) | 🌟 | 🌟🌟🌟 | ✅ 已完成 |
| | [字符串处理](./docs/02-core-java/08-string.md) | 🌟🌟 | 🌟🌟🌟🌟🌟 | 📝 待撰写 |
| | [流程控制](./docs/02-core-java/09-control-flow.md) | 🌟 | 🌟🌟🌟🌟🌟 | 📝 待撰写 |
| | [数组与集合](./docs/02-core-java/10-collections.md) | 🌟🌟🌟 | 🌟🌟🌟🌟🌟 | 📝 待撰写 |
| | [IO 与时间日期](./docs/02-core-java/11-io.md) | 🌟🌟🌟 | 🌟🌟🌟🌟 | 📝 待撰写 |
| | [异常处理](./docs/02-core-java/12-exception.md) | 🌟🌟 | 🌟🌟🌟🌟 | 📝 待撰写 |
| | [线程与并发](./docs/02-core-java/13-concurrency.md) | 🌟🌟🌟🌟🌟 | 🌟🌟🌟🌟🌟 | 📝 待撰写 |
| | [反射、注解与泛型](./docs/02-core-java/14-reflection-annotation-generic.md) | 🌟🌟🌟🌟 | 🌟🌟🌟🌟 | 📝 待撰写 |
| 三、Docker & 数据库 | [Docker](./docs/03-database/01-docker.md) | 🌟🌟 | 🌟🌟🌟🌟 | 📝 待撰写 |
| | [MySQL](./docs/03-database/02-mysql.md) | 🌟🌟🌟🌟 | 🌟🌟🌟🌟🌟 | 📝 待撰写 |
| | [PostgreSQL](./docs/03-database/03-postgresql.md) | 🌟🌟🌟 | 🌟🌟🌟 | 📝 待撰写 |
| | [Redis](./docs/03-database/04-redis.md) | 🌟🌟🌟 | 🌟🌟🌟🌟🌟 | 📝 待撰写 |
| 四、框架 | [Maven](./docs/04-frameworks/01-maven.md) | 🌟🌟 | 🌟🌟🌟🌟🌟 | 📝 待撰写 |
| | [Spring Boot](./docs/04-frameworks/02-spring-boot.md) | 🌟🌟🌟🌟 | 🌟🌟🌟🌟🌟 | 📝 待撰写 |
| | [MyBatis](./docs/04-frameworks/03-mybatis.md) | 🌟🌟🌟 | 🌟🌟🌟🌟🌟 | 📝 待撰写 |
| 五、项目实战 & 部署 | [图书馆管理系统](./projects/library-system) | 🌟🌟🌟 | 🌟🌟🌟🌟🌟 | 📝 待开工 |
| | [后台管理系统（含部署）](./projects/admin-system) | 🌟🌟🌟🌟 | 🌟🌟🌟🌟🌟 | 📝 待开工 |

## 适合谁

- 前端想转全栈，需要快速建立 Java 后端能力
- 已熟悉 JS / Node，不想从 "Hello World" 重学一遍
- 偏好「用已知学未知」的学习方式

## 三个思维跳跃（先建立预期）

| 维度 | JS 世界 | Java 世界 |
|------|---------|-----------|
| 类型系统 | 动态类型，运行时才知道 | 静态类型，编译期检查 |
| 并发模型 | 单线程 + 事件循环 | 多线程 + 共享内存 |
| 运行方式 | 解释执行（V8） | 编译为字节码跑在 JVM 上 |

## 说明

这不是大而全的 Java 教程，是一条为前端定制的**速通路线**——够快、够用、能落地。

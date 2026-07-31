# exercises/ — 双语言对照练习

按 docs 章节组织的双语言练习骨架。**先写 JS（你会的），再写 Java（你学的）**——用已知撬动未知，这就是本仓库的核心训练法。

## 目录

```
java-core/                       # 对应 docs/02-core-java 的 14 个章节
├── 01-class-field-method/       # 类、字段、方法
│   ├── Counter.java             # Java 练习骨架（含练习要求注释）
│   └── counter.js               # JS 对照骨架
├── 02-inheritance-polymorphism/ # 继承与多态
├── 03-abstract-interface/       # 抽象类与接口
├── 04-static-enum/              # 静态成员与枚举
├── 05-package-module/           # 包与模块
├── 06-data-types/               # 数据类型
├── 07-operators/                # 数值运算
├── 08-string/                   # 字符串处理
├── 09-control-flow/             # 流程控制
├── 10-collections/              # 数组与集合
├── 11-io/                       # IO 与时间日期
├── 12-exception/                # 异常处理
├── 13-concurrency/              # 线程与并发
└── 14-reflection-annotation-generic/  # 反射、注解与泛型
```

## 规则

1. 每章一对双语言文件：**同名同题**，如 `Counter.java` ↔ `counter.js`
2. 文件头部注释写清**练习要求**（不含答案），代码由学习者自己完成
3. 先读 `docs/` 对应章节，再做练习；做完对照两版代码总结差异
4. Java 文件的 `public class` 名必须与文件名一致（编译要求）
5. 部分章节（如并发、IO）JS 侧以"验证差异、写对照理解"为主，不必强求对等实现

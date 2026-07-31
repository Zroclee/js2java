# exercises/ — 双语言对照练习

同一道题，JS 与 Java 各实现一遍。**先写 JS（你会的），再写 Java（你学的）**，写 Java 时对照 JS 实现找差异——这就是本仓库的核心训练法。

## 目录

```
javascript/     # JS 实现（用于对照）
├── basics/       # 基础语法对照：类型、字符串、集合、流程控制
└── algorithms/   # 算法题对照：排序、查找、字符串处理
java/           # Java 实现
├── basics/       # 与 javascript/basics 一一对应
└── algorithms/   # 与 javascript/algorithms 一一对应
```

## 规则

1. 两边文件**同名同题**，如 `javascript/basics/reverse-string.js` ↔ `java/basics/ReverseString.java`
2. Java 文件头部注释标明对应的 JS 文件路径
3. 每道题注释里写一句「本题 JS ↔ Java 核心差异」
4. 练习与 `docs/` 章节配套：学完一章，做对应主题的对照练习

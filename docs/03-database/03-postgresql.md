# PostgreSQL：更强大的那个"备胎"，还是另一种主流？

> 状态：✅ 已完成

## 简介
本章学习内容：为什么有了 MySQL 还要了解 PostgreSQL、两者的共同点与核心区别、JSONB 实战、项目选型依据
前置知识：完成「02-mysql」（PG 语法不再从零教，只讲差异）
阅读时长：约 25 分钟
难度：🌟🌟🌟
重要程度：🌟🌟🌟
当前进度：80%

---

## 为什么有了 MySQL，还要看 PostgreSQL

上一章你已经能干活了，这章不是"再学一个"，而是回答一个真实问题：**技术选型时，选项里为什么会出现 PostgreSQL（简称 PG）？**

三个现实：

1. **PG 是"学院派优等生"**：自称"世界上最先进的开源关系型数据库"——SQL 标准遵循最严格、功能最丰富。MySQL 是"够用的实用派"，靠简单易用和生态称王。两家常年霸榜数据库流行度前二（开源领域）💼
2. **行业版图在变**：国内互联网传统上是 MySQL 天下（RuoYi 等脚手架默认它 🔥），但国外 PG 占比很高，近年国内技术驱动型公司、金融、出海项目明显向 PG 倾斜；云厂商 RDS 两家都是一等公民
3. **有些需求 MySQL 天然吃力**：半结构化数据（JSON）、地理信息（GIS）、复杂查询——这是 PG 的主场（下面细讲）

所以定位是：**MySQL 必修，PG 了解差异、知道何时选它**——这正是本章的全部任务。

## 共同点：你已经会的那 90% 😌（迁移层速过）

| 维度 | 共同点 |
|------|--------|
| 模型 | 都是关系型：表、行、列、主键、外键 |
| 语言 | 都讲 SQL：CRUD / JOIN / GROUP BY / 子查询语法几乎一致 |
| 事务 | ACID、隔离级别概念完全相同（默认值不同，下详） |
| 索引 | B+ 树索引、联合索引、EXPLAIN 都在 |
| 部署 | Docker 一条命令拉起，Navicat/IDEA 都能连 |

```bash
# 熟悉的起手式：30 秒拉起 PG
docker run -d -p 5432:5432 \
  -e POSTGRES_PASSWORD=123456 \
  -e POSTGRES_DB=demo \
  --name pg16 postgres:16

docker exec -it pg16 psql -U postgres -d demo   # 进命令行（psql ≈ mysql 命令行）
```

## 核心区别：SQL 方言速查表 ⚠️

日常写 SQL 时真正会绊到你的差异，一张表收编：

| 场景 | MySQL | PostgreSQL |
|------|-------|-----------|
| 自增主键 | `id BIGINT AUTO_INCREMENT` | `id BIGSERIAL`（或 `GENERATED ALWAYS AS IDENTITY`） |
| 分页 | `LIMIT 10 OFFSET 20` 或 `LIMIT 20,10` | `LIMIT 10 OFFSET 20`（不支持逗号简写） |
| 字符串拼接 | `CONCAT(a, b)` | `a \|\| b`（CONCAT 也有） |
| 标识符引号 | 反引号 `` `user` `` | 双引号 `"user"`（⚠️ PG 里单引号只能包字符串值） |
| 布尔类型 | `TINYINT(1)` 假布尔 | **真 `BOOLEAN`**（TRUE/FALSE）🆕 |
| 空值处理 | `IFNULL(a, b)` | `COALESCE(a, b)`（标准函数，MySQL 也支持） |
| 当前时间 | `NOW()` | `NOW()` / `CURRENT_TIMESTAMP` |
| 类型严格性 | **宽松**：超长字符串静默截断、`abc`+1 隐式转换 ⚠️ | **严格**：类型不匹配直接报错 ✅ |

最后一行是两家性格的真实写照：**MySQL 是"能跑就行"的宽容派，PG 是"错了就报"的严格派**——从 JS 转来的你应该投严格派一票，它让 bug 显形。

## PG 的杀手锏：数据类型富得流油 🔥

MySQL 的类型是"基本款"，PG 的类型是"豪华套餐"——这也是选型时 PG 翻盘的常见理由：

### JSONB：半结构化数据的一等公民

```sql
CREATE TABLE products (
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(100),
    attrs   JSONB            -- 存 JSON 文档，还能索引和查询！
);

INSERT INTO products (name, attrs) VALUES
    ('机械键盘', '{"brand": "Keychron", "color": "black", "switches": ["red", "brown"]}'),
    ('鼠标',     '{"brand": "Logitech", "color": "white"}');

-- 直接查 JSON 内部字段 🔥
SELECT name FROM products WHERE attrs->>'brand' = 'Keychron';
-- ->> 取字段为文本；-> 取字段仍为 JSON；attrs ? 'color' 判断键存在
```

想一下在 MySQL 里实现同样的事：要么把 JSON 当 TEXT 存（查询靠全文字符串匹配，哭），要么拆成 EAV 表（设计扭曲）——**PG 让"关系型 + 文档型"合体**， ≈ 内置了一个能 JOIN 的 MongoDB 🆕。

### 其他类型（一提即可）

- **数组**：`tags TEXT[]`——一列直接存标签列表
- **UUID**：原生主键类型（分布式 ID 方案之一）
- **几何/GIS 类型** + PostGIS 扩展：地理围栏、"查附近 5km 门店"——行业事实标准，MySQL 望尘莫及 💼
- **范围类型**：`tstzrange`（时间段），排期/预订系统的神器

### 其他差异（两句话说清）

- **MVCC 实现不同**：PG 用元组多版本，MySQL 用 undo log——**PG 默认隔离级别 READ COMMITTED，MySQL 是 REPEATABLE READ** 💼（面试加分项，知道结论即可）
- **扩展哲学不同**：MySQL 可插拔的是**存储引擎**（InnoDB/MyISAM）；PG 可插拔的是**功能扩展**（PostGIS、pg_trgm 模糊搜索……）——所以 PG 外号"数据库界的乐高"

## 选型：项目里到底用谁 💼

按"判断依据"逐条过，比背结论有用：

| 判断依据 | 倾向 MySQL | 倾向 PostgreSQL |
|----------|-----------|-----------------|
| **团队熟悉度**（权重最高 🔥） | 团队都在用 MySQL——别折腾，运维和招聘成本最低 | 团队有 PG 经验 |
| **框架生态** | RuoYi 等国内脚手架默认 MySQL，开箱即用 | 自研或海外框架（Rails/Django 社区偏爱 PG） |
| **数据形态** | 标准关系型 CRUD（后台管理、订单用户） | 大量 JSON 半结构化数据 → JSONB 🔥 |
| **特殊需求** | —— | 地理信息（PostGIS）、复杂报表、全文搜索 |
| **合规与标准** | 业务求快，可容忍宽松 | 金融级严谨，要 SQL 标准严格遵循 |
| **云与托管** | 各家 RDS 都支持，平手 | 同左 |

**给前端转全栈的你的建议**：

1. **主力先吃透 MySQL**——国内岗位面 MySQL 的比例远高于 PG，RuoYi 实战也用它
2. **PG 保持"会用 + 知差异"**——本章的方言表 + JSONB 就够你应对 90% 的场景
3. 真遇到"数据是 JSON 为主"或"要做地图附近的人"的需求，**主动提 PG**——这就是你比其他初级后端多一分的选型嗅觉 💡

## 练习

1. 用 Docker 拉起 postgres:16，建 `products` 表（含 JSONB 字段）插入 3 条数据，完成：(a) 按 JSON 内部 brand 查询；(b) 查询"含有 color 键"的商品；(c) 体会 `->` 和 `->>` 返回值的差异。
2. 方言对照练习：把上一章 users 表的建表 SQL"翻译"成 PG 版本（自增主键、布尔字段、分页写法），并在一个 PG 实例里执行验证。
3. 思考并写下你的判断：一个"外卖骑手的实时位置上报系统"，你会选 MySQL 还是 PG？依据是什么？（提示：骑手位置是经纬度 + 要查"附近 3km 的骑手"）

## 本章总结

- 为什么学 PG：开源前二、国外主流国内崛起、JSON/GIS 场景 MySQL 天然吃力
- 共同点 90%：关系模型、SQL、事务、索引概念全通用，方言差异一张表收编（自增/拼接/引号/布尔/严格性）
- PG 杀手锏：**JSONB**（能索引能查询的 JSON ≈ 内置 MongoDB 🔥）、数组/UUID/PostGIS；默认隔离级别 READ COMMITTED（MySQL 是 RR）
- 选型依据：团队熟悉度权重最高 → 框架生态 → 数据形态 → 特殊需求；国内默认 MySQL，JSON/GIS 重场景主动考虑 PG

下一章：[Redis](./04-redis.md)——数据库板块最后一站，缓存的世界

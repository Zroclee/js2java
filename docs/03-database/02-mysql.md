# MySQL：后端的数据大本营

> 状态：✅ 已完成

## 简介
本章学习内容：MySQL 与关系型数据库、建库建表、CRUD、多表查询（JOIN/聚合）、索引、事务与隔离级别、锁
前置知识：完成「01-docker」（已拉起 mysql8 容器）；无任何数据库经验要求
阅读时长：约 60 分钟（数据库板块主菜）
难度：🌟🌟🌟🌟
重要程度：🌟🌟🌟🌟🌟
当前进度：71%

---

## 先回答：前端为什么需要数据库

你在前端处理的数据住哪？内存（刷新就没）、localStorage（单机、几 MB）。而真实系统的数据要**持久化**（重启还在）、**多用户共享**（万人同时读写）、**复杂查询**（"找出近 30 天下单超过 3 次的用户"）——这三件事都需要一个专门的系统：**数据库**。

**MySQL 是什么**：世界上最流行的**关系型数据库**（RDBMS），开源免费，国内后端事实标准。"关系型"的意思很朴素：**数据以"表"（table）组织，表和表之间靠"关系"（外键）连接**。

前端类比秒懂 😌：**一张表 ≈ 一个 JS 对象数组**——

```javascript
// JS 里的"users 表"
const users = [
  { id: 1, name: '张三', age: 25 },
  { id: 2, name: '李四', age: 30 },
];
```

```
MySQL 的 users 表（≈ Excel 表格）
┌────┬───────┬─────┐
│ id │ name  │ age │   ← 列（column/字段）：有类型约束，呼应你的静态类型思维
├────┼───────┼─────┤
│  1 │ 张三  │  25 │   ← 行（row/记录）≈ 数组里的一个对象
│  2 │ 李四  │  30 │
└────┴───────┴─────┘
```

区别：**每一列都有强制类型**（静态类型思维再次上线），且数据躺在硬盘上、为并发访问而生。

**SQL 是什么**：操作这些表的标准语言。它是**声明式**的——你描述"要什么"，不用写"怎么找"（没有 for 循环、没有 if）：

```sql
SELECT name FROM users WHERE age > 20 ORDER BY age DESC LIMIT 10;
-- 读出来就是需求本身：找出年龄大于 20 的用户名，按年龄降序，取前 10
```

## 准备工作：连上上一章的 MySQL

上一章已拉起容器（没拉的话先执行）：

```bash
docker run -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456 -e MYSQL_DATABASE=demo --name mysql8 mysql:8
```

连接方式三选一：

1. **Navicat**（付费 GUI，行业常用 🔥）：新建连接 → MySQL → 主机 `localhost`、端口 `3306`、用户 `root`、密码 `123456`（≈ MongoDB Compass，图形化建表查表）
2. **IDEA Ultimate**：右侧 Database 面板 → 数据源 → MySQL，参数同上（学框架后这个最顺手）
3. **命令行**（学习原理最直接）：`docker exec -it mysql8 mysql -uroot -p123456`

本章 SQL 三种方式都能执行，命令行示例为主。

## 建库建表：先有容器再装数据

```sql
-- 建库（≈ 建一个独立的命名空间装一组表）
CREATE DATABASE IF NOT EXISTS shop DEFAULT CHARSET utf8mb4;
USE shop;   -- 后续操作都发生在这个库里

-- 建表：用户表
CREATE TABLE users (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,  -- 主键：每行唯一标识，自增
    name        VARCHAR(50) NOT NULL,               -- 变长字符串，最多 50 字符
    age         INT,
    balance     DECIMAL(10, 2) DEFAULT 0.00,        -- 定点小数：金额专用（呼应 BigDecimal！）
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP  -- 默认当前时间
);
```

常用列类型速查 🔥：

| 类型 | 用途 | 注意 |
|------|------|------|
| `INT` / `BIGINT` | 整数（id 用 BIGINT） | |
| `VARCHAR(n)` | 变长字符串 | n 是**字符数**不是字节 ⚠️；姓名/标题用 |
| `TEXT` | 长文本（正文、JSON） | 不能加默认值 |
| `DECIMAL(m,n)` | **金额必选** | 定点精确小数，绝不用 FLOAT/DOUBLE 存钱 ⚠️（浮点丢精度） |
| `DATETIME` / `TIMESTAMP` | 日期时间 | TIMESTAMP 带时区换算、范围到 2038 ⚠️ |

## CRUD：四大基本操作 🔥

先插入一批演练数据：

```sql
-- C：Create（插入）
INSERT INTO users (name, age, balance) VALUES
    ('张三', 25, 1000.00),
    ('李四', 30, 500.50),
    ('王五', 17, 200.00),
    ('赵六', 35, 9999.99);
```

### R：查询（SQL 的灵魂，80% 的时间在写它）

```sql
SELECT * FROM users;                            -- 全表（生产环境慎用 *，只取需要的列）
SELECT name, age FROM users WHERE age > 20;     -- 条件过滤
SELECT * FROM users WHERE name LIKE '张%';       -- 模糊：张开头（% 任意字符，_ 单字符）
SELECT * FROM users WHERE age IN (17, 25);      -- 集合匹配
SELECT * FROM users WHERE age BETWEEN 20 AND 30; -- 范围（含两端）
SELECT * FROM users WHERE balance IS NULL;       -- ⚠️ 判断空用 IS NULL，不能写 = NULL

-- 排序 + 分页（后端列表页标配）
SELECT * FROM users ORDER BY balance DESC, id ASC   -- 先按余额降序，再按 id 升序
LIMIT 10 OFFSET 20;                                  -- 第 3 页（每页 10 条）

-- LIMIT 的简写：LIMIT 20, 10 等价于 LIMIT 10 OFFSET 20
```

### U：更新 ⚠️（事故高发区）

```sql
UPDATE users SET balance = balance - 100 WHERE id = 1;   -- ✅ 带条件的精准更新

-- UPDATE users SET balance = 0;   -- 💥 忘了 WHERE = 全表清零！生产事故经典剧本
```

### D：删除 ⚠️（同样高发）

```sql
DELETE FROM users WHERE id = 4;      -- ✅ 按主键删一行

-- DELETE FROM users;                -- 💥 全表删除（但保留表结构）
-- TRUNCATE TABLE users;             -- 清空并重置自增（更快，不可回滚，慎用）
```

🔥 **军规：UPDATE / DELETE 之前，先拿同样的 WHERE 跑一次 SELECT 确认命中范围**。养成这个习惯能避开 90% 的数据事故。

## 多表查询：表与表的连接

真实系统的数据分散在多张表。加一张订单表（`user_id` 指向 `users.id`，这就是"关系"）：

```sql
CREATE TABLE orders (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id    BIGINT NOT NULL,                  -- 逻辑外键：指向 users.id
    product    VARCHAR(100),
    amount     DECIMAL(10, 2),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO orders (user_id, product, amount) VALUES
    (1, '机械键盘', 399.00),
    (1, '鼠标', 99.00),
    (2, '显示器', 1299.00);
```

> 💼 **关于物理外键**：教材会教你 `FOREIGN KEY (user_id) REFERENCES users(id)`——但实际生产**大多只用"逻辑外键"**（就是普通列，靠代码保证关联），因为物理外键会拖慢写入、增加耦合。知道这个潜规则即可。

### JOIN：把两张表拼起来查

```sql
-- INNER JOIN：只取两边都匹配的（交集）
SELECT u.name, o.product, o.amount
FROM orders o
JOIN users u ON o.user_id = u.id;

-- LEFT JOIN：左表全保留，右表没匹配补 NULL（"所有用户及其订单，没下单的也在"）
SELECT u.name, o.product
FROM users u
LEFT JOIN orders o ON o.user_id = u.id;
```

一句话记：**INNER 要交集，LEFT 保左边** 💼。`o` `u` 是表的别名（≈ JS 的 import as），让 SQL 更短。

### GROUP BY 与聚合：统计分析

```sql
-- 每个用户的订单数和总消费（≈ JS 里 reduce 分组统计）
SELECT u.name,
       COUNT(o.id)   AS order_count,
       SUM(o.amount) AS total_amount
FROM users u
LEFT JOIN orders o ON o.user_id = u.id
GROUP BY u.id, u.name
HAVING COUNT(o.id) >= 1          -- ⚠️ 分组后的过滤用 HAVING（WHERE 管分组前）
ORDER BY total_amount DESC;
```

聚合函数：`COUNT` 计数、`SUM` 求和、`AVG` 平均、`MAX/MIN` 最值。执行顺序记忆：**WHERE → GROUP BY → HAVING → ORDER BY → LIMIT** 💼。

## 索引：从"全表翻"到"直接定位"

### 为什么需要

```sql
SELECT * FROM users WHERE name = '张三';
```

没有索引时，MySQL 只能**全表扫描**——一行行比对过去（≈ JS 的 `arr.find()`，O(n)）。表有一千万行就扫一千万行。索引就是给列建的"字典目录"：**用空间换时间，把查询变成 O(log n)**。

底层结构是 **B+ 树**（多路平衡查找树）——不用手写实现，记住它"有序、矮胖、叶子相连"三个特征，面试再深入 💼。

### 怎么用

```sql
CREATE INDEX idx_name ON users(name);              -- 给 name 列建普通索引
CREATE INDEX idx_age_name ON users(age, name);     -- 联合索引（多列）
-- 建表时也可以写：INDEX idx_name (name)

SHOW INDEX FROM users;        -- 查看表上所有索引
EXPLAIN SELECT * FROM users WHERE name = '张三';   -- 🔥 看执行计划：是否用上索引（key 列非 NULL 即命中）
```

### 三个必知规则 💼⚠️

1. **主键自带聚簇索引**（数据就存在主键索引的叶子上），按 id 查最快；普通索引叶子存的是主键值，查完要"回表"再捞一次数据——`SELECT *` 比 `SELECT 索引列` 多这一步
2. **最左前缀原则**：联合索引 `(age, name)` 能服务 `WHERE age=?`、`WHERE age=? AND name=?`，但**服务不了单独 `WHERE name=?`**——就像字典按"姓+名"排序，跳过姓直接查名只能全翻
3. **索引失效的常见姿势**：对列用函数（`WHERE YEAR(created_at)=2026` ❌ 改成范围查询 ✅）、`LIKE '%张'`（开头通配 ❌）、类型不匹配（字符串列传数字 ❌）

🔥 不是索引越多越好：每个索引都要占空间、拖慢写入（写数据时要同步维护索引）。**为高频查询建，为低频列忍**。

## 事务：同生共死的一组操作 💼

### 场景：转账

张三给李四转 100 块，对应两条 UPDATE：

```sql
UPDATE users SET balance = balance - 100 WHERE id = 1;   -- 张三扣 100
-- 假如此刻系统崩了……张三的钱扣了，李四没收到，100 块人间蒸发 💥
UPDATE users SET balance = balance + 100 WHERE id = 2;   -- 李四加 100
```

**事务（Transaction）就是打包：这几条 SQL 要么全部成功，要么全部回滚（≈ 全有或全无的批处理）**：

```sql
START TRANSACTION;                                        -- 开启事务
UPDATE users SET balance = balance - 100 WHERE id = 1;
UPDATE users SET balance = balance + 100 WHERE id = 2;
COMMIT;                                                   -- 提交：两条一起生效
-- 或者中途发现异常：ROLLBACK;                            -- 回滚：全部撤销，像什么都没发生
```

### ACID 四特性（用转账记）

| 特性 | 含义 | 转账场景 |
|------|------|----------|
| **A**tomicity 原子性 | 全部成功或全部失败 | 扣钱和加钱不可拆分 |
| **C**onsistency 一致性 | 事务前后数据合法 | 两人余额之和恒等于 1500 |
| **I**solation 隔离性 | 并发事务互不干扰 | 李四查询时看不到"扣了一半"的中间态 |
| **D**urability 持久性 | 提交后永久保存 | 提交后断电也不丢 |

### 隔离级别（并发访问的代价 💼）

并发事务会互相偷看，产生三种怪象，隔离级别就是"防到哪种程度"的旋钮：

| 隔离级别 | 脏读（看到别人未提交的） | 不可重复读（同事务内两次读不一样） | 幻读（两次范围查询行数变了） |
|----------|:---:|:---:|:---:|
| READ UNCOMMITTED | ❌ 有 | ❌ | ❌ |
| READ COMMITTED | ✅ 防 | ❌ 有 | ❌ |
| **REPEATABLE READ（MySQL 默认）** 🔥 | ✅ | ✅ | ✅ 基本防（InnoDB 的 MVCC 机制） |
| SERIALIZABLE | ✅ | ✅ | ✅（但慢如串行，基本不用） |

```sql
SELECT @@transaction_isolation;                          -- 查看当前级别
SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;  -- 本会话调整
```

记结论即可：**MySQL 默认 REPEATABLE READ 够用；Oracle/PG 默认 READ COMMITTED；面试问 MVCC 就是答"读已提交和可重复读靠快照实现"** 💼。

## 锁：并发改数据的交通指挥

事务隔离解决"看"的问题，**锁解决"改"的问题**——两个事务同时改同一行，必须排队（呼应并发章的竞态条件！数据库版）。

- **行锁**：InnoDB（MySQL 默认引擎）的招牌——只锁命中的行，并发度高 🔥。注意：**WHERE 条件没走索引时行锁会升级锁全表** ⚠️（又一个索引重要的理由）
- **表锁**：锁整张表，MyISAM 老引擎的方式，了解即可

**悲观锁 vs 乐观锁**（面试高频 💼）：

```sql
-- 悲观锁：先锁住再操作（悲观地认为一定有人来抢）
START TRANSACTION;
SELECT stock FROM products WHERE id = 1 FOR UPDATE;   -- 加排他锁，别人进不来
UPDATE products SET stock = stock - 1 WHERE id = 1;
COMMIT;

-- 乐观锁：不锁，更新时检查"版本没变过"（CAS 思想，呼应并发章 AtomicInteger）
-- 表里加个 version 字段
UPDATE products
SET stock = stock - 1, version = version + 1
WHERE id = 1 AND version = 3;      -- 版本已被别人改过就更新 0 行，业务层重试
```

实战选型：**秒杀/库存防超卖，乐观锁（version 版本号）是常见答案** 🔥。

## 练习

1. **CRUD 全家桶**：按本章建 users 表并插入 5 条数据，完成——(a) 查询 20~35 岁用户按年龄降序；(b) 把姓"张"的用户余额加 100；(c) 删除余额最少的用户（LIMIT 配合子查询或排序）；(d) 故意不带 WHERE 跑一次 UPDATE 前，先 SELECT 验证会命中多少行。
2. **多表统计**：建 orders 表插入数据，写一条 SQL 查出"每个用户的订单数与总消费，只显示消费超过 100 的用户，按总消费降序"。
3. **事务演练**：开两个终端（或两个 Navicat 查询窗口），窗口 A 开启事务扣张三 100 但不提交，窗口 B 查询张三余额——观察 B 看到的是什么？然后 A 执行 ROLLBACK，B 再查。解释现象与隔离级别的关系。
4. **乐观锁实战**：给 products 表加 stock 和 version 字段，模拟两个事务同时扣库存，体会 version 条件如何让后到的更新"更新 0 行"。

## 本章总结

- 表 ≈ JS 对象数组但列有类型；SQL 是**声明式**语言：描述要什么，不写怎么找
- CRUD 军规：**UPDATE/DELETE 前先 SELECT 验证 WHERE** ⚠️；金额用 DECIMAL；分页 `LIMIT n OFFSET m`
- JOIN 记口诀"INNER 要交集，LEFT 保左边"；聚合链路 WHERE → GROUP BY → HAVING → ORDER BY
- 索引 = 空间换时间的 B+ 树；**最左前缀**、**函数/前置%/类型不匹配会失效**；`EXPLAIN` 验证命中 💼
- 事务打包操作同生共死（转账场景）；ACID 背四个词；MySQL 默认 REPEATABLE READ
- 锁防并发改：行锁（要走索引！）；**乐观锁 version 版本号**是防超卖实战答案 🔥

下一章：[PostgreSQL](./03-postgresql.md)——看看另一个主流选择有何不同

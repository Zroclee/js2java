# Redis：内存里的数据闪电战

> 状态：✅ 已完成

## 简介
本章学习内容：Redis 的作用与必要性、Docker 安装与 redis-cli、五大数据类型、过期策略、真实场景实战（缓存/会话/计数器/排行榜）
前置知识：完成「02-mysql」（理解关系型数据库的负担在哪）
阅读时长：约 40 分钟
难度：🌟🌟🌟
重要程度：🌟🌟🌟🌟🌟
当前进度：85%

---

## Redis 是什么，为什么需要它

**一句话**：Redis 是把数据放在**内存**里的 key-value 数据库——读写微秒级，比 MySQL 快几个数量级。

回忆一下 MySQL：数据在磁盘，查询要走 B+ 树、可能要回表——毫秒级。日常够用，但遇到**高频读取**就成了瓶颈：商城首页的商品列表，每秒几千人刷，每次都查库，数据库迟早被压垮 💥。

解决方案朴素得可爱：**把热数据提前放进内存，请求先来内存拿，拿不到再查库**——这就是缓存，而 Redis 是缓存的事实标准 🔥。

前端类比秒懂 😌：你在前端早干过这事——

```javascript
// 前端版"Redis"：全局 Map 缓存接口数据
const cache = new Map();
async function getUser(id) {
  if (cache.has(id)) return cache.get(id);   // 先查缓存
  const user = await api.fetchUser(id);      // 没有再请求后端
  cache.set(id, user);
  return user;
}
```

Redis 就是这个 Map 的**服务端专业版**：独立进程、全服务共享、支持过期、数据结构丰富、还能持久化。**它是内存数据库，不只是缓存工具**——会话存储、计数器、排行榜、分布式锁、轻量消息队列，都是它的日常 💼。

## Docker 安装与连接

```bash
# 熟悉的起手式
docker run -d -p 6379:6379 --name redis7 redis:7

# 进入 redis-cli（Redis 的交互命令行 ≈ mysql 命令行）
docker exec -it redis7 redis-cli
```

Redis 命令的节奏很舒服：**`动词 键 [值]`**，所见即所得：

```bash
127.0.0.1:6379> SET name "js2java"
OK
127.0.0.1:6379> GET name
"js2java"
127.0.0.1:6379> KEYS *          # 看所有键（学习用；生产环境禁用，会卡死 ⚠️）
```

## 五大常用数据类型 🔥（本章主菜）

每种类型按"命令 → JS 类比 → 真实场景"三步走。

### 1. String（字符串）：万能基础款

```bash
SET user:1:name "张三"          # 存
GET user:1:name                 # 取
SET visits 100                  # 数字也能存
INCR visits                     # 原子自增（≈ count++ 但线程安全 🔥）
SETEX session:abc 3600 "uid=42" # 存并设置 3600 秒过期（SET + EXPIRE 一步完成）
```

- **JS 类比**：就是 Map 的 `set/get`，外加原子计数器
- **场景**：缓存 JSON 文本、**阅读数/点赞数计数器**（INCR 原子性防并发错乱，呼应并发章）、**登录 token 存会话**

⚠️ 键命名约定：`业务:对象:字段`（如 `user:1:name`），冒号分层，可读可检索。

### 2. Hash（哈希）：存对象的最佳姿势

```bash
HSET user:1 name "张三" age 25 city "北京"    # 一个键装一个对象的多个字段
HGET user:1 name                            # "张三"（取单字段）
HGETALL user:1                              # 取出整个对象
HINCRBY user:1 age 1                        # 单个字段自增
```

- **JS 类比**：`{ name: '张三', age: 25 }`——**Hash ≈ JS 对象**，String 只能整个存取，Hash 能改单字段
- **场景**：用户信息、商品详情这类"对象型"缓存——改一个字段不用整体重写 🔥

### 3. List（列表）：有序的队列

```bash
LPUSH news "标题A" "标题B"      # 左侧推入
RPOP news                       # 右侧弹出（LPUSH + RPOP = 队列 FIFO）
LRANGE news 0 4                 # 取最新 5 条（左起 0~4）
```

- **JS 类比**：数组 + `push/shift/pop`
- **场景**：**最新文章列表**（LPUSH 进来，LRANGE 取前 N 条）、简单的任务队列

### 4. Set（集合）：去重 + 集合运算

```bash
SADD article:1:tags "java" "docker" "java"   # 重复的 "java" 自动去重
SMEMBERS article:1:tags                      # 全部标签
SISMEMBER article:1:tags "docker"            # 1（存在）
SADD user:1:follows 100 101 102
SADD user:2:follows 101 102 103
SINTER user:1:follows user:2:follows         # 交集：共同关注 🔥
```

- **JS 类比**：`new Set()`，外加 JS 没有的**交集/并集/差集**运算 🆕
- **场景**：标签系统、**共同好友/共同关注**、抽奖去重

### 5. ZSet（有序集合）：排行榜之王 🆕🔥

```bash
ZADD rank:read 1500 "文章A" 2300 "文章B" 800 "文章C"   # 成员 + 分数
ZRANGE rank:read 0 -1 WITHSCORES                       # 按分数升序看全部
ZREVRANGE rank:read 0 2 WITHSCORES                     # 降序取前 3 = 排行榜！
ZINCRBY rank:read 100 "文章A"                          # 文章A 阅读 +100，排名自动调整
```

- **JS 类比**：没有对应物——"每个成员自带分数、自动按分数排序的 Set" 🆕
- **场景**：**阅读/销量/积分排行榜**——ZSet 是排行榜的标准答案，MySQL 排序做这个要全表 ORDER BY，ZSet 是 O(log n) 💼

## 过期策略：缓存的生命线

```bash
EXPIRE session:abc 3600     # 给已存在的键设过期（秒）
TTL session:abc             # 看剩余秒数（-1 永不过期，-2 已不存在）
SET session:abc "uid=42" EX 3600   # 写入时就带过期（推荐姿势 🔥）
```

**为什么缓存必须过期**：内存是有限的（不能无限堆），且数据要保"新鲜"（MySQL 改了，缓存迟早要作废重来）。过期时间 = 容忍数据"旧多久"的业务权衡。

💼 内存写满时怎么办：Redis 有**淘汰策略**（`maxmemory-policy`），生产常用 `allkeys-lru`——**淘汰最近最少使用的键**（LRU，≈ 浏览器缓存的思路）。配置级知识，了解即可。

## 真实场景实战 🔥（本章精华）

### 场景 1：商品详情缓存（Cache-Aside 模式）

后端最经典的缓存读写模式，伪代码背下来：

```
读：查 Redis → 命中则返回 → 未命中查 MySQL → 写入 Redis（带过期）→ 返回
写：更新 MySQL → 删除 Redis 对应缓存（下次读时重建）
```

```bash
# 首次访问：库里查出商品后塞进缓存
SET product:1001 '{"name":"机械键盘","price":399}' EX 600
# 之后 10 分钟内的访问全部命中内存，MySQL 零压力 ✅
```

💡 "先更新库再删缓存"（而不是更新缓存）是为了避免并发下缓存写乱——这套 **Cache-Aside** 模式面试必考 💼。

### 场景 2：登录会话存储

```bash
# 用户登录成功：token 作键，用户 id 作值，过期 2 小时
SET token:8f3a2b1c "42" EX 7200
# 每次请求带 token，后端 GET token:xxx 验证 → 秒级完成
# 用户登出：DEL token:8f3a2b1c
```

为什么不用 MySQL 存会话？高频验证每次查库太贵；为什么要过期？token 不该永生 🔥。

### 场景 3：计数器——文章阅读数

```bash
INCR article:1001:views    # 每次阅读 +1，原子操作，万人并发也不乱
GET article:1001:views
```

INCR 的原子性是免费的（Redis 单线程执行命令）——对比 MySQL 的 `UPDATE views = views + 1`（行锁、慢），高下立判 💼。

### 场景 4：排行榜——ZSet 标准答案（前面已演示，不再重复）

### ⚠️ 缓存三大问题（概念级，面试必考 💼）

| 问题 | 一句话 | 常见对策 |
|------|--------|----------|
| **穿透** | 查**不存在**的数据，缓存永远 miss，请求全打到 DB | 缓存空值 / 布隆过滤器 |
| **击穿** | 某个**热点键**刚过期，瞬间万请求同时查库重建 | 互斥锁（只放一个请求去查库） |
| **雪崩** | **大批键同时过期**，DB 瞬间被打满 | 过期时间加随机抖动，错峰失效 |

现在只需记住三个名字和一句话区分——Spring Boot 章节写缓存代码时会重逢。

## 持久化：内存数据库就不怕丢吗（一提即可）

Redis 提供两种落盘机制：**RDB**（定时快照，≈ 定期存档）和 **AOF**（追加命令日志，≈ 完整操作录像，丢了能重放）。学习阶段默认配置即可，知道"Redis 不是断电全没"就行 💡。

## 对比总表

| 维度 | MySQL | Redis |
|------|-------|-------|
| 数据位置 | 磁盘 | **内存**（快几个数量级） |
| 数据模型 | 表（结构化、强类型） | key-value + 五大数据结构 |
| 查询语言 | SQL（声明式） | 命令（`动词 键 值`） |
| 持久性 | 天然持久 | 需 RDB/AOF 辅助 |
| 角色 | 数据的**最终真相** | 热数据的**加速器** |
| 前端类比 | 后端数据库 | 全局 Map 缓存的服务端专业版 |

## 练习

1. 用 redis-cli 完成一个"用户缓存"小流程：`HSET` 存一个用户对象 → `HGETALL` 取出 → `HINCRBY` 给年龄 +1 → 给这个键设 60 秒过期并用 `TTL` 观察倒计时。
2. 实现"文章阅读排行榜"：`ZADD` 插入 5 篇文章的初始阅读量 → `ZINCRBY` 模拟阅读增长 → `ZREVRANGE` 输出前三名及分数。
3. 场景设计题（写思路即可）：一个秒杀活动页面预计 QPS 10 万，商品库存 100 件。你会把哪些数据放进 Redis、用什么类型、过期怎么设？可能撞上"穿透/击穿/雪崩"中的哪一个，怎么防？

## 本章总结

- Redis = 内存 key-value 数据库，定位是**热数据加速器**，MySQL 才是最终真相
- 必要性：高频读场景查库太贵 → 缓存层挡在 MySQL 前面（前端 Map 缓存的服务端专业版）
- 五大类型配场景：**String**（缓存文本/INCR 计数）、**Hash**（对象缓存）、**List**（最新列表/队列）、**Set**（标签/共同好友）、**ZSet**（排行榜标准答案 🔥）
- 缓存必须带**过期**（内存有限 + 数据要新鲜）；写满靠 LRU 淘汰
- Cache-Aside 模式：读先缓存、写删缓存 💼；穿透/击穿/雪崩三个名字先挂号

数据库板块收官 🎉 下一站框架篇：[Maven](../04-frameworks/01-maven.md)——正式进入 Spring 的世界

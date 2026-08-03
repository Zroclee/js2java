# library-system — 图书馆管理系统（设计文档）

> 对应 ROADMAP「六、项目实战 · 图书馆管理系统」（难度 🌟🌟🌟 / 重要度 🌟🌟🌟🌟🌟）
> 完成标准：能独立完成一个完整 CRUD 项目
> **本文档只负责设计，实现由你自己完成**——遇到卡壳再回来查 docs 对应章节。

## 0. 项目概述

| 项 | 内容 |
|----|------|
| 功能范围 | 图书管理（增删改查/分页/条件搜索）+ 借阅归还（借阅人/时间/状态） |
| 前端 | Vue 3 + Vite + Tailwind CSS |
| 后端 | Spring Boot 4.1 + MyBatis-Plus + PostgreSQL 16（公司技术栈） |
| 通信 | REST API + JSON，前后端分离（前端 5173，后端 8080） |
| 不包含 | 登录认证、权限（留给 admin-system；本项目专注打通全流程） |

**环境准备**（用 Docker 拉起 PG）：

```bash
docker run -d -p 5432:5432 \
  -e POSTGRES_PASSWORD=123456 \
  -e POSTGRES_DB=library \
  --name pg-library postgres:16
```

> ⚠️ **版本适配提醒**：Spring Boot 4.x 较新，添加 MyBatis-Plus 依赖时请核对其官方文档对 Boot 4 的适配版本（若所用 MP 版本暂未适配 Boot 4，可将后端回退至 Boot 3.5.x，代码无需改动）。

---

## 1. 项目目录结构设计

单仓库前后端两目录（≈ 简化版 monorepo）：

```
library-system/
├── README.md                       # 本设计文档
├── backend/                        # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/library/
│       │   ├── LibraryApplication.java       # 启动类
│       │   ├── controller/                   # Web 层：接参/校验/返回，不写业务
│       │   │   ├── BookController.java
│       │   │   └── BorrowController.java
│       │   ├── service/                      # 业务层：规则、事务边界在这里
│       │   │   ├── BookService.java
│       │   │   └── BorrowService.java
│       │   ├── mapper/                       # 数据层：BaseMapper 继承
│       │   │   ├── BookMapper.java
│       │   │   └── BorrowRecordMapper.java
│       │   ├── entity/                       # 表对应的实体
│       │   │   ├── Book.java
│       │   │   └── BorrowRecord.java
│       │   ├── dto/                          # 出入参对象（不暴露 entity 给前端）
│       │   │   ├── BookQuery.java            # 搜索条件（keyword/pageNum/pageSize）
│       │   │   ├── BorrowReq.java            # 借阅请求（bookId/borrower）
│       │   │   └── Result.java               # 统一响应体 {code, message, data}
│       │   ├── enums/
│       │   │   └── BorrowStatus.java         # BORROWED / RETURNED
│       │   ├── config/
│       │   │   └── MybatisPlusConfig.java    # 分页插件注册
│       │   └── exception/
│       │       ├── BizException.java         # 业务异常（带错误码）
│       │       └── GlobalExceptionHandler.java
│       └── resources/
│           ├── application.yml               # 数据源/MP 配置
│           └── mapper/                       # 复杂 SQL 的 XML（单表靠 MP 可不需要）
└── frontend/                       # Vue 前端
    ├── index.html
    ├── package.json
    ├── vite.config.js                        # 含 /api 代理配置
    ├── tailwind.config.js
    └── src/
        ├── main.js
        ├── App.vue                           # 布局壳：侧边菜单 + 内容区
        ├── style.css                         # tailwind 指令
        ├── router/index.js                   # 两个页面路由
        ├── api/
        │   ├── request.js                    # axios 实例：baseURL=/api + 响应拦截（拆 Result）
        │   ├── book.js                       # 图书相关接口函数
        │   └── borrow.js                     # 借阅相关接口函数
        ├── views/
        │   ├── BookListView.vue              # 图书管理页
        │   └── BorrowListView.vue            # 借阅记录页
        └── components/
            ├── BookFormDialog.vue            # 新增/编辑图书对话框（复用一个组件）
            ├── SearchBar.vue                 # 搜索栏
            └── Pagination.vue                # 分页条（也可直接用 Element Plus 等）
```

**分层铁律**（写代码时默念）：Controller 只做"接参 → 校验 → 调 Service → 包 Result"；业务规则（能不能借、库存怎么变）**只在 Service**；SQL 只在 Mapper。层与层之间用 DTO 传递，**不要把 entity 直接返回给前端**（避免暴露表结构、便于脱敏）。

## 2. 后端依赖清单

| 依赖坐标 | 作用 | 备注 |
|----------|------|------|
| `spring-boot-starter-web` | Web MVC + 内嵌服务器 | 核心 |
| `spring-boot-starter-validation` | `@Valid` 参数校验 | 借阅人/书名非空等 |
| MyBatis-Plus starter | ORM（BaseMapper + 分页插件） | ⚠️ 核对 Boot 4 适配版本 |
| `org.postgresql:postgresql` | PG 驱动（runtime scope） | |
| `lombok`（可选） | `@Data` 自动生成 getter/setter | 减少样板代码，公司项目常用 |
| `spring-boot-starter-test` | 单元测试（test scope） | 可选 |

`application.yml` 关键配置（参考）：

```yaml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/library
    username: postgres
    password: 123456
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true    # 下划线列名 → 驼峰字段
  global-config:
    banner: false
```

## 3. 数据库设计（PostgreSQL 方言）

```sql
-- 图书表
CREATE TABLE books (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    author      VARCHAR(100) NOT NULL,
    isbn        VARCHAR(20) UNIQUE,                  -- ISBN 唯一约束
    stock       INT NOT NULL DEFAULT 0 CHECK (stock >= 0),  -- 库存不为负
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 借阅记录表
CREATE TABLE borrow_records (
    id          BIGSERIAL PRIMARY KEY,
    book_id     BIGINT NOT NULL REFERENCES books(id),  -- 逻辑关联 books
    borrower    VARCHAR(50) NOT NULL,                  -- 借阅人姓名
    borrowed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    returned_at TIMESTAMP,                             -- NULL = 未归还
    status      VARCHAR(10) NOT NULL DEFAULT 'BORROWED'
                CHECK (status IN ('BORROWED', 'RETURNED'))
);

CREATE INDEX idx_borrow_book_id ON borrow_records(book_id);   -- 按书查借阅历史
CREATE INDEX idx_borrow_status ON borrow_records(status);     -- 按状态筛选
```

**设计说明**：
- `status` 与 `returned_at` 双标记：`status` 用于快速筛选（未还列表是高频查询），`returned_at` 存具体时间；两者由 Service 保证一致
- `stock` 是**当前可借库存**：借出 -1、归还 +1，变更必须在**事务**里和借阅记录写入同生共死
- PG 特性应用：`BIGSERIAL` 自增、`CHECK` 约束兜底数据合法性（Java 校验之外的第二道防线）
- 枚举 `BorrowStatus` 用 VARCHAR 存字符串（可读性好），Java 侧对应枚举类

## 4. 前端 UI 设计

**整体布局**：左侧固定菜单（📚 图书管理 / 📖 借阅记录）+ 右侧内容区，简洁后台风。

**页面 1：图书管理** `BookListView.vue`

```
┌──────────────────────────────────────────────┐
│  [书名/作者搜索框........]  [搜索]  [+ 新增图书] │
│ ┌──────────────────────────────────────────┐ │
│ │ 书名    作者    ISBN    库存    操作       │ │
│ │ 三体    刘慈欣  978...   3     编辑 删除  │ │
│ │ ...（库存为 0 时数字标红）                 │ │
│ └──────────────────────────────────────────┘ │
│              [< 1 2 3 ... 10 >]               │
└──────────────────────────────────────────────┘
```

- 搜索：书名/作者共用一个 keyword 输入框，模糊匹配，回车或点按钮触发
- 新增/编辑：共用一个 `BookFormDialog`（有 id 即编辑），字段：书名/作者/ISBN/库存
- 删除：二次确认；后端可能返回"有未还记录不可删除"的错误提示

**页面 2：借阅记录** `BorrowListView.vue`

- 顶部：状态筛选 Tab（全部 / 未归还 / 已归还）+ "新建借阅"按钮（弹窗：选书 + 填借阅人）
- 表格：书名 / 借阅人 / 借出时间 / 归还时间 / 状态标签 / 操作
- 状态标签 Tailwind 配色：**未归还 `bg-amber-100 text-amber-700`**、**已归还 `bg-green-100 text-green-700`**
- 操作列：未归还的记录显示"归还"按钮（点击确认后调归还接口）

**Tailwind 约定**：表格行 hover 底色、主按钮 `bg-blue-600 text-white`、危险按钮 `text-red-600`，统一风格即可，不追求花哨。

---

## 5. API 接口契约（前后端联调基石）

统一响应体：`{ "code": 0, "message": "ok", "data": ... }`（code 非 0 即业务错误，前端拦截统一提示）

| 方法 | 路径 | 说明 | 关键参数 |
|------|------|------|----------|
| GET | `/api/books` | 图书分页列表 | `keyword`, `pageNum=1`, `pageSize=10` |
| GET | `/api/books/{id}` | 图书详情 | — |
| POST | `/api/books` | 新增图书 | `{title, author, isbn, stock}` |
| PUT | `/api/books/{id}` | 编辑图书 | 同上 |
| DELETE | `/api/books/{id}` | 删除图书 | 有未还记录时拒绝（业务错误） |
| GET | `/api/borrows` | 借阅记录分页 | `status`（可选）, `pageNum`, `pageSize` |
| POST | `/api/borrows` | 新建借阅 | `{bookId, borrower}` |
| PUT | `/api/borrows/{id}/return` | 归还 | — |

分页数据建议结构：`data: { records: [...], total: 100, pageNum: 1, pageSize: 10 }`

**业务规则**（Service 层实现）：
1. 借阅：`stock > 0` 才能借 → 插借阅记录 + 库存 -1，**同一事务**；库存为 0 抛 `BizException(1001, "库存不足")`
2. 归还：更新记录（status → RETURNED、returned_at → now）+ 库存 +1，同一事务
3. 删除图书：存在 `BORROWED` 状态的借阅记录时拒绝删除
4. ISBN 重复：新增/编辑时捕获唯一约束冲突，转成友好提示

## 6. 开发期联调：Vite 代理

不用配置 CORS，开发时用 Vite 代理转发（`vite.config.js`）：

```js
server: {
  proxy: { '/api': 'http://localhost:8080' }
}
```

前端代码里所有请求写 `/api/...` 即可，部署时再处理跨域或同源部署。

## 7. 建议开发路线（里程碑）

1. **后端骨架**：start.spring.io 生成项目 → 配依赖 → 建表 → books CRUD 跑通（用 curl/Postman 验证）
2. **借阅闭环**：借阅/归还 + 事务 + 业务规则，接口全部自测通过
3. **前端骨架**：Vite 项目 → Tailwind 接入 → 布局与路由 → axios 封装
4. **联调两页**：图书页 → 借阅页，全流程走通
5. **收尾**：统一异常提示、参数校验、空状态与加载态

## 状态

📐 设计就绪，待开发

# Docker：把环境打成镜像，一条命令拉起整个后端

> 状态：✅ 已完成

## 简介
本章学习内容：Docker 概念、macOS / Windows 安装、Docker Desktop 使用、终端核心命令、镜像与容器操作实战
前置知识：完成「02-core-java」全部章节；命令行基础
阅读时长：约 35 分钟
难度：🌟🌟
重要程度：🌟🌟🌟🌟
当前进度：65%

---

## Docker 是什么：先建立两个类比

**官方定义**：容器化平台——把应用连同它的依赖环境打包成"镜像"，在任何装了 Docker 的机器上跑出一模一样的"容器"。

**前端类比** 😌：你在前端已经体会过"环境不一致"的痛——同事 npm install 出来的 node_modules 和你版本不同，"在我机器上能跑"成了玄学。Docker 把这个问题的解法推到极限：**不只锁依赖版本，连操作系统、运行时、配置全部打包**，交付的不再是代码，而是"可复现的环境快照"。

**OOP 类比**（刚学完，趁热）：**镜像（Image）≈ 类，容器（Container）≈ 实例**。镜像是只读模板，容器是用模板 new 出来的运行实例——一个镜像可以起 N 个容器，就像一个类可以 new N 个对象。

```
Dockerfile（配方）──build──▶ 镜像（模板）──run──▶ 容器（运行实例）
   ≈ webpack 配置              ≈ 类                    ≈ 对象实例
```

## 为什么后端转全栈必学它 🔥

接下来的章节需要 MySQL、Redis、PostgreSQL。没有 Docker 的日子：去各官网下载安装包、配环境变量、处理版本冲突、卸载残留……每个服务折腾半天。

有了 Docker：

```bash
docker run -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456 mysql:8   # 30 秒拉起 MySQL
docker run -d -p 6379:6379 redis:7                                  # 10 秒拉起 Redis
```

**不污染本机、随时删除重建、版本随意切换**——这就是后端开发的日常起手式 💪。

## 安装

### macOS

```bash
# 方式 1：Homebrew（推荐）
brew install --cask docker

# 方式 2：官网下载 https://www.docker.com/products/docker-desktop/
# 注意区分 Apple Silicon（M 系列）和 Intel 芯片的安装包
```

装完**启动 Docker Desktop 应用**（启动后菜单栏出现鲸鱼图标 🐳）。

⚠️ 认知要点：Docker 容器基于 Linux 内核，macOS（和 Windows）并不直接支持——**Docker Desktop 在你系统里偷偷跑了一个轻量 Linux 虚拟机**，所有容器实际住在那个 VM 里。这就是为什么 macOS/Windows 必须装桌面端，也是 `-v` 挂载、端口映射偶尔让人困惑的根源。

### Windows

1. 确保开启 **WSL2**（Windows 的 Linux 子系统）：管理员 PowerShell 执行 `wsl --install`，重启
2. 官网下载 Docker Desktop 安装包，安装时**勾选 `Use WSL 2 instead of Hyper-V`** ✅
3. 启动 Docker Desktop，系统托盘出现鲸鱼图标

### 验证安装（两平台相同）

```bash
docker --version        # Docker version 27.x.x
docker run hello-world  # 拉取测试镜像并运行，看到 "Hello from Docker!" 即成功 ✅
```

### 国内加速（拉取镜像慢/失败时）⚠️

Docker Hub 在国外，裸连经常超时。Docker Desktop → 右上角设置 ⚙️ → **Docker Engine**，在 JSON 里加镜像源：

```json
{
  "registry-mirrors": [
    "https://docker.1ms.run",
    "https://docker.xuanyuan.me"
  ]
}
```

保存后点 **Apply & Restart**。镜像源时效性强，失效了就搜"docker 国内镜像源"换新的（2026 年可用性以当时为准）。

## Docker Desktop 使用

桌面端是"可视化控制台"：左侧导航 **Containers**（容器列表：启动/停止/删除/看日志/进终端）、**Images**（镜像列表：拉取/删除/看体积）、**Volumes**（数据卷）。命令行做的所有事这里都能点出来，初期建议**两边对照着用**：终端敲命令，桌面端看效果，理解会快很多。

**靠谱攻略链接**（亲测可用）：

- 📘 [Docker 官方 Get Started 教程](https://docs.docker.com/get-started/) —— 官方手把手，权威且更新及时
- 📘 [Docker Desktop 官方文档](https://docs.docker.com/desktop/) —— 桌面端每个按钮的说明
- 📗 [Docker 教程 · 菜鸟教程](https://www.runoob.com/docker/docker-tutorial.html) —— 中文速查，命令检索方便
- 📗 [Win11 安装 Docker Desktop 图文教程（2025）](https://lino-ai.blog.csdn.net/article/details/155263711) —— 含 WSL2 安装、磁盘迁移、镜像源配置的完整踩坑记录
- 📗 [Docker Desktop 入门教程 Windows & macOS（2025）](https://blog.csdn.net/yweng18/article/details/149486875) —— 图文界面导览 + 常用场景

## 终端命令：真正要背的家底 🔥

桌面端再方便，服务器上可没有图形界面——**命令行才是本体**。按"镜像"和"容器"两族记：

### 镜像命令（管模板）

```bash
docker pull nginx:latest     # 拉取镜像 ≈ npm install
docker images                # 查看本地镜像列表
docker rmi nginx             # 删除镜像（先删依赖它的容器）
docker search nginx          # 搜索镜像（不如直接上 hub.docker.com 网页版好用）
```

### 容器命令（管实例）

```bash
docker ps                    # 运行中的容器
docker ps -a                 # 全部容器（含已停止的）
docker stop my-web           # 停止
docker start my-web          # 启动（已存在的容器）
docker restart my-web        # 重启
docker rm my-web             # 删除容器（需先停止；-f 强制）
docker logs -f my-web        # 看日志（-f 持续跟踪 ≈ tail -f）🔥 排查问题第一手
docker exec -it my-web bash  # 进入容器内部开终端 🔥🔥 高频！进去就是一台精简 Linux
```

### docker run：最重要的一条命令

```bash
docker run -d -p 8080:80 --name my-web -v /host/path:/container/path -e KEY=value --rm nginx
```

逐参数拆解 💼：

| 参数 | 作用 | 类比/记忆 |
|------|------|-----------|
| `-d` | 后台运行（detached） | 不加则霸占当前终端 |
| `-p 8080:80` | 端口映射：**宿主机:容器** ⚠️ 顺序别反 | 浏览器访问 localhost:8080 → 容器内 80 |
| `--name` | 给容器起名 | 不起名 Docker 会随机分配怪名字 |
| `-v 宿主:容器` | 目录挂载：打通两边文件系统 | ≈ 软链，改宿主机文件容器内同步可见 |
| `-e KEY=value` | 注入环境变量 | ≈ Node 的 process.env，MySQL 密码就靠它传 |
| `--rm` | 容器退出后自动删除 | 一次性测试容器必备 |
| `-it` | 交互式 + 伪终端 | 配合 bash 进容器用 |

## 实战三连：从 hello-world 到 MySQL

### 第一发：Nginx（体验完整生命周期）

```bash
docker run -d -p 8080:80 --name my-web nginx
# 浏览器打开 http://localhost:8080 → 看到 "Welcome to nginx!" ✅

docker logs my-web              # 看访问日志
docker exec -it my-web bash     # 进容器逛逛：nginx 首页在 /usr/share/nginx/html
exit

docker stop my-web && docker rm my-web   # 收尾
```

### 第二发：换个首页（体验挂载）

```bash
# 在本地建个 html 文件，挂载进容器替换默认首页
mkdir -p ~/docker-demo && cd ~/docker-demo
echo "<h1>Hello from Docker!</h1>" > index.html

docker run -d -p 8080:80 -v ~/docker-demo:/usr/share/nginx/html --name my-web2 nginx
# 刷新 localhost:8080 → 显示你的 HTML ✅ 改本地文件，刷新即变
```

### 第三发：MySQL（为下一章预热 🔥）

```bash
docker run -d -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=123456 \
  -e MYSQL_DATABASE=demo \
  --name mysql8 mysql:8

docker exec -it mysql8 mysql -uroot -p123456   # 直接进 MySQL 命令行
# mysql> SHOW DATABASES;  → 能看到 demo 库 ✅
# 先留着这个容器，下一章「MySQL」直接用它
```

## 两个进阶概念（提一笔，后面章节展开）

- **Dockerfile**：把"怎么构建镜像"写成配方文件（FROM 基础镜像 → COPY 文件 → RUN 装依赖 → CMD 启动）——≈ 把 package.json + 构建脚本 + 部署步骤合为一个文件。给自己的 Java 应用打镜像时（部署章）详细写
- **Docker Compose**：一个 yml 文件描述**多个**容器（应用 + MySQL + Redis 一整套），`docker compose up -d` 一键齐活。单容器手动 run 还行，三个以上就该它了——admin-system 项目见

## 练习

1. 用命令行完成：拉取 `redis:7` 镜像 → 后台运行并命名 `my-redis`、映射 6379 端口 → `docker exec -it my-redis redis-cli` 进去执行 `set name "js2java"` 再 `get name` → 依次停止、删除容器、删除镜像。
2. 挂载实战：本地写一个 `index.html`，用 `-v` 挂载方式让 nginx 展示它；改文件内容刷新浏览器验证"宿主机修改即时生效"。
3. 思考题：`docker run -p 8080:80` 中两个数字分别代表什么？如果本机 8080 已被占用，怎么改？

## 本章总结

- Docker = 可复现的环境快照：**镜像 ≈ 类，容器 ≈ 实例**；解决"在我机器上能跑"
- macOS/Windows 装 Docker Desktop（本质内置 Linux VM）；国内记得配镜像源 ⚠️
- 桌面端用来"看"，终端命令才是本体：`pull / images / run / ps / stop / rm / logs / exec -it`
- `docker run` 参数五虎：`-d` 后台、`-p` 端口（宿主:容器）、`--name` 命名、`-v` 挂载、`-e` 环境变量 💼
- 30 秒拉起 MySQL/Redis——后续章节的装备全靠它

下一章：[MySQL](./02-mysql.md)——刚拉起来的 mysql8 容器，直接开用

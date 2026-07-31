# projects/ — 实战项目

对应 ROADMAP「六、项目实战」与「七、部署」，把前面所有知识串起来。

## 项目清单

| 项目 | 对应大纲 | 说明 | 技术栈 |
|------|----------|------|--------|
| [library-system/](./library-system) | 六 · 图书馆管理系统 | 完整 CRUD 入门项目，独立完成 | Spring Boot + MyBatis + MySQL |
| [admin-system/](./admin-system) | 六 · RuoYi 源码学习 / 七 · 部署 | 完整后台管理系统，含部署脚本 | Spring Boot + Vue + MySQL + Redis + Nginx |

## 建议顺序

1. **library-system**：先独立做一个最小完整 CRUD，建立信心
2. **admin-system**：再挑战完整后台（权限、菜单、日志），并用 Docker + Linux 部署上线

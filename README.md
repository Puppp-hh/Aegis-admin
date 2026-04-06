# AegisAdmin 企业级权限管理平台

## 项目简介
AegisAdmin 是一个基于 Spring Boot 3.x 开发的企业级后台管理系统，
核心实现 RBAC（基于角色的访问控制）权限模型，支持用户、角色、菜单
的灵活配置与管理。

项目采用 Maven 多模块架构，随学习进度持续迭代升级：
- V1.0 单体版（当前）
- V2.0 引入 Redis 缓存
- V3.0 Docker 容器化部署
- V4.0 Spring Cloud 微服务改造

## 技术栈
| 技术 | 说明 |
|------|------|
| Spring Boot 3.x | 核心后端框架 |
| Spring Security | 认证与授权 |
| JWT | 无状态登录凭证 |
| MyBatis-Plus | ORM 框架 |
| MySQL 8.0 | 关系型数据库 |
| RabbitMQ | 异步消息队列 |
| Maven 多模块 | 项目结构管理 |

## 项目结构
aegis-admin/
├── aegis-common/     # 公共模块（工具类、统一响应、异常）
├── aegis-system/     # 系统模块（用户、角色、菜单、日志）
└── aegis-app/        # 启动模块（入口、配置）

## 核心功能
- 用户管理：增删改查、启用禁用、重置密码、分配角色
- 角色管理：增删改查、绑定菜单权限
- 菜单管理：树形结构、目录/菜单/按钮三级
- 操作日志：AOP 无侵入采集，RabbitMQ 异步写入
# 运行项目指南

## 前置条件
1. 已安装Java 21和Maven
2. 数据库：H2内存数据库（默认）或PostgreSQL

## 数据库配置

### 默认配置：H2内存数据库（推荐用于快速启动）
当前项目默认使用H2内存数据库，无需额外配置即可启动。

### 切换到PostgreSQL数据库
如果需要使用PostgreSQL，请按照以下步骤配置：

#### 1. 创建数据库
在PostgreSQL中执行以下SQL命令：

```sql
CREATE DATABASE campus_life_platform;
```

#### 2. 修改配置文件
将`src/main/resources/application.yml`中的数据库配置修改为：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/campus_life_platform
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    dialect: org.hibernate.dialect.PostgreSQLDialect
```

#### 3. 验证数据库连接
确保PostgreSQL在localhost:5432上运行：

```bash
psql -U postgres -d campus_life_platform
```

## 运行项目

### 方法1：使用Maven命令
```bash
cd c:/Users/hz/Desktop/web-project1/campus-life-platform
mvn spring-boot:run
```

### 方法2：先打包再运行
```bash
cd c:/Users/hz/Desktop/web-project1/campus-life-platform
mvn clean package
java -jar target/campus-life-platform-0.0.1-SNAPSHOT.jar
```

### 方法3：在IDE中运行
1. 打开`CampusLifePlatformApplication.java`文件
2. 右键点击文件中的`main`方法
3. 选择"Run"或"Debug"

## 访问应用
项目启动成功后，应用将在以下地址运行：
- 应用地址：http://localhost:8081
- H2控制台（默认配置）：http://localhost:8081/h2-console

## 数据库表结构
项目启动后会自动创建以下表（基于JPA的ddl-auto=update配置）：
- study_plans (学习计划表)
- library_seat_global (图书馆座位全局表)
- library_seat_operation (图书馆座位操作记录表)

## 测试数据
项目启动时会自动初始化学习计划测试数据，包括：
- 今日任务（5条）
- 过去一周的任务（13条）
- 过去一个月的任务（20条）

## 常见问题

### 问题1：连接数据库失败（PostgreSQL）
**解决方案**：
- 检查PostgreSQL服务是否启动
- 验证数据库用户名和密码是否正确
- 确认数据库`campus_life_platform`已创建

### 问题2：端口被占用
**解决方案**：
- 修改`application.yml`中的端口配置
- 或停止占用8081端口的其他应用

### 问题3：Hibernate方言错误
**解决方案**：
- 确保配置了正确的JPA方言
- H2使用`org.hibernate.dialect.H2Dialect`
- PostgreSQL使用`org.hibernate.dialect.PostgreSQLDialect`

## 快速启动（使用默认H2配置）
如果只是想快速测试项目，使用默认H2配置即可：

```bash
cd c:/Users/hz/Desktop/web-project1/campus-life-platform
mvn spring-boot:run
```

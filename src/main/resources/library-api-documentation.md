# 图书馆学习时长功能接口文档

## 项目背景
- 数据库中已存在 `library_study_records` 表，用于记录图书馆学习时长
- 项目的基础URL为本地地址：`http://localhost:8080`

## 1. 进入图书馆接口

### 接口信息
- **完整接口地址**：`http://localhost:8080/api/library/enter`
- **接口路径**：`POST /api/library/enter`
- **请求方法**：POST

### 请求参数
| 参数名 | 类型 | 必填 | 描述 |
| --- | --- | --- | --- |
| `userId` | Long | 是 | 用户ID |

### 请求示例
```bash
# 进入图书馆
POST http://localhost:8080/api/library/enter?userId=1
```

### 返回结构
**成功示例**：
```json
{
  "code": 200,
  "message": "进入图书馆成功",
  "data": 1
}
```

**失败示例**：
```json
{
  "code": 500,
  "message": "用户ID不能为空",
  "data": null
}
```

### 返回字段解释
| 字段名 | 类型 | 描述 |
| --- | --- | --- |
| `code` | Integer | 状态码：成功200/失败500 |
| `message` | String | 提示语 |
| `data` | Long | 插入记录的ID（供前端离开时传参使用） |

## 2. 离开图书馆接口

### 接口信息
- **完整接口地址**：`http://localhost:8080/api/library/leave`
- **接口路径**：`POST /api/library/leave`
- **请求方法**：POST

### 请求参数
| 参数名 | 类型 | 必填 | 描述 |
| --- | --- | --- | --- |
| `recordId` | Long | 是 | 记录ID（表主键id） |

### 请求示例
```bash
# 离开图书馆
POST http://localhost:8080/api/library/leave?recordId=1
```

### 返回结构
**成功示例**：
```json
{
  "code": 200,
  "message": "离开图书馆成功",
  "data": null
}
```

**失败示例**：
```json
{
  "code": 500,
  "message": "记录ID不能为空",
  "data": null
}
```

### 返回字段解释
| 字段名 | 类型 | 描述 |
| --- | --- | --- |
| `code` | Integer | 状态码：成功200/失败500 |
| `message` | String | 提示语 |
| `data` | null | 无返回数据 |

## 3. 查询今日总学习时长接口

### 接口信息
- **完整接口地址**：`http://localhost:8080/api/library/today-duration`
- **接口路径**：`GET /api/library/today-duration`
- **请求方法**：GET

### 请求参数
| 参数名 | 类型 | 必填 | 描述 |
| --- | --- | --- | --- |
| `userId` | Long | 是 | 用户ID |

### 请求示例
```bash
# 查询今日总学习时长
GET http://localhost:8080/api/library/today-duration?userId=1
```

### 返回结构
**成功示例**：
```json
{
  "code": 200,
  "message": "查询今日总学习时长成功",
  "data": 120
}
```

**失败示例**：
```json
{
  "code": 500,
  "message": "用户ID不能为空",
  "data": null
}
```

### 返回字段解释
| 字段名 | 类型 | 描述 |
| --- | --- | --- |
| `code` | Integer | 状态码：成功200/失败500 |
| `message` | String | 提示语 |
| `data` | Integer | 今日总学习时长（分钟），无符合条件记录则返回0 |

## 4. 技术实现说明

### 4.1 业务逻辑
- **进入图书馆**：向 `library_study_records` 插入一条记录，`enter_time` 自动取当前时间，`leave_time` 为 NULL，`duration_minutes` 默认 0
- **离开图书馆**：根据 `recordId` 更新对应记录，`leave_time` 设为当前时间；自动计算本次时长（`leave_time - enter_time`），取整为分钟赋值给 `duration_minutes`
- **查询今日总学习时长**：仅统计该 `userId` 今日（当日00:00至当前时间）的记录，筛选条件：`leave_time IS NOT NULL`（已完成学习）+ `enter_time >= 今日凌晨0点`

### 4.2 技术栈
- 后端：Spring Boot 3.5.8 + MyBatis-Plus + PostgreSQL
- 数据库：PostgreSQL 15.15

### 4.3 代码结构
```
├── src/main/java/com/school/
│   ├── controller/
│   │   └── LibraryStudyRecordController.java  # 图书馆学习时长控制器
│   ├── entity/
│   │   ├── LibraryStudyRecord.java            # 图书馆学习时长记录实体类
│   │   └── ResultVO.java                      # 统一返回值类
│   ├── mapper/
│   │   └── LibraryStudyRecordMapper.java      # 图书馆学习时长记录Mapper
│   ├── service/
│   │   ├── LibraryStudyRecordService.java     # 图书馆学习时长记录服务接口
│   │   └── impl/
│   │       └── LibraryStudyRecordServiceImpl.java # 图书馆学习时长记录服务实现
├── src/main/resources/
│   └── library-api-documentation.md           # 图书馆学习时长功能接口文档
```